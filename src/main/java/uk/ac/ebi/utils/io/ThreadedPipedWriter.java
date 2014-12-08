package uk.ac.ebi.utils.io;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Writer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * This is a piped writer that runs write operations as a background thread. This is useful in situations
 * where there is a component that gives you some output via a {@link Writer} and you want to send such output
 * straight to a reader (e.g., a connection proxy). This is ispired by this 
 * <a href = 'http://ostermiller.org/convert_java_writer_reader.html'>ostermiller post</a>.
 *  
 * TODO: Never used/tested! 
 * 
 * <dl><dt>date</dt><dd>3 Nov 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ThreadedPipedWriter extends Writer
{
	private final PipedReader reader = new PipedReader ();
	private final PipedWriter writer;
	
	private IOException ex = null;
	
	ExecutorService texec = Executors.newSingleThreadExecutor ();
	
	private abstract class IORunnable implements Runnable 
	{
		/**
		 * Catch IO exceptions and set {@link #ex}, so that the main thread can raise the same to the caller.
		 * Override bareRun and not this.
		 */
		public final void run () 
		{
			try {
				bareRun ();
			}
			catch ( IOException ex ) 
			{
				ThreadedPipedWriter.this.ex = new IOException ( 
					"I/O problem while piping a writer to a reader: " + ex.getMessage (), ex );
			}
		}
		public abstract void bareRun () throws IOException;
	}
	
	
	public ThreadedPipedWriter () throws IOException
	{
		writer = new PipedWriter ( reader );
	}
	
	@Override
	public void write ( final char[] cbuf, final int off, final int len ) throws IOException
	{
		if ( this.ex != null ) throw new IOException ( 
			"Cannot write() due to previous exception:" + ex.getMessage (), ex );
		
		texec.submit ( new IORunnable() 
		{
			@Override
			public void bareRun () throws IOException
			{
				writer.write ( cbuf, off, len );
			}
		});
	}

	@Override
	public void write ( final int c ) throws IOException
	{
		if ( this.ex != null ) throw new IOException ( 
			"Cannot write() due to previous exception:" + ex.getMessage (), ex );
		
		texec.submit ( new IORunnable() 
		{
			@Override
			public void bareRun () throws IOException
			{
				writer.write ( c );
			}
		});
	}

	@Override
	public void write ( final char[] cbuf ) throws IOException
	{
		if ( this.ex != null ) throw new IOException ( 
			"Cannot write() due to previous exception:" + ex.getMessage (), ex );
		
		texec.submit ( new IORunnable() 
		{
			@Override
			public void bareRun () throws IOException
			{
				writer.write ( cbuf );
			}
		});
	}

	@Override
	public void write ( final String str ) throws IOException
	{
		if ( this.ex != null ) throw new IOException ( 
			"Cannot write() due to previous exception:" + ex.getMessage (), ex );
		
		texec.submit ( new IORunnable() 
		{
			@Override
			public void bareRun () throws IOException
			{
				writer.write ( str );
			}
		});
	}

	@Override
	public void write ( final String str, final int off, final int len ) throws IOException
	{
		if ( this.ex != null ) throw new IOException ( 
			"Cannot write() due to previous exception:" + ex.getMessage (), ex );
		
		texec.submit ( new IORunnable() 
		{
			@Override
			public void bareRun () throws IOException
			{
				writer.write ( str, off, len );
			}
		});
	}

	@Override
	public Writer append ( final CharSequence csq ) throws IOException
	{
		if ( this.ex != null ) throw new IOException ( 
			"Cannot append() due to previous exception:" + ex.getMessage (), ex );
		
		texec.submit ( new IORunnable() 
		{
			@Override
			public void bareRun () throws IOException
			{
				writer.append ( csq );
			}
		});
		return this;
	}

	@Override
	public Writer append ( final CharSequence csq, final int start, final int end ) throws IOException
	{
		if ( this.ex != null ) throw new IOException ( 
			"Cannot append() due to previous exception:" + ex.getMessage (), ex );
		
		texec.submit ( new IORunnable() 
		{
			@Override
			public void bareRun () throws IOException
			{
				writer.append ( csq, start, end );
			}
		});
		return this;
	}

	@Override
	public Writer append ( final char c ) throws IOException
	{
		if ( this.ex != null ) throw new IOException ( 
			"Cannot append() due to previous exception:" + ex.getMessage (), ex );
		
		texec.submit ( new IORunnable() 
		{
			@Override
			public void bareRun () throws IOException
			{
				writer.append ( c );
			}
		});
		return this;
	}

	@Override
	public void flush () throws IOException
	{
		if ( this.ex != null ) throw new IOException ( 
			"Cannot flush() due to previous exception:" + ex.getMessage (), ex );
			
		texec.submit ( new IORunnable() 
		{
			@Override
			public void bareRun () throws IOException
			{
				writer.flush ();
			}
		});
	}

	@Override
	public void close () throws IOException
	{
		if ( this.ex != null ) throw new IOException ( 
			"Cannot close() due to previous exception:" + ex.getMessage (), ex );
			
		texec.submit ( new IORunnable() 
		{
			@Override
			public void bareRun () throws IOException
			{
				writer.close ();
			}
		});
	}
	
	public PipedReader getReader ()
	{
		return reader;
	}

	public IOException getIOException ()
	{
		return ex;
	}
	
	public void resetException ()
	{
		this.ex = null;
	}
}
