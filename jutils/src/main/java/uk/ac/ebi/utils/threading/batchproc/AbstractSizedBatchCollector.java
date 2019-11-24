package uk.ac.ebi.utils.threading.batchproc;

public abstract class AbstractSizedBatchCollector<B> implements SizedBatchCollector<B>
{
	private long maxBatchSize = 1000;
	
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