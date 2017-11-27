package uk.ac.ebi.utils.ids;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

/**
 * Utilities about management of identifiers.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 Aug 2015</dd></dl>
 *
 */
public class IdUtils
{
	/**
	 * This is needed by {@link #createCompactUUID()}, to convert binary UUIDs into BASE64 strings.
	 */
	private static ThreadLocal<ByteBuffer> uuidBuffer = new ThreadLocal<ByteBuffer> () 
	{
		@Override
		protected ByteBuffer initialValue ()
		{
			return ByteBuffer.allocate ( 2 * Long.SIZE / 8 );
		}		
	};
	
	
	/**
	 * @return an UUID-based new unique identifier, which is also turned into a readable and size-optimised string.
	 * This is based on encoding the 16 bytes returned by {@link UUID#randomUUID()} in BASE64 format, which yields
	 * a 24 character string, where the last two are always '==' padding characters, hence these are removed and
	 * a final 22-character result is returned. This is the best compromise that we know between size, readability and
	 * portability across non-binary formats.
	 * 
	 */
	public static String createCompactUUID ()
	{
		UUID uuid = UUID.randomUUID ();
		ByteBuffer buf = uuidBuffer.get ();
		
		buf.clear ();
		buf.putLong ( uuid.getMostSignificantBits () );
		buf.putLong ( uuid.getLeastSignificantBits () );
		byte[] hash = buf.array ();

		return DatatypeConverter.printBase64Binary ( hash ).substring ( 0, 22 );
	}
	
	/**
	 * Takes a string that is supposed to represent the identifier of a resource and turns it into an opaque compact and 
	 * URI-compatible representation. At the moment it hashes the parameter (via MD5) and converts the hash into lower-case
	 * hexadecimal. 
	 * 
	 */
	public static String hashUriSignature ( String sig ) 
	{
		if ( sig == null ) throw new IllegalArgumentException ( "Cannot hash a null URI" );
		
		MessageDigest messageDigest = null;		
		try {
			messageDigest = MessageDigest.getInstance ( "MD5" );
		} 
		catch ( NoSuchAlgorithmException ex ) {
			throw new IllegalArgumentException ( "Internal error, cannot get the MD5 digester from the JVM", ex );
		}
	
		String hash = DatatypeConverter.printHexBinary ( messageDigest.digest ( sig.getBytes () ) );
		hash = hash.toLowerCase ();
		
		// log.trace ( "Returning hash '{}' from input '{}'", hash, sig );
		
		return hash;
	}
	
	/** 
	 * Invokes {@link URLEncoder#encode(String, String)} with UTF-8 encoding and wraps the generated exception with 
	 * an {@link IllegalArgumentException}.
	 * 
	 * @return null if the parameter is null, or the URL-encoded string.
	 */
	public static String urlEncode ( String queryStringUrl )
	{
		try {
			if ( queryStringUrl == null ) return null;
			return URLEncoder.encode ( queryStringUrl, "UTF-8" );
		}
		catch ( UnsupportedEncodingException ex ) {
			throw new IllegalArgumentException ( "That's strange, UTF-8 encoding seems wrong for encoding '" + queryStringUrl + "'" );
		}
	}	
}
