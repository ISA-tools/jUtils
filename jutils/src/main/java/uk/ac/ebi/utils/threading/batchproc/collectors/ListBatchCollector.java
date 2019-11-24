package uk.ac.ebi.utils.threading.batchproc.collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ListBatchCollector<E> extends CollectionBatchCollector<List<E>, E>
{
	public ListBatchCollector ( Supplier<List<E>> batchFactory, int maxBatchSize )
	{
		super ( batchFactory, maxBatchSize );
	}

	public ListBatchCollector ( int maxBatchSize )
	{
		this ( ArrayList::new, maxBatchSize );
	}

	public ListBatchCollector ()
	{
		super ( ArrayList::new );
	}
}