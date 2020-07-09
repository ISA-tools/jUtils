package uk.ac.ebi.utils.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Mar 2020</dd></dl>
 *
 */
public class ThreadUtils
{
	/**
	 * A simple wrapper of {@link ThreadFactoryBuilder} that creates a {@link ThreadFactory} naming new threads like
	 * `<prefix>%d`.
	 * 
	 * This is useful for {@link ThreadPoolExecutor#setThreadFactory(ThreadFactory)}
	 */
	public static ThreadFactory createNamingThreadFactory ( String prefix, ThreadFactory baseFactory )
	{
		ThreadFactoryBuilder builder = new ThreadFactoryBuilder ()
			.setNameFormat ( prefix + "%d" );
		if ( baseFactory != null ) builder.setThreadFactory ( baseFactory );
		return builder.build ();
		
	}

	/**
	 * Uses a default base factory.
	 */
	public static ThreadFactory createNamingThreadFactory ( String prefix )
	{
		return createNamingThreadFactory ( prefix, null );
	}

	/**
	 * Uses {@link Class#getSimpleName()} as prefix.
	 */
	public static ThreadFactory createNamingThreadFactory ( Class<?> namingClass, ThreadFactory baseFactory )
	{
		return createNamingThreadFactory ( namingClass.getSimpleName () + "_", baseFactory );
	}

	/**
	 * Uses a default base factory.
	 */
	public static ThreadFactory createNamingThreadFactory ( Class<?> namingClass )
	{
		return createNamingThreadFactory ( namingClass, null );
	}
	
	/**
	 * Uses {@link #createNamingThreadFactory(String, ThreadFactory)} to setup a naming thread factory for 
	 * this executor, which wraps the existing one. 
	 */
	public static void setNamingThreadFactory ( String prefix, ThreadPoolExecutor executor )
	{
		executor.setThreadFactory ( createNamingThreadFactory ( prefix, executor.getThreadFactory () ) );
	}

	/**
	 * Wraps {@link #setNamingThreadFactory(String, ThreadPoolExecutor)} if executor is an instance of
	 * {@link ThreadPoolExecutor}, else has no effect.
	 */
	public static void setNamingThreadFactory ( String prefix, ExecutorService executor )
	{
		if ( !(executor instanceof ThreadPoolExecutor) ) return;
		setNamingThreadFactory ( prefix, (ThreadPoolExecutor) executor );
	}

	/**
	 * Works like {@link #setNamingThreadFactory(String, ThreadPoolExecutor)}, but uses
	 * {@link #createNamingThreadFactory(Class, ThreadFactory)}.
	 */
	public static void setNamingThreadFactory ( Class<?> namingClass, ThreadPoolExecutor executor )
	{
		executor.setThreadFactory ( createNamingThreadFactory ( namingClass, executor.getThreadFactory () ) );
	}

	/**
	 * Works like {@link #setNamingThreadFactory(String, ExecutorService)}, but invokes 
	 * {@link #setNamingThreadFactory(Class, ThreadPoolExecutor)} instead.
	 */
	public static void setNamingThreadFactory ( Class<?> namingClass, ExecutorService executor )
	{
		if ( !(executor instanceof ThreadPoolExecutor) ) return;
		setNamingThreadFactory ( namingClass, (ThreadPoolExecutor) executor );
	}
}
