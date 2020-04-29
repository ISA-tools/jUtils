package uk.ac.ebi.utils.exceptions;

/**
 * An unchecked version of {@link InterruptedException}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 Apr 2020</dd></dl>
 *
 */
public class UncheckedInterruptedException extends RuntimeException
{
	private static final long serialVersionUID = -6578041841532709062L;

	public UncheckedInterruptedException ( String message, InterruptedException cause )
	{
		super ( message, cause );
	}

	public UncheckedInterruptedException ( String message )
	{
		super ( message );
	}
}
