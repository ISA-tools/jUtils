package uk.ac.ebi.utils.threading;


import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pool-based thread execution service, that dynamically optimises its size.
 * 
 * This is a base class that allow you to manage the execution of a number of {@link BatchServiceTask tasks} in parallel.
 * This is achieved by means of a thread pool, which of size is dynamically adjusted via {@link PoolSizeTuner}, 
 * in order to optimise the task throughput (i.e., the number of tasks that complete their execution in the unit of time).
 *
 * You should initialise this class with a moderate initial pool size (default is 40 threads), cause the tuning algorithm
 * works well (i.e. converges) when it approaches the best value from the left. 
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
	protected int threadPoolSize;

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
	private Condition freeTasksCond = submissionLock.newCondition (), noTasksCond = submissionLock.newCondition ();
	
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
			submissionLock.lock ();
			threadPoolSize = size;
			((ThreadPoolExecutor) executor ).setCorePoolSize ( size );
			((ThreadPoolExecutor) executor ).setMaximumPoolSize ( size );
			submissionLock.unlock ();
		}
		
		@Override
		public int getThreadPoolSize () {
			return threadPoolSize;
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

	/** Initialises a pool service with this number of initial threads. */
	public BatchService ( int initialThreadPoolSize ) 
	{
		this.threadPoolSize = initialThreadPoolSize;
		this.poolSizeTuner = new BatchServiceTuner ();
		this.executor = Executors.newFixedThreadPool ( threadPoolSize );
	}
	
	/** 
	 * Initialises a pool service with this number of initial threads and a custom {@link PoolSizeTuner}. 
	 * In most cases you will be fine with the default {@link BatchServiceTuner}. If you need to change its tuning 
	 * parameters, use the {@link #poolSizeTuner} field.
	 */
	public BatchService ( int initialThreadPoolSize, PoolSizeTuner poolSizeTuner )
	{
		this.threadPoolSize = initialThreadPoolSize;
		this.poolSizeTuner = poolSizeTuner;
		this.executor = Executors.newFixedThreadPool ( threadPoolSize );
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
			while ( busyTasks >= threadPoolSize )
				try {
					freeTasksCond.await ();
				}
				catch ( InterruptedException ex ) {
					throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
			}
			busyTasks++;
			log.info ( 
				"Submitted: " + batchServiceTask.getName () + ", " + busyTasks + " task(s) running, " 
				+ completedTasks + " completed, please wait" 
			);
	
			// Now submit a new task, decorated with releasing code
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
						// Release after service run 
						submissionLock.lock ();
						try
						{
							// keep track of the exit code. 
							int taskExitCode = batchServiceTask.getExitCode ();
							if ( taskExitCode != 0 ) {
								if ( lastExitCode == 0 ) lastExitCode = taskExitCode; else if ( lastExitCode != taskExitCode ) lastExitCode = 1;
							}
							
							// Decrease the no. of currently actually running tasks, broadcast the empty pool event.
							if ( --busyTasks < threadPoolSize ) 
							{
								freeTasksCond.signal ();
								if ( busyTasks == 0 ) noTasksCond.signalAll ();
							}
							
							// Used by the pool size tuner and for stat purposes.
							completedTasks++;
							
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
			}); // decorated runnable
		} // try on submissionLock  
		finally {
			submissionLock.unlock ();
		}
	} // submit()
	
	
	
	/**
	 * This can be used this after you have submitted all the tasks that you have to run, to wait until all of them 
	 * complete their execution. The method starts a timer that reports the current state (with INFO log messages). 
	 *  
	 */
	public void waitAllFinished ()
	{
		// I'm alive message
		Timer notificationTimer = new Timer ( this.getClass ().getSimpleName () + " Alive Notification" );
		notificationTimer.scheduleAtFixedRate ( new TimerTask() {
			@Override
			public void run () {
				log.info ( "" + busyTasks + " task(s) still running, " + completedTasks + " completed, please wait" );
			}
		}, 60000, 60000 );

		// no-task condition, which is triggered by the code wrapping task in submit()
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
