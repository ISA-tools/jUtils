package uk.ac.ebi.utils.streams;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import uk.ac.ebi.utils.collections.TupleSpliterator;

/**
 * Stream Utils
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jul 2017</dd></dl>
 *
 */
public class StreamUtils
{
	/**
	 * Returns a stream of tuples built from base streams, by populating each tuple item with one item from the underlining
	 * streams. This is basically a wrapper of {@link TupleSpliterator}, made by means of 
	 * {@link Spliterators#spliteratorUnknownSize(Iterator, int)} and 
	 * {@link StreamSupport#stream(Spliterator, boolean)}. 
	 * 
	 * @param characteristics this is passed to {@link Spliterators#spliteratorUnknownSize(Iterator, int)}, 
	 * {@link Spliterator#IMMUTABLE} is always OR-ed to whatever value you pass here, since the underlining tuple iterator
	 * is read-only.
	 * 
	 * @param isParallel this is passed to {@link StreamSupport#stream(Spliterator, boolean)}, since the underlining 
	 * iterator is immutable, creating a parallel stream as result shouldn't be a problem, unless you've some strange
	 * things in the base streams.
	 *  
	 * @param streams the base streams from which the result is built.
	 * 
	 * @see the unit tests for examples of usage.
	 * 
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> Stream<T[]> tupleStream ( 
		int characteristics, boolean isParallel, Stream<? extends T>... streams 
	)
	{
		if ( streams == null ) throw new NullPointerException ( 
			"Cannot create a tuple stream from a null stream array" 
		);
			
		Spliterator<Object> strmSpltrs[] = new Spliterator [ streams.length ];
		
		for ( int i = 0; i < streams.length; i++ )
			strmSpltrs [ i ] = (Spliterator<Object>) streams [ i ].spliterator ();
		
		TupleSpliterator<T> tupleItr = new TupleSpliterator<> ( (Spliterator<T>[]) strmSpltrs );
		return StreamSupport.stream ( tupleItr, isParallel );
	}
	
	/**
	 * Defaults to 0 (which implies {@link Spliterator#IMMUTABLE} only) and false (i.e., non-parallel result stream).
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> Stream<T[]> tupleStream ( Stream<? extends T>... streams ) {
		return tupleStream ( 0, false, streams );
	}
	
//  TODO: remove, doens't work
//
//	@SuppressWarnings ( "unchecked" )
//	public static <S,B> StreamEx<B> sliceStream ( Stream<S> sources, long batchSize, Collector<S, ?, B> batchBuilder )
//	{
//		
//		Map<S, Long> index = Collections.synchronizedMap ( new HashMap<> () );
//
//		Stream<Pair<S, Long>> batchIndexedStrm = sources.map ( s -> 
//		{
//			Long idxValue;
//			synchronized ( index ) {
//				idxValue = index.computeIfAbsent ( s, ks -> Long.valueOf ( index.size () ) );
//			}
//			System.out.format ( "%s: %d\n", s, idxValue );
//			return Pair.of ( s, idxValue );
//		});
//
//				
//		BiConsumer<Object, S> baseAccumulator = (BiConsumer<Object, S>) batchBuilder.accumulator ();
//
//		BiConsumer<Object, Pair<S, Long>> pairAccumulator = 
//			(a, pair) -> baseAccumulator.accept ( a, ( (Pair<S, Long>) pair ).getKey () );
//		
//		Set<Characteristics> baseCharacteristics = batchBuilder.characteristics ();
//									
//		Collector<Pair<S, Long>, Object, B> pairCollector = Collector.of ( 
//			(Supplier<Object>) batchBuilder.supplier (), 
//			pairAccumulator, 
//			(BinaryOperator<Object>) batchBuilder.combiner (),
//			(Function<Object, B>) batchBuilder.finisher (),
//			baseCharacteristics.toArray ( new Characteristics [ baseCharacteristics.size () ] )
//		);
//					
//		return StreamEx.of ( batchIndexedStrm ).collapse ( 
//			(p1, p2) -> {
//				long cluster1 = p1.getValue () / batchSize;
//				long cluster2 = p2.getValue () / batchSize;
//				
//				System.out.format ( "CMP: %s:%d, %s:%d\n", p1.getKey (), cluster1, p2.getKey (), cluster2 );
//				return cluster1 == cluster2; 
//			}, 
//			pairCollector
//		);
//	}	
}
