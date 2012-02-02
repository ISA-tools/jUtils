package uk.ac.ebi.utils.regex;

import java.util.regex.Pattern;

import static java.lang.System.out;
import static org.junit.Assert.*;

import org.junit.Test;

public class RegExTest
{
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
