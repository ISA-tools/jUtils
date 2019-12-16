package uk.ac.ebi.utils.threading.batchproc.collectors;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.ac.ebi.utils.threading.batchproc.AbstractSizedBatchCollector;
import uk.ac.ebi.utils.threading.batchproc.ItemizedSizedBatchCollector;

/**
 * Collectors of this type are dedicated to {@link Collection Java collections}. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Nov 2019</dd></dl>
 *
 * @param <C>
 * @param <E>
 */
public abstract class CollectionBatchCollector<C extends Collection<E>, E>
	extends AbstractSizedBatchCollector<C>
	implements ItemizedSizedBatchCollector<C, E>
{
	private final Supplier<C> batchFactory;
	
	/**
	 * Default size is inherited by {@link AbstractSizedBatchCollector}.
	 */
	public CollectionBatchCollector ( Supplier<C> batchFactory )
	{
		super ();
		this.batchFactory = batchFactory;
	}

	public CollectionBatchCollector ( Supplier<C> batchFactory, int maxBatchSize )
	{
		this ( batchFactory );
		this.setMaxBatchSize ( maxBatchSize );
	}
	
	@Override
	public Supplier<C> batchFactory () {
		return batchFactory;
	}
	
	@Override
	public BiConsumer<C, E> accumulator () {
		return (coll,elem) -> coll.add ( elem );
	}

	@Override
	public Function<C, Long> batchSizer () {
		return b -> (long) b.size ();
	}
}