package uk.ac.ebi.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Mar 2018</dd></dl>
 *
 */
public class Unix4jUtilsTest
{
	protected static String getReplacement ( String s, String regex, String repl ) throws IOException
	{
		StringReader testReader = new StringReader ( s );
		ReaderInputStream ins = new ReaderInputStream ( testReader, "UTF-8" );
		InputStream wrappedIns = Unix4jUtils.sedFilter ( ins, regex, repl );
		String outStr = IOUtils.toString ( wrappedIns, "UTF-8" );
		return outStr;
	}

	protected static String runSedScript ( String s, String sedScript ) throws IOException
	{
		StringReader testReader = new StringReader ( s );
		ReaderInputStream ins = new ReaderInputStream ( testReader, "UTF-8" );
		InputStream wrappedIns = Unix4jUtils.sedFilter ( ins, sedScript );
		String outStr = IOUtils.toString ( wrappedIns, "UTF-8" );
		return outStr;
	}
	
	
	@Test
	public void testSubstitutionIn () throws IOException
	{
		String outStr = getReplacement ( "just a test string", "test", "fancy" );
		Assert.assertEquals ( "Wrong sed result!", "just a fancy string", outStr );
	}

	@Test
	public void testSubstBackRefIn () throws IOException
	{
		String outStr = getReplacement ( "just a test string", "(test|string)", "'$1'" );
		// 'g' is disabled, only the first is replaced
		Assert.assertEquals ( "Wrong sed result!",  "just a 'test' string", outStr );
	}
	
	
	@Test
	public void testSedScriptIn () throws IOException
	{
		String outStr = runSedScript ( "just a test string", "s/(test|string)/'$1'/g" );
		Assert.assertEquals ( "Wrong sed result!",  "just a 'test' 'string'", outStr );
	}
	
	@Test
	public void testMultiLine () throws IOException
	{
		String outStr = runSedScript ( "just a test string\nAnother string", "s/(test|string)/'$1'/g" );
		Assert.assertEquals ( "Wrong sed result!",  "just a 'test' 'string'\nAnother 'string'", outStr );
	}
	
}
