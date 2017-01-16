package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.Executor;

import com.google.common.util.concurrent.RateLimiter;

/**
 * A rate limiting executor, based on {@link RateLimiter}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>5 Oct 2015</dd></dl>
 *
 */
public class RateLimitedExecutor implements Executor
{
	protected RateLimiter rateLimiter = null;
	
	/**
	 * @see #getRate()
	 */
	public RateLimitedExecutor ( double requestsPerSecond ) {
		this.rateLimiter = RateLimiter.create ( requestsPerSecond );
	}

	/**
	 * Controls the execution of the action in such a way that it is run at no more than {@link #getRate()} speed within 
	 * a given JVM. This is useful when you have a service that is hurt if hammered too fast.
	 */
	public void execute ( Runnable action )
	{
		this.rateLimiter.acquire ();
		action.run ();
	}
	
	/**
	 * The maximum speed allowed by this executor, in actions received per second.
	 * 0 means no limit is applied. 
	 */
	public double getRate () {
		return this.rateLimiter.getRate ();
	}
	
	/**
	 * Note that the new actual rate might be slightly different than the passed parameters, dependa on how 
	 * {@link RateLimiter} is implemented. 
	 */
	public void setRate ( double requestsPerSecond ) 
	{
		this.rateLimiter.setRate ( requestsPerSecond );
	}	
}
