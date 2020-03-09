package uk.ac.ebi.utils.threading.batchproc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.exceptions.UnexpectedEventException;
import uk.ac.ebi.utils.threading.HackedBlockingQueue;
import uk.ac.ebi.utils.threading.ThreadUtils;
import uk.ac.ebi.utils.threading.batchproc.collectors.CollectionBatchCollector;
import uk.ac.ebi.utils.threading.batchproc.processors.CollectionBasedBatchProcessor;


/**  
 * ## Batch Processor Skeleton 
 * 
 * A simple skeleton to manage processing of data in multi-thread mode.  
 * 
 * The idea for which this class and {@link BatchCollector batch collectors} provide scaffolding for
 * is that a data source is used to build `B` batches and, when 
 * {@link BatchCollector#batchReadyFlag() a batch is ready to be processed}, it is submitted to a `BJ`
 * job and run in parallel by an {@link #getExecutor()}.   
 * 
 * This class is just a skeleton, for a concrete implementation you need to realise the above source
 * processing loop on your own. In order to facilitate that, {@link #handleNewBatch(Object, boolean)}
 * is provided. You should call it periodically, and it checks if the current batch (that you're filling)
 * is ready for submission and, if yes, it submits the current batch to a new parallel job, 
 * {@link BatchCollector#batchFactory() creates a new batch} and returns it (normally it returns the current
 * batch).  
 * 
 * At the end of such a loop, you should call {@link #waitExecutor(String)}, so that the final batch you were building
 * is forced into processing and other close-up operations are performed.  
 * 
 * As you can see above, {@link BatchCollector} is another facility to manage batch creation and decide if a
 * batch is ready to be processed.   
 * 
 * Specific implementations are provided for both {@link BatchCollector} and this processor class.
 * For instance, We have an {@link ItemizedBatchProcessor item-based processor}, which 
 * {@link ItemizedBatchProcessor#process(Consumer, Object...) implements} the above-described source dispatching loop
 * as the fetch from a stream of items, which are sent to batches.  
 * 
 * This specific processor uses {@link ItemizedSizedBatchCollector item-based collectors}, which of default implementation
 * decides if a batch is ready to go on the basis of the {@link ItemizedSizedBatchCollector#batchSizer() number of items}
 * it has. Other specific processors are those {@link CollectionBasedBatchProcessor based on Java collections}, which 
 * makes use of corresponding {@link CollectionBatchCollector}.  
 * 
 * Note that, while we consider sychronisation issues for the {@link #getBatchJob() batch processing jobs}, we assume that
 * the initialisation and submission operations are run by a single thread (eg, `main`), so the code about that isn't 
 * thread-safe. For instance, {@link #waitExecutor(String)} won't synchronise over the {@link #getExecutor() current executor}
 * and {@link #setBatchJob(Consumer)} won't synchronise over the current job. Instantiating multiple processors is a 
 * safe way to deal with such a multi-multi-thread scenario. Likely, you'll want to share the executor job in such a case.
 * 
 *  
 * @param <B> the type of batch to be handled
 * @param <BC> the type of {@link BatchCollector} to use for batch operations
 * @param <BJ> the type of job that is able to process a batch of type `B`
 *  
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Dec 2017</dd></dl>
 *
 */
public abstract class BatchProcessor<B, BC extends BatchCollector<B>, BJ extends Consumer<B>>
{
	private BJ batchJob;
	private BC batchCollector;
	
	private ExecutorService executor = HackedBlockingQueue.createExecutor ();
	
	private AtomicLong submittedBatches = new AtomicLong ( 0 );
	private AtomicLong completedBatches = new AtomicLong ( 0 );

	/**
	 * Set to true by {@link #waitExecutor(String)}, when it needs to wait for the 
	 * completion of all {@link #submittedBatches}, and {@link Object#notify() notified} by 
	 * {@link #wrapBatchJob(Runnable)}, when it sees that {@link #completedBatches} == {@link #submittedBatches}.
	 * 
	 * It's a mutable because it's also used in `synchronized` blocks. Moreover, it's not a simple
	 * void object, cause {@link #wrapBatchJob(Runnable)} reports how many jobs it completed when this is 
	 * true. 
	 * 
	 */
	private Mutable<Boolean> waitingCompletion = new MutableBoolean ();
	
	
	/** @see {@link #wrapBatchJob(Runnable)} */
	protected long jobLogPeriod = 1000;
	
