package uk.ac.ebi.utils.exceptions;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.reflect.ConstructorUtils;

/**
 * Utilities related to exception handling.
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
	
	
	/**
	 * Helper to ease the building of an exception and its message.
	 *  
	 * Builds an exception instance of exType, with the given message template instantiated with
	 * the given paramenters. The message template is passed to {@link String#format(String, Object...)}, 
	 * so you have to use the printf-style rules.
	 * 
	 * The new exception is assigned a cause, if the corresponding parameter is non-null.
	 * 
	 */
	public static <E extends Throwable> E buildEx (
		Class<E> exType, Throwable cause, String messsgeTemplate, Object... params
	)
	{		
		try
		{
			String msg = String.format ( messsgeTemplate, params );
			return cause == null
				?	ConstructorUtils.invokeConstructor ( exType, msg )
				: ConstructorUtils.invokeConstructor ( exType, msg, cause );
		}
		catch ( NoSuchMethodException | SecurityException | InstantiationException 
						| IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
		{
			throw buildEx ( 
				IllegalArgumentException.class,
				ex,
				"Error while throwing exception for the message \"%s\": %s",
				messsgeTemplate,
				ex.getMessage ()
			);
		}
	}
	
	/**
	 * Wrapper with no cause.
	 * 
	 */
	public static <E extends Throwable> E buildEx (
		Class<E> exType, String messsgeTemplate, Object... params
	)
	{
		return buildEx ( exType, null, messsgeTemplate, params );
	}
	
	/**
	 * This calls {@link #buildEx(Class, Throwable, String, Object...)} and then throws the built exception. 
	 * Note that jave will consider this method as throwing checked/unchecked exception code, depending on
	 * the type of E.
	 * 
	 * Note that you cannot always use this to wrap the body of a function, since, if you do for a checked exception,
	 * Java will think that you are not returning any value after the catch clause. 
	 */
	public static <E extends Throwable> void throwEx (
		Class<E> exType, Throwable cause, String messsgeTemplate, Object... params
	) throws E
	{
		throw buildEx ( exType, cause, messsgeTemplate, params );
	}	

	/**
	 * A wrapper with no cause.
	 */
	public static <E extends Throwable> void throwEx (
		Class<E> exType, String messsgeTemplate, Object... params
	) throws E
	{
		throwEx ( exType, null, messsgeTemplate, params );
	}	
}
