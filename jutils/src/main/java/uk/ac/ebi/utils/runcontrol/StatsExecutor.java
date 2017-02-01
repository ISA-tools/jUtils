package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.time.XStopWatch;

/**
 * An executor logs statistics on the executions it receive to run.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>5 Oct 2015</dd></dl>
 *
 */
public class StatsExecutor implements Executor
{
	private long samplingTime;
	private String serviceName = "[Unspecified]";
	private boolean popUpExceptions = true;
	
	private AtomicInteger totalCalls = new AtomicInteger ( 0 ), failedCalls = new AtomicInteger ( 0 );
	private int lastTotalCalls = 0, lastFailedCalls = 0;

	private XStopWatch timer = new XStopWatch ();
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	
	public StatsExecutor ( String serviceName, long samplingTime )
	{
		this.samplingTime = samplingTime;
		this.serviceName = serviceName;
	}

	public StatsExecutor ( String serviceName ) {
		 this ( serviceName, 5 * 60 * 1000 );
	}
	
	/**
	 * Runs the action, intercepts any {@link Exception} it generates. increase {@link #getTotalCalls()} and,
	 * if there is an exception, {@link #getFailedCalls()} too. Eventually invokes {@link #doStats()}.
	 * 
	 * If {@link #isPopUpExceptions()} exceptions are let to reach the invoker. 
	 */
	@Override
	public void execute ( Runnable action )
	{
		try {
			action.run ();
		}
		catch ( Exception ex ) 
		{
			this.failedCalls.incrementAndGet ();
			if ( this.popUpExceptions ) throw ex;
			
			log.warn ( "Call to {} failed, due to: {}", this.serviceName, ex.getMessage () );
			if ( log.isTraceEnabled () ) log.trace ( "Call to " + this.serviceName + ", reason:", ex );
		}
		finally {
			this.totalCalls.incrementAndGet ();
		}
		doStats ();
	}	
	
	/**
	 * Checks if {@link #getSamplingTime()} has passed. If yes, logs statistics on recorded calls/min and failed calls.
	 */
	protected synchronized boolean doStats ()
	{
		if ( this.timer.isStopped () ) {
			// First call
			timer.start ();
			return false;
		}
		
		if ( this.timer.getTime () < this.samplingTime ) return false;
		
		int totalCalls = this.totalCalls.get ();
		int failedCalls = this.failedCalls.get ();
		
		double avgCalls = 1d * totalCalls / this.samplingTime;  
		double avgFails = totalCalls == 0  
			? 0d
			: 1d * this.failedCalls.get () / this.totalCalls.get ();

		log.info ( String.format ( 
			"---- %s Statistics, throughput: %.0f calls/min, failed: %.1f %%",
			serviceName,
			avgCalls * 60000, avgFails * 100
		));
		
		this.lastTotalCalls = totalCalls;
		this.lastFailedCalls = failedCalls;
		this.totalCalls.set ( 0 ); 
		this.failedCalls.set ( 0 );
		timer.restart ();
		return true;
	}

	/**
	 * This is reset every {@link #getSamplingTime()} ms.
	 */
	protected synchronized int getTotalCalls ()
	{
		return totalCalls.get ();
	}

	/**
	 * This is reset every {@link #getSamplingTime()} ms.
	 */
	protected synchronized int getFailedCalls ()
	{
		return failedCalls.get ();
	}	
	
	/**
	 * This is updated every {@link #getSamplingTime()}
	 */
	public synchronized int getLastTotalCalls ()
	{
		return lastTotalCalls;
	}

	/**
	 * This is updated every {@link #getSamplingTime()}
	 */
	public synchronized int getLastFailedCalls ()
	{
		return lastFailedCalls;
	}

	
	/**
	 * @see #execute(Runnable). If true, exceptions are let to reach the invoker. If you have a service that is known
	 * to fail, you may want to just record failures and let your program to continue, e.g., with null result.
	 */
	public boolean isPopUpExceptions ()
	{
		return popUpExceptions;
	}

	public StatsExecutor setPopUpExceptions ( boolean popUpExceptions )
	{
		this.popUpExceptions = popUpExceptions;
		return this;
	}

	/**
	 * Statistics are logged and then reset every this amount of time, in ms. 
	 */
	public long getSamplingTime ()
	{
		return samplingTime;
	}

	public void setSamplingTime ( long samplingTime )
	{
		this.samplingTime = samplingTime;
	}
		
}
