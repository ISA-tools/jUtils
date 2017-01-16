package uk.ac.ebi.utils.runcontrol;

/**
 * Like {@link RateLimitedExecutor}, but contains a hook where you can dynamically decide which new rate you want to
 * set. This can be useful to dynamically regulate the throughput of a service, eg, based on its efficiency, measure 
 * via {@link StatsExecutor}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Oct 2015</dd></dl>
 *
 */
public abstract class DynamicRateExecutor extends RateLimitedExecutor
{
	/**
	 * Default is to start with no rate limitation, hence {@link Double#MAX_VALUE}. 
	 */
	public DynamicRateExecutor ()
	{
		this ( Double.MAX_VALUE );
	}
	
	public DynamicRateExecutor ( double requestsPerSecond )
	{
		super ( requestsPerSecond );
	}
	
	@Override
	public void execute ( Runnable action )
	{
		double newRate = setNewRate ();
		
		synchronized ( this.rateLimiter ) 
		{
			double oldRate = this.getRate ();
			// the rate limiter tends to recalculate the rate parameter, so we need to
			// compare them with some tolerance
			if ( oldRate == 0 || Math.abs ( newRate / oldRate - 1 ) > 1d/1000 ) 
				this.setRate ( newRate );
		}
		
		super.execute ( action );
	}

	/**
	 * Determines the possible new rate, e.g., based on current performance measurement. Note that this 
	 * is highest when {@link Double#MAX_VALUE} is set, 0 or negative values are not accepted.
	 * 
	 * Note that the new rate must be different than the old one by more than 0.1%, otherwise no change takes effect. 
	 */
	protected abstract double setNewRate ();	
}
