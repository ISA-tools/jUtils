package uk.ac.ebi.utils.threading.batchproc.processors;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import uk.ac.ebi.utils.threading.batchproc.collectors.ListBatchCollector;

public class ListBasedBatchProcessor<E, BJ extends Consumer<List<E>>>
	extends CollectionBasedBatchProcessor<E, List<E>, ListBatchCollector<E>, BJ>
{
	public ListBasedBatchProcessor ( BJ batchJob, Supplier<List<E>> batchFactory, int maxBatchSize ) {
		super ( batchJob, new ListBatchCollector<> ( batchFactory, maxBatchSize ) );
	}

	public ListBasedBatchProcessor ( BJ batchJob, int maxBatchSize ) 
	{
		this ( maxBatchSize );
		this.setBatchJob ( batchJob );
	}

	public ListBasedBatchProcessor ( int maxBatchSize )
	{
		this ();
		this.getBatchCollector ().setMaxBatchSize ( maxBatchSize );
	}
	
	public ListBasedBatchProcessor () {
		super ( null, new ListBatchCollector<> () );
	}
}