package uk.ac.ebi.utils.threading.batchproc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Nov 2019</dd></dl>
 *
 */
public class BatchCollectors
{

	private BatchCollectors () {
	}

	public static <B, C extends Collection<B>> SizedBatchCollector<C> ofCollection ( Supplier<C> batchFactory, int maxBatchSize ) 
	{
		return new SizedBatchCollector<C>() 
		{
			@Override
			public Supplier<C> batchFactory () {
				return batchFactory;
			}

			@Override
			public Function<C, Long> batchSizer () {
				return b -> (long) b.size ();
			}

			@Override
			public long getMaxBatchSize () {
				return maxBatchSize;
			}
		};
	} // of ( Collection )
	
	public static <B> SizedBatchCollector<List<B>> ofList ( Supplier<List<B>> batchFactory, int maxBatchSize )
	{
		return ofCollection ( batchFactory, maxBatchSize );
	}

	public static <B> SizedBatchCollector<List<B>> ofList ( int maxBatchSize )
	{
		return ofList ( ArrayList::new, maxBatchSize );
	}

	
	public static <B> SizedBatchCollector<Set<B>> ofSet ( Supplier<Set<B>> batchFactory, int maxBatchSize )
	{
		return ofCollection ( batchFactory, maxBatchSize );
	}

	public static <B> SizedBatchCollector<Set<B>> ofSet ( int maxBatchSize )
	{
		return ofSet ( HashSet::new, maxBatchSize );
	}
	
	
}
