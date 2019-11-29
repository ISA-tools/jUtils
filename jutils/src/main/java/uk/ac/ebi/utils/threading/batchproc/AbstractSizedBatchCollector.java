package uk.ac.ebi.utils.threading.batchproc;

/**
 * A default scaffolding {@link SizedBatchCollector}, which has a getter for {@link #maxBatchSize()} 
 * and a default value for it.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Nov 2019</dd></dl>
 *
 * @param <B>
 */
public abstract class AbstractSizedBatchCollector<B> implements SizedBatchCollector<B>
{
	private long maxBatchSize = 1000;
	
	/**
	 * Takes the default value of 1000.
	 */
	protected AbstractSizedBatchCollector () {
	}

	protected AbstractSizedBatchCollector ( long maxBatchSize )
	{
		super ();
		this.maxBatchSize = maxBatchSize;
	}

	@Override
	public long maxBatchSize () {
		return this.maxBatchSize;
	}

	public void setMaxBatchSize ( long maxBatchSize ) {
		this.maxBatchSize = maxBatchSize;
	}
	
}