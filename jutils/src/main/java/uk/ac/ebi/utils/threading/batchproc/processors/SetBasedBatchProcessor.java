package uk.ac.ebi.utils.threading.batchproc.processors;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import uk.ac.ebi.utils.threading.batchproc.collectors.SetBatchCollector;

public class SetBasedBatchProcessor<E, BJ extends Consumer<Set<E>>>
	extends CollectionBasedBatchProcessor<E, Set<E>, SetBatchCollector<E>, BJ>
{
	public SetBasedBatchProcessor ( BJ batchJob, Supplier<Set<E>> batchFactory, int maxBatchSize ) {
		super ( batchJob, new SetBatchCollector<> ( batchFactory, maxBatchSize ) );
	}

	public SetBasedBatchProcessor ( BJ batchJob, int maxBatchSize ) 
	{
		this ( maxBatchSize );
		this.setBatchJob ( batchJob );
	}

	public SetBasedBatchProcessor ( int maxBatchSize )
	{
		this ();
		this.getBatchCollector ().setMaxBatchSize ( maxBatchSize );
	}
	
	public SetBasedBatchProcessor () {
		super ( null, new SetBatchCollector<> () );
	}	
}