package uk.ac.ebi.utils.threading.batchproc;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Nov 2019</dd></dl>
 *
 */
public interface BatchCollector<B>
{
	public abstract Supplier<B> batchFactory();
	public abstract Predicate<B> batchReadyFlag();
}