	protected Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	{
		ThreadUtils.setNamingThreadFactory ( this.getClass (), executor );
	}
			
	public BatchProcessor ( BJ batchJob, BC batchCollector )
	{
		super ();
		this.batchJob = batchJob;
		this.batchCollector = batchCollector;
	}
	
	
	public BatchProcessor ( BJ batchJob ) {
		this ( batchJob, null );
	}
	
	public BatchProcessor () {
		this ( null, null );
	}
	
		
	protected B handleNewBatch ( B currentBatch ) {
		return handleNewBatch ( currentBatch, false );
	}

	/**
	 * This is the method that possibly issues a new task, via the {@link #getExecutor()}, which runs 
	 * the {@link #getBatchJob()} against the current batch. 
	 * 
	 * Note that the batch job will be executed under the {@link #wrapBatchJob(Runnable) default wrapper}.  
	 * 
	 * This method also resets the internal {@link ExecutorService}, which will be recreated (once) upon the first 
	 * invocation of {@link #getExecutor()}. This behaviour ensures that a processor can be invoked multiple times 
	 * reusing the same batchJob instance (normally that's not possible for an {@link ExecutorService} after its 
	 * {@link ExecutorService#awaitTermination(long, TimeUnit)} method is called).   
	 * 
	 * @param forceFlush if true it flushes the data independently of {@link BatchCollector#batchReadyFlag()}. Which is
	 * typically needed when you've exhausted a stream of data and you have a last partially-filled batch to process.
	 * 
	 */
	protected B handleNewBatch ( B currentBatch, boolean forceFlush )
	{		
		BatchCollector<B> bcoll = this.batchCollector;
		if ( !( forceFlush || bcoll.batchReadyFlag ().test ( currentBatch ) ) ) return currentBatch;

		getExecutor ().submit ( wrapBatchJob ( () -> batchJob.accept ( currentBatch ) ) );
		
		long submitted = this.submittedBatches.incrementAndGet ();
		if ( this.jobLogPeriod > 0 && submitted % this.jobLogPeriod == 0 ) 
			log.info ( "{} batch jobs submitted", submitted );
		
		return bcoll.batchFactory ().get ();
	}
	
	/**
	 * As explained in {@link BatchCollector}, this is used to create a new batch and decide if it's ready for submission
	 * to a new {@link #getBatchJob() job}.
	 * @return
	 */
	public BC getBatchCollector () {
		return batchCollector;
	}

	public void setBatchCollector ( BC batchCollector ) {
		this.batchCollector = batchCollector;
	}

	
	/**
	 * {@link #handleNewBatch(Object, boolean)} launches this job every time the current batch being considered is 
	 * {@link BatchCollector#batchReadyFlag() ready for processing}.  
	 * 
	 * Note that your job is wrapped into {@link #wrapBatchJob(Runnable)}.
	 */
	public BJ getBatchJob () {
		return batchJob;
	}

	public void setBatchJob ( BJ batchJob ) {
		this.batchJob = batchJob;
	}
	
	/**
	 *
	 * The executor service used by {@link #handleNewBatch(Object)} to submit {@link #getBatchJob() batch jobs and 
	 * run them in parallel}.   
	 * 
	 * By default this is {@link HackedBlockingQueue#createExecutor()}, ie, a fixed size executor
	 * pool, which is able to block and wait when it's full. Moreover, such executor is equipped with a convenient 
	 * {@link ThreadUtils#setNamingThreadFactory(Class, ThreadPoolExecutor) naming thread factory}, which names the 
	 * threads based on the processor class (ie, myself or one extension of mine).     
	 * 
	 * Normally you shouldn't need to change this parameter, unless you want some particular execution policy. A 
	 * situation where you want to use the {@link #setExecutor(ExecutorService) setter for this property} is when 
	 * you want to hook multiple batch processors to the same executor, to avoid recreating threads too many times, and/or
	 * to decide an overall thread management policy.
	 * 
	 */
	public ExecutorService getExecutor () {
		return this.executor;
	}

