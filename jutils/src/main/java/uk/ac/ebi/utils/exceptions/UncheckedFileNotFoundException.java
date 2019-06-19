package uk.ac.ebi.utils.exceptions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * An unchecked version of {@link FileNotFoundException}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Apr 2018</dd></dl>
 *
 */
public class UncheckedFileNotFoundException extends UncheckedIOException
{
	private static final long serialVersionUID = -3643823729853990709L;

	public UncheckedFileNotFoundException ( String message, IOException cause )
	{
		super ( message, cause );
	}

	public UncheckedFileNotFoundException ( String message ) {
		this ( message, new FileNotFoundException ( "<No root cause>" ) );
	}

}
