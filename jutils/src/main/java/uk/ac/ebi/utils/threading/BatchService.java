package uk.ac.ebi.utils.threading;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import uk.ac.ebi.utils.threading.BatchServiceTask.TaskComparator;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jext.Logger;
import uk.org.lidalia.slf4jext.LoggerFactory;


/**
 * <p>A pool-based thread execution service, that dynamically optimises its size.</p>
 * 
 * <p>This is a base class that allow you to manage the execution of a number of {@link BatchServiceTask tasks} in parallel.
 * This is achieved by means of a thread pool, which of size is dynamically adjusted via {@link PoolSizeTuner}, 
 * in order to optimise the task throughput (i.e., the number of tasks that complete their execution in the unit of time).</p>
 *
 * <p>You should initialise this class with a moderate initial pool size (default is the number of available processors), 
 * cause the tuning algorithm works well (i.e. converges) when it approaches the best value from the left.</p>
 * 
 * <p>The <a href = "http://codeidol.com/java/java-concurrency/Applying-Thread-Pools/Sizing-Thread-Pools/">theory</a> 
 * says that the optimal number of threads is given by the number of available processors and the ratio 
 * between task waiting time and real CPU consumption time. However things can be more complicated when task latencies
 * depend on how much they communicate or interfere each other, for instance by hitting transactions on the same database.
 * This is the rationale to base thread optimisation on live performance measurement.</p> 
 * 
 * <dl><dt>date</dt><dd>8 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class BatchService<TK extends BatchServiceTask>
{
	/**
	 * The current pools size (no of max parallel threads the service is running).
	 */
	private int threadPoolSize;

	/**
	 * This should be 1 if there are multiple and different exit codes returned by submitted tasks {@link BatchServiceTask#getExitCode()}.
	 * It should be a given value if all the submitted tasks returned that same value and it is non-zero.
	 * Should be 0 in all other cases. 
	 */
	protected int lastExitCode = 0;
	
	protected PoolSizeTuner poolSizeTuner = null;

	private ExecutorService executor;  

	private int busyTasks = 0;
	private long completedTasks = 0;
	private Lock submissionLock = new ReentrantLock ();
	private Condition freeThreadsCond = submissionLock.newCondition (), noTasksCond = submissionLock.newCondition ();

	private Timer notificationTimer = null;
	
	
	private Level submissionMsgLogLevel = Level.INFO;
	protected Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/**
	 * The custom {@link PoolSizeTuner} that is used to optimise this service. You probably will never need to extend this, 
	 * if you need to change the tuner parameters, use the field {@link BatchService#poolSizeTuner}.   
	 * 
	 * <dl><dt>date</dt><dd>8 Oct 2013</dd></dl>
	 * @author Marco Brandizi
	 *
	 */
	protected class BatchServiceTuner extends PoolSizeTuner
	{
		/**
		 * This is invoked by the tuner to change the service's pool size.
		 * 
		 * This implementation does some synchronisation work and you're strongly advised to never override it, unless 
		 * you're a masochist looking for deluxe treat...
		 */
		@Override
		protected void setThreadPoolSize ( int size ) 
		{
			BatchService.this.setThreadPoolSize ( size );
		}
		
		@Override
		public int getThreadPoolSize () {
			return BatchService.this.getThreadPoolSize ();
		}
		
		@Override
		public long getCompletedTasks () {
			return completedTasks;
		}
	}
	
	
	/**
	 * Defaults to {@link Runtime#availableProcessors() Runtime.getRuntime().availableProcessors()}
	 */
	public BatchService ()
	{
		this ( Runtime.getRuntime().availableProcessors() );
	}
	
	/** 
	 * Initialises a pool service with this number of initial threads.
	 */
	public BatchService ( int initialThreadPoolSize )
	{
		this.setThreadPoolSize ( initialThreadPoolSize );
		this.poolSizeTuner = this.newPoolSizeTuner ();
		this.executor = newThreadPoolExecutor ( initialThreadPoolSize );
	}
	
	
	/**
	 * Allows you to initialise with a custom {@link BatchServiceTuner}. In most cases you will be fine with the default. 
	 * If you need to change its tuning parameters, use the {@link #poolSizeTuner} field.
	 * 
	 * This is called by the {@link #BatchService(int)} constructor (and all the others).
	 * 
	 */
	protected BatchServiceTuner newPoolSizeTuner () {
		return new BatchServiceTuner ();
	}

	/**
	 * Allows you to initialise with a custom {@link ExecutorService}. WARNING: this class was designed and tested with
	 * {@link Executors#newFixedThreadPool(int) fixed pools} in mind. This method is supposed to instantiate variants
	 * of fixed thread pools, in other to accommodate specific needs. One example is when you need to give different 
	 * priorities to the tasks in a pool. We have {@link TaskComparator} for that, which can be instantiated from this
	 * method this way (method inspired to <a href = 'http://tinyurl.com/jjktm53'>this</a>)): 
	 * 
	 * <pre>
	 * return new ThreadPoolExecutor ( 
	 *   initialThreadPoolSize, initialThreadPoolSize, 0L, TimeUnit.MILLISECONDS,
   *   new PriorityBlockingQueue<Runnable> ( initialThreadPoolSize, new BatchServiceTask.TaskComparator () )
   * );
   * </pre>
	 * 
	 * Use executors other than fixed pool size at your own risk!
	 * 
	 * @param initialThreadPoolSize the initial thread pool size.
	 */
	protected ExecutorService newThreadPoolExecutor ( int initialThreadPoolSize ) {
		return Executors.newFixedThreadPool ( initialThreadPoolSize );
	}
	
	
	/**
	 * Submits a task into the pool, synchronising updates requested from {@link #poolSizeTuner} and other internal 
	 * state information.
	 */
	public void submit ( final TK batchServiceTask )
	{
		if ( poolSizeTuner != null && !poolSizeTuner.isActive () ) poolSizeTuner.start ();
		
		submissionLock.lock ();
		try
		{
			// Wait until the pool has available threads
			while ( busyTasks >= this.getThreadPoolSize () )
				try {
					freeThreadsCond.await ();
				}
				catch ( InterruptedException ex ) {
					throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
			}
	
			// Now submit a new task, decorated with releasing code
			//
			executor.submit ( new Runnable() 
			{
				public void run ()
				{
					try
					{
						Thread.currentThread ().setName ( batchServiceTask.getName () );
						batchServiceTask.run ();
					} 
					finally 
					{
						// Release (in the sense of marking there is one fewer thread and one more that finished) after service run
						//
						submissionLock.lock ();
						try
						{
							// Used by the pool size tuner and for stat purposes.
							completedTasks++;

							// keep track of the exit code. 
							int taskExitCode = batchServiceTask.getExitCode ();
							if ( taskExitCode != 0 ) {
								if ( lastExitCode == 0 ) lastExitCode = taskExitCode; else if ( lastExitCode != taskExitCode ) lastExitCode = 1;
							}
							
							// Decrease the no. of currently actually running tasks, broadcast the empty pool event.
							if ( --busyTasks < getThreadPoolSize () )
							{
								freeThreadsCond.signal ();
								if ( busyTasks == 0 ) noTasksCond.signalAll ();
							}
							
							log.trace ( 
								Thread.currentThread ().getName () + " released, " + busyTasks + " task(s) running, " 
								+ completedTasks + ", completed" 
							);
						}
						finally {
							submissionLock.unlock ();
						}
						
					} // run().finally
				} // run()
			}); // decorated runnable and submit() opeation
			
			// Update data about the submission of a new thread that just happened
			busyTasks++;
			log.log ( 
				this.submissionMsgLogLevel,
				"Submitted: " + batchServiceTask.getName () + ", " + busyTasks + " task(s) running, " 
				+ completedTasks + " completed, please wait" 
			);
			if ( !this.log.isEnabled ( this.submissionMsgLogLevel ) && this.notificationTimer == null )
				this.initNotificationTimer ();
		} // try{} on submissionLock  
		finally {
			submissionLock.unlock ();
		}
	} // submit()
	
	
	
	/**
	 * This can be used after you have submitted all the tasks that you have to run, when you want to wait that all of them 
	 * complete their execution. The method starts a {@link #initNotificationTimer() timer} that reports the current state (with INFO log messages). 
	 *  
	 */
	public void waitAllFinished ()
	{
		// I'm alive message
		if ( this.notificationTimer == null ) this.initNotificationTimer ();

		// no-tasks condition, which is triggered by the code wrapping the task in submit()
		submissionLock.lock ();
		try 
		{
			while ( this.busyTasks > 0 )
				noTasksCond.await ();
		}	
		catch ( InterruptedException ex ) {
			throw new RuntimeException ( "Internal error with multi-threading: " + ex.getMessage (), ex );
		}
		finally 
		{
			submissionLock.unlock ();
			notificationTimer.cancel ();
		}
	}
	
	/**
	 * Initialises an internal timer, which notifies about current service activity (running tasks, completed tasks
	 * etc) every 60 secs. This is enabled when {@link #getSubmissionMsgLogLevel()} is not currently enabled in the
	 * logging system, and during {@link #waitAllFinished()}.
	 *   
	 */
	private void initNotificationTimer ()
	{
		this.notificationTimer = new Timer ( this.getClass ().getSimpleName () + "/Notifier" );
		this.notificationTimer.scheduleAtFixedRate ( new TimerTask() {
			@Override
			public void run () {
				log.info ( "" + busyTasks + " task(s) running, " + completedTasks + " completed, please wait" );
			}
		}, 5 * 60000, 5 * 60000 );		
	}
	
	
	/**
	 * The no of completed tasks. This method is not synchronised, so you might get a number slightly lower than the real
	 * one. 
	 */
	public long getCompletedTasks () {
		return this.completedTasks;
	}
	
	/**
	 * Used to dynamically adjust the no. threads that the service runs in parallel. Most cases you will be fine with 
	 * the {@link BatchServiceTuner default implementation of this}. If not, you should set this field in a  
	 * {@link #BatchService(int, PoolSizeTuner) constructor}.
	 * 
	 */
	public PoolSizeTuner getPoolSizeTuner ()
	{
		return poolSizeTuner;
	}

	public int getThreadPoolSize ()
	{
		submissionLock.lock ();
		try {
			return threadPoolSize;
		}
		finally {
			submissionLock.unlock ();
		}
	}
	
	public void setThreadPoolSize ( int threadPoolSize )
	{
		submissionLock.lock ();
		try 
		{
			this.threadPoolSize = threadPoolSize;
			if ( BatchService.this.executor == null ) return; 
			
			((ThreadPoolExecutor) executor ).setCorePoolSize ( threadPoolSize );
			((ThreadPoolExecutor) executor ).setMaximumPoolSize ( threadPoolSize );
		}
		finally {
			submissionLock.unlock ();
		}
	}

	public int getLastExitCode ()
	{
		return lastExitCode;
	}

	public int getBusyTasks ()
	{
		submissionLock.lock ();
		try {
			return busyTasks;
		}
		finally {
			submissionLock.unlock ();
		}
	}

	/**
	 * The submission of a new task is notified to the logging system via this level ({@link Level#INFO} by default).
	 * 
	 * If your application has very many tasks and you don't want to get bothered with these messages, you can 
	 * change the log granularity here. If the level you set is disabled, this class will instead log 
	 * a {@link #initNotificationTimer() notification} from time to time. 
	 *  
	 */
	public Level getSubmissionMsgLogLevel () {
		return submissionMsgLogLevel;
	}

	public void setSubmissionMsgLogLevel ( Level submissionMsgLogLevel ) {
		this.submissionMsgLogLevel = submissionMsgLogLevel;
	}
	
	
	/**
	 * It's like the {@link ThreadPoolExecutor#setThreadFactory(ThreadFactory)} and might be useful here as well.
	 */
	public void setThreadFactory ( ThreadFactory threadFactory ) {
		((ThreadPoolExecutor) executor).setThreadFactory ( threadFactory );
	}
	
	

	/**
	 * Stops {@link #poolSizeTuner}.
	 */
	@Override
	protected void finalize () throws Throwable
	{
		if ( poolSizeTuner != null ) this.poolSizeTuner.stop ();
		super.finalize ();
	}
}
