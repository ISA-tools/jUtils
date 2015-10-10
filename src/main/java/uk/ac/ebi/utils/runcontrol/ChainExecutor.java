package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.Executor;

/**
 * An {@link Executor} that is able to chain/stack two other executors.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Oct 2015</dd>
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
	 * The external executor calls the run() method of the internal executor, passing the action to it. 
	 * The result is that the two executors wrap their job around the action in a nested way.
	 */
	@Override
	public void execute ( final Runnable action )
	{
		this.externalExecutor.execute ( new Runnable() {
			@Override
			public void run ()
			{
				ChainExecutor.this.internalExecutor.execute ( action );
			}
		});
	}

}
