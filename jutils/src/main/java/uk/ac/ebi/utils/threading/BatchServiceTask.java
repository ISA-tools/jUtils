package uk.ac.ebi.utils.threading;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Used by {@link BatchService}. It's essentially a {@link Runnable} with some utility stuff, such as an exit code and 
 * a task name. 
 *
 * <dl><dt>date</dt><dd>8 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class BatchServiceTask implements Runnable, Comparable<Runnable>
{
	protected String name;
	protected int exitCode = 0;
	private int priority = 0;

	protected Logger log = LoggerFactory.getLogger ( this.getClass () );
		
	/**
	 * Provides a task comparison based on {@link BatchServiceTask#getPriority()} (higher priorities comes first). This 
	 * is useful when you override {@link BatchService#newThreadPoolExecutor(int)} to return an executor service that
	 * uses {@link PriorityBlockingQueue}. 
	 *
	 * @author brandizi
	 * <dl><dt>Date:</dt><dd>28 Jan 2016</dd></dl>
	 *
	 */
	public static class TaskComparator implements Comparator<Runnable>
	{
		@Override
		public int compare ( Runnable r1, Runnable r2 )
		{
			if ( r1 == null || ! ( r1 instanceof BatchServiceTask ) ) 
				return ( r2 == null || ! ( r2 instanceof BatchServiceTask ) ) ? 0 : -1;

			return ((BatchServiceTask) r1).compareTo ( r2 );
		}
	}
	
	
	
	protected BatchServiceTask ( String name )
	{
		this.name = name;
	}

	public int getExitCode ()
	{
		return exitCode;
	}

	public String getName ()
	{
		return name;
	}
	
	/**
	 * Provides a task priority, where 0 is the default and tasks having higher values are supposed to be run before the
	 * others. This is ignored unless you override {@link BatchService#newThreadPoolExecutor(int)}.  
	 */
	public int getPriority () {
		return this.priority;
	}

	public void setPriority ( int priority )
	{
		this.priority = priority;
	}
	
	/**
	 * Orders according to {@link #getPriority()} (in descending order, the first is the one with the bigger priority).
	 */
	public int compareTo ( Runnable o )
	{
		// shouldn't happen, but just in case
		if ( o == null ) return +1; 
		if ( ! ( o instanceof BatchServiceTask ) ) return +1; 
		
		return ((BatchServiceTask) o).getPriority () - this.getPriority ();
	}
}
