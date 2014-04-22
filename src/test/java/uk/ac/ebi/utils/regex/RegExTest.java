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
package uk.ac.ebi.utils.regex;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

public class RegExTest
{
	@Test
	public void testBasics ()
	{
		RegEx re = new RegEx ( "foo.*", Pattern.CASE_INSENSITIVE  );
		assertTrue ( "foo didn't match fool!", re.matches ( "fool" ) );
		assertFalse ( "foo matched fox!", re.matches ( "fox" ) );
	}
	
	@Test
	public void testGroups ()
	{
		RegEx re = new RegEx ( "(.*):(.*)" );
		String input = "First:Second";
		String groups[] = re.groups ( input );
		out.println ( "I have the groups: " + ArrayUtils.toString ( groups ) );
		assertNotNull ( "Group matching returns null", groups );
		assertEquals ( "Wrong no of returned groups!", 3, groups.length );
		assertTrue ( "Wrong value #0 in returned groups!", input.equals ( groups [ 0 ] ) );
		assertTrue ( "Wrong value #1 in returned groups!", "First".equals ( groups [ 1 ] ) );
		assertTrue ( "Wrong value #2 in returned groups!", "Second".equals ( groups [ 2 ] ) );
	}
	
	@Test
	public void testMatchesAny ()
	{
		assertTrue ( "Wrong result for matchesAny()!", 
			RegEx.matchesAny ( 
				"A test String", 
				Pattern.compile ( "foo" ), 
				Pattern.compile ( "^.*TEST.*$", Pattern.CASE_INSENSITIVE ) )
		);
	}
	
}
