/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.sli.SliPluginUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class used to streamline the interface between Java plugins,
 * the Service Logic Context, and Directed Graphs.
 * @version 7.0.1
 * @see org.openecomp.sdnc.sli.SvcLogicContext
 */
public class SliPluginUtils implements SvcLogicJavaPlugin {
	public enum LogLevel {
		TRACE, DEBUG, INFO, WARN, ERROR;
	}

	private static final Logger LOG = LoggerFactory.getLogger(SliPluginUtils.class);


	// ========== CONSTRUCTORS ==========

	public SliPluginUtils() {}

	public SliPluginUtils( Properties props ) {}



	// ========== CONTEXT MEMORY FUNCTIONS ==========

	/**
	 * Removes 1 or more elements from a list in context memory.
	 * <p>
	 * Values are removed based on either the index in the list, a key-value
	 * pair, or a list of key-value pairs that all must match in the element.
	 * @param parameters
	 * @param ctx Reference to context memory
	 * @throws SvcLogicException All exceptions are wrapped in
	 * SvcLogicException for compatibility with SLI.
	 * @since 7.0.1
	 */
	public void ctxListRemove( Map<String,String> parameters, SvcLogicContext ctx ) throws SvcLogicException {
		try{
			LOG.debug( "ENTERING Execute Node \"ctxListRemove\"" );

			// Validate, Log, & read parameters
			checkParameters(parameters, new String[]{"list_pfx"}, LOG);
			logExecuteNodeParameters(parameters, LOG, LogLevel.DEBUG);
			String list_pfx = parameters.get("list_pfx");
			String param_index = parameters.get("index");
			String param_key = parameters.get("key");
			String param_value = parameters.get("value");
			String param_keys_length = parameters.get("keys_length");

			// Initialize context memory list mimic
			SvcLogicContextList list;

			// Process based on input parameters:
			//   index: remove object at specific index
			//   key & value: remove all objects with key-value pair
			//   keys_length: remove all objects that match all key-value pairs
			//                in list
			if( param_index != null ) {
				// Parse index
				LOG.trace("executing remove by index logic");
				int index;
				try {
					index = Integer.parseInt(param_index);
				}
				catch( NumberFormatException e ) {
					throw new IllegalArgumentException("\"index\" parameter is not a number. index = " + param_index, e);
				}

				// Extract list from context memory & remove object @ index
				LOG.trace("extracting list from context memory");
				list = SvcLogicContextList.extract(ctx, list_pfx);
				LOG.trace("removing elements from list");
				list.remove(index);
			}
			else if( param_value != null ) {
				if( param_key == null ) { param_key = ""; }

				// Extract list from context memory & remove objects with
				// key-value pair
				LOG.trace("executing remove by key-value pair logic");
				LOG.trace("extracting list from context memory");
				list = SvcLogicContextList.extract(ctx, list_pfx);
				LOG.trace("removing elements from list");
				list.remove( param_key, param_value );
			}
			else if( param_keys_length != null ) {
				// Parse keys_length
				LOG.trace("executing remove by key-value pair list logic");
				int keys_length;
				try {
					keys_length = Integer.parseInt(param_keys_length);
				}
				catch( NumberFormatException e ) {
					throw new IllegalArgumentException("\"keys_length\" parameters is not a number. keys_length = " + param_keys_length, e);
				}

				// Obtain key-value pairs to check from parameters
				LOG.trace("reading keys parameter list");
				HashMap<String,String> keys_values = new HashMap<String,String>();
				for( int i = 0; i < keys_length; i++ ) {
					keys_values.put(parameters.get("keys[" + i + "].key"), parameters.get("keys[" + i + "].value"));
				}

				// Extract list from context memory & remove objects with all
				// key-value pairs matching
				LOG.trace("extracting list from context memory");
				list = SvcLogicContextList.extract(ctx, list_pfx);
				LOG.trace("removing elements from list");
				list.remove(keys_values);
			}
			else {
				throw new IllegalArgumentException("Required parameters missing. Requires one of: index, key & value, or keys_length array");
			}

			// Remove index from list
			LOG.trace("writing list back into context memory");
			list.writeToContext(ctx);
		}
		catch( Exception e ) {
			throw new SvcLogicException( "An error occurred in the ctxListRemove Execute node", e );
		}
		finally {
			LOG.debug( "EXITING Execute Node \"ctxListRemove\"" );
		}
	}

