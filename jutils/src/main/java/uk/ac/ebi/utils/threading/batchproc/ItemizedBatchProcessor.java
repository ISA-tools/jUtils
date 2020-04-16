package uk.ac.ebi.utils.threading.batchproc;

import java.util.Iterator;
import java.util.function.BiConsumer;
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
	 * @param sourceItemsGenerator this is a consumer of an item consumer, which should yield all the items to be processed 
	 * and passe each of them to the single-item consumer that we pass it from here. This allows us to collect the 
	 * generated item, save it the current batch and decide if this has to be dispatched to a new job and a new batch
	 * should be generated. In other words, such generator will receive the code that realises the per-item iteration 
	 * of our hereby processor. This is a generalisation for {@link #process(Stream, boolean) streams} and
	 * {@link #process(Iterator, boolean) iterators}, we suggest that you use those variants if they fit into your specific 
	 * case.
	 * 
	 * @param waitCompletion if true (default), {@link #waitExecutor(String) waits} for all the submitted batch jobs to 
	 * complete. You might want this to be false when you use this method to send multiple item sources. **WARNING**: if
	 * you do so, **there is no synchronisation** across multiple invocations of this method. For instance, it might be 
	 * unsafe to {@link #setBatchJob(Consumer) switch to a new batch job type}, since previous source items might still be 
	 * bound to the older job.  
	 * 
	 */
	protected void process ( Consumer<Consumer<E>> sourceItemsGenerator, boolean waitCompletion )
	{
		ItemizedBatchCollector<B,E> bcoll = this.getBatchCollector ();
		Supplier<B> bfact = bcoll.batchFactory ();
		BiConsumer<B, E> baccumulator = bcoll.accumulator ();
		
		// The lambda below wants final vars, so this does the trick
		MutableObject<B> currentBatchWrp = new MutableObject<> ( bfact.get () );
		
		sourceItemsGenerator.accept ( item -> 
		{
			B currentBatch = currentBatchWrp.getValue ();
			baccumulator.accept ( currentBatch, item );
			
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
	protected void process ( Consumer<Consumer<E>> sourceItemsGenerator ) {
		process ( sourceItemsGenerator, true );
	}
	
	/**
	 * Uses {@link Stream#forEach(Consumer)} as generator.
	 */
	protected void process ( Stream<E> sourceItemsGenerator, boolean waitCompletion )
	{
		this.process ( sourceItemsGenerator::forEach, waitCompletion );
	}

	protected void process ( Stream<E> sourceItemsGenerator )
	{
		this.process ( sourceItemsGenerator, true );
	}
	
	/**
	 * Uses {@link Iterator#forEachRemaining(Consumer)} as generator.
	 */
	protected void process ( Iterator<E> sourceItemsGenerator, boolean waitCompletion )
	{
		this.process ( sourceItemsGenerator::forEachRemaining, waitCompletion );
	}

	protected void process ( Iterator<E> sourceItemsGenerator )
	{
		this.process ( sourceItemsGenerator, true );
	}
	
}
