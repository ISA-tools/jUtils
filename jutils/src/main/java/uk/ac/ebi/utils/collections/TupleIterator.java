package uk.ac.ebi.utils.collections;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import uk.ac.ebi.utils.streams.StreamUtils;

/**
 * Takes an array of iterators and returns an iterator of tuples. Each tuple is built by taking the 
 * {@link Iterator#next() next element} of each iterator. The iteration stops as son as there is at least one
 * iterator that {@link Iterator#hasNext() hasn't any more elements to return}.
 * 
 * The result is read-only and {@link Iterator#remove()} throws an {@link UnsupportedOperationException}, since
 * its default implementation is not touched.
 * 
 * @see StreamUtils#tupleStream(int, boolean, java.util.stream.Stream...) and 
 * {@link uk.ac.ebi.utils.streams.StreamUtilsTest} for usage examples.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jul 2017</dd></dl>
 *
 */
public class TupleIterator<T> implements Iterator<T[]>
{
	private Iterator<T>[] iterators;
	
	@SuppressWarnings ( "unchecked" )
	public TupleIterator ( Iterator<? extends T>[] iterators ) {
		this.iterators = (Iterator<T>[]) iterators;
	}

	@Override
	public boolean hasNext ()
	{
		if ( this.iterators == null ) throw new NullPointerException (
			"Internal error: base iterators for a TupleIterator are null" 
		);
				
		for ( int i = 0; i < iterators.length; i++ )
			if ( !iterators [ i ].hasNext () ) return false;

		return true;
	}

	@Override
	@SuppressWarnings ( "unchecked" )
	public T[] next ()
	{
		T[] result = (T[]) new Object [ iterators.length ];
		
		for ( int i = 0; i < iterators.length; i++ )
			result [ i ] = iterators [ i ].next ();
		
		return result;
	}
	
	@SuppressWarnings ( "unchecked" )
	public static <TT> TupleIterator<TT> of ( Iterator<? extends TT> ...iterators ) {
		return new TupleIterator<TT> ( iterators );
	}

}
