package uk.ac.ebi.utils.security;

import java.nio.ByteBuffer;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

/**
 * Utilities about management of identifiers.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 Aug 2015</dd>
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
}