	public void setExecutor ( ExecutorService executor ) {
		this.executor = executor;
	}


	/**
	 * <p>Waits that all the parallel jobs submitted to the batchJob are finished. It keeps polling
	 * {@link ExecutorService#isTerminated()} and invoking {@link ExecutorService#awaitTermination(long, TimeUnit)}.</p>
	 * 
	 * <p>As explained above, this resets the {@link ExecutorService} that is returned by {@link #getExecutor()}, so that
	 * the next time that method is invoked, it will get a new executor from {@link #getExecutorFactory()}.</p>
	 * 
	 * @param pleaseWaitMessage the message to be reported (via logger/INFO level) while waiting.
	 */
	protected void waitExecutor ( String pleaseWaitMessage )
	{
		try
		{
			while ( true )
			{
				if ( this.jobLogPeriod > -1 ) log.info ( pleaseWaitMessage );
				synchronized ( this.waitingCompletion )
				{
					this.waitingCompletion.setValue ( true );
					try {
						if ( this.getCompletedBatches () == this.getSubmittedBatches () ) break;
						this.waitingCompletion.wait ( 5 * 60 * 1000 );
					}
					finally {
						this.waitingCompletion.setValue ( false );
					}
				}
			}
		}
		catch ( InterruptedException ex ) {
			throw new UnexpectedEventException ( 
				"Unexpected interruption while waiting for batchJob termination: " + ex.getMessage (), ex 
			);
		}
	}
	
	/**
	 * Wraps the task into some common operations. At the moment,
	 * 
	 *   * wraps exception,
	 *   * logs the progress of completed tasks every {@link #jobLogPeriod} completed tasks.
	 *   
	 */
	protected Runnable wrapBatchJob ( Runnable batchJob )
	{
		return () -> 
		{
			try {
				batchJob.run ();
			}
			catch ( Exception ex )
			{
				log.error ( 
					String.format ( 
						"Error while running batch batchJob thread %s: %s", 
						Thread.currentThread ().getName (), ex.getMessage () 
					),
					ex
				);
			}
			finally 
			{
				long completed = this.completedBatches.incrementAndGet ();
				long submitted = this.submittedBatches.get ();
				synchronized ( this.waitingCompletion )
				{
					if ( this.jobLogPeriod > 0 ) 
						if ( completed == submitted && this.waitingCompletion.getValue ()
						     || ( completed % this.jobLogPeriod == 0 ) )
							log.info ( "{}/{} batch jobs completed", completed, submitted );
					
					if ( completed == submitted )
						this.waitingCompletion.notify ();
				}
			}
		};
	}


	public long getSubmittedBatches ()
	{
		return submittedBatches.get ();
	}


	public long getCompletedBatches ()
	{
		return completedBatches.get ();
	}


	/**
	 * If &gt; 0, methods like {@link #handleNewBatch(Object, boolean)} and {@link #waitExecutor(String)} log messages 
	 * about how many submitted and completed {@link #getBatchJob() jobs} the processor is dealing with, and does it every
	 * a number of submitted jobs equal to this property. Additionally, {@link #waitExecutor(String)} logs the parameter it
	 * receives.  
	 * 
	 * If it's 0, just does the latter.  
	 * 
	 * If it's -1, none of such logging happens.  
	 * 
	 * It might be useful to disable these messages with 0 or -1 if the caller wants to use its own logging about similar
	 * events. In particular, it might be useful when you use mukltiple processors in parallel, or the same for multiple
	 * times.
	 * 
	 * **WARNING**: implementors of this class's subclasses should comply with the above semantics for this parameter, at 
	 * least if those classes are general purpose and meant to be reused by third parties.
	 */
	public void setJobLogPeriod ( long jobLogPeriod )
	{
		this.jobLogPeriod = jobLogPeriod;
	}
	
}
