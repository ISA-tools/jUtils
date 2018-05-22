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
package uk.ac.ebi.utils.string;

import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;


public class StringSearchUtils
{
	private StringSearchUtils () {}
	
	/**
	 * Tells if the string contains one of the matches.
	 */
  public static boolean containsOneOf ( String target, String... matches ) 
  {
  	if ( target == null ) throw new IllegalArgumentException ( 
  		"StringSearchUtils.containsOneOf(): target is null!" 
  	);
  	if ( matches == null || matches.length == 0 ) throw new IllegalArgumentException ( 
  		"containsOneOf(): no match to check!" 
  	);
  	for ( String match: matches )
  		if ( target.contains ( match ) ) return true;
    return false;
  }

  /**
	 * Tells if the string contains one of the matches.
	 */
  public static boolean containsOneOfIgnoreCase ( String target, String... matches ) 
  {
  	if ( target == null ) throw new IllegalArgumentException ( 
  		"StringSearchUtils.containsOneOfIgnoreCase(): target is null!" 
  	);
  	if ( matches == null || matches.length == 0 ) throw new IllegalArgumentException ( 
    	"StringSearchUtils.containsOneOfIgnoreCase(): no match to check!" 
  	);
  	for ( String match: matches )
  		if ( StringUtils.containsIgnoreCase ( target, match ) ) return true;
    return false;
  }

  /**
   * An extension of {@link StringUtils#indexOfAny(CharSequence, char...)} that allows to specify the
   * start position.
   */
  public static int indexOfAny ( final String s, int startPos, char... chars )
  {
  	return indexOfAny ( s, startPos, (str, ks) -> StringUtils.indexOfAny ( str, chars ), chars );
  }
 
  /**
   * An extension of {@link StringUtils#indexOfAny(CharSequence, CharSequence...)} that allows to specify the
   * start position.
   */  
  public static int indexOfAny ( String s, int startPos, CharSequence... chars )
  {
  	return indexOfAny ( s, startPos, (str, ks) -> StringUtils.indexOfAny ( str, chars ), chars );
  }

  /**
   * All the above methods are based on this template: the search function searches for keys in some input (internal
   * to the function, defined by the invoker of this method), returns -1 or the string index where one of the
   * keys was found.
   */
  protected static <KA> int indexOfAny ( String s, int startPos, BiFunction<String, KA, Integer> searcher, KA keys )
  {
  	s = s.substring ( startPos );
  	int idx = searcher.apply ( s, keys );
  	if ( idx == -1 ) return -1;
  	return idx + startPos;
  }
}
