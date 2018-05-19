package uk.ac.ebi.utils.exceptions;

/**
 * To be used when the execution flow of a program has met an event that is unusual, rare, or alike. E.g., it can 
 * be used to wrap {@link InterruptedException} with an unchecked exception. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>14 Dec 2017</dd></dl>
 *
 */
public class UnexpectedEventException extends RuntimeException
{
	private static final long serialVersionUID = 5321447570062748987L;

	public UnexpectedEventException ( String message, Throwable cause ) {
		super ( message, cause );
	}

	public UnexpectedEventException ( String message ) {
		super ( message );
	}
}
