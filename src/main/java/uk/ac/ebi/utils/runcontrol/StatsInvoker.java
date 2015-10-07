package uk.ac.ebi.utils.runcontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.time.XStopWatch;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>5 Oct 2015</dd>
 *
 */
public class StatsInvoker implements WrappedInvoker<Boolean>
{
	private long samplingTime;
	private String serviceName = "[Unspecified]";
	private boolean popUpExceptions = true;
	
	private int totalCalls = 0, failedCalls = 0;
	private XStopWatch timer = new XStopWatch ();
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	
	public StatsInvoker ( String serviceName, long samplingTime )
	{
		this.samplingTime = samplingTime;
		this.serviceName = serviceName;
	}

	public StatsInvoker ( String serviceName ) {
		 this ( serviceName, 5 * 60 * 1000 );
	}
	
	@Override
	public Boolean run ( Runnable action )
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
		return doStats ();
	}	
	
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

	public synchronized int getTotalCalls ()
	{
		return totalCalls;
	}

	public synchronized int getFailedCalls ()
	{
		return failedCalls;
	}

	public boolean isPopUpExceptions ()
	{
		return popUpExceptions;
	}

	public StatsInvoker setPopUpExceptions ( boolean popUpExceptions )
	{
		this.popUpExceptions = popUpExceptions;
		return this;
	}
	
}