    /**
     * ctxSortList
     * @param parameters - the set of required parameters must contain list and delimiter.
     * @param ctx Reference to context memory
     * @throws SvcLogicException if a required parameter is missing an exception is thrown
     */
	public void ctxSortList( Map<String, String> parameters, SvcLogicContext ctx ) throws SvcLogicException {
		checkParameters(parameters, new String[]{"list","delimiter"}, LOG);
		ArrayList<SortableCtxListElement> list = new ArrayList<SortableCtxListElement>();

		String[] sort_fields = null;
		if( parameters.containsKey("sort-fields") ) {
			sort_fields = parameters.get("sort-fields").split(parameters.get("delimiter"), 0);
		}

		String ctx_list_str = parameters.get("list");
		int listSz = getArrayLength(ctx, ctx_list_str);



		for( int i = 0; i < listSz; i++ ) {
			list.add( new SortableCtxListElement(ctx, ctx_list_str + '[' + i + ']', sort_fields) );
		}
		Collections.sort(list);

		ctxBulkErase(ctx, ctx_list_str);
		int i = 0;
		for( SortableCtxListElement list_element : list ) {
			for( Map.Entry<String,String> entry : list_element.child_elements.entrySet() ) {
				if( sort_fields == null ) {
					ctx.setAttribute(ctx_list_str + '[' + i + ']', entry.getValue());
				}
				else {
					ctx.setAttribute(ctx_list_str + '[' + i + "]." + entry.getKey(), entry.getValue());
				}
			}
			i++;
		}
		// Reset list length (removed by ctxBulkErase above)
		ctx.setAttribute(ctx_list_str+"_length",  ""+listSz);
	}

    /**
     * generates a UUID and writes it to context memory
     * @param parameters - ctx-destination is a required parameter
     * @param ctx Reference to context memory
     * @throws SvcLogicException thrown if a UUID cannot be generated or if ctx-destination is missing or null
     */
	public void generateUUID( Map<String, String> parameters, SvcLogicContext ctx )  throws SvcLogicException {
		checkParameters(parameters, new String[]{"ctx-destination"}, LOG);
		ctx.setAttribute(parameters.get("ctx-destination"), UUID.randomUUID().toString() );
	}

	/**
	 * Provides substring functionality to Directed Graphs.
	 * <p>
	 * Calls either String.substring(String beginIndex) or
	 * String.substring(String beginInded, String endIndex) if the end-index
	 * is present or not.
	 * @param parameters HashMap<String,String> of parameters passed by the DG to this function
	 * <table border="1">
	 * 	<thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
	 * 	<tbody>
	 * 		<tr><td>string</td><td>Mandatory</td><td>String to perform substring on</td></tr>
	 * 		<tr><td>result</td><td>Mandatory</td><td>Key in context memory to populate the resulting string in</td></tr>
	 * 		<tr><td>begin-index</td><td>Mandatory</td><td>Beginning index to pass to Java substring function</td></tr>
	 * 		<tr><td>end-index</td><td>Optional</td><td>Ending index to pass to Java substring function. If not included, String.substring(begin) will be called.</td></tr>
	 * 	</tbody>
	 * </table>
	 * @param ctx Reference to context memory
	 * @throws SvcLogicException
	 * @since 8.0.1
	 * @see SliPluginUtils#substring(Map, SvcLogicContext)
	 */
	@Deprecated
	public void substring( Map<String, String> parameters, SvcLogicContext ctx ) throws SvcLogicException {
		try {
			checkParameters( parameters, new String[]{"string","begin-index","result"}, LOG );
			final String string = parameters.get("string");
			final String result = parameters.get("result");
			final String begin = parameters.get("begin-index");
			final String end = parameters.get("end-index");

			if( StringUtils.isEmpty(end) ) {
				ctx.setAttribute( result, string.substring(Integer.parseInt(begin)) );
			}
			else {
				ctx.setAttribute( result, string.substring(Integer.parseInt(begin), Integer.parseInt(end)) );
			}
		}
		catch( Exception e ) {
			throw new SvcLogicException( "An error occurred while the Directed Graph was performing a substring", e );
		}
	}



