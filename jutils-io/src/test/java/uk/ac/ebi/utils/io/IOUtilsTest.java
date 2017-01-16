package uk.ac.ebi.utils.io;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Nov 2016</dd></dl>
 *
 */
public class IOUtilsTest
{
	@Test
	public void testReadResource () throws IOException
	{
		String result = IOUtils.readResource ( "logback.xml" );
		Assert.assertTrue ( "readResource( path ) error!", result.contains ( "<configuration>" ) );
	}

	@Test
	public void testReadResourceWithClass () throws IOException
	{
		String result = IOUtils.readResource ( IOUtilsTest.class, "logback.xml" );
		Assert.assertTrue ( "readResource( class, path ) error!", result.contains ( "<configuration>" ) );
	}

	
	@Test
	public void testGetMD5FromString ()
	{
		Assert.assertEquals ( 
			"getMD5() doesn't work!", 
			"65a8e27d8879283831b664bd8b7f0ad4", IOUtils.getMD5 ( "Hello, World!" ) 
		);
	}
	
}
