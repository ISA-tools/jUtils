package uk.ac.ebi.utils.memory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.google.common.cache.CacheBuilder;

/**
 * A SimpleCache that uses feature from {@link LinkedHashMap}, as explained in 
 * <a href = "http://java-planet.blogspot.co.uk/2005/08/how-to-set-up-simple-lru-cache-using.html">this post</a>.
 *
 * @deprecated Use {@link CacheBuilder}. We are now is now using it internally, but we plan to remove this class in 
 * future.
 *
 * <dl><dt>date</dt><dd>May 27, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@Deprecated
public class SimpleCache<K, V> implements ConcurrentMap<K, V> 
{
	private final ConcurrentMap<K, V> base;
	
	@SuppressWarnings ( "unchecked" )
	public SimpleCache ( int capacity ) 
	{
		this.base = (ConcurrentMap<K,V>) CacheBuilder.newBuilder ().maximumSize ( capacity ).build ().asMap ();
	}

	public V putIfAbsent ( K key, V value )
	{
		return base.putIfAbsent ( key, value );
	}

	public boolean remove ( Object key, Object value )
	{
		return base.remove ( key, value );
	}

	public boolean replace ( K key, V oldValue, V newValue )
	{
		return base.replace ( key, oldValue, newValue );
	}

	public V replace ( K key, V value )
	{
		return base.replace ( key, value );
	}

	public int size ()
	{
		return base.size ();
	}

	public boolean isEmpty ()
	{
		return base.isEmpty ();
	}

	public boolean containsKey ( Object key )
	{
		return base.containsKey ( key );
	}

	public boolean containsValue ( Object value )
	{
		return base.containsValue ( value );
	}

	public V get ( Object key )
	{
		return base.get ( key );
	}

	public V put ( K key, V value )
	{
		return base.put ( key, value );
	}

	public V remove ( Object key )
	{
		return base.remove ( key );
	}

	public void putAll ( Map<? extends K, ? extends V> m )
	{
		base.putAll ( m );
	}

	public void clear ()
	{
		base.clear ();
	}

	public Set<K> keySet ()
	{
		return base.keySet ();
	}

	public Collection<V> values ()
	{
		return base.values ();
	}

	public Set<Entry<K, V>> entrySet ()
	{
		return base.entrySet ();
	}

	public boolean equals ( Object o )
	{
		if ( ! ( o instanceof ConcurrentMap ) ) return false;
		return base.equals ( o );
	}

	public int hashCode ()
	{
		return base.hashCode ();
	}

}