	// ========== PUBLIC STATIC UTILITY FUNCTIONS ==========

	/**
	 * Throws an exception and writes an error to the log file if a required
	 * parameters is not found in the parametersMap.
	 * <p>
	 * Use at the beginning of functions that can be called by Directed Graphs
	 * and can take parameters to verify that all parameters have been provided
	 * by the Directed Graph.
	 * @param parametersMap parameters Map passed to this node
	 * @param requiredParams Array of parameters required by the calling function
	 * @param log Reference to Logger to log to
	 * @throws SvcLogicException if a String in the requiredParams array is
	 * not a key in parametersMap.
	 * @since 1.0
	 */
        public static final void checkParameters(Map<String, String> parametersMap, String[] requiredParams, Logger log) throws SvcLogicException {
        if( requiredParams == null || requiredParams.length < 1){
            log.debug("required parameters was empty, exiting early.");
            return;
        }
        if (parametersMap == null || parametersMap.keySet().size() < 1){
            String errorMessage = "This method requires the parameters [" +   StringUtils.join(requiredParams,",") + "], but no parameters were passed in.";
            log.error(errorMessage);
            throw new SvcLogicException(errorMessage);
        }

    	for (String param : requiredParams) {
    	    if (!parametersMap.containsKey(param)) {
    		String errorMessage = "Required parameter \"" + param + "\" was not found in parameter list.";
    		log.error(errorMessage);
    		log.error("Total list of required parameters is [" + StringUtils.join(requiredParams, ",") + "].");
    		throw new SvcLogicException(errorMessage);
    	    }
    	}
        }

	/**
	 * Removes all key-value pairs with keys that begin with pfx
	 * @param ctx Reference to context memory
	 * @param pfx Prefix of key-value pairs to remove
	 * @since 1.0
	 */
	public static final void ctxBulkErase( SvcLogicContext ctx, String pfx ) {
		ArrayList<String> Keys = new ArrayList<String>( ctx.getAttributeKeySet() );
		for( String key : Keys ) {
			if( key.startsWith( pfx ) ) {
				ctx.setAttribute( pfx + key.substring(pfx.length()) , null);
			}
		}
	}

	/**
	 * Copies all context memory key-value pairs that start with src_pfx to
	 * the keys that start with dest_pfx + suffix, where suffix is the result
	 * of {@code key.substring(src_pfx.length())}.
	 * <p>
	 * Does NOT guarantee removal of all keys at the destination before
	 * copying, but will overwrite any destination keys that have a
	 * corresponding source key. Use {@link #ctxBulkErase(SvcLogicContext, String) ctxBulkErase}
	 * before copy to erase destination root before copying from source.
	 * @param ctx Reference to context memory.
	 * @param src_pfx Prefix of the keys to copy values from.
	 * @param dest_pfx Prefix of the keys to copy values to.
	 * @since 1.0
	 */
	public static final void ctxBulkCopy( SvcLogicContext ctx, String src_pfx, String dest_pfx ) {
		// Remove trailing period from dest_pfx
		if( dest_pfx.charAt(dest_pfx.length()-1) == '.' ) {
			dest_pfx = dest_pfx.substring(0,dest_pfx.length()-1);
		}

		// For each context key that begins with src_pfx, set the value of the
		// key dest_pfx + the suffix of the key to the key's value
		ArrayList<String> Keys = new ArrayList<String>(ctx.getAttributeKeySet());
		for( String key : Keys ) {
			if( key.startsWith(src_pfx) ) {
				// Get suffix (no leading period)
				String suffix = key.substring(src_pfx.length());
				if( suffix.charAt(0) == '.') {
					suffix = suffix.substring(1);
				}

				// Set destination's value to key's value
				ctx.setAttribute(dest_pfx + '.' + suffix, ctx.getAttribute(key));
			}
		}
	}

