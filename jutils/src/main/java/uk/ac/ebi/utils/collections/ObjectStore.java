/*
 * __________
 *  CREDITS
 * __________
 *
 * Team page: http://isatab.sf.net/
 *  - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
 *  - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
 *  - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
 *  - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
 *  - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
 *
 * Contributors:
 *  - Manon Delahaye (ISA team trainee:  BII web services)
 *  - Richard Evans (ISA team trainee: rISAtab)
 *
 *  ______________________
 * Contacts and Feedback:
 * ______________________
 *
 * Project overview: http://isatab.sourceforge.net/
 *
 * To follow general discussion: isatab-devel@list.sourceforge.net
 * To contact the developers: isatools@googlegroups.com
 *
 * To report bugs: http://sourceforge.net/tracker/?group_id=215183&atid=1032649
 * To request enhancements:  http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 * __________
 * License
 * __________
 *
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in partby the EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package uk.ac.ebi.utils.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements an object store.
 * Basically, allows to associate a Java object to a type and a key/identifier.
 *
 * User: brandizi
 * Date: Oct 29, 2007
 *
 */
public class ObjectStore<T, K, V>
{
	private Map<T, Map<K, V>> types = new HashMap<T, Map<K, V>> ();
	protected int size = 0;

	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	protected Map<T, Map<K, V>> getInternalTypes ()
	{
		return types;
	}
	
	
	/**
	 * Stores an object, identified by a type and an identifier
	 * If value is null deletes the entry.
	 *
	 */
	public void put ( T type, K key, V value )
	{
		Map<T, Map<K, V>> types = this.getInternalTypes ();
		Map<K, V> idmap = types.get ( type );
		
		if ( idmap == null ) {
			idmap = new HashMap<K, V> ();
			types.put ( type, idmap );
		}

		if ( value == null ) {
			if ( idmap.containsKey ( key ) ) {
				idmap.remove ( key );
				if ( size > 0 ) size--;
			}
		}
		else {
			if ( !idmap.containsKey ( key ) ) size++;
			idmap.put ( key, value );
		}
	}


	/**
	 * Remove all the values associated to the type
	 */
	public void remove ( T type )
	{
		Map<K, V> idmap = this.getInternalTypes ().get ( type );
		if ( idmap == null ) return; 
		
		size -= idmap.size ();
		idmap.clear ();
	}

	
	/**
	 * Gets an object identified by type and key. Returns null in case the entry is
	 * empty.
	 *
	 */
	public V get ( T type, K key )
	{
		Map<K, V> idmap =  this.getInternalTypes ().get ( type );
		if ( idmap == null ) return null;
		return idmap.get ( key );
	}


	public int size () {
		return size;
	}


	/** All the types in the store */
	public Set<T> types () {
		return getInternalTypes ().keySet ();
	}

	/** All the identifiers of objects belonging to a given type */
	public Set<K> typeKeys ( T type ) {
		Map<K, V> idmap = this.getInternalTypes ().get ( type );
		if ( idmap == null ) return null;
		return idmap.keySet ();
	}


	/** All the values of a certain type. Never returns null. */
	public Collection<V> values ( T type ) 
	{
		Map<K,V> idmap = this.getInternalTypes ().get ( type );
		if ( idmap == null ) return Collections.emptySet ();
		return Collections.unmodifiableCollection ( idmap.values () );
	}
	

	
	/**
	 * Prints full contents of the object store.
	 *
	 */
	public String toStringVerbose ()
	{
		StringBuilder result = new StringBuilder ();

		for ( T type : this.types () )
			for (  K key : this.typeKeys ( type ) )
				result.append ( String.format ( "<%s, %s>:\n%s\n", type, key, this.get ( type, key ) ) );
		return result.toString ();
	}


}
