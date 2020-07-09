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
package uk.ac.ebi.utils.memory;

import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * An helper that allows to release/reset resources when free memory goes beyond a given limit. This is needed in certain
 * cases, eg, when it's not clear why Hibernate keep filling up the memory and it has been proved that reinitialising 
 * the enity manager fixes the problem.
 *
 * <dl><dt>date</dt><dd>Jul 11, 2011</dd></dl>
 * @author brandizi
 *
 */
public class MemoryUtils
{
	private final static Logger log = LoggerFactory.getLogger ( MemoryUtils.class );

	private MemoryUtils () {}


	
	/**
	 * <p>Invoke this when you think it's safe to invoke the action (eg, when you can get rid of
	 * all the objects and connections in an Hibernate entity manager). The method checks the amount of free memory 
	 * still available to the JVM and, if this is &lt; minFreeMemoryRatio, executes the action and then, if callGc is true, 
	 * invokes the {@link Runtime#gc() garbage collector}.</p>
	 * 
	 * <p>The event triggering is also logged with trace level.</p>
	 * 
	 * <p>The amount of free memory is computed as:</p>
	 *  
	 *   <dl><dd><i>runtime.maxMemory () - ( runtime.totalMemory () - runtime.freeMemory () )</i></dd></dl>
	 *   
	 * <p>That's because the JVM allocates more memory, up to maxMemory(), when freeMemory() reaches the totalMemory()
	 * amount. (hence the above formula first computes the occupied memory and then the true free one</p>
	 * 
	 * <p>Note the method is synchronised, so that you can avoid that a number of threads trigger a memory reset almost
	 * simultaneously.</p> 
	 */
	public static synchronized boolean checkMemory ( Runnable action, double minFreeMemoryRatio, boolean callGc )
	{
		Runtime runtime = Runtime.getRuntime ();
		
		// actual free mem = max - occupied -> max - (total - free)
		float freeMemRatio = ( (float) runtime.maxMemory () - ( runtime.totalMemory () - runtime.freeMemory () ) ) / runtime.maxMemory ();

		if ( freeMemRatio < minFreeMemoryRatio )
		{
			
			if ( log.isTraceEnabled () )
			{
				log.trace ( String.format (  
					"Invoking memory cleaning to increase the quota of %.1f%% free memory", freeMemRatio * 100
				));
			}
			action.run ();
			if ( callGc ) runtime.gc ();
			return true;
		}
		return false;
	}
	
	/**
	 * Defaults to callGc = true.
	 */
	public static synchronized boolean checkMemory ( Runnable action, double minFreeMemory )
	{
		return checkMemory ( action, minFreeMemory, true );
	}


	/**
	 * Defaults to 0.2 (20%) and true.
	 * 
	 */
	public static synchronized boolean checkMemory ( Runnable action ) {
		return checkMemory ( action, 0.2 );
	}

	
	/**
	 * Simple wrapper for the new J9 {@link Cleaner} interface, which replaces 
	 * the old {@code finalize()} method.
	 * 
	 * @param cleanAction is the old finalize method, something you want to do when 
	 * the object isn't reachable anymore. This is {@link Cleaner#register(Object, Runnable) registered}
	 * into a new cleaner and hence set to be auto-invoked when necessary.
	 * 
	 * @see CleaningObject
	 */
	public static Cleanable registerCleaner ( Object obj, Runnable cleanAction )
	{
		return Cleaner.create ().register ( obj, cleanAction );
	}
}
