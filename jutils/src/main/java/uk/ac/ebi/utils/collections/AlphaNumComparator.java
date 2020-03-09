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

/**
 * The Alphanum Algorithm is an improved sorting algorithm for strings
 * containing numbers. Instead of sorting numbers in ASCII order like
 * a standard sort, this algorithm sorts numbers in numeric order. 
 * The Alphanum Algorithm is discussed at http://www.DaveKoelle.com<p/>
 * 
 * This is an updated version with enhancements made by Daniel Migowski, Andre Bogus, and David Koelle.<p/>
 * 
 * To convert to use Templates (Java 1.5+): - Change "implements Comparator" to "implements Comparator<String>" - Change
 * "compare(Object o1, Object o2)" to "compare(String s1, String s2)" - Remove the type checking and casting in
 * compare().<p/>
 * 
 * To use this class: for example, use the static "sort" method from the {@link java.util.Collections} class: 
 * Collections.sort(your list, new AlphanumComparator()); See {@link AlphaNumComparatorTest} for examples.
 * 
 * <h3>Notes</h3>
 * <ul> 
 *   <li>null is considered lower than anything else.</li>
 *   <li>"Item 3" comes before "Item 01", when you have zero-padding, it is taken into account.</li>
 * </ul> 
 * 
 */
public class AlphaNumComparator<T> implements Comparator<T>
{
	private final boolean isCaseSensitive;
	
	/** Default is true */
	public AlphaNumComparator ( boolean isCaseSensitive ) {
		this.isCaseSensitive = isCaseSensitive;
	} 
	
	/** Default is true */
	public AlphaNumComparator () {
		this ( true );
	}
	
	private boolean isDigit ( char ch )
	{
		return ch >= '0' && ch <= '9';
	}

	/**
	 * Length of string is passed in for improved efficiency (only need to calculate it once) *
	 */
	private String getChunk ( String s, int slength, int marker )
	{
		StringBuilder chunk = new StringBuilder ();
		char c = s.charAt ( marker );
		chunk.append ( c );
		marker++;
		if ( isDigit ( c ) )
		{
			while ( marker < slength )
			{
				c = s.charAt ( marker );
				if ( !isDigit ( c ) )
				{
					break;
				}
				chunk.append ( c );
				marker++;
			}
		} else
		{
			while ( marker < slength )
			{
				c = s.charAt ( marker );
				if ( isDigit ( c ) )
				{
					break;
				}
				chunk.append ( c );
				marker++;
			}
		}
		return chunk.toString ();
	}

	public int compare ( T o1, T o2 )
	{
		if ( o1 == null ) return o2 == null ? 0 : -1; 
		if ( o2 == null ) return +1; // o1 != null here
				
		String s1 = o1.toString ();
		String s2 = o2.toString ();

		if ( s1 == null ) return s2 == null ? 0 : -1; 
		if ( s2 == null ) return +1; // s1 != null here

		int thisMarker = 0;
		int thatMarker = 0;
		int s1Length = s1.length ();
		int s2Length = s2.length ();

		while ( thisMarker < s1Length && thatMarker < s2Length )
		{
			String thisChunk = getChunk ( s1, s1Length, thisMarker );
			thisMarker += thisChunk.length ();

			String thatChunk = getChunk ( s2, s2Length, thatMarker );
			thatMarker += thatChunk.length ();

			// If both chunks contain numeric characters, sort them numerically
			int result = 0;
			if ( isDigit ( thisChunk.charAt ( 0 ) ) && isDigit ( thatChunk.charAt ( 0 ) ) )
			{
				// Simple chunk comparison by length.
				int thisChunkLength = thisChunk.length ();
				result = thisChunkLength - thatChunk.length ();
				// If equal, the first different number counts
				if ( result == 0 )
				{
					for ( int i = 0; i < thisChunkLength; i++ )
					{
						result = thisChunk.charAt ( i ) - thatChunk.charAt ( i );
						if ( result != 0 )
						{
							return result;
						}
					}
				}
			} else
			{
				result = isCaseSensitive ? thisChunk.compareTo ( thatChunk ) : thisChunk.compareToIgnoreCase ( thatChunk );
			}

			if ( result != 0 )
			{
				return result;
			}
		}

		return s1Length - s2Length;
	}
}
