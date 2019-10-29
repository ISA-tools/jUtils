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
}
