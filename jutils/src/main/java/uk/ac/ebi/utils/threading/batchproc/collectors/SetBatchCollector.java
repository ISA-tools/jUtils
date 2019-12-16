package uk.ac.ebi.utils.threading.batchproc.collectors;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import uk.ac.ebi.utils.threading.batchproc.AbstractSizedBatchCollector;

/**
 * 
 * Uses {@link HashSet} as default and the default max size provided by {@link AbstractSizedBatchCollector}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Nov 2019</dd></dl>
 *
 * @param <E>
 */
public class SetBatchCollector<E> extends CollectionBatchCollector<Set<E>, E>
{
	public SetBatchCollector ( Supplier<Set<E>> batchFactory, int maxBatchSize )
	{
		super ( batchFactory, maxBatchSize );
	}

	public SetBatchCollector ( int maxBatchSize ) {
		this ( HashSet::new, maxBatchSize );
	}
	
	public SetBatchCollector () {
		super ( HashSet::new );
	}

}