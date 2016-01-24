package uk.ac.ebi.utils.threading;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>A Thread Pool Size Optimiser</p>
 * 
 * <p>Implements a simple algorithm to optimise a thread pool, based on periodic measured throughput (i.e., the no of 
 * threads that completed between one measurement step and another. See below for details.</p>
 * 
 * <p>You should start the tuning (e.g., via {@link BatchService}) with a moderate initial pool size, cause the 
 * tuning algorithm works well (i.e. converges) when it approaches the best value from the left.</p> 
 *
 * <dl><dt>date</dt><dd>8 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class PoolSizeTuner
{
	private int minThreads = 5, maxThreads = 200, maxThreadIncr = 50, minThreadIncr = 5;
	private double threadDeltaTolerance = 10d/100d;
	
	/**
	 * How much the thread pool size should be increased the next time that the {@link #run() monitoring step} runs. 
	 * The initial value for this this is adjusted depending on the measurement throughput gradient (see #run() below).
	 * 
	 * It can be either negative (decreases), positive or zero (i.e., no change occurred in the last execution).
	 * 
	 */
	private int threadIncr;
	
	private int periodMsecs = 5 * 60 * 1000;

	/**
	 * the pool size seen the last time {@link #run()} was ran.
	 */
	private int prevThreadPoolSize;
	
	/**
	 * The thread throughput (no. of completed tasks in the time {@link #getPeriodMSecs()} measured the last time
	 * {@link #run()} was ran.
	 */
	private long prevThroughput;

	/**
	 * The total number of tasks completed the last time {@link #run()} was ran.
	 */
	private long prevCompletedTasks;
	
	/**
	 * The Java {@link Timer} used to run this periodically, see {@link #start()}.
	 */
	private Timer poolSizeTunerTimer = null;
	
	protected Logger log = LoggerFactory.getLogger ( this.getClass () );

	
	/**
	 * <p>The monitoring step.</p>
	 * 
	 * <p>This is executed after {@link #start()}, every {@link #getPeriodMSecs()} minutes. It does a simple thread pool
	 * size optimisation, based on the measured throughput given by of the current size. In turn, this is done computing
	 * gradient quantities over the last time this monitoring step ran.</p>
	 *  
	 * <p>{@link #getThreadPoolSize()}, {@link #getCompletedTasks()} are used to evaluate such gradient quantities.
	 * {@link #setThreadPoolSize(int)} is used to implement the computed new thread pool size.</p>
	 * 
	 * <p>The algorithm used to get such new size is very simple:
	 * <ul>
	 * 	<li>if the throughput increased 
	 *    <ul>
	 *      <li>and the pools size was made bigger: keep increasing the pool size (but not bigger than {@link #getMaxThreads()}, and
	 *          make even a bigger {@link #threadIncr size increase} next time (but not more than {@link #getMaxThreadIncr()}.</li>
	 *      <li>else, if the pool size was shrunk: keep making it smaller (but not smaller than {@link #getMinThreads()}, and
	 *          make even a bigger {@link #threadIncr size decrease} next time (but not more than -{@link #getMaxThreadIncr()})</li>
	 *    </ul>
	 *  </li>
	 * </ul>
	 * 
	 * Something similar is done when the throughput decreases since the last check.</p>
   *   
	 */
	private void run ()
	{
		final long curCompletedTasks = getCompletedTasks ();
		final long curThru = curCompletedTasks - prevCompletedTasks;
		final long deltaThru = curThru - prevThroughput;
		
		final double relDeltaThru = (double) deltaThru / prevThroughput;
		
		int curThreadPoolSize = getThreadPoolSize ();
		
		if ( deltaThru > 0 && relDeltaThru > threadDeltaTolerance )
		{
			if ( threadIncr > 0 )
			{
				// Throughput increased after a thread pool enlargement, let's enlarge it again
				setThreadPoolSize ( min ( curThreadPoolSize + threadIncr, maxThreads ) );
				threadIncr = min ( 2 * threadIncr, maxThreadIncr );
			}
			else if ( threadIncr < 0 )
			{
				// Throughput increased after a thread pool shrinking, let's shrink it again
				setThreadPoolSize ( max ( curThreadPoolSize + threadIncr, minThreads ) );
				threadIncr = - min ( - 2 * threadIncr, maxThreadIncr );
			}
			else // threadIncr == 0 
				// throughput didn't increase in reaction to pool size variation, let's see if an increase amplifies this
				setThreadPoolSize ( min ( curThreadPoolSize + ( threadIncr = minThreadIncr ), maxThreads ) );
		}
		else if ( deltaThru < 0 && -relDeltaThru > threadDeltaTolerance )
		{
			// The optimal is likely in between, do an average using the throughputs as weights
			int newPoolSize = round ( 
				( 1f * prevThreadPoolSize * prevThroughput + curThreadPoolSize * curThru ) / ( prevThroughput + curThru ) );
			setThreadPoolSize ( (int) newPoolSize );

			if ( threadIncr > 0 )
			{
				// Throughput got worse after a thread pool enlargement, let's go back to smaller size decreases
				threadIncr = - max ( round ( threadIncr / 2f ), minThreadIncr );
			}
			else if ( threadIncr < 0 )
			{
				// Throughput got worse after a thread pool shrinking, let's start enlarging it
				threadIncr = min ( - 2 * threadIncr, maxThreadIncr );
				//threadIncr = min ( - round ( threadIncr / 2f ), maxThreadIncr );
			}
			else // threadIncr == 0 
				// throughput didn't decrease in reaction to pool size variation, let's see if a decrease can mitigate this
				setThreadPoolSize ( max ( curThreadPoolSize + ( threadIncr = -minThreadIncr ), minThreads ) );
		}
		else
			// No significant throughput variation observed, let's zero the current thread increment and let's leave the pool
			// size as it is
			threadIncr = 0;

		if ( log.isTraceEnabled () )
			log.trace ( String.format ( 
				"Pool Size Tuner, throughput: %d (%.2f%%), new increment: %d, new pool size: %s", 
				curThru, relDeltaThru * 100, threadIncr, getThreadPoolSize () 
		));

		prevCompletedTasks = curCompletedTasks;
		prevThroughput = curThru;
		
		prevThreadPoolSize = curThreadPoolSize;
	}
	
	public abstract int getThreadPoolSize ();
	protected abstract void setThreadPoolSize ( int size );
	public abstract long getCompletedTasks ();

	/**
	 * Starts the periodic monitoring of the thread pool and its dynamic adjustment.
	 */
	public void start ()
	{
		validateParameters ();
		
		log.trace ( "Starting the thread pool tuner" );
		this.stop (); // Be sure it's off
		
		initVariables ();
		poolSizeTunerTimer = new Timer ( "BatchService Pool Size Optimiser" );
		poolSizeTunerTimer.scheduleAtFixedRate
		( 
			new TimerTask() 
			{
				@Override
				public void run () {
					PoolSizeTuner.this.run ();
				}
			}, 
			periodMsecs, periodMsecs 
		);
	}
	
	/**
	 * Stop the periodic monitoring of the thread pool.
	 * 
	 */
	public void stop ()
	{
		if ( !this.isActive () ) return; 
		poolSizeTunerTimer.cancel ();
		poolSizeTunerTimer = null;
		log.trace ( "Thread pool tuner stopped" );
	}
	
	/**
	 * True if I was started with {@link #start()} and never {@link #stop()}ped.
	 */
	public boolean isActive () {
		return this.poolSizeTunerTimer != null;
	}
	
	private void initVariables ()
	{
		threadIncr = Math.round ( ( minThreadIncr + maxThreadIncr ) / 3.0f );

		prevThreadPoolSize = getThreadPoolSize ();
		prevThroughput = 0; prevCompletedTasks = 0;
	}
	
	private void validateParameters ()
	{
		if ( minThreads <= 0 ) throw new IllegalArgumentException ( "minThreads parameter should be a positive integer" );
		if ( maxThreads < minThreads ) throw new IllegalArgumentException ( "maxThreads parameter should be >= minThreads" );

		if ( maxThreadIncr <= 0 ) throw new IllegalArgumentException ( "maxThreadIncr parameter should be a positive integer" );
		if ( maxThreadIncr < minThreadIncr ) throw new IllegalArgumentException ( "maxThreadIncr parameter should be >= minThreadIncr" );

		if ( periodMsecs <= 0 ) throw new IllegalArgumentException ( "periodMsecs parameter should be a positive integer" );
}
	
	/**
	 * The minimum number of threads that the thread pool should always contain. See the source code for defaults.
	 */
	public int getMinThreads ()
	{
		return minThreads;
	}

	public void setMinThreads ( int minThreads )
	{
		this.minThreads = minThreads;
	}

	/**
	 * The maximum number of threads that the thread pool can contain. See the source code for defaults.
	 */
	public int getMaxThreads ()
	{
		return maxThreads;
	}

	public void setMaxThreads ( int maxThreads )
	{
		this.maxThreads = maxThreads;
	}

	/**
	 * How much the thread pool size can be incremented at every {@link #run() optimisation iteration}.
	 * This should be a positive value.
	 * 
	 */
	public int getMaxThreadIncr ()
	{
		return maxThreadIncr;
	}

	public void setMaxThreadIncr ( int maxThreadIncr )
	{
		this.maxThreadIncr = maxThreadIncr;
	}

	/**
	 * How muc the thread pool size can be shrunk at every {@link #run() optimisation iteration}.
	 * This should be a positive value.
	 */
	public int getMinThreadIncr ()
	{
		return minThreadIncr;
	}

	public void setMinThreadIncr ( int minThreadIncr )
	{
		this.minThreadIncr = minThreadIncr;
	}

	/**
	 * The time between {@link #run() optimisation step} executions. Should be positive.
	 */
	public int getPeriodMSecs ()
	{
		return periodMsecs;
	}

	public void setPeriodMSecs ( int periodMSecs )
	{
		this.periodMsecs = periodMSecs;
	}

	/**
	 * If the number of tasks between one {@link #run() optimisation step} and another remained within this tolerance, 
	 * then the thread pool throughput is considered stable and no action is taken over the pool size.
	 */
	public double getThreadDeltaTolerance ()
	{
		return threadDeltaTolerance;
	}

	public void setThreadDeltaTolerance ( double threadDeltaTolerance )
	{
		this.threadDeltaTolerance = threadDeltaTolerance;
	}
	
	
}
