package uk.ac.ebi.utils.string;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 May 2018</dd></dl>
 *
 */
public class StringSearchUtilsTest
{
	@Test
	public void testIndexOfAny ()
	{
		assertEquals ( "Bad result!", "a test".length (), StringSearchUtils.indexOfAny ( "a test string", 2, ' ' ) );
		assertEquals ( "Bad result (key in first pos)!", 1, StringSearchUtils.indexOfAny ( "a test string", 1, " te" ) );
		assertEquals ( "Bad result (key before startPos)!", -1, StringSearchUtils.indexOfAny ( "_test string", 1, '_', 'z' ) );
		assertEquals ( "Bad result (second key)!", 1, StringSearchUtils.indexOfAny ( "A_test string", 0, ' ', '_' ) );
	}
}
