package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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
	protected double requestsPerSecond = 0;
	protected RateLimiter rateLimiter = null;
	
	public RateLimitedInvoker ( double requestsPerSecond ) {
		this.requestsPerSecond = requestsPerSecond;
	}

	
	@Override
	public Boolean run ( Runnable action )
	{
		if ( this.rateLimiter == null ) {
			this.rateLimiter = RateLimiter.create ( requestsPerSecond );
			return false;
		}
		boolean result = rateLimiter.acquire () > 0;
		action.run ();
		return result;
	}
	
}
