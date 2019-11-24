package uk.ac.ebi.utils.threading.batchproc;

import java.util.function.BiConsumer;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Nov 2019</dd></dl>
 *
 */
public interface ItemizedBatchCollector<B,E> extends BatchCollector<B>
{
	public BiConsumer<B, E> accumulator();
}
