package uk.ac.ebi.utils.exceptions;

/**
 * Some operation has met too few values unexpectedly, eg, a collection is empty.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>10 Dec 2019</dd></dl>
 *
 */
public class TooFewValuesException extends UnexpectedValueException
{
	private static final long serialVersionUID = -5861844454140890391L;

	public TooFewValuesException ( String message, Throwable cause ) {
		super ( message, cause );
	}

	public TooFewValuesException ( String message ) {
		super ( message );
	}
}
