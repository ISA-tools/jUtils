package uk.ac.ebi.utils.threading;

/**
 * A {@link BatchProcessor} where {@link #decideNewTask(Object)} is based on some 
 * {@link #getDestinationSize(Object) size measurement} of the current destination D object.   
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Dec 2017</dd></dl>
 *
 */
public abstract class SizeBasedBatchProcessor<S, D> extends BatchProcessor<S, D>
{
	private long destinationMaxSize;
	
	/**
	 * The max destination size that the destination D can have on each new processing thread.
	 * 
	 * The idea is to issue a new thread when this number of items have been submitted to the processor via 
	 * {@link #process(Object, Object...)}.
	 * 
	 */
	public long getDestinationMaxSize ()
	{
		return destinationMaxSize;
	}

	public SizeBasedBatchProcessor<S, D> setDestinationMaxSize ( long destinationMaxSize )
	{
		this.destinationMaxSize = destinationMaxSize;
		return this;
	}

	/**
	 * Tells the size of dest.
	 */
	protected abstract long getDestinationSize ( D dest );

	/**
	 * Decides to switch to a new task based on {@link #getDestinationSize(Object)} and {@link #getDestinationMaxSize()}.
	 */
	@Override
	protected boolean decideNewTask ( D dest ) {
		return this.getDestinationSize ( dest ) >= this.getDestinationMaxSize ();
	}	
}
