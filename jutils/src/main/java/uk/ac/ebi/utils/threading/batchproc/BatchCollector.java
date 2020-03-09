package uk.ac.ebi.utils.threading.batchproc;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * ## The Batch Collector
 * 
 * A batch collector is a container of methods to manage matches during the operations of 
 * {@link BatchProcessor}. This default interface offers a {@link #batchFactory() batch factory}, used
 * to obtain a new batch when needed, and a {@link #batchReadyFlag() ready flag}, which establishes if
 * a batch is ready for being processed (eg, it's full wrt o a given size).  
 * 
 * Significant specific sub-interface are the {@link SizedBatchCollector sized-based collectors}, the
 * {@link ItemizedSizedBatchCollector item-based one} and their {@link ItemizedSizedBatchCollector intersection}.  
 *
 * Due to some similarity, batch collectors are named after {@link Collector Java stream collectors}.
 *
 * @param <B> the type of batch that the collector manages.
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
