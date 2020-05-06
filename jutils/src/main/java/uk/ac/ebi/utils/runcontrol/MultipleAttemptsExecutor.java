package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.Executor;

import org.apache.commons.lang3.RandomUtils;

import com.machinezoo.noexception.throwing.ThrowingRunnable;

import uk.ac.ebi.utils.exceptions.UncheckedInterruptedException;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jext.Logger;
import uk.org.lidalia.slf4jext.LoggerFactory;


/**
 * An executor to attempt an operation multiple times, knowing it can fail from time to time (e.g. getting data from
 * a REST API).
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Oct 2015</dd></dl>
 *
 */
public class MultipleAttemptsExecutor implements Executor
{
	private int maxAttempts = 3;
	private long maxPauseTime = 3000;
	private long minPauseTime = 0;
	
	private Class<Exception>[] interceptedExceptions;
	
	private Level attemptMsgLogLevel = Level.INFO;
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@SafeVarargs
	@SuppressWarnings ( "unchecked" )
	public MultipleAttemptsExecutor ( 
		int maxAttempts, long minPauseTimeMs, long maxPauseTimeMs, Class<? extends Exception> ...interceptedExceptions 
	)
	{
		this.maxAttempts = maxAttempts;
		this.minPauseTime = minPauseTimeMs;
		this.maxPauseTime = maxPauseTimeMs;
		this.interceptedExceptions = ( Class<Exception>[] ) interceptedExceptions;
	}

	@SafeVarargs
	public MultipleAttemptsExecutor ( Class<? extends Exception> ...interceptedExceptions ) {
		this ( 3, 0, 3000, interceptedExceptions );
	}

	/**
	 * Overrides {@link Executor#execute(Runnable)}, by calling {@link #executeChecked(Runnable)}.
	 * and intercepting the thrown checked exception. 
	 */
	@Override
	public void execute ( Runnable action )
	{
		try {
			executeChecked ( action::run );
		}
		catch ( Exception ex ) 
		{
			if ( ex instanceof RuntimeException ) throw (RuntimeException) ex;
			// Shouldn't happen, but just in case
			throw new RuntimeException ( 
				"Error while running " + this.getClass ().getSimpleName () + ": " + ex.getMessage (), 
				ex
			);
		}
	}

	
	/**
	 * Tries to run the action and, if it fails with one of {@link #getInterceptedExceptions()}, re-run it up to 
	 * {@link #getMaxAttempts()}. A pause with a random time between {@link #getMinPauseTime()} and {@link #getMaxPauseTime()}
	 * is inserted between attempts, so that, in case of race conditions between parallel threads, they don't 
	 * re-attempt the same conflicting operation at the same time.
	 * 
	 * This version is based on the possibility that a checked exception occurs, use {@link #execute(Runnable)}
	 * when you have only unchecked exceptions. 
	 */
	public void executeChecked ( ThrowingRunnable action ) throws Exception
	{
		try
		{
			int attempts = 0; 
			Exception lastInterceptedEx = null; 
			
			for ( attempts = this.maxAttempts; attempts > 0; attempts-- )
			{
				try {
					action.run ();
					break;
				}
				catch ( RuntimeException ex ) 
				{
					boolean mustReattempt = false;
					lastInterceptedEx = ex;
					
					for ( Class<Exception> exi: interceptedExceptions )
					{
						if ( exi.isAssignableFrom ( ex.getClass () ) ) 
						{
							log.log ( attemptMsgLogLevel,
								"Operation failure due to: {}, re-attempting for {} more time(s)", ex.getMessage (), attempts - 1
							);
					
							mustReattempt = true;
							break;
						}
					}
					if ( !mustReattempt )
						throw ex;

					// Let's pause
					if ( this.maxPauseTime - this.minPauseTime > 0 )
						Thread.sleep ( RandomUtils.nextLong ( this.minPauseTime, this.maxPauseTime + 1 ) );
					
				} // catch attempt
			} // attempts
			
			if ( attempts == 0 && lastInterceptedEx != null ) {
				log.error ( "Operation failed after {} attempts, rethrowing exception", this.maxAttempts );
				throw lastInterceptedEx;
			}
		
		} // outer try
		catch ( InterruptedException ex ) {
			throw new UncheckedInterruptedException ( "Internal error: " + ex.getMessage (), ex );
		}
	} // execute ( action )

	/**
	 * If the operation run by {@link #execute(Runnable)} fails even after this number of times, the exception it 
	 * raises is re-thrown to the caller. Default is 3.
	 */
	public int getMaxAttempts ()
	{
		return maxAttempts;
	}


	public void setMaxAttempts ( int maxAttempts )
	{
		this.maxAttempts = maxAttempts;
	}

	
	/**
	 * After a failed attempt, {@link #execute(Runnable)} pauses for a random time between this and {@link #getMaxPauseTime()}
	 * ms. This may be useful for those operations that have concurrent access problems (it's a brutal way to cope with 
	 * them, but might be reasonable sometime). Default is 0. If these two values are both 0, no pause occurs between
	 * attempts.
	 * 
	 */
	public long getMinPauseTime ()
	{
		return minPauseTime;
	}


	public void setMinPauseTime ( long minPauseTimeMs )
	{
		this.minPauseTime = minPauseTimeMs;
	}

	/**
	 * @see #getMinPauseTime(). Default is 3000.
	 */
	public long getMaxPauseTime ()
	{
		return maxPauseTime;
	}


	public void setMaxPauseTime ( long maxPauseTimeMs )
	{
		this.maxPauseTime = maxPauseTimeMs;
	}

	/**
	 * {@link #execute(Runnable)} considers only these exceptions (or their subclasses), when evaluating if a failed
	 * operation has to be re-attempted. Any other exception is re-thrown to the caller and the operation is not 
	 * re-attempted. 
	 * 
	 */
	public Class<Exception>[] getInterceptedExceptions ()
	{
		return interceptedExceptions;
	}


	@SuppressWarnings ( "unchecked" )
	public void setInterceptedExceptions ( Class<? extends Exception>[] interceptedExceptions )
	{
		this.interceptedExceptions = (Class<Exception>[]) interceptedExceptions;
	}

	/**
	 * Failed attempts are logged with this logging level. Default is INFO.
	 */
	public Level getAttemptMsgLogLevel ()
	{
		return attemptMsgLogLevel;
	}


	public void setAttemptMsgLogLevel ( Level attemptMsgLogLevel )
	{
		this.attemptMsgLogLevel = attemptMsgLogLevel;
	}
	
}
