package uk.ac.ebi.utils.threading.batchproc.processors;

import java.util.Collection;
import java.util.function.Consumer;

import uk.ac.ebi.utils.threading.batchproc.ItemizedBatchProcessor;
import uk.ac.ebi.utils.threading.batchproc.collectors.CollectionBatchCollector;

public abstract class CollectionBasedBatchProcessor
	<E, B extends Collection<E>, BC extends CollectionBatchCollector<B,E>, BJ extends Consumer<B>>
  extends ItemizedBatchProcessor<E, B, BC, BJ>
{
	public CollectionBasedBatchProcessor ( BJ batchJob, BC batchCollector ) {
		super ( batchJob, batchCollector );
	}

	public CollectionBasedBatchProcessor ( BJ batchJob ) {
		super ( batchJob );
	}

	public CollectionBasedBatchProcessor () {
		super ();
	}
}