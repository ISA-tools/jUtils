package uk.ac.ebi.utils.threading.batchproc;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableObject;

/**
 * ## Item-based batch processor.
 * 
 * This processor implements a complete {@link #process(Consumer, Object...) processing loop}, which is based on the 
 * common idea of processing a flow of input items and dispatching them to the batches.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Nov 2019</dd></dl>
 *
 */
public abstract class ItemizedBatchProcessor<E, B, BC extends ItemizedBatchCollector<B,E>, BJ extends Consumer<B>> 
  extends BatchProcessor<B, BC, BJ>
{
	
	public ItemizedBatchProcessor ( BJ batchJob, BC batchCollector ) {
		super ( batchJob, batchCollector );
	}

	public ItemizedBatchProcessor ( BJ batchJob ) {
		super ( batchJob );
	}

	public ItemizedBatchProcessor () {
		super ();
	}
	
	/**
	 * Gets an input flow of items and dispatches them to batches and batch jobs, as explained in the 
	 * {@link BatchProcessor} super-class.
	 * 
	 * @param sourceItemConsumer this yields all the items to be processed and passes each of them to the single-item 
	 * consumer that we provide hereby. The latter collects the item in the current batch and handles the batch dispatch
	 * logics explained above. Examples of methods compatible with this parameter are {@link Stream#forEach(Consumer)}
	 * and {@link Iterator#forEachRemaining(Consumer)}.
	 * 
	 * @param waitCompletion if true (default), {@link #waitExecutor(String) waits} for all the submitted batch jobs to 
	 * complete. You might want this to be false when you use this method to send multiple item sources. **WARNING**: if
	 * you do so, **there is no synchronisation** across multiple invocations of this method. For instance, it might be 
	 * unsafe to {@link #setBatchJob(Consumer) switch to a new batch job type}, since previous source items might still be 
	 * bound to the older job.  
	 * 
	 */
	protected void process ( Consumer<Consumer<E>> sourceItemConsumer, boolean waitCompletion )
	{
		ItemizedBatchCollector<B,E> bcoll = this.getBatchCollector ();
		Supplier<B> bfact = bcoll.batchFactory ();
		// The lambda below wants final vars, so this does the trick
		MutableObject<B> currentBatchWrp = new MutableObject<> ( bfact.get () );
		
		sourceItemConsumer.accept ( item -> 
		{
			B currentBatch = currentBatchWrp.getValue ();
			this.consumeItem ( currentBatch, item );
			B newBatch = this.handleNewBatch ( currentBatch );
			if ( newBatch == currentBatch ) return;
			currentBatchWrp.setValue ( newBatch );
		});
		
		// Submit residues
		this.handleNewBatch ( currentBatchWrp.getValue (), true );

		if ( !waitCompletion ) return;
		this.waitExecutor ( "Waiting for the batch processor to finish" );
		if ( this.jobLogPeriod > -1 ) log.info ( "Batch processor finished" );
	}
	
	/**
	 * Defaults to true, ie, it waits for all the batches submitted from the source items to be completed.
	 * 
	 */
	protected void process ( Consumer<Consumer<E>> sourceItemConsumer ) {
		process ( sourceItemConsumer, true );
	}

	
	/**
	 * This simply invokes {@link ItemizedSizedBatchCollector#accumulator()}. It's provided as a separated method, in case
	 * you want to decorate with some pre/post fetching actions, without needing to change {@link #process(Consumer)}.
	 * 
	 */
	protected void consumeItem ( B currentBatch, E item ) {
		this.getBatchCollector ().accumulator ().accept ( currentBatch, item );
	}
	
}
