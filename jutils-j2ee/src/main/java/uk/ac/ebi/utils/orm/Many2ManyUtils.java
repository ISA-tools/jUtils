package uk.ac.ebi.utils.orm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Facilities to manager many-to-many relations in JPA.
 *
 * <dl><dt>date</dt><dd>Mar 25, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class Many2ManyUtils
{
	/**
	 * Synchronises the addition of an object to a many2many relationship. For example, if you have: 
	 * Product.orders and Order.products, you can implement:
	 * 
	 * <pre>boolean Product.addOrder( Order prod )</pre>
	 * 
	 * as: 
	 * 
	 * <pre>return addManyToMany ( this, order, "addProduct", this.orders ); // or this.getOrders()</pre>
	 *  
	 * where this.orders is the set internal to Product that backs the corresponding
	 * relation. This call invokes this.orders.add ( order ) and, in case this operation returns true (i.e., the order
	 * was not already inside the collection), it invokes order.addProduct ( this ) too. 
	 * 
	 * You should implement Order.product() in a dual way. The check that the element was added to the set avoids 
	 * infinite loops. 
	 *  
	 * @param obj the object to which added is added
	 * @param added the added object
	 * @param inverseRelationAddMethod the name of the method to be called to add obj to added, using the inverse side of 
	 * the relation obj-added relation.
	 * @param internalCollection the collection for the side from obj to added to which added is actually added
	 * @return true if the added was really added to obj.
	 */
	public static <O,A> boolean  addMany2Many ( O obj, A added, String inverseRelationAddMethod, Collection<A> internalCollection ) 
	{
		Exception theEx = null;
		try
		{
			if ( !internalCollection.add ( added ) ) return false;
			Method inverseAdder = added.getClass ().getMethod ( inverseRelationAddMethod, obj.getClass () );
			inverseAdder.invoke ( added, obj );
		} 
		catch ( NoSuchMethodException ex ) { theEx = ex; }
	  catch ( SecurityException ex ) { theEx = ex; }
		catch ( IllegalAccessException ex ) { theEx = ex; }
		catch ( IllegalArgumentException ex ) { theEx = ex; }
		catch ( InvocationTargetException ex ) { theEx = ex; }
		finally {
			if ( theEx != null )
				throw new IllegalArgumentException ( "Internal error: " + theEx.getMessage (), theEx );
		}
		return true;
	}
	
	/**
	 * The opposite of {@link #addMany2Many(Object, Object, String, Collection)}, the parameters and the return value 
	 * have an analogous meaning.
	 * 
	 */
	public static <O,A> boolean  deleteMany2Many ( O obj, A deleted, String inverseRelationDeleteMethod, Collection<A> internalCollection ) 
	{
		Exception theEx = null;
		try
		{
			if ( !internalCollection.remove ( deleted ) ) return false;
			Method inverseRemover = deleted.getClass ().getMethod ( inverseRelationDeleteMethod, obj.getClass () );
			inverseRemover.invoke ( deleted, obj );
		} 
		catch ( NoSuchMethodException ex ) { theEx = ex; }
	  catch ( SecurityException ex ) { theEx = ex; }
		catch ( IllegalAccessException ex ) { theEx = ex; }
		catch ( IllegalArgumentException ex ) { theEx = ex; }
		catch ( InvocationTargetException ex ) { theEx = ex; }
		finally {
			if ( theEx != null )
				throw new IllegalArgumentException ( "Internal error: " + theEx.getMessage (), theEx );
		}
		return true;
	}
	
}
