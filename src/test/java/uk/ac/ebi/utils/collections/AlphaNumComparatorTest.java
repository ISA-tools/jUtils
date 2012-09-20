package uk.ac.ebi.utils.collections;

import java.util.Arrays;

import static org.junit.Assert.*;
import org.junit.Test;

import static java.lang.System.out;


public class AlphaNumComparatorTest
{
	@Test
	public void testBasics ()
	{
		String[] alphaNumStrings = new String[] { "Item 2", "Item 1", "Foo", "Foo 10", "Foo 1", "Item 3", "Ultra-Foo" };
		Arrays.sort ( alphaNumStrings, new AlphaNumComparator<String> () );
		
		out.println ( "Sorting Result: " + Arrays.toString ( alphaNumStrings ) );
		
		String expected[] = new String[] { "Foo", "Foo 1", "Foo 10", "Item 1", "Item 2", "Item 3", "Ultra-Foo" };
		assertTrue ( 
			String.format ( "Result different than what it is expected!\n  Result: %s\n  Result: %s", alphaNumStrings, expected ), 
			Arrays.equals ( alphaNumStrings, expected ) 
		);
	}

	@Test
	public void testZeroPadding ()
	{
		String[] alphaNumStrings = new String[] { "Item 02", "Item 01", "Item 3", "Abc", null, "Item 100" };
		Arrays.sort ( alphaNumStrings, new AlphaNumComparator<String> () );
		
		out.println ( "Sorting Result: " + Arrays.toString ( alphaNumStrings ) );

		String expected[] = new String[] { null, "Abc", "Item 3", "Item 01", "Item 02", "Item 100" };
		assertTrue ( 
			String.format ( "Result different than what it is expected!\n  Result: %s\n  Result: %s", alphaNumStrings, expected ), 
			Arrays.equals ( alphaNumStrings, expected ) 
		);
	}

}