package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.Executor;

/**
 * An {@link Executor} that is able to chain/stack two other executors.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Oct 2015</dd></dl>
 *
 */
public class ChainExecutor implements Executor
{
	private Executor internalExecutor;
	private Executor externalExecutor; 
	
	/**
	 * @see #execute(Runnable).
	 */
	public ChainExecutor ( Executor externalExecutor, Executor internalExecutor )
	{
		super ();
		this.internalExecutor = internalExecutor;
		this.externalExecutor = externalExecutor;
	}

	/**
	 * This reduces to running one executor only, so it's conceptually equivalent to
	 * use the {@code internalExecutor} straight, but it's a base to use {@link #andThen(Executor)}. 
	 * 
	 */
	public ChainExecutor ( Executor internalExecutor )
	{
		this ( null, internalExecutor );
	}

	/**
	 * The external executor calls the run() method of the internal executor, passing the action to it. 
	 * The result is that the two executors wrap their job around the action in a nested way.
	 */
	@Override
	public void execute ( final Runnable action )
	{
		if ( this.externalExecutor == null )
			this.internalExecutor.execute ( action );
		else
			this.externalExecutor.execute ( 
				() -> this.internalExecutor.execute ( action ) 
			);
	}
	
	/**
	 * <p>This allows you to indent multiple executors, without having to write big
	 * nested expressions. This is equivalent to creating a new executor using 
	 * {@link #ChainExecutor(Executor, Executor)} with the current instance as
	 * internal executor. So, it wraps the current executor with a new one.</p>
	 * 
	 * <p>This also implies that a composition like {@code ex3(ex2(ex1))} here must
	 * be created via {@code wrap( ex1 ).wrap ( ex2 ).wrap ( ex3 )}.</p> 
	 * 
	 */
	public ChainExecutor wrap ( Executor externalExecutor ) {
		return new ChainExecutor ( externalExecutor, this );
	} 
}
