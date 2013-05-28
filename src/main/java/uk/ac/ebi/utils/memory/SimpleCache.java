package uk.ac.ebi.utils.memory;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * A SimpleCache that uses feature from {@link LinkedHashMap}, as explained in 
 * <a href = "http://java-planet.blogspot.co.uk/2005/08/how-to-set-up-simple-lru-cache-using.html">this post</a>.
 *
 * <dl><dt>date</dt><dd>May 27, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class SimpleCache<K, V> extends LinkedHashMap<K, V>
{
	private final int capacity;
	private static final long serialVersionUID = -8004530435716408045L;
	
	public SimpleCache ( int capacity ) 
	{
		super ( capacity + 1, capacity * 1.1f, true );
		this.capacity = capacity;
	}

	@Override
	protected boolean removeEldestEntry ( Entry<K, V> eldest ) {		
    return size() > capacity;
  }
}
