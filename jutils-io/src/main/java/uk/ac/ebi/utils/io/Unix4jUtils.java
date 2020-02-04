package uk.ac.ebi.utils.io;

import java.io.InputStream;
import java.io.OutputStream;

import org.unix4j.Unix4j;
import org.unix4j.builder.To;
import org.unix4j.builder.Unix4jCommandBuilder;
import org.unix4j.command.Command;

import com.gc.iotools.stream.is.InputStreamFromOutputStream;

/**
 * <p>Utilities for the great <a href = 'https://github.com/tools4j/unix4j'>Unix4j</a> library.</p>
 *
 * <p><b>WARNING</b>: In order to avoid too many dependency for jUtils, you have to declare the dependency on
 * both Unix4j and EasyStream (copy-paste them from the junit-io's pom).</p>
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Mar 2018</dd></dl>
 *
 */
public class Unix4jUtils
{
	/**
	 * <p>Pipes the output of a Unix4j command</p>
	 * <p>This sends an {@link InputStream} to a Unix4j command and returns another input stream that is the
	 * {@link To#toOutputStream(OutputStream) output} of that command.</p> 
	 * 
	 * <p>Differently from {@link Command#join(Command)}, this emulates the pipe '|' operator in Unix in a way such that 
	 * the command's output can be sent to some Java input consumer, after it has been filtered by the command.</p>
	 *  
	 * <p>Also note that we're doing this dynamically, the initial input stream is processed while a consumer reads
	 * the stream returned by this method. 
	 * This is achieved through the <a href = 'https://sourceforge.net/projects/easystream.io-tools.p/'>EasyStream</a>
	 * library</p>
	 *  
	 */
	public static InputStream unixFilter ( final To command, final InputStream inStream )
	{
		InputStreamFromOutputStream<Void> pipeIn = new InputStreamFromOutputStream<Void> () 
		{
			@Override
			protected Void produce ( OutputStream sink ) throws Exception
			{
				command.toOutputStream ( sink );
				return null;
			}
		};
		
		return pipeIn;
	}
	
	/**
	 * Just a wrapper that uses {@link #unixFilter(To, InputStream)} to process an input stream via the  
	 * {@link Unix4jCommandBuilder#sed(String) sed command}.  
	 */
	public static InputStream sedFilter ( final InputStream inStream, String sedScript ) {
		return unixFilter (  Unix4j.from ( inStream ).sed ( sedScript ), inStream );
	}
	
	/**
	 * Just a wrapper of {@link #sedFilter(InputStream, String)} that tweaks a stream dynamically, to replace text
	 * based on a regular expression. The repl parameter accepts backreferences like {@code \1}.
	 * 
	 * @return the filter that does the replacement, as an {@link InputStream} in which the filtering operation is 
	 * invoked dynamically while, the stream is read.
	 */
	public static InputStream sedFilter ( final InputStream inStream, String regex, String repl ) {
		return unixFilter (  Unix4j.from ( inStream ).sed ( regex, repl ), inStream );
	}
}
