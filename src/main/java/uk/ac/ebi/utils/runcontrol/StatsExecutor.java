package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.time.XStopWatch;

/**
 * An executor logs statistics on the executions it receive to run.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>5 Oct 2015</dd>
 *
 */
public class StatsExecutor implements Executor
{
	private long samplingTime;
	private String serviceName = "[Unspecified]";
	private boolean popUpExceptions = true;
	
	private int totalCalls = 0, failedCalls = 0;
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
			this.failedCalls++;
			if ( this.popUpExceptions )
				throw ex;
			
			log.warn ( "Call to {} failed, due to: {}", this.serviceName, ex.getMessage () );
			if ( log.isTraceEnabled () ) log.trace ( "Call to " + this.serviceName + ", reason:", ex );
		}
		finally {
			this.totalCalls++;
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
		
		double avgCalls = 1.0 * this.totalCalls / this.samplingTime;  
		double avgFails = this.totalCalls == 0  
			? 0d
			: 1.0 * this.failedCalls / this.totalCalls;

		log.info ( String.format ( 
			"---- %s Statistics, throughput: %.0f calls/min, failed: %.1f %%",
			serviceName,
			avgCalls * 60000, avgFails * 100
		));
		
		this.totalCalls = this.failedCalls = 0;
		timer.restart ();
		return true;
	}

	/**
	 * This is reset every {@link #getSamplingTime()} ms.
	 */
	public synchronized int getTotalCalls ()
	{
		return totalCalls;
	}

	/**
	 * This is reset every {@link #getSamplingTime()} ms.
	 */
	public synchronized int getFailedCalls ()
	{
		return failedCalls;
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
