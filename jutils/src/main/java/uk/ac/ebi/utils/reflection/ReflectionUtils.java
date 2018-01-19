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
package uk.ac.ebi.utils.reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Jan 15, 2008
 * @author brandizi
 *
 *
 */
public class ReflectionUtils
{
	
	/**
	 * This method has been copied from: 
   * http://www.artima.com/weblogs/viewpostP.jsp?thread=208860
   * 
   * WARNING: This method won't work if type is a generic, no matter if the generic variable is bound
   * in the object assignment (see the post above for details). If that is the case, re-declare type
   * as abstract and use anonymous classes. 
	 *
   * Gets the underlying class for a type, or null if the type is a variable type.
   * 
   * @param type the type
   * @return the underlying class
   * 
   */
  public static Class<?> getTypeClass ( Type type ) 
  {
    if (type instanceof Class) {
      return (Class<?>) type;
    }
    else if (type instanceof ParameterizedType) {
      return getTypeClass(((ParameterizedType) type).getRawType());
    }
    else if (type instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      Class<?> componentClass = getTypeClass(componentType);
      if (componentClass != null ) {
        return Array.newInstance(componentClass, 0).getClass();
      }
      else {
        return null;
      }
    }
    else {
      return null;
    }
  }	
	
	
	
  /**
   * This method has been copied from: 
   *   http://www.artima.com/weblogs/viewpostP.jsp?thread=208860
   *   
   * Gets the actual type arguments a child class has used to extend a generic base class.
   *
   * WARNING: This method won't work if childClass is a generic, no matter if the generic variables are bound
   * in the object assignment (see the post above for details). If that is the case, re-declare childClass
   * as abstract and use anonymous classes. 

   * @param baseClass the base class
   * @param childClass the child class
   * @return a list of the raw classes for the actual type arguments.
   * 
   */
  public static <T> List<Class<?>> getTypeArguments ( Class<T> baseClass, Class<? extends T> childClass ) 
  {
    Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
    Type type = childClass;
    // start walking up the inheritance hierarchy until we hit baseClass
    while (! getTypeClass(type).equals(baseClass)) {
      if (type instanceof Class) {
        // there is no useful information for us in raw types, so just keep going.
        type = ((Class<?>) type).getGenericSuperclass();
      }
      else {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
  
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
        for (int i = 0; i < actualTypeArguments.length; i++) {
        	Type actualTypeArgument = actualTypeArguments [ i ];
          resolvedTypes.put(typeParameters[i], actualTypeArgument);
        }
  
        if (!rawType.equals(baseClass)) {
          type = rawType.getGenericSuperclass();
        }
      }
    }
  
    // finally, for each actual type argument provided to baseClass, determine (if possible)
    // the raw class for that type argument.
    Type[] actualTypeArguments;
    if (type instanceof Class) {
      actualTypeArguments = ((Class<?>) type).getTypeParameters();
    }
    else {
      actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
    }
    List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
    // resolve types by chasing down type variables.
    for (Type baseType: actualTypeArguments) {
      while (resolvedTypes.containsKey(baseType)) {
        baseType = resolvedTypes.get(baseType);
      }
      typeArgumentsAsClasses.add(getTypeClass(baseType));
    }
    return typeArgumentsAsClasses;
  } 	


  /**
   * A wrapper to getTypeArgument which retrieves the ith argument 
   * 
   * WARNING: This method won't work if the generic argument i in childClass is a generic, no matter if it is bound
   * in the object assignment (see the post above for details). If that is the case, re-declare childClass
   * as abstract and use anonymous classes. 

   * @param <Argument> the type of i parameter
   * @param <BaseType> the type of base class
   * @param baseClass base class
   * @param childClass your class
   * @param argIndex the argument you want
   * 
   */
  @SuppressWarnings("unchecked")
  public static <ArgumentType, BaseType> Class<ArgumentType> getTypeArgument ( 
		Class<BaseType> baseClass, Class<? extends BaseType> childClass, int argIndex )
  {
  	List<Class<?>> args = getTypeArguments ( baseClass, childClass );
  	if ( args == null || argIndex >= args.size () )
  		throw new IllegalArgumentException ( "Could not get the argument " + argIndex + " for " 
				+ baseClass.getCanonicalName () + " / " + childClass.getCanonicalName () 
  	);
  	return ( Class<ArgumentType> ) args.get ( argIndex );
  }

  
  /**
   * Invokes a method by means of reflection. This is a shortcut for the classes and methods existing in the standard
   * reflection package.
   * 
   * @param makeAccessible if true, uses {@link AccessibleObject#setAccessible(boolean)} to make the method accessible, even
   * when it's protected. As Java documentation says, this will possibly trigger exceptions (wrapped by a {@link RuntimeException}).
   * 
   * @param object the object for which the method is invoked 
   * @param methodClass the class where the method is declared (might be an objec't superclass and you need to identify the
   * declaring class).
   * @param methodName the method name
   * @param paramTypes the types of parameters the method accepts (it's like in {@link Class#getDeclaredMethod(String, Class...)}.
   * @param params the actual parameters for the invocation.
   * 
   * @return whatever the method returns, or null if it's void.
   */
  @SuppressWarnings ( "unchecked" )
	public static <T, RT> RT invoke ( 
  	boolean makeAccessible, T object, Class<? super T> methodClass, String methodName, 
  	Class<?>[] paramTypes, Object... params 
  )
  {
		try
		{
			if ( object == null ) throw new NullPointerException ( 
				"Cannot invoke the method '" + methodName + "' over a null object" 
			);
			
			Method setPropM = methodClass.getDeclaredMethod ( methodName, paramTypes );
			if ( makeAccessible ) setPropM.setAccessible ( true );
			return (RT) setPropM.invoke ( object, params );
		} 
		catch ( 
			NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException 
			| InvocationTargetException ex ) 
		{
			String clsName = Optional.ofNullable ( object )
				.map ( o -> o.getClass ().getSimpleName () )
				.orElse ( "<null>" );
			
			throw new RuntimeException ( String.format ( 
				"Internal Error while invoking '%s'.'%s': %s", clsName, methodName, ex.getMessage () 
			), ex );
		}
  }

  /**
   * Default class is object.getClass(). {@link NullPointerException} if object is null.
   */
	@SuppressWarnings ( "unchecked" )
	public static <RT> RT invoke ( boolean makeAccessible, Object object, String methodName, Class<?>[] paramTypes, Object... params )
	{
		if ( object == null ) throw new NullPointerException ( 
			"Cannot invoke the method '" + methodName + "' over a null object" 
		);
		
		return invoke ( makeAccessible, object, (Class<Object>) object.getClass (), methodName, paramTypes, params );
	}
  
  /**
   * Defaults to makeAccessible = true.
   */
  @SuppressWarnings ( "unchecked" )
	public static <T, RT> RT invoke ( Object object, Class<? super T> methodClass, String methodName, Class<?>[] paramTypes, Object... params )
  {
  	return invoke ( true, object, (Class<Object>) methodClass, methodName, paramTypes, params );
  }

  /**
   * Defaults to methodClass = object.getClass() and makeAccessible = true. {@link NullPointerException} if object is null.
   */
  @SuppressWarnings ( "unchecked" )
	public static <T,RT> RT invoke ( Object object, String methodName, Class<?>[] paramTypes, Object... params )
  {
  	return invoke ( true, object, (Class<Object>) object.getClass (), methodName, paramTypes, params );
  }

}
