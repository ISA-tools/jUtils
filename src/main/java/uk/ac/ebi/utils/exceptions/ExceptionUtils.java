package uk.ac.ebi.utils.exceptions;

/**
 * Utilities related to excption handling.
 *
 * <dl><dt>date</dt><dd>2 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ExceptionUtils
{
	private ExceptionUtils () {}
	
	/**
	 * Gets the root cause of an exception.
	 */
	public static Throwable getRootCause ( Throwable ex )
	{
		for ( Throwable cause; ; ex = cause )
			if ( ( cause = ex.getCause () ) == null ) return ex;
	}
	
}
