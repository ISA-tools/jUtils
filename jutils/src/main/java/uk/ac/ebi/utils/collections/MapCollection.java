package uk.ac.ebi.utils.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Function;

/**
 * Creates a view of a Map that is a collection. Does so by using a keyFunc {@link Function function}, which applies to map
 * values.
 * 
 * WARNING: Not fully tested yet!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 Oct 2015</dd></dl>
 *
 */
public class MapCollection<K, V> implements Collection<V>
{
	private final Map<K, V> base;
	private final Function<V, K> keyFunc; 
	
	/**
	 * @see #getKey().
	 */
	public MapCollection ( Map<K, V> base, Function<V, K> keyFunc )
	{
		this.base = base;
		this.keyFunc = keyFunc;
	}

	@Override
	public int size ()
	{
		return base.size ();
	}

	@Override
	public boolean isEmpty ()
	{
		return base.isEmpty ();
	}

	/**
	 * Checks the entry ({@link #getKeyFunction() keyFunc.apply(e)}, e)
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public boolean contains ( Object o )
	{
		return base.containsKey ( keyFunc.apply ( (V) o ) );
	}

	/**
	 * Refers to {@link #getBase()}.{@link Map#values()} (the map's values).
	 */
	@Override
	public Iterator<V> iterator ()
	{
		return base.values ().iterator ();
	}

	/**
	 * Refers to {@link #getBase()}.{@link Map#values()} (the map's values).
	 */
	@Override
	public Object[] toArray ()
	{
		return base.values ().toArray ();
	}

	/**
	 * Refers to {@link #getBase()}.{@link Map#values()} (the map's values).
	 */
	@Override
	public <T> T[] toArray ( T[] a )
	{
		return base.values ().toArray ( a );
	}

	/**
	 * Adds the entry ({@link #getKeyFunction() keyFunc.apply(e)}, e)
	 */
	@Override
	public boolean add ( V e )
	{
		V old = base.put ( keyFunc.apply ( e ), e );
		return old == null || !old.equals ( e );
	}

	/**
	 * Removes the entry ({@link #getKeyFunction() keyFunc.apply(e)}, e)
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public boolean remove ( Object o )
	{
		return base.remove ( keyFunc.apply ( (V) o ) ) != null;
	}

	/**
	 * Iteratively calls {@link #contains(Object)}. 
	 */
	@Override
	public boolean containsAll ( Collection<?> c )
	{
		for ( Object e: c ) if ( !contains ( e ) ) return false;
		return true;
	}

	/**
	 * Iteratively calls {@link #add(Object)}.
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public boolean addAll ( Collection<? extends V> c )
	{
		boolean result = false;
		for ( Object e: c )
			result |= add ( (V) e );
		return result;
	}

	/**
	 * Iteratively calls {@link #remove(Object)}.
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public boolean removeAll ( Collection<?> c )
	{
		boolean result = false;
		for ( Object e: c )
			result |= remove ( (V) e );
		return result;
	}

	@Override
	public boolean retainAll ( Collection<?> c )
	{
		boolean result = false;
		for ( Iterator<V> itr = base.values ().iterator (); itr.hasNext (); )
		{
			V e = itr.next ();
			if ( !c.contains ( e ) ) {
				itr.remove ();
				result = true;
			}
		}
		return result;
	}

	@Override
	public void clear ()
	{
		base.clear ();
	}

	/**
	 * The underlining map.
	 */
	public Map<K, V> getBase ()
	{
		return base;
	}

	/**
	 * The key function should return different values for different arguments and should always return the same value
	 * when an argument is presented multiple times.
	 */
	public Function<V, K> getKeyFunction ()
	{
		return keyFunc;
	}
	
}
