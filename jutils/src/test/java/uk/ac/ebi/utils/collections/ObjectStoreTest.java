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
 * - Manon Delahaye (ISA team trainee: BII web services)
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
 * To request enhancements: �http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * Reciprocal Public License 1.5 (RPL1.5)
 * [OSI Approved License]
 *
 * Reciprocal Public License (RPL)
 * Version 1.5, July 15, 2007
 * Copyright (C) 2001-2007
 * Technical Pursuit Inc.,
 * All Rights Reserved.
 *
 * http://www.opensource.org/licenses/rpl1.5.txt
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package uk.ac.ebi.utils.collections;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class ObjectStoreTest
{
	private ObjectStore<String, Integer, String> store;
	private String testObj = new String ( "Object 2.1" );

	public ObjectStoreTest ()
	{
		store = new ObjectStore<String, Integer, String> ();

		store.put ( "type1", 1, "Object 1.1" );
		store.put ( "type2", 1, testObj );
		store.put ( "type1", 2, "Object 1.2" );
	}


	@Test
	public void testGet ()
	{
		Object o = store.get ( "type2", 1 );
		assertNotNull ( "Could not retrieve the original object!",  o );
		assertEquals ( "Retrieved object is not the same!", o, testObj );
		out.println ( "Retrieved Object: " + o );
	}





	@Test
	public void testRemove ()
	{
		Object o = store.get ( "type1", 2 );
		assertNotNull ( "Could not retrieve the original object!",  o );
		assertTrue ( "Retrieved object is wrong!", "Object 1.2".equals ( o ) );
		out.println ( "Retrieved Object: " + o );

		store.put ( "type1", 2, null );
		Object o1 = store.get ( "type1", 2 );
		assertNull ( "Could not delete the entry '" + o + "'", o1 );
		out.println ( "Entry '" + o + "' successfully deleted" );

	}


	@Test
	public void testSize ()
	{
		// Addition
		int sz = store.size ();
		assertEquals ( "Wrong size: " + sz, sz, 3 );
		out.println ( "OK, size is 3" );

		// Removal
		store.put ( "type1", 1, null );
		sz = store.size ();
		assertEquals ( "Wrong size: " + sz, sz, 2 );
		out.println ( "OK, size is 2 after removal" );

		// Non existing entry
		store.put ( "type1", 100, null );
		sz = store.size ();
		assertEquals ( "Wrong size: " + sz, sz, 2 );
		out.println ( "OK, size is still 2 after removal of non existing entry" );

		// Non existing entry / 2
		store.put ( "type11", 100, null );
		sz = store.size ();
		assertEquals ( "Wrong size: " + sz, sz, 2 );
		out.println ( "OK, size is still 2 after removal of non existing entry/2" );

		// removal idempotency
		store.put ( "type1", 1, null );
		sz = store.size ();
		assertEquals ( "Wrong size: " + sz, sz, 2 );
		out.println ( "OK, size is still 2 after re-removal" );

		// Try to remove something from empty store
		ObjectStore<Integer, Integer, String> store1 = new ObjectStore<Integer, Integer, String> ();
		store1.put ( 1, 2, null );
		sz = store1.size ();
		assertEquals ( "Wrong size: " + sz, sz, 0 );
		out.println ( "OK, size is 0 after removal of an empty key" );
	}


	@Test
	public void testGetAll ()
	{
		int ct = 0, sz = store.size ();
		for ( String type: store.types () )
			for ( int id: store.typeKeys ( type )) {
				out.println ( String.format (
					"<%s,%s>: %s", type, id, store.get ( type, id ) ) );
				ct++;
		}

		assertEquals ( "Uh?! I've printed the wrong number of items", ct, sz );
		out.println ( "I've printed " + sz + " values, as expected" );
	}

	
	@Test
	public void testSorted ()
	{
		SortedObjectStore<String, Integer, String> sortedStore = new SortedObjectStore<> ();
		for ( String type: store.types () )
			for ( int key: store.typeKeys ( type ) )
				sortedStore.put ( type, key, store.get ( type, key ) );
		
		List<String> ltypes = sortedStore.types ().stream ().collect ( Collectors.toList () );
		assertEquals ( "Wrong types (size)!", 2, ltypes.size () );
		assertEquals ( "Wrong type (0)!", "type1", ltypes.get ( 0 ) );
		assertEquals ( "Wrong type (1)!", "type2", ltypes.get ( 1 ) );
		
		List<Integer> lkeys = sortedStore.typeKeys ( "type1" ).stream ().collect ( Collectors.toList () );
		assertEquals ( "Wrong keys (size)!", 2, lkeys.size () );
		assertEquals ( "Wrong key (0)!", 1, (int) lkeys.get ( 0 ) );
		assertEquals ( "Wrong key (1)!", 2, (int) lkeys.get ( 1 ) );		
	}
	
}