	/**
	 * Creates and returns a {@code Map<String, String>} that is a subset of
	 * context memory where all keys begin with the prefix.
	 * @param ctx Reference to context memory.
	 * @param prefix Returned map's keys should all begin with this value.
	 * @return A {@code Map<String, String>} containing all the key-value pairs
	 * in ctx whose key begins with prefix.
	 */
	public static final Map<String, String> ctxGetBeginsWith( SvcLogicContext ctx, String prefix ) {
		Map<String, String> prefixMap = new HashMap<String, String>();

		for( String key : ctx.getAttributeKeySet() ) {
			if( key.startsWith(prefix) ) {
				prefixMap.put( key, ctx.getAttribute(key) );
			}
		}

		return prefixMap;
	}

	/**
	 * Returns true if key's value in context memory is "" or if it doesn't
	 * exist in context memory.
	 * @param ctx Reference to context memory.
	 * @param key Key to search for.
	 * @return true if key's value in context memory is "" or if it doesn't
	 * exist in context memory.
	 * @since 1.0
	 */
	public static final boolean ctxKeyEmpty( SvcLogicContext ctx, String key ) {
		String value = ctx.getAttribute(key);
		return value == null || value.isEmpty();
	}

	/**
	 * Adds all key-value pairs in the entries Map to context memory.
	 * @param ctx Reference to context memory. Value's {@code toString()}
	 * function is used to add it.
	 * @param entries {@code Map<String, ?>} of key-value pairs to add to
	 * context memory. Value's {@code toString()} function is used to add it.
	 * @return Reference to context memory to be used for function chaining.
	 */
	public static final SvcLogicContext ctxPutAll( SvcLogicContext ctx, Map<String, ?> entries ) {
		for( Map.Entry<String, ?> entry : entries.entrySet() ) {
			ctxSetAttribute( ctx, entry.getKey(), entry.getValue() );
			//ctx.setAttribute(entry.getKey(), entry.getValue().toString());
		}

		return ctx;
	}

	/**
	 * Sets a key in context memory to the output of object's toString(). The
	 * key is deleted from context memory if object is null.
	 * @param ctx Reference to context memory.
	 * @param key Key to set.
	 * @param object Object whose toString() will be the value set
	 */
	public static final void ctxSetAttribute( SvcLogicContext ctx, String key, Object object ) {
		if( object == null ) {
			ctx.setAttribute(key, null);
		}
		else {
			ctx.setAttribute(key, object.toString());
		}
	}

	/**
	 * Sets a key in context memory to the output of object's toString().
	 * <p>
	 * The key is deleted from context memory if object is null. The key and
	 * value set in context memory are logged to the Logger at the provided
	 * logLevel level.
	 * @param <O> Any Java object
	 * @param ctx Reference to context memory.
	 * @param key Key to set.
	 * @param obj Object whose toString() will be the value set
	 * @param LOG Logger to log to
	 * @param logLevel level to log at in Logger
	 */
	public static final <O extends Object> void ctxSetAttribute( SvcLogicContext ctx, String key, O obj, Logger LOG, LogLevel logLevel ) {
		String value = Objects.toString( obj, null );
		ctx.setAttribute( key, value );
		if( logLevelIsEnabled(LOG, logLevel ) ) {
			if( value == null ) {
				logMessageAtLevel( LOG, logLevel, "Deleting " + key );
			}
			else {
				logMessageAtLevel( LOG, logLevel, "Setting " + key + " = " + value );
			}
		}
	}

	/**
	 * Utility function used to get an array's length from context memory.
	 * Will return 0 if key doesn't exist in context memory or isn't numeric.
	 * <p>
	 * Use to obtain a context memory array length without having to worry
	 * about throwing a NumberFormatException.
	 * @param ctx Reference to context memory
	 * @param key Key in context memory whose value is the array's length. If
	 * the key doesn't end in "_length", then "_length is appended.
	 * @param log Reference to Logger to log to
	 * @return The array length or 0 if the key is not found in context memory.
	 * @since 1.0
	 */
	public static final int getArrayLength( SvcLogicContext ctx, String key ) {
		return getArrayLength(ctx, key, null, null, null);
	}

