package uk.ac.ebi.utils.exceptions;

/**
 * A {@link NumberFormatException} that allows to include a reference to what caused the exception.
 * 
 * In Java, number format exceptions cannot have a cause, yet, this might be useful, eg, when you want
 * to wrap a number parsing error with details on what you were doing when the problem occurred.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jun 2019</dd></dl>
 *
 */
public class CausalNumberFormatException extends NumberFormatException
{

	private static final long serialVersionUID = -5836506237352493658L;

	public CausalNumberFormatException ( String s ) {
		super ( s );
	}

	public CausalNumberFormatException ( String s, Throwable cause ) {
		super ( s );
		this.initCause ( cause );
	}
}
