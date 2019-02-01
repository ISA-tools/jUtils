package uk.ac.ebi.utils.exceptions;

/**
 * An operation has met some unexpected value.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>26 Oct 2016</dd></dl>
 *
 */
public class UnexpectedValueException extends RuntimeException
{
	private static final long serialVersionUID = 6254096583521200227L;


	public UnexpectedValueException ( String message, Throwable cause ) {
		super ( message, cause );
	}

	public UnexpectedValueException ( String message ) {
		super ( message );
	}
}
