/*
 * __________
 * CREDITS
 * __________
 *
 * Team page: http://isatab.sf.net/
 * - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
 * - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
 * - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
 * - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
 * - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
 *
 * Contributors:
 * - Manon Delahaye (ISA team trainee:  BII web services)
 * - Richard Evans (ISA team trainee: rISAtab)
 *
 *
 * ______________________
 * Contacts and Feedback:
 * ______________________
 *
 * Project overview: http://isatab.sourceforge.net/
 *
 * To follow general discussion: isatab-devel@list.sourceforge.net
 * To contact the developers: isatools@googlegroups.com
 *
 * To report bugs: http://sourceforge.net/tracker/?group_id=215183&atid=1032649
 * To request enhancements:  http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */
package uk.ac.ebi.utils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.input.ReaderInputStream;

import com.google.common.io.Resources;

/**
 * Miscellanea of small IO utilities 
 * 
 * <dl><dt>date</dt><dd>July 29, 2007, 1:03 PM</dd></dl>
 * @author brandizi
 *
 */
public class IOUtils 
{
	private IOUtils () {}
		
	/**
	 * A wrapper of {@link org.apache.commons.io.IOUtils#toString(InputStream, String)} which opens an input stream
	 * from a file path.
	 */
	public static String readFile ( String path, String charSet ) throws IOException {
		return org.apache.commons.io.IOUtils.toString ( new FileInputStream ( path ), charSet );
	}

	/**
	 * Defaults to UTF-8
	 */
	public static String readFile ( String path ) throws IOException { 
		return readFile ( path, "UTF-8" );
	}

	/**
	 * Facility to get a reader from a resource, uses {@link Resources#getResource(Class, String)} and
	 * {@link URL#openStream()}.
	 */
	public static Reader openResourceReader ( Class<?> clazz, String path, Charset charset ) throws IOException
	{
		return new InputStreamReader ( Resources.getResource ( clazz, path ).openStream (), charset );
	}	

	public static Reader openResourceReader ( Class<?> clazz, String path, String charset ) throws IOException
	{
		return openResourceReader ( clazz, path, Charset.forName ( charset ) );
	}	

	/**
	 * Defaults to "UTF-8"
	 */
	public static Reader openResourceReader ( Class<?> clazz, String path ) throws IOException
	{
		return openResourceReader ( clazz, path, "UTF-8" );
	}
	
	
	public static Reader openResourceReader ( String path, Charset charset ) throws IOException
	{
		return new InputStreamReader ( Resources.getResource ( path ).openStream (), charset );
	}

	public static Reader openResourceReader ( String path, String charset ) throws IOException
	{
		return openResourceReader ( path, Charset.forName ( charset ) );
	}	
	
	/**
	 * Defaults to "UTF-8"
	 */
	public static Reader openResourceReader ( String path ) throws IOException
	{
		return openResourceReader ( path, "UTF-8" );
	}
	
	
	

	/**
	 * Facility to read a resource from the class loader associated to a class. 
	 */
	public static String readResource ( Class<?> clazz, String path, Charset charset ) throws IOException
	{
		return readResource ( clazz.getClassLoader (), path, charset );
	}

	public static String readResource ( Class<?> clazz, String path, String charset ) throws IOException 
	{
		return readResource ( clazz, path, Charset.forName ( charset ) );
	}

	/** 
	 * <b>WARNING</b>: after 5.0 this uses UTF-8 as default and not the system default!
	 */
	public static String readResource ( Class<?> clazz, String path ) throws IOException
	{
		return readResource ( clazz, path, "UTF-8" );
	}
	
	
	/**
	 * Uses the class loader in the current thread, or the one in {@link Resources}.
	 */
	public static String readResource ( String path, Charset charset ) throws IOException
	{
		URL url = Resources.getResource ( path );
		return Resources.toString ( url, charset );
	}

	public static String readResource ( String path, String charset ) throws IOException
	{
		return readResource ( path, Charset.forName ( charset ) );
	}

