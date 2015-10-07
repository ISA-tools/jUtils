package uk.ac.ebi.utils.runcontrol;

import com.google.common.util.concurrent.RateLimiter;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>5 Oct 2015</dd>
 *
 */
public class RateLimitedInvoker implements WrappedInvoker<Boolean>
{
	protected RateLimiter rateLimiter = null;
	
	public RateLimitedInvoker ( double requestsPerSecond ) {
		this.rateLimiter = RateLimiter.create ( requestsPerSecond );
	}

	
	@Override
	public Boolean run ( Runnable action )
	{
		boolean result = this.rateLimiter.acquire () > 0;
		
		action.run ();
		return result;
	}
	
	public double getRate () {
		return this.rateLimiter.getRate ();
	}
	
	public void setRate ( double requestsPerSecond ) {
		this.rateLimiter.setRate ( requestsPerSecond );
	}
	
}
