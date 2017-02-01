package uk.ac.ebi.utils.exceptions;

/**
 * Some operation has met too many values unexpectedly.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>26 Oct 2016</dd></dl>
 *
 */
public class TooManyValuesException extends UnexpectedValueException
{
	private static final long serialVersionUID = -8507636676141906245L;

	public TooManyValuesException () {
		super ();
	}

	public TooManyValuesException ( String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace )
	{
		super ( message, cause, enableSuppression, writableStackTrace );
	}

	public TooManyValuesException ( String message, Throwable cause ) {
		super ( message, cause );
	}

	public TooManyValuesException ( String message ) {
		super ( message );
	}

	public TooManyValuesException ( Throwable cause ) {
		super ( cause );
	}

}
