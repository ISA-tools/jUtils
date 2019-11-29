package uk.ac.ebi.utils.threading.batchproc.processors;

import java.util.Collection;
import java.util.function.Consumer;

import uk.ac.ebi.utils.threading.batchproc.ItemizedBatchProcessor;
import uk.ac.ebi.utils.threading.batchproc.SizedBatchCollector;
import uk.ac.ebi.utils.threading.batchproc.collectors.CollectionBatchCollector;

/**
 * An {@link ItemizedBatchProcessor item-based batch processor} that is based on Java collection batches.
 * This is mainly an entry point for specific sub-classes, which map concrete Java collections.  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Nov 2019</dd></dl>
 *
 */
public abstract class CollectionBasedBatchProcessor
	<E, B extends Collection<E>, BC extends CollectionBatchCollector<B,E>, BJ extends Consumer<B>>
  extends ItemizedBatchProcessor<E, B, BC, BJ>
{
	public CollectionBasedBatchProcessor ( BJ batchJob, BC batchCollector ) {
		super ( batchJob, batchCollector );
	}

	/**
	 * Initialises with a default collection, which should also have a default 
	 * {@link SizedBatchCollector#maxBatchSize() max size}.
	 */
	public CollectionBasedBatchProcessor ( BJ batchJob ) {
		super ( batchJob );
	}

	/**
	 * This initialises with a default collection and size, like {@link #CollectionBasedBatchProcessor(Consumer)}, 
	 * and no job (which needs to be set later via {@link #setBatchJob(Consumer)}).
	 * 
	 */
	public CollectionBasedBatchProcessor () {
		super ();
	}
}