	/**
	 * Utility function used to get an array's length from context memory.
	 * Will return 0 if key doesn't exist in context memory or isn't numeric
	 * and print the provided log message to the configured log file.
	 * <p>
	 * Use to obtain a context memory array length without having to worry
	 * about throwing a NumberFormatException.
	 * @param ctx Reference to context memory.
	 * @param key Key in context memory whose value is the array's length. If
	 * the key doesn't end in "_length", then "_length is appended.
	 * @param log Reference to Logger to log to. Doesn't log if null.
	 * @param logLevel Logging level to log the message at if the context
	 * memory key isn't found. Doesn't log if null.
	 * @param log_message Message to log if the context memory key isn't found.
	 * Doesn't log if null.
	 * @return The array length or 0 if the key is not found in context memory.
	 * @since 1.0
	 */
	public static final int getArrayLength( SvcLogicContext ctx, String key, Logger log, LogLevel logLevel, String log_message ) {
		String ctxKey = ( key.endsWith("_length") ) ? key : key + "_length";
		try {
			return Integer.parseInt(ctx.getAttribute(ctxKey));
		}
		catch( NumberFormatException e ) {
			if( log != null && logLevel != null && log_message != null ) {
				switch( logLevel ) {
					case TRACE:
						log.trace(log_message);
					case DEBUG:
						log.debug(log_message);
						break;
					case INFO:
						log.info(log_message);
						break;
					case WARN:
						log.warn(log_message);
						break;
					case ERROR:
						log.error(log_message);
						break;
				}
			}
		}

		return 0;
	}

	/**
	 * Prints sorted context memory key-value pairs to the log file at the log
	 * level. Returns immediately if the log level isn't enabled.
	 * <p>
	 * O(n log(n)) time where n = size of context memory
	 * @param ctx Reference to context memory
	 * @param log Reference to Logger to log to
	 * @param logLevel Logging level to log the context memory key-value pairs
	 * at.
	 * @since 1.0
	 */
	public static final void logContextMemory( SvcLogicContext ctx, Logger log, LogLevel logLevel ) {
		logLevelIsEnabled( log, logLevel );

		// Print sorted context memory key-value pairs to the log
		ArrayList<String> keys = new ArrayList<String>(ctx.getAttributeKeySet());
		Collections.sort(keys);
		for( String key : keys ) {
			logMessageAtLevel( log, logLevel, key + " = " + ctx.getAttribute(key) );
		}
	}



	// ========== PRIVATE FUNCTIONS ==========

	// TODO: javadoc
	/**
	 *
	 * @param parameters
	 * @param log
	 * @param loglevel
	 * @since 7.0.1
	 */
	public static final void logExecuteNodeParameters( Map<String,String> parameters, Logger log, LogLevel loglevel ) {
		logLevelIsEnabled( log, loglevel );

		for( Map.Entry<String,String> param : parameters.entrySet() ) {
			logMessageAtLevel( log, loglevel, "PARAM: " + param.getKey() + " = " + param.getValue() );
		}
	}

	// TODO: javadoc
	/**
	 * Returns true if the loglevel is enabled. Otherwise, returns false.
	 * @param log Reference to logger
	 * @param loglevel Log level to check if enabled
	 * @return True if the loglevel is enabled. Otherwise, false
	 * @since 7.0.1
	 */
	private static final boolean logLevelIsEnabled( Logger log, LogLevel loglevel ) {
		// Return immediately if logging level isn't enabled
		switch( loglevel ) {
			case TRACE:
				if( log.isTraceEnabled() ) { return true; }
				return false;
			case DEBUG:
				if( log.isDebugEnabled() ) { return true; }
				return false;
			case INFO:
				if( log.isInfoEnabled() ) { return true; }
				return false;
			case WARN:
				if( log.isWarnEnabled() ) { return true; }
				return false;
			case ERROR:
				if( log.isErrorEnabled() ) { return true; }
				return false;
			default:
				throw new IllegalArgumentException("Unknown LogLevel: " + loglevel.toString());
		}
	}

	// TODO: javadoc
	/**
	 *
	 * @param log
	 * @param loglevel
	 * @param msg
	 * @since 7.0.1
	 */
	private static final void logMessageAtLevel( Logger log, LogLevel loglevel, String msg ) {
		switch( loglevel ) {
			case TRACE:
				log.trace(msg);
				return;
			case DEBUG:
				log.debug(msg);
				return;
			case INFO:
				log.info(msg);
				return;
			case WARN:
				log.warn(msg);
				return;
			case ERROR:
				log.error(msg);
				return;
		}
	}