	/**
	 * Defaults to UTF-8 
	 */
	public static String readResource ( String path ) throws IOException
	{
		return readResource ( path, "UTF-8" );
	}
	
	
	/**
	 * Facility to read a resource from a class loader.
	 * @see ClassLoader#getResource(String).
	 * 
	 */
	public static String readResource ( ClassLoader classLoader, String path, Charset charset ) throws IOException
	{
		URL url = classLoader.getResource ( path );
		return Resources.toString ( url, charset );
	}

	public static String readResource ( ClassLoader classLoader, String path, String charset ) throws IOException
	{
		return readResource ( classLoader, path, Charset.forName ( charset ) );
	}
	
	/**
	 * Defaults to UTF-8
	 */
	public static String readResource ( ClassLoader classLoader, String path ) throws IOException
	{
		return readResource ( classLoader, path, "UTF-8" );
	}

	
	/**
	 * Reads the input stream and returns an hash for it, based on the algorithm passed as parameter. algorithm 
	 * is passed to {@link MessageDigest} 
	 */
	public static String getHash ( InputStream is, String algorithm ) throws IOException, NoSuchAlgorithmException 
	{
		MessageDigest md = MessageDigest.getInstance ( algorithm );
	  byte buffer[] = new byte [ 1024 ];
	  
	  try {
	  	for ( int read = is.read ( buffer ); read != -1; read = is.read ( buffer ) )
	  		if ( read > 0 ) md.update ( buffer, 0, read );
		} 
	  finally {
			is.close();
		}
	  byte[] digest = md.digest();
	  if ( digest == null ) return null;
	  StringBuilder strDigest = new StringBuilder ();
	  for ( int i = 0; i < digest.length; i++ )
	    strDigest.append ( Integer.toString ( ( digest[i] & 0xff ) + 0x100, 16).substring ( 1 ) );
	  return strDigest.toString ();	
	}

	/**
	 * A wrapper to {@link #getHash(InputStream, String)} that opens a file.
	 */
	public static String getHash ( File f, String algorithm ) throws IOException, NoSuchAlgorithmException {
		return getHash ( new FileInputStream ( f ), algorithm );
	}

	/**
	 * Hashes a string, using {@link #getHash(InputStream, String)}.
	 */
	public static String getHash ( String string, String algorithm ) throws NoSuchAlgorithmException
	{
		try {
			return getHash ( new ReaderInputStream ( new StringReader ( string ), "UTF-8" ), algorithm
			);
		}
		catch ( IOException ex ) {
			throw new IllegalArgumentException ( "Internal error: " + ex.getMessage (), ex );
		}
	}

	/**
	 * A wrapper of {@link #getHash(InputStream, String)} that uses the MD5 algorithm.
	 */
	public static String getMD5 ( InputStream is ) throws IOException 
	{
		try {
			return getHash ( is, "MD5" );
		}
		catch ( NoSuchAlgorithmException ex ) {
			throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
		}
	} 	
	
	/**
	 * A wrapper of {@link #getHash(InputStream, File)} that uses the MD5 algorithm.
	 */
	public static String getMD5 ( File f ) throws IOException 
	{
		try {
			return getHash ( f, "MD5" );
		}
		catch ( NoSuchAlgorithmException ex ) {
			throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
		}
	}

	/**
	 * A wrapper for {@link #getHash(String, String)} that uses MD5.
	 */
	public static String getMD5 ( String string )
	{
		try {
			return getHash ( string, "MD5" );
		}
		catch ( NoSuchAlgorithmException ex ) {
			throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
		}
	}
	
	
	/**
	 * Creates a URI and intercepts of URISyntaxException, throwing IllegalArgumentException if such an exception
	 * occurs. I've created this method because it's often very annoying to have a checked exception here.
	 */
	public static URI uri ( String uriStr ) 
	{
		try {
			return new URI ( uriStr );
		}
		catch ( URISyntaxException ex ) {
			throw new IllegalArgumentException ( "Internal error with URI '" + uriStr + "'", ex );
		}
	}

	/**
	 * @see #uri(String)
	 */
	public static URL url ( String urlStr ) 
	{
		try {
			return new URL ( urlStr );
		}
		catch ( MalformedURLException ex ) {
			throw new IllegalArgumentException ( "Internal error with URI '" + urlStr + "'", ex );
		}
	}
}
