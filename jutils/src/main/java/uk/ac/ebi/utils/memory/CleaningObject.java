package uk.ac.ebi.utils.memory;

/**
 * Simulates the old {@code finalize()} method in Java >= 9.
 * 
 * <p>This is based on {@link MemoryUtils#registerCleaner(Object, Runnable) registering a new cleaner}
 * when an instance of this class (or subclasses) is created, such cleaner invokes {@link #close()}, 
 * which plays the same role of the old finalizer method.</p>
 * 
 * <p>This can be useful when you have a set of classes with an old finalize method, which you can 
 * subclass, after having renamed {@code finalize()} into {@code close()}.</p>
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>31 Mar 2020</dd></dl>
 *
 */
public class CleaningObject implements AutoCloseable
{
	{
		MemoryUtils.registerCleaner ( this, this::close );
	}
	
	@Override
	public void close ()
	{
		// I don't need to do anything, but your subclass does
	}
}