	// ========== LOCAL CLASSES ==========

	private class SortableCtxListElement implements Comparable<SortableCtxListElement> {
		HashMap<String,String> child_elements = new HashMap<String,String>();
		String[] sort_fields;

		public SortableCtxListElement( SvcLogicContext ctx, String root, String[] sort_fields ) {
			this.sort_fields = sort_fields;

			for( String key : ctx.getAttributeKeySet() ) {
				if( key.startsWith(root) ) {
					if( key.length() == root.length() ) {
						child_elements.put("", ctx.getAttribute(key));
						break;
					}
					else {
						child_elements.put(key.substring(root.length()+1), ctx.getAttribute(key));
					}
				}
			}
		}

		@Override
		public int compareTo(SortableCtxListElement arg0) {
			if( sort_fields == null ) {
				return this.child_elements.get("").compareTo(arg0.child_elements.get(""));
			}

			for( String field : this.sort_fields ) {
				int result = this.child_elements.get(field).compareTo(arg0.child_elements.get(field));
				if( result != 0 ) {
					return result;
				}
			}

			return 0;
		}
	}

	/**
     * Creates a file that contains the content of context memory.
     * @param parameters - must contain the parameter filename
     * @param ctx Reference to context memory
     * @throws SvcLogicException thrown if file cannot be created or if parameters are missing
     */
	public static void printContext(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
		if (parameters == null || parameters.isEmpty()) {
			throw new SvcLogicException("no parameters passed");
		}

		checkParameters(parameters, new String[]{"filename"}, LOG);

		String fileName = parameters.get("filename");


		try (FileOutputStream fstr = new FileOutputStream(new File(fileName));
			 PrintStream pstr = new PrintStream(fstr, true);)
		{
			pstr.println("#######################################");
			for (String attr : ctx.getAttributeKeySet()) {
				pstr.println(attr + " = " + ctx.getAttribute(attr));
			}
		} catch (Exception e) {
			throw new SvcLogicException("Cannot write context to file " + fileName, e);
		}


	}

	 /**
     * Checks context memory for a set of required parameters
     * Every parameter aside from prefix will be treated as mandatory
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>prefix</td><td>Optional</td><td>the prefix will be added to each parameter</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
	public static void requiredParameters(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
		if (parameters == null || parameters.keySet().size() < 1) {
            String errorMessage = "requiredParameters should not be called if the parameters hashmap is null or empty!";
            LOG.error(errorMessage);
            throw new SvcLogicException(errorMessage);
		}
		String prefixValue = null;
		String prefix = "prefix";
		if(parameters.containsKey(prefix)){
		    prefixValue = parameters.get(prefix);
		    parameters.remove(prefix);
		}
		checkParameters(prefixValue, ctx.getAttributeKeySet(), parameters.keySet(), LOG);
	}

    private static void checkParameters(String prefixValue, Set<String> ctx, Set<String> parameters, Logger log) throws SvcLogicException {
        for (String param : parameters) {
            if (prefixValue != null) {
                param = prefixValue + param;
            }
            if (!ctx.contains(param)) {
                String errorMessage = "This method requires the parameters [" + StringUtils.join(parameters, ",")
                        + "], but " + param + " was not passed in.";
                log.error(errorMessage);
                throw new SvcLogicException(errorMessage);
            }
        }
    }

    /**
     *  is in a different DG invocation just before/after we call NCS and set the state to InProgress
     */
    /**
    * setTime write the current date time to a string located at outputPath
    * @param parameters - requires outputPath to not be null
    * @param ctx Reference to context memory
    * @throws SvcLogicException if a required parameter is missing an exception is thrown
    */
    public static void setTime(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException
    {
        checkParameters(parameters, new String[] { "outputPath" }, LOG);

        // Set the DateFormat
        // "2015-03-16T12:18:35.138Z"
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        // Parse the date
        String ctxVariable = parameters.get("outputPath");
        try {
            String dateTime = format.format(new Date());
            ctx.setAttribute(ctxVariable, dateTime);
        } catch (Exception ex) {
            throw new SvcLogicException("problem with setTime", ex);
        }
    }
}
