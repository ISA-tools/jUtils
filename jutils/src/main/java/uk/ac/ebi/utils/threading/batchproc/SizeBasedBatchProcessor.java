package uk.ac.ebi.utils.threading.batchproc;

/**
 * A {@link BatchProcessor} where {@link #decideNewBatch(Object)} is based on some 
 * {@link #getCurrentBatchSize(Object) size measurement} of the current batch B object.   
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Dec 2017</dd></dl>
 *
 */
public abstract class SizeBasedBatchProcessor<S, B> extends BatchProcessor<S, B>
{
	private long batchMaxSize;
	
	/**
	 * The max batch size that the current batch B can have on each new processing job.
	 * 
	 * The idea is to issue a new job when this number of items have been submitted to the processor via 
	 * {@link #process(Object, Object...)}.
	 * 
	 */
	public long getBatchMaxSize ()
	{
		return batchMaxSize;
	}

	public SizeBasedBatchProcessor<S, B> setBatchMaxSize ( long batchMaxSize )
	{
		this.batchMaxSize = batchMaxSize;
		return this;
	}

	/**
	 * Tells the size of dest.
	 */
	protected abstract long getCurrentBatchSize ( B batch );

	/**
	 * Decides to switch to a new task based on {@link #getCurrentBatchSize(Object)} and {@link #getBatchMaxSize()}.
	 */
	@Override
	protected boolean decideNewBatch ( B batch ) {
		return this.getCurrentBatchSize ( batch ) >= this.getBatchMaxSize ();
	}	
}
