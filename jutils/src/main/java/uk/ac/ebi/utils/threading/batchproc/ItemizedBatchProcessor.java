package uk.ac.ebi.utils.threading.batchproc;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.mutable.MutableObject;

/**
 * TODO: comment me!
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
	

	protected void process ( Consumer<Consumer<E>> sourceItemConsumer, Object... opts )
	{
		ItemizedBatchCollector<B,E> bcoll = this.getBatchCollector ();
		Supplier<B> bfact = bcoll.batchFactory ();
		MutableObject<B> currentBatchWrp = new MutableObject<> ( bfact.get () );
		
		sourceItemConsumer.accept ( item -> 
		{
			B currentBatch = currentBatchWrp.getValue ();
			B newBatch = this.consumeItem ( item, currentBatch );
			if ( newBatch == currentBatch ) return;
			currentBatchWrp.setValue ( newBatch );
		});
		
		// Submit residues
		this.handleNewBatch ( currentBatchWrp.getValue (), true );
		
		this.waitExecutor ( "Waiting for the batch processor to finish" );
		log.info ( "Batch processor finished" );
	}
	
	protected B consumeItem ( E item, B currentBatch )
	{
		this.getBatchCollector ().accumulator ().accept ( currentBatch, item );
		return this.handleNewBatch ( currentBatch );
	}
	
}
