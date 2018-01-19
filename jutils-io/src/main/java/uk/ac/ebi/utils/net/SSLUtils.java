package uk.ac.ebi.utils.net;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

/**
 * SSL Utilities.
 * 
 * At the moment contains methods to disable SSL certificate verification in HTTP connections.
 * <b>WARNING</b>: doing this is <b>UNSAFE</b>, you should fix the bad certificate on server side instead.  
 * 
 * Courtesy of <a href =
 * "http://stackoverflow.com/questions/6047996/ignore-self-signed-ssl-cert-using-jersey-client">
 * this</a>.
 *
 * @author brandizi
 *         <dl>
 *         <dt>Date:</dt>
 *         <dd>29 Nov 2016</dd>
 *         </dl>
 *
 */
public final class SSLUtils
{
	/**
	 * Trusts all certificates.
	 *
	 * @author brandizi
	 * <dl><dt>Date:</dt><dd>19 Jan 2017</dd></dl>
	 *
	 */
	public static class FakeX509TrustManager implements X509TrustManager 
	{
		public X509Certificate[] getAcceptedIssuers () {
			return new X509Certificate [ 0 ];
		}
		public void checkClientTrusted ( X509Certificate[] certs, String authType ) {}
		public void checkServerTrusted ( X509Certificate[] certs, String authType )	{}
	}
	
	
	/**
	 * Set the default Hostname Verifier to an instance of a fake class that trust all hostnames.
	 */
	public static void trustAllHostnames ()
	{
		HttpsURLConnection.setDefaultHostnameVerifier ( new HostnameVerifier() 
		{
			public boolean verify ( String hostname, SSLSession session ) {
				return true;
			}
		});
	}

	/**
	 * Set the default X509 Trust Manager to an instance of a fake class that trust all certificates, even the
	 * self-signed ones.
	 */
	public static void trustAllHttpsCertificates ()
	{
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new FakeX509TrustManager () };

		// Install the all-trusting trust manager
		SSLContext sc;
		try
		{
			sc = SSLContext.getInstance ( "SSL" );
			sc.init ( null, trustAllCerts, new SecureRandom () );
			HttpsURLConnection.setDefaultSSLSocketFactory ( sc.getSocketFactory () );
		}
		catch ( NoSuchAlgorithmException | KeyManagementException ex )
		{
			throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
		}
	}
	
	/**
	 * Gets an {@link HttpClient} that doesn't do any SSL certificate verification.
	 * If user is null, returns a client that doesn't deal with authentication, else sets up a client
	 * that does basic {@link BasicCredentialsProvider HTTP Auth}. 
	 */
	public static HttpClient noCertClient ( String user, String pwd )
	{
		try
		{
			CredentialsProvider credsProvider = null;
			if ( user != null )
			{
				credsProvider = new BasicCredentialsProvider ();
				Credentials credentials = new UsernamePasswordCredentials ( user, pwd );
				credsProvider.setCredentials ( AuthScope.ANY, credentials );
			}
			
			SSLContext sslcontext = 
				SSLContexts
				.custom ()
				.loadTrustMaterial ( 
					null, 
					new TrustStrategy ()
					{
						public boolean isTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
							return true;
						}
					})
				.build();
				
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory ( 
				sslcontext, null, null, new NoopHostnameVerifier () 
			);
			
			HttpClientBuilder builder = HttpClients
				.custom()
				.setSSLSocketFactory ( sslsf );
			
			if ( credsProvider != null ) builder.setDefaultCredentialsProvider ( credsProvider );
			
		  return builder.build();
		}
		catch ( KeyManagementException | NoSuchAlgorithmException | KeyStoreException ex )
		{
			throw new RuntimeException ( 
				"Internal error while setting up no-cert HTTP connection: " + ex.getMessage (),
				ex 
			);
		}			
	}
}
