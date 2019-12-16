package uk.ac.ebi.utils.threading.batchproc.collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import uk.ac.ebi.utils.threading.batchproc.AbstractSizedBatchCollector;

/**
 * 
 * Uses {@link ArrayList} as default and the default max size provided by {@link AbstractSizedBatchCollector}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Nov 2019</dd></dl>
 *
 * @param <E>
 */
public class ListBatchCollector<E> extends CollectionBatchCollector<List<E>, E>
{
	public ListBatchCollector ( Supplier<List<E>> batchFactory, int maxBatchSize )
	{
		super ( batchFactory, maxBatchSize );
	}

	/**
	 * {@link ArrayList} as default.
	 */
	public ListBatchCollector ( int maxBatchSize ) {
		this ( ArrayList::new, maxBatchSize );
	}

	/**
	 * {@link ArrayList} as default.
	 */
	public ListBatchCollector () {
		super ( ArrayList::new );
	}
}