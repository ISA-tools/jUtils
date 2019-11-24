package uk.ac.ebi.utils.threading.batchproc.collectors;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class SetBatchCollector<E> extends CollectionBatchCollector<Set<E>, E>
{
	public SetBatchCollector ( Supplier<Set<E>> batchFactory, int maxBatchSize )
	{
		super ( batchFactory, maxBatchSize );
	}

	public SetBatchCollector ( int maxBatchSize )
	{
		this ( HashSet::new, maxBatchSize );
	}
	
	public SetBatchCollector ()
	{
		super ( HashSet::new );
	}

}