package uk.ac.ebi.utils.threading;

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
public abstract class BatchServiceTask implements Runnable
{
	protected String name;
	protected int exitCode = 0;

	protected Logger log = LoggerFactory.getLogger ( this.getClass () );
		
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
}
