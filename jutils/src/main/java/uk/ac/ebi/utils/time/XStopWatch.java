package uk.ac.ebi.utils.time;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

/**
 * A {@link StopWatch} with extended functionality.
 *
 * <dl><dt>date</dt><dd>9 Apr 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class XStopWatch extends StopWatch
{
	/**
	 * If {@link #isSuspended()} invokes {@link #resume()} as usually. If {@link #isStopped()} calls {@link #start()}, 
	 * if neither is true, it means it's already started and hence does nothing.
	 */
	public void resumeOrStart ()
	{
		if ( isStopped () )
		{
			this.reset ();
			this.start ();
		}
		else if ( isSuspended () ) this.resume ();
		// or, it is already running
	}
	
	/**
	 * Invokes {@link #reset()} and then {@link #start()}, ie, start a new timing session.
	 */
	public void restart () 
	{
		this.reset ();
		this.start ();
	}

	/**
	 * Facility to profile (i.e., to time) a task.
	 * 
	 * @return the time elapsed between before and after {@link Runnable#run() task.run()}. 
	 */
	public static long profileNano ( Runnable task ) 
	{
		XStopWatch timer = new XStopWatch ();
		timer.start ();
		task.run ();
		return timer.getNanoTime ();
	}

	/**
	 * Wrapper of {@link #profileNano(Runnable)} returning time in ms.
	 */
	public static long profile ( Runnable task ) 
	{
		return TimeUnit.NANOSECONDS.toMillis ( profileNano ( task ) );
	}
}
