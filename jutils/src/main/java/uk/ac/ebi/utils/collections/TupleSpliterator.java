package uk.ac.ebi.utils.collections;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import uk.ac.ebi.utils.streams.StreamUtils;

/**
 * <p>Takes an array of spliterators and returns a spliterator of tuples. Each tuple is built by taking the 
 * {@link Spliterator#tryAdvance(Consumer) next element} of each spliterator. 
 * The iteration stops as soon as there is at least one spliterator which of {@link Spliterator#tryAdvance(Consumer)} 
 * method returns false.</p>
 * 
 * Parallelism support is limited: {@link #trySplit()} succeeds as long as all the underlining spliterators are able
 * to return prefixes having all the same size (tails of different size have chances to be managed by the behaviour of
 * {@link #tryAdvance(Consumer)}, by cutting results at the shortest one).   
 * 
 * Another restriction is that all base spliterators must be {@link Spliterator#IMMUTABLE} and 
 * non-{@link Spliterator#CONCURRENT}.  
 * 
 * 
 * @see StreamUtils#tupleStream(int, boolean, java.util.stream.Stream...) and 
 * {@link uk.ac.ebi.utils.streams.StreamUtilsTest} for usage examples.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>26 Jul 2017</dd></dl>
 *
 * @param <T>
 */
public class TupleSpliterator<T> implements Spliterator<T[]>
{
	private Spliterator<T>[] spliterators;
	private int characteristics;
	private long size = 0;
	
	/**
	 * Initialises with base spliterators. See above for details. @see also {@link #characteristics()}.
	 */
	@SuppressWarnings ( "unchecked" )
	public TupleSpliterator ( Spliterator<? extends T>[] spliterators ) 
	{		
		this.spliterators = (Spliterator<T>[]) spliterators;
		
		this.characteristics = NONNULL | DISTINCT | IMMUTABLE;
		
		if ( spliterators == null ) return;

		// The result has these characteristics if all its components have
		for ( int checkCharateristic: new int[] { ORDERED, SIZED, SUBSIZED } )
		if ( IntStream.range ( 0, this.spliterators.length )
				 .mapToObj ( i -> this.spliterators [ i ] )
				 .filter ( splitr -> splitr != null )
				 .allMatch ( splitr -> ( splitr.characteristics () & checkCharateristic ) != 0 ) )
			this.characteristics |= checkCharateristic;
		
		this.size = IntStream.range ( 0, this.spliterators.length )
			.mapToObj ( i -> this.spliterators [ i ] )
			.map ( itr -> itr == null ? 0 : itr.estimateSize () )
			.min ( Long::compare )
			.orElse ( 0L );
	}

	/**
	 * Just a facility to avoid too much genetics fiddling.
	 */
	@SafeVarargs
	@SuppressWarnings ( "unchecked" )
	public static <TT> TupleSpliterator<TT> of ( Spliterator<? extends TT> ...spliterators ) {
		return new TupleSpliterator<> ( spliterators );
	}

	/**
	 * As explained above, if all the underlining spliterators have an element to return (ie, their tryAdvance()) is
	 * invoked to get the element they have to return and the return value checked to be true), a tuple is built
	 * with all such elements and then passed to the action parameter. If that doens't happen, returns false and 
	 * the action here is not invoked.
	 *  
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public boolean tryAdvance ( Consumer<? super T[]> action )
	{
		if ( this.spliterators == null ) return false;
		
		Object[] elems = new Object [ this.spliterators.length ];
		
		for ( int i = 0; i < this.spliterators.length; i++ ) 
		{
			if ( this.spliterators [ i ]  == null ) return false;
			
			final int i1 = i;
			if ( !this.spliterators [ i ].tryAdvance ( t -> elems [ i1 ] = t ) ) return false;
		}
		
		action.accept ( (T[]) elems );
		return true;
	}

	
	/**
	 * A splits succeeds when all the base spliterators return a prefix of the same length. If that is the case the new 
	 * split result will be a new {@link TupleIterator} based on the prefixes returned by the #trySplit() operation 
	 * invoked upon the base spliterators. This iterator is left with the base iterators it already has, so with its 
	 * tails. Due to the behaviour of {@link #tryAdvance(Consumer)}, such tails can have different sizes, they'll determine
	 * a tuple iterator sized like the shortest tail.
	 * 
	 * Examples (assume the splits happen as described)
	 * 
	 * <pre>
	 *   TupleIterator 1:
	 *     1, 2, 3, 4, 5, 6 => 1, 2, 3 | 4, 5, 6
	 *     a, b, c, d, e, f => a, b, c | d, e, f
	 *     
	 *   TupleIterator 2:
	 *     1, 2, 3, 4, 5, 6 => 1, 2, 3 | 4, 5, 6
	 *     a, b, c, d       => a, b, c | d
	 * </pre>
	 * 
	 * trySplit() over the second tuple spliterator will return a tuple iterator yielding 3 elements (1a, 2b, 3c) and 
	 * will leave an original spliterator with one element only (4d).
	 */
	@Override
	public Spliterator<T[]> trySplit ()
	{
		
		@SuppressWarnings ( { "unchecked" } )
		Spliterator<T>[] result = new Spliterator [ this.spliterators.length ];
		
		long newReturnedSize = -1;
		this.size = Long.MAX_VALUE;
		
		for ( int i = 0; i < this.spliterators.length; i++ )
		{
			if ( this.spliterators [ i ] == null ) return null;
			
			if ( ( result [ i ] = this.spliterators [ i ].trySplit () ) == null ) return null;
			
			long isize = this.spliterators [ i ].estimateSize ();
			if ( isize < this.size ) this.size = isize;
			
			if ( i == 0 ) { 
				newReturnedSize = result [ 0 ].estimateSize ();
				continue;
			}

			/**
			 * New splits must have all the same size, when that doesn't happen, we don't know how to split.
			 */
			if ( newReturnedSize != result [ i ].estimateSize () ) 
				return null;
		}
		return new TupleSpliterator<> ( result );
	}

	/**
	 * This is the shortest size found in base spliterators. Hence, it will be {@link Long#MAX_VALUE} if all of them
	 * return that, including when they are infinite or don't know their count. Having a non-precise result doesn't
	 * prevent {@link #trySplit()} from splitting the way we requires. 
	 */
	@Override
	public long estimateSize () {
		return this.size; 
	}

	/**
	 * As a minimum, this will contain NONNULL | DISTINCT | IMMUTABLE. It will never contain SORTED, CONCURRENT.
	 * Might contain SIZED, SUBSIZED, ORDERED, if all the base spliterators do.
	 * 
	 * Details to explain this are: 
	 *  
	 * <ul>
	 *   <li>SORTED, we don't currently provide a {@link #getComparator()} and hence this is not set.</li>
	 *   <li>DISTINCT, this is set, we return arrays and they're all technically distinct (as per the default 
	 *   {@link #equals(Object)})</li>
	 *   <li>CONCURRENT, is not set, we're IMMUTABLE</li>
	 *   <li>IMMUTABLE, this is set, we expect base spliterator to be immutable too (or that you know what you're doing)</li>
	 *   <li>SIZED, SUBSIZED, ORDERED, are set for the result if all the base spliterators have these flags</li>
	 *   <li>NONNULL is always set for the result, for its items are non-null tuples, which of elements might be null</li>
	 * </ul>
	 */	
	@Override
	public int characteristics ()
	{
		return this.characteristics;
	}

}
