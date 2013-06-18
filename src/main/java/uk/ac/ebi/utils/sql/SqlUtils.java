package uk.ac.ebi.utils.sql;

/**
 * Utilities for SQL definition and manipulation.
 *
 * <dl><dt>date</dt><dd>Jun 17, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class SqlUtils
{
	/**
	 * Builds a string like ":paramName IS NULL AND fieldName IS NULL OR fieldName = :paramName". This is useful for 
	 * building parameterised queries where you want to match a particular value, including the null value. 
	 */
	public static String parameterizedWithNullSql ( String fieldName, String paramName ) {
		return String.format ( "(:%2$s IS NULL AND %1$s IS NULL OR %1$s = :%2$s)", fieldName, paramName );
	}
}
