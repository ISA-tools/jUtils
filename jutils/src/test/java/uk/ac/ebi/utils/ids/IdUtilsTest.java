package uk.ac.ebi.utils.ids;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Feb 2020</dd></dl>
 *
 */
public class IdUtilsTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	public void testHashUriSignature ()
	{
		String s = "Hello, World!";
		String hash = IdUtils.hashUriSignature ( s );
	
		log.info ( "Hash for \"{}\" is '{}'", s, hash );
		Assert.assertNotNull ( "hash is null!", hash );
		Assert.assertTrue ( "hash doesn't match!", hash.contains ( "65a8e27d8879283831b664bd8b7f0ad4" ) );
		
		// Just in case
		Assert.assertFalse ( "Same hashes?!", hash.equals ( IdUtils.hashUriSignature ( s.toLowerCase () ) ));
	}


	@Test
	public void testCreateCompactUUID ()
	{
		String id = IdUtils.createCompactUUID ();
		
		log.info ( "ID is '{}'", id );
		Assert.assertNotNull ( "Id is null!", id );
		Assert.assertEquals ( "ID length is wrong! ", 22, id.length () );
		
		// Just in case
		IntStream.rangeClosed ( 1, 100 ).forEach ( 
			i -> Assert.assertFalse ( "A duplicate ID?!?", id.equals ( IdUtils.createCompactUUID () ) )
		);
	}
	
}
