package uk.ac.ebi.utils.sql;

import static java.lang.String.format;

import javax.persistence.Query;

/**
 * Utilities for SQL definition and manipulation.
 *
 * <dl><dt>date</dt><dd>Jun 17, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class HqlUtils
{
	/**
	 * Builds a string like ":paramName IS NULL AND fieldName IS NULL OR fieldName = :paramName". This is useful for 
	 * building parameterised queries where you want to match a particular value, including the null value. 
	 */
	public static String parameterizedWithNullHql ( String fieldName, String paramName ) {
		return format ( "(:%2$s IS NULL AND %1$s IS NULL OR %1$s = :%2$s)", fieldName, paramName );
	}

	/**
	 * Builds a query clause that restricts fieldName in the specified range. Uses {@code '<=' }, {@code '<='} or 
	 * {@code BETWEEN}, depending on the lo, hi value. The returned HQL string contains only paramLo, paramHi, not 
	 * the lo, hi values themselves. You should use 
	 * {@link #parameterizedRangeBinding(Query, String, String, Object, Object)} with a {@link Query} built this way.
	 * 
	 */
	public static <T> String parameterizedRangeClause ( String fieldName, String paramLo, String paramHi, T lo, T hi ) 
	{
		return lo == null 
			? hi == null ? "" : format ( "%s <= :%s", fieldName, paramHi )
			: hi == null 
				? format ( "%s >= :%s", fieldName, paramLo ) 
				: format ( "%s BETWEEN ( :%s AND :%s )", fieldName, paramLo, paramHi );
	}

	/**
	 * Binds a clause built with {@link #parameterizedRangeClause(String, String, String, Object, Object)} with 
	 * the values lo, hi.  
	 */
	public static <T> Query parameterizedRangeBinding ( Query q, String paramLo, String paramHi, T lo, T hi ) 
	{
		if ( lo != null ) q.setParameter ( paramLo, lo );
		if ( hi != null ) q.setParameter ( paramHi, hi );
		return q;
	}

}
