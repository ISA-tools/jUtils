package uk.ac.ebi.utils.threading;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>Tricks the behaviour of {@link LinkedBlockingQueue} so that methods like {@link #offer(Object)}
 * and {@link #poll()} actually invoke {@link #put(Object)} and {@link #take()}, i.e., the queue always 
 * waits for itself to be free-of/filled-with-some value/s.</p>
 * 
 * <p>This can be used in {@link ThreadPoolExecutor}, which of {@link ThreadPoolExecutor#submit(Runnable)}
 * methods can raise {@link RejectedExecutionException} by calling offer(). By passing this class to its
 * constructor, the executor will always wait for a free thread, either in the executor or in the task queue.</p>
 * 
 * <p><a href = "https://goo.gl/LtV8QL">Credits</a>.</p>
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Dec 2017</dd></dl>
 *
 */
public class HackedBlockingQueue<E> extends LinkedBlockingQueue<E>
{
	private static final long serialVersionUID = -8208126041901869228L;

	public HackedBlockingQueue () {
		super ();
	}

	public HackedBlockingQueue ( Collection<? extends E> c ) {
		super ( c );
	}

	public HackedBlockingQueue ( int capacity ) {
		super ( capacity );
	}

	@Override
	public boolean offer ( E e )
	{
		try {
			put ( e );
			return true;
		}
		catch ( InterruptedException ie ) {
			Thread.currentThread ().interrupt ();
		}
		return false;
	}

	@Override
	public boolean offer ( E e, long timeout, TimeUnit unit ) throws InterruptedException {
		return this.offer ( e );
	}

	@Override
	public E poll ( long timeout, TimeUnit unit ) throws InterruptedException {
		return this.poll ();
	}

	@Override
	public E poll ()
	{
		try {
			return take ();
		}
		catch ( InterruptedException ie ) {
			Thread.currentThread ().interrupt ();
		}
		return null;
	}

	
	/**
	 * Using an instance of this class, returns a fixed size {@link ThreadPoolExecutor} that blocks 
	 * {@link ExecutorService#submit(Runnable)} when both the pool and the submission queue are full.
	 * 
	 */
	public static ThreadPoolExecutor createExecutor ( int poolSize, int queueSize )
	{
		return new ThreadPoolExecutor (
			poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new HackedBlockingQueue<> ( queueSize ) 
		);		
	}
	
	/**
	 * Defaults to {@link Runtime#availableProcessors()} and a submission queue that is twice this number.
	 */
	public static ThreadPoolExecutor createExecutor ()
	{
		int poolSize = Runtime.getRuntime().availableProcessors();
		return createExecutor ( poolSize, poolSize * 2 );
	}

}
