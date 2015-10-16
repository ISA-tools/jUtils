package uk.ac.ebi.utils.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Function;

/**
 * Creates a view of a Map that is a collection. Does so by using a key function, which applies to map
 * values.
 * 
 * WARNING: Not fully tested yet!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 Oct 2015</dd>
 *
 */
public class MapCollection<K, V> implements Collection<V>
{
	private Map<K, V> base;
	private Function<V, K> key; 
	
	/**
	 * @see #getKey().
	 */
	public MapCollection ( Map<K, V> base, Function<V, K> key )
	{
		this.base = base;
		this.key = key;
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
	 * Checks the entry ({@link #getKey() key.apply(e)}, e)
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public boolean contains ( Object o )
	{
		return base.containsKey ( key.apply ( (V) o ) );
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
	 * Adds the entry ({@link #getKey() key.apply(e)}, e)
	 */
	@Override
	public boolean add ( V e )
	{
		V old = base.put ( key.apply ( e ), e );
		return old == null || !old.equals ( e );
	}

	/**
	 * Removes the entry ({@link #getKey() key.apply(e)}, e)
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public boolean remove ( Object o )
	{
		return base.remove ( key.apply ( (V) o ) ) != null;
	}

	/**
	 * Iteratively calls {@link #contains(Object)}. 
	 */
	@Override
	public boolean containsAll ( Collection<?> c )
	{
		for ( Object e: c ) if ( !contains ( c ) ) return false;
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
	public Function<V, K> getKey ()
	{
		return key;
	}
	
}
