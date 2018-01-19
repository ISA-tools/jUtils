/*
 * __________
 * CREDITS
 * __________
 *
 * Team page: http://isatab.sf.net/
 * - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
 * - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
 * - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
 * - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
 * - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
 *
 * Contributors:
 * - Manon Delahaye (ISA team trainee:  BII web services)
 * - Richard Evans (ISA team trainee: rISAtab)
 *
 *
 * ______________________
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
 *
 * __________
 * License:
 * __________
 *
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */
 
package uk.ac.ebi.utils.collections;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Like {@link ObjectStore}, but with types and keys kept sorted.
 *  
 * @author brandizi
 * <b>date</b>: Mar 3, 2010
 * 
 */
public class SortedObjectStore<T, K, V> extends ObjectStore<T, K, V>
{
	private SortedMap<T, SortedMap<K, V>> types; 
	private final Comparator<K> keyComparator;
	
	public SortedObjectStore () {
		this ( null, null );
	}

	public SortedObjectStore ( Comparator<T> typeComparator, Comparator<K> keyComparator ) 
	{
		types = new TreeMap<T, SortedMap<K,V>> ( typeComparator );
		this.keyComparator = keyComparator;
	}

	
	@Override
	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	protected Map<T, Map<K, V>> getInternalTypes () {
		return (Map) this.types;
	}


	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	public void put ( T type, K key, V value )
	{
		@SuppressWarnings ( { "rawtypes", "unchecked" } )
		SortedMap<T, SortedMap<K,V>> types = (SortedMap<T, SortedMap<K,V>>) (Map) this.getInternalTypes ();
		SortedMap<K, V> idmap = (SortedMap<K,V>) types.get ( type );

		if ( idmap == null ) {
			idmap = new TreeMap<K, V> ( keyComparator );
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
}
