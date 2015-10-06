package uk.ac.ebi.utils.runcontrol;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Oct 2015</dd>
 *
 */
public class ChainedWrappedInvoker<WV> implements WrappedInvoker<WV>
{
	private WrappedInvoker<?> internalInvoker;
	private WrappedInvoker<WV> externalInvoker; 
	
	
	public ChainedWrappedInvoker ( WrappedInvoker<WV> externalInvoker, WrappedInvoker<?> internalInvoker )
	{
		super ();
		this.internalInvoker = internalInvoker;
		this.externalInvoker = externalInvoker;
	}


	@Override
	public WV run ( final Runnable action )
	{
		return this.externalInvoker.run ( new Runnable() {
			@Override
			public void run ()
			{
				ChainedWrappedInvoker.this.internalInvoker.run ( action );
			}
		});
	}

}
