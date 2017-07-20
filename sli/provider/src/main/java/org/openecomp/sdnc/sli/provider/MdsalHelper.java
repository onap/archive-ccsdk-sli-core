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

package org.openecomp.sdnc.sli.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MdsalHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MdsalHelper.class);
    private static Properties yangMappingProperties = new Properties();

    @Deprecated
    public static void setProperties(Properties input) {
        setYangMappingProperties(input);
    }
    
    public static void setYangMappingProperties(Properties properties) {
        for (Object propNameObj : properties.keySet()) {
            String propName = (String) propNameObj;
            MdsalHelper.yangMappingProperties.setProperty(propName, properties.getProperty(propName));
        }
    }

    public static void loadProperties(String propertiesFile) {
	File file = new File(propertiesFile);
	Properties properties = new Properties();
	InputStream input = null;
	if (file.isFile() && file.canRead()) {
	    try {
		input = new FileInputStream(file);
		properties.load(input);
		MdsalHelper.setYangMappingProperties(properties);
		LOG.info("Loaded properties from " + propertiesFile);
	    } catch (Exception e) {
		LOG.error("Failed to load properties " + propertiesFile + "\n", e);
	    } finally {
		if (input != null) {
		    try {
			input.close();
		    } catch (IOException e) {
			LOG.error("Failed to close properties file " + propertiesFile + "\n", e);
		    }
		}
	    }
	}else{
	    LOG.error("Failed to load the properties file " + propertiesFile + "\n");
	    LOG.error("Either isFile or canRead returned false for " + propertiesFile + "\n");
	}
    }

    public static Properties toProperties(Properties props, Object fromObj) {
	Class fromClass = null;

	if (fromObj != null) {
	    fromClass = fromObj.getClass();
	}
	return (toProperties(props, "", fromObj, fromClass));
    }

    public static Properties toProperties(Properties props, String pfx, Object fromObj) {
	Class fromClass = null;

	if (fromObj != null) {
	    fromClass = fromObj.getClass();
	}

	return (toProperties(props, pfx, fromObj, fromClass));
    }

    public static Properties toProperties(Properties props, String pfx, Object fromObj, Class fromClass) {

	if (fromObj == null) {
	    return (props);
	}

	String simpleName = fromClass.getSimpleName();

	LOG.trace("Extracting properties from " + fromClass.getName() + " class");
	if (fromObj instanceof List) {

	    // Class is a List. List should contain yang-generated classes.
	    LOG.trace(fromClass.getName() + " is a List");

	    List fromList = (List) fromObj;

	    for (int i = 0; i < fromList.size(); i++) {
		toProperties(props, pfx + "[" + i + "]", fromList.get(i), fromClass);
	    }
	    props.setProperty(pfx + "_length", "" + fromList.size());

	} else if (isYangGenerated(fromClass)) {
	    // Class is yang generated.
	    LOG.trace(fromClass.getName() + " is a Yang-generated class");

	    String propNamePfx = null;

	    // If called from a list (so prefix ends in ']'), don't
	    // add class name again
	    if (pfx.endsWith("]")) {
		propNamePfx = pfx;
	    } else {
		if ((pfx != null) && (pfx.length() > 0)) {
		    propNamePfx = pfx;
		} else {
		    propNamePfx = toLowerHyphen(fromClass.getSimpleName());
		}

		if (propNamePfx.endsWith("-builder")) {
		    propNamePfx = propNamePfx.substring(0, propNamePfx.length() - "-builder".length());
		}

		if (propNamePfx.endsWith("-impl")) {
		    propNamePfx = propNamePfx.substring(0, propNamePfx.length() - "-impl".length());
		}
	    }

	    // Iterate through getter methods to figure out values we need to
	    // save from

	    int numGetters = 0;
	    String lastGetterName = null;
	    String propVal = null;

	    for (Method m : fromClass.getMethods()) {
		if (isGetter(m)) {

		    numGetters++;
		    lastGetterName = m.getName();

		    Class returnType = m.getReturnType();
		    String fieldName;
		    if (m.getName().startsWith("get")) {
			fieldName = toLowerHyphen(m.getName().substring(3));
		    } else {

			fieldName = toLowerHyphen(m.getName().substring(2));
		    }

		    fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);

		    // Is the return type a yang generated class?
		    if (isYangGenerated(returnType)) {
			// Is it an enum?
			if (returnType.isEnum()) {
			    // Return type is a typedef. Save its value.
			    try {
				boolean isAccessible = m.isAccessible();
				if (!isAccessible) {
				    m.setAccessible(true);
				}

				Object retValue = m.invoke(fromObj);

				if (!isAccessible) {
				    m.setAccessible(isAccessible);
				}
				if (retValue != null) {
				    String propName = propNamePfx + "." + fieldName;
				    propVal = retValue.toString();
				    props.setProperty(propName, mapEnumeratedValue(fieldName, propVal));
				}
			    } catch (Exception e) {
				LOG.error("Caught exception trying to convert Yang-generated enum returned by " + fromClass.getName() + "." + m.getName() + "() to Properties entry", e);
			    }
			} else if (isIpv4Address(returnType)) {
			    // Save its value
			    try {
				String propName = propNamePfx + "." + fieldName;
				boolean isAccessible = m.isAccessible();
				if (!isAccessible) {
				    m.setAccessible(true);
				}
				Ipv4Address retValue = (Ipv4Address) m.invoke(fromObj);
				if (!isAccessible) {
				    m.setAccessible(isAccessible);
				}

				if (retValue != null) {
				    propVal = retValue.getValue().toString();
				    LOG.debug("Setting property " + propName + " to " + propVal);
				    props.setProperty(propName, propVal);

				}
			    } catch (Exception e) {
				LOG.error("Caught exception trying to convert value returned by " + fromClass.getName() + "." + m.getName() + "() to Properties entry", e);
			    }
			} else if (isIpv6Address(returnType)) {
			    // Save its value
			    try {
				String propName = propNamePfx + "." + fieldName;
				boolean isAccessible = m.isAccessible();
				if (!isAccessible) {
				    m.setAccessible(true);
				}
				Ipv6Address retValue = (Ipv6Address) m.invoke(fromObj);
				if (!isAccessible) {
				    m.setAccessible(isAccessible);
				}

				if (retValue != null) {
				    propVal = retValue.getValue().toString();
				    LOG.debug("Setting property " + propName + " to " + propVal);
				    props.setProperty(propName, propVal);

				}
			    } catch (Exception e) {
				LOG.error("Caught exception trying to convert value returned by " + fromClass.getName() + "." + m.getName() + "() to Properties entry", e);
			    }
			} else if (isIpAddress(returnType)) {
			    // Save its value
			    try {
				String propName = propNamePfx + "." + fieldName;
				boolean isAccessible = m.isAccessible();
				if (!isAccessible) {
				    m.setAccessible(true);
				}
				IpAddress retValue = (IpAddress) m.invoke(fromObj);
				if (!isAccessible) {
				    m.setAccessible(isAccessible);
				}

				if (retValue != null) {
				    propVal = new String(retValue.getValue());
				    LOG.debug("Setting property " + propName + " to " + propVal);
				    props.setProperty(propName, propVal);

				}
			    } catch (Exception e) {
				LOG.error("Caught exception trying to convert value returned by " + fromClass.getName() + "." + m.getName() + "() to Properties entry", e);
			    }
			} else if (isIpPrefix(returnType)) {
			    // Save its value
			    try {
				String propName = propNamePfx + "." + fieldName;
				boolean isAccessible = m.isAccessible();
				if (!isAccessible) {
				    m.setAccessible(true);
				}
				IpPrefix retValue = (IpPrefix) m.invoke(fromObj);
				if (!isAccessible) {
				    m.setAccessible(isAccessible);
				}

				if (retValue != null) {
				    propVal = new String(retValue.getValue());
				    LOG.debug("Setting property " + propName + " to " + propVal);
				    props.setProperty(propName, propVal);

				}
			    } catch (Exception e) {
				LOG.error("Caught exception trying to convert value returned by " + fromClass.getName() + "." + m.getName() + "() to Properties entry", e);
			    }
			} else {
			    try {
				boolean isAccessible = m.isAccessible();
				if (!isAccessible) {
				    m.setAccessible(true);
				}
				Object retValue = m.invoke(fromObj);

				if (retValue instanceof byte[]) {
				    LOG.trace(m.getName() + " returns a byte[]");
				    retValue = new String((byte[]) retValue, "UTF-8");
				    LOG.trace("Converted byte array " + propNamePfx + "." + fieldName + "to string " + retValue);
				}
				if (!isAccessible) {
				    m.setAccessible(isAccessible);
				}
				if (retValue != null) {
				    toProperties(props, propNamePfx + "." + fieldName, retValue, returnType);
				}
			    } catch (Exception e) {

				if (m.getName().equals("getKey")) {
				    LOG.trace("Caught " + e.getClass().getName() + " exception trying to convert results from getKey() - ignoring");
				} else {
				    LOG.error("Caught exception trying to convert Yang-generated class returned by" + fromClass.getName() + "." + m.getName() + "() to Properties entry", e);
				}
			    }
			}
		    } else if (returnType.equals(Class.class)) {

			LOG.trace(m.getName() + " returns a Class object - not interested");

		    } else if (List.class.isAssignableFrom(returnType)) {

			// This getter method returns a list.
			try {
			    boolean isAccessible = m.isAccessible();
			    if (!isAccessible) {
				m.setAccessible(true);
			    }
			    Object retList = m.invoke(fromObj);
			    if (!isAccessible) {
				m.setAccessible(isAccessible);
			    }
			    // Figure out what type of elements are stored in
			    // this array.
			    Type paramType = m.getGenericReturnType();
			    Type elementType = ((ParameterizedType) paramType).getActualTypeArguments()[0];
			    toProperties(props, propNamePfx + "." + fieldName, retList, (Class) elementType);
			} catch (Exception e) {
			    LOG.error("Caught exception trying to convert List returned by " + fromClass.getName() + "." + m.getName() + "() to Properties entry", e);
			}

		    } else {

			// Method returns something that is not a List and not
			// yang-generated.
			// Save its value
			try {
			    String propName = propNamePfx + "." + fieldName;
			    boolean isAccessible = m.isAccessible();
			    if (!isAccessible) {
				m.setAccessible(true);
			    }
			    Object propValObj = m.invoke(fromObj);
			    if (!isAccessible) {
				m.setAccessible(isAccessible);
			    }

			    if (propValObj != null) {
				if (propValObj instanceof byte[]) {
				    LOG.trace(m.getName() + " returns a byte[]");
				    propVal = new String((byte[]) propValObj, "UTF-8");
				    LOG.trace("Converted byte array " + propNamePfx + "." + fieldName + "to string " + propVal);

				} else {
				    propVal = propValObj.toString();
				}
				LOG.debug("Setting property " + propName + " to " + propVal);
				props.setProperty(propName, propVal);

			    }
			} catch (Exception e) {
			    if (m.getName().equals("getKey")) {
				LOG.trace("Caught " + e.getClass().getName() + " exception trying to convert results from getKey() - ignoring");
			    } else {
				LOG.error("Caught exception trying to convert value returned by" + fromClass.getName() + "." + m.getName() + "() to Properties entry", e);
			    }
			}
		    }

		}
	    }

	    // End of method loop. If there was only one getter, named
	    // "getValue", then
	    // set value identified by "prefix" to that one value.
	    if ((numGetters == 1) && ("getValue".equals(lastGetterName))) {
		LOG.trace("getValueFIX : " + propNamePfx + " only has getValue() getter - setting " + propNamePfx + " = " + propVal);
		props.setProperty(propNamePfx, propVal);
	    } else {
		LOG.trace("getValueFIX : " + propNamePfx + " has " + numGetters + " getter(s), last one found was " + lastGetterName);

	    }

	} else {
	    // Class is not yang generated and not a list
	    // It must be an element of a leaf list - set "prefix" to value
	    String fromVal = null;
	    if (fromObj instanceof byte[]) {
		try {
		    fromVal = new String((byte[]) fromObj, "UTF-8");
		    LOG.trace("Converted byte array " + pfx + "to string " + fromVal);
		} catch (Exception e) {
		    LOG.warn("Caught exception trying to convert " + pfx + " from byte[] to String", e);
		    fromVal = fromObj.toString();
		}

	    } else {
		fromVal = fromObj.toString();
	    }
	    LOG.debug("Setting property " + pfx + " to " + fromVal);
	    props.setProperty(pfx, fromVal);
	}

	return (props);
    }

    public static Object toBuilder(Properties props, Object toObj) {

	return (toBuilder(props, "", toObj));
    }

    public static List toList(Properties props, String pfx, List toObj, Class elemType) {

	int maxIdx = -1;
	boolean foundValue = false;

	LOG.trace("Saving properties to List<" + elemType.getName() + ">  from " + pfx);

	if (props.contains(pfx + "_length")) {
	    try {
		int listLength = Integer.parseInt(props.getProperty(pfx + "_length"));

		if (listLength > 0) {
		    maxIdx = listLength - 1;
		}
	    } catch (Exception e) {
		// Ignore exception
	    }
	}

	if (maxIdx == -1) {
	    // Figure out array size
	    for (Object pNameObj : props.keySet()) {
		String key = (String) pNameObj;

		if (key.startsWith(pfx + "[")) {
		    String idxStr = key.substring(pfx.length() + 1);
		    int endloc = idxStr.indexOf("]");
		    if (endloc != -1) {
			idxStr = idxStr.substring(0, endloc);
		    }

		    try {
			int curIdx = Integer.parseInt(idxStr);
			if (curIdx > maxIdx) {
			    maxIdx = curIdx;
			}
		    } catch (Exception e) {
			LOG.error("Illegal subscript in property " + key);
		    }

		}
	    }
	}

	LOG.trace(pfx + " has max index of " + maxIdx);
	for (int i = 0; i <= maxIdx; i++) {

	    String curBase = pfx + "[" + i + "]";

	    if (isYangGenerated(elemType)) {
		String builderName = elemType.getName() + "Builder";
		try {
		    Class builderClass = Class.forName(builderName);
		    Object builderObj = builderClass.newInstance();
		    Method buildMethod = builderClass.getMethod("build");
		    builderObj = toBuilder(props, curBase, builderObj, true);
		    if (builderObj != null) {
			LOG.trace("Calling " + builderObj.getClass().getName() + "." + buildMethod.getName() + "()");
			Object builtObj = buildMethod.invoke(builderObj);
			toObj.add(builtObj);
			foundValue = true;
		    }

		} catch (ClassNotFoundException e) {
		    LOG.warn("Could not find builder class " + builderName, e);
		} catch (Exception e) {
		    LOG.error("Caught exception trying to populate list from " + pfx);
		}
	    } else {
		// Must be a leaf list
		String curValue = props.getProperty(curBase, "");

		toObj.add(curValue);

		if ((curValue != null) && (curValue.length() > 0)) {
		    foundValue = true;
		}
	    }

	}

	if (foundValue) {
	    return (toObj);
	} else {
	    return (null);
	}

    }

    public static Object toBuilder(Properties props, String pfx, Object toObj) {
	return (toBuilder(props, pfx, toObj, false));
    }

    public static Object toBuilder(Properties props, String pfx, Object toObj, boolean preservePfx) {
	Class toClass = toObj.getClass();
	boolean foundValue = false;

	LOG.trace("Saving properties to " + toClass.getName() + " class from " + pfx);

	Ipv4Address addr;

	if (isYangGenerated(toClass)) {
	    // Class is yang generated.
	    LOG.trace(toClass.getName() + " is a Yang-generated class");

	    String propNamePfx = null;
	    if (preservePfx) {
		propNamePfx = pfx;
	    } else {

		if ((pfx != null) && (pfx.length() > 0)) {
		    propNamePfx = pfx + "." + toLowerHyphen(toClass.getSimpleName());
		} else {
		    propNamePfx = toLowerHyphen(toClass.getSimpleName());
		}

		if (propNamePfx.endsWith("-builder")) {
		    propNamePfx = propNamePfx.substring(0, propNamePfx.length() - "-builder".length());
		}

		if (propNamePfx.endsWith("-impl")) {
		    propNamePfx = propNamePfx.substring(0, propNamePfx.length() - "-impl".length());
		}
	    }

	    if (toObj instanceof Identifier) {
		LOG.trace(toClass.getName() + " is a Key - skipping");
		return (toObj);
	    }

	    // Iterate through getter methods to figure out values we need to
	    // set

	    for (Method m : toClass.getMethods()) {
		if (isSetter(m)) {
		    Class paramTypes[] = m.getParameterTypes();
		    Class paramClass = paramTypes[0];

		    String fieldName = toLowerHyphen(m.getName().substring(3));
		    fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);

		    String propName = propNamePfx + "." + fieldName;

		    String paramValue = props.getProperty(propName);
		    if (paramValue == null) {
			LOG.trace(propName + " is unset");
		    } else {
			LOG.trace(propName + " = " + paramValue);
		    }

		    // Is the return type a yang generated class?
		    if (isYangGenerated(paramClass)) {
			// Is it an enum?
			if (paramClass.isEnum()) {

			    LOG.trace(m.getName() + " expects an Enum");
			    // Param type is a typedef.
			    if ((paramValue != null) && (paramValue.length() > 0)) {
				Object paramObj = null;

				try {
				    paramObj = Enum.valueOf(paramClass, toJavaEnum(paramValue));
				} catch (Exception e) {
				    LOG.error("Caught exception trying to convert field " + propName + " to enum " + paramClass.getName(), e);
				}

				try {
				    boolean isAccessible = m.isAccessible();
				    if (!isAccessible) {
					m.setAccessible(true);
				    }

				    LOG.trace("Calling " + toObj.getClass().getName() + "." + m.getName() + "(" + paramValue + ")");
				    m.invoke(toObj, paramObj);

				    if (!isAccessible) {
					m.setAccessible(isAccessible);
				    }
				    foundValue = true;

				} catch (Exception e) {
				    LOG.error("Caught exception trying to create Yang-generated enum expected by" + toClass.getName() + "." + m.getName() + "() from Properties entry", e);
				}
			    }
			} else {

			    String simpleName = paramClass.getSimpleName();

			    if ("Ipv4Address".equals(simpleName) || "Ipv6Address".equals(simpleName) || "IpAddress".equals(simpleName)) {

				if ((paramValue != null) && (paramValue.length() > 0)) {
				    try {
					IpAddress ipAddr = IpAddressBuilder.getDefaultInstance(paramValue);

					if ("Ipv4Address".equals(simpleName)) {
					    m.invoke(toObj, ipAddr.getIpv4Address());
					} else if ("Ipv6Address".equals(simpleName)) {
					    m.invoke(toObj, ipAddr.getIpv6Address());

					} else {
					    m.invoke(toObj, ipAddr);
					}
					foundValue = true;
				    } catch (Exception e) {
					LOG.error("Caught exception calling " + toClass.getName() + "." + m.getName() + "(" + paramValue + ")", e);

				    }
				} else {
				    try {
					boolean isAccessible = m.isAccessible();
					if (!isAccessible) {
					    m.setAccessible(true);
					}
					LOG.trace("Calling " + toObj.getClass().getName() + "." + m.getName() + "(" + paramValue + ")");
					m.invoke(toObj, paramValue);
					if (!isAccessible) {
					    m.setAccessible(isAccessible);
					}
					foundValue = true;

				    } catch (Exception e) {
					LOG.error("Caught exception trying to call " + toClass.getName() + "." + m.getName() + "() with Properties entry", e);
				    }
				}
			    } else if ("IpPrefix".equals(simpleName)) {
				if ((paramValue != null) && (paramValue.length() > 0)) {
				    try {
					IpPrefix ipPrefix = IpPrefixBuilder.getDefaultInstance(paramValue);
					m.invoke(toObj, ipPrefix);
					foundValue = true;
				    } catch (Exception e) {
					LOG.error("Caught exception calling " + toClass.getName() + "." + m.getName() + "(" + paramValue + ")", e);
				    }
				}
			    } else {
				// setter expects a yang-generated class. Need
				// to
				// create a builder to set it.

				String builderName = paramClass.getName() + "Builder";
				Class builderClass = null;
				Object builderObj = null;
				Object paramObj = null;

				Object constObj = null;

				LOG.trace(m.getName() + " expects a yang-generated class - looking for builder " + builderName);
				try {
				    builderClass = Class.forName(builderName);
				    builderObj = builderClass.newInstance();
				    paramObj = toBuilder(props, propNamePfx, builderObj);
				} catch (ClassNotFoundException e) {

				    if (paramValue == null) {
					try {
					    boolean isAccessible = m.isAccessible();
					    if (!isAccessible) {
						m.setAccessible(true);
					    }
					    LOG.trace("Calling " + toObj.getClass().getName() + "." + m.getName() + "(null)");
					    m.invoke(toObj, new Object[] { null });
					    if (!isAccessible) {
						m.setAccessible(isAccessible);
					    }
					    foundValue = true;

					} catch (Exception e1) {
					    LOG.error("Caught exception trying to cally" + toClass.getName() + "." + m.getName() + "() with Properties entry", e1);
					}
				    } else {
					try {
					    // See if I can find a constructor I
					    // can
					    // use
					    Constructor[] constructors = paramClass.getConstructors();
					    // Is there a String constructor?
					    for (Constructor c : constructors) {
						Class[] cParms = c.getParameterTypes();
						if ((cParms != null) && (cParms.length == 1)) {
						    if (String.class.isAssignableFrom(cParms[0])) {
							constObj = c.newInstance(paramValue);
						    }
						}
					    }

					    if (constObj == null) {
						// Is there a Long constructor?
						for (Constructor c : constructors) {
						    Class[] cParms = c.getParameterTypes();
						    if ((cParms != null) && (cParms.length == 1)) {
							if (Long.class.isAssignableFrom(cParms[0])) {
							    constObj = c.newInstance(Long.parseLong(paramValue));
							}
						    }
						}

					    }

					    if (constObj == null) {

						// Last chance - see if
						// parameter class has a static
						// method
						// getDefaultInstance(String)
						try {
						    Method gm = paramClass.getMethod("getDefaultInstance", String.class);

						    int gmodifier = gm.getModifiers();
						    if (Modifier.isStatic(gmodifier)) {
							// Invoke static
							// getDefaultInstance(String)
							paramObj = gm.invoke(null, paramValue);
						    }

						} catch (Exception gme) {
						    // Ignore exceptions
						}
					    }

					} catch (Exception e1) {
					    LOG.warn("Could not find a suitable constructor for " + paramClass.getName(), e1);
					}

					if (constObj == null) {
					    LOG.warn("Could not find builder class " + builderName + " and could not find a String or Long constructor or static getDefaultInstance(String) - trying just to set passing paramValue");

					}
				    }
				} catch (Exception e) {
				    LOG.error("Caught exception trying to create builder " + builderName, e);
				}

				if (paramObj != null) {

				    try {

					Method buildMethod = builderClass.getMethod("build");
					LOG.trace("Calling " + paramObj.getClass().getName() + "." + buildMethod.getName() + "()");
					Object builtObj = buildMethod.invoke(paramObj);

					boolean isAccessible = m.isAccessible();
					if (!isAccessible) {
					    m.setAccessible(true);
					}

					LOG.trace("Calling " + toObj.getClass().getName() + "." + m.getName() + "()");
					m.invoke(toObj, builtObj);
					if (!isAccessible) {
					    m.setAccessible(isAccessible);
					}
					foundValue = true;

				    } catch (Exception e) {
					LOG.error("Caught exception trying to set Yang-generated class expected by" + toClass.getName() + "." + m.getName() + "() from Properties entry", e);
				    }
				} else {
				    try {
					boolean isAccessible = m.isAccessible();
					if (!isAccessible) {
					    m.setAccessible(true);
					}

					if (constObj != null) {

					    LOG.trace("Calling " + toObj.getClass().getName() + "." + m.getName() + "(" + constObj.toString() + ")");
					    m.invoke(toObj, constObj);
					} else {
					    LOG.trace("Calling " + toObj.getClass().getName() + "." + m.getName() + "(" + paramValue + ")");
					    m.invoke(toObj, paramValue);

					}
					if (!isAccessible) {
					    m.setAccessible(isAccessible);
					}
					foundValue = true;

				    } catch (Exception e) {
					LOG.error("Caught exception trying to convert value returned by" + toClass.getName() + "." + m.getName() + "() to Properties entry", e);
				    }
				}
			    }
			}
		    } else {

			// Setter's argument is not a yang-generated class. See
			// if it is a List.

			if (List.class.isAssignableFrom(paramClass)) {

			    LOG.trace("Parameter class " + paramClass.getName() + " is a List");

			    // Figure out what type of args are in List and pass
			    // that to toList().

			    Type paramType = m.getGenericParameterTypes()[0];
			    Type elementType = ((ParameterizedType) paramType).getActualTypeArguments()[0];
			    Object paramObj = new LinkedList();
			    try {
				paramObj = toList(props, propName, (List) paramObj, (Class) elementType);
			    } catch (Exception e) {
				LOG.error("Caught exception trying to create list expected as argument to " + toClass.getName() + "." + m.getName());
			    }

			    if (paramObj != null) {
				try {
				    boolean isAccessible = m.isAccessible();
				    if (!isAccessible) {
					m.setAccessible(true);
				    }
				    LOG.trace("Calling " + toObj.getClass().getName() + "." + m.getName() + "(" + paramValue + ")");
				    m.invoke(toObj, paramObj);
				    if (!isAccessible) {
					m.setAccessible(isAccessible);
				    }
				    foundValue = true;

				} catch (Exception e) {
				    LOG.error("Caught exception trying to convert List returned by" + toClass.getName() + "." + m.getName() + "() to Properties entry", e);
				}
			    }
			} else {

			    // Setter expects something that is not a List and
			    // not yang-generated. Just pass the parameter value

			    LOG.trace("Parameter class " + paramClass.getName() + " is not a yang-generated class or a List");

			    if ((paramValue != null) && (paramValue.length() > 0)) {

				Object constObj = null;

				try {
				    // See if I can find a constructor I can use
				    Constructor[] constructors = paramClass.getConstructors();
				    // Is there a String constructor?
				    for (Constructor c : constructors) {
					Class[] cParms = c.getParameterTypes();
					if ((cParms != null) && (cParms.length == 1)) {
					    if (String.class.isAssignableFrom(cParms[0])) {
						constObj = c.newInstance(paramValue);
					    }
					}
				    }

				    if (constObj == null) {
					// Is there a Long constructor?
					for (Constructor c : constructors) {
					    Class[] cParms = c.getParameterTypes();
					    if ((cParms != null) && (cParms.length == 1)) {
						if (Long.class.isAssignableFrom(cParms[0])) {
						    constObj = c.newInstance(Long.parseLong(paramValue));
						}
					    }
					}

				    }

				    if (constObj != null) {
					try {
					    LOG.trace("Calling " + toObj.getClass().getName() + "." + m.getName() + "(" + constObj + ")");
					    m.invoke(toObj, constObj);
					    foundValue = true;
					} catch (Exception e2) {
					    LOG.error("Caught exception trying to call " + m.getName(), e2);
					}
				    } else {
					try {
					    boolean isAccessible = m.isAccessible();
					    if (!isAccessible) {
						m.setAccessible(true);
					    }
					    LOG.trace("Calling " + toObj.getClass().getName() + "." + m.getName() + "(" + paramValue + ")");
					    m.invoke(toObj, paramValue);
					    if (!isAccessible) {
						m.setAccessible(isAccessible);
					    }
					    foundValue = true;

					} catch (Exception e) {
					    LOG.error("Caught exception trying to convert value returned by" + toClass.getName() + "." + m.getName() + "() to Properties entry", e);
					}
				    }
				} catch (Exception e1) {
				    LOG.warn("Could not find a suitable constructor for " + paramClass.getName(), e1);
				}

			    }
			}
		    }
		} // End of section handling "setter" method
	    } // End of loop through Methods
	} // End of section handling yang-generated class

	if (foundValue) {
	    return (toObj);
	} else {
	    return (null);
	}
    }

    public static void printPropertyList(PrintStream pstr, String pfx, Class toClass) {
	boolean foundValue = false;

	LOG.trace("Analyzing " + toClass.getName() + " class : pfx " + pfx);

	if (isYangGenerated(toClass) && (!Identifier.class.isAssignableFrom(toClass))) {
	    // Class is yang generated.
	    LOG.trace(toClass.getName() + " is a Yang-generated class");

	    if (toClass.getName().endsWith("Key")) {
		if (Identifier.class.isAssignableFrom(toClass)) {
		    LOG.trace(Identifier.class.getName() + " is assignable from " + toClass.getName());
		} else {

		    LOG.trace(Identifier.class.getName() + " is NOT assignable from " + toClass.getName());
		}
	    }

	    String propNamePfx = null;
	    if (pfx.endsWith("]")) {
		propNamePfx = pfx;
	    } else {

		if ((pfx != null) && (pfx.length() > 0)) {
		    propNamePfx = pfx + "." + toLowerHyphen(toClass.getSimpleName());
		} else {
		    propNamePfx = toLowerHyphen(toClass.getSimpleName());
		}

		if (propNamePfx.endsWith("-builder")) {
		    propNamePfx = propNamePfx.substring(0, propNamePfx.length() - "-builder".length());
		}

		if (propNamePfx.endsWith("-impl")) {
		    propNamePfx = propNamePfx.substring(0, propNamePfx.length() - "-impl".length());
		}
	    }

	    // Iterate through getter methods to figure out values we need to
	    // set

	    for (Method m : toClass.getMethods()) {
		LOG.trace("Is " + m.getName() + " method a getter?");
		if (isGetter(m)) {
		    LOG.trace(m.getName() + " is a getter");
		    Class returnClass = m.getReturnType();

		    String fieldName = toLowerHyphen(m.getName().substring(3));
		    fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);

		    String propName = propNamePfx + "." + fieldName;

		    // Is the return type a yang generated class?
		    if (isYangGenerated(returnClass)) {
			// Is it an enum?
			if (returnClass.isEnum()) {

			    LOG.trace(m.getName() + " is an Enum");
			    pstr.print("\n\n     * " + propName);

			} else {

			    String simpleName = returnClass.getSimpleName();

			    if ("Ipv4Address".equals(simpleName) || "Ipv6Address".equals(simpleName) || "IpAddress".equals(simpleName) || "IpPrefix".equals(simpleName)) {
				LOG.trace(m.getName() + " is an " + simpleName);
				pstr.print("\n\n     * " + propName);
			    } else {
				printPropertyList(pstr, propNamePfx, returnClass);
			    }

			}
		    } else {

			// Setter's argument is not a yang-generated class. See
			// if it is a List.

			if (List.class.isAssignableFrom(returnClass)) {

			    LOG.trace("Parameter class " + returnClass.getName() + " is a List");

			    // Figure out what type of args are in List and pass
			    // that to toList().

			    Type returnType = m.getGenericReturnType();
			    Type elementType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
			    Class elementClass = (Class) elementType;
			    LOG.trace("Calling printPropertyList on list type (" + elementClass.getName() + "), pfx is (" + pfx + "), toClass is (" + toClass.getName() + ")");
			    printPropertyList(pstr, propNamePfx + "." + toLowerHyphen(elementClass.getSimpleName()) + "[]", elementClass);

			} else if (!returnClass.equals(Class.class)) {

			    // Setter expects something that is not a List and
			    // not yang-generated. Just pass the parameter value

			    LOG.trace("Parameter class " + returnClass.getName() + " is not a yang-generated class or a List");

			    pstr.print("\n\n     * " + propName);

			}
		    }
		} // End of section handling "setter" method
	    } // End of loop through Methods
	} // End of section handling yang-generated class

    }

    public static boolean isYangGenerated(Class c) {
	if (c == null) {
	    return (false);
	} else {
	    return (c.getName().startsWith("org.opendaylight.yang.gen."));
	}
    }

    public static boolean isIpPrefix(Class c) {

	if (c == null) {
	    return (false);
	}
	String simpleName = c.getSimpleName();
	return ("IpPrefix".equals(simpleName));
    }

    public static boolean isIpv4Address(Class c) {

	if (c == null) {
	    return (false);
	}
	String simpleName = c.getSimpleName();
	return ("Ipv4Address".equals(simpleName));
    }

    public static boolean isIpv6Address(Class c) {

	if (c == null) {
	    return (false);
	}
	String simpleName = c.getSimpleName();
	return ("Ipv6Address".equals(simpleName));
    }

    public static boolean isIpAddress(Class c) {

	if (c == null) {
	    return (false);
	}
	String simpleName = c.getSimpleName();
	return ("IpAddress".equals(simpleName));
    }

    public static String toLowerHyphen(String inStr) {
	if (inStr == null) {
	    return (null);
	}

	String str = inStr.substring(0, 1).toLowerCase();
	if (inStr.length() > 1) {
	    str = str + inStr.substring(1);
	}

	String regex = "(([a-z0-9])([A-Z]))";
	String replacement = "$2-$3";

	String retval = str.replaceAll(regex, replacement).toLowerCase();

	LOG.trace("Converting " + inStr + " => " + str + " => " + retval);
	return (retval);
    }

    //This is called when mapping the yang value back to a valid java enumeration
    public static String toJavaEnum(String inStr) {
	if (inStr == null) {
	    return (null);
	} else if (inStr.length() == 0) {
	    return (inStr);
	}

	//This will strip out all periods, which cannot be in a java enum
    inStr = inStr.replaceAll("\\.", "");

	String[] terms = inStr.split("-");
	StringBuffer sbuff = new StringBuffer();

	//appends an _ if the string starts with a digit to make it a valid java enum
	if (Character.isDigit(inStr.charAt(0))) {
	    sbuff.append('_');
	}
	//If the string contains hyphens it will convert the string to upperCamelCase without hyphens
	for (String term : terms) {
	    sbuff.append(term.substring(0, 1).toUpperCase());
	    if (term.length() > 1) {
		sbuff.append(term.substring(1));
	    }
	}
	return (sbuff.toString());

    }

    public static boolean isGetter(Method m) {
	if (m == null) {
	    return (false);
	}

	if (Modifier.isPublic(m.getModifiers()) && (m.getParameterTypes().length == 0)) {
	    if (m.getName().matches("^get[A-Z].*") && !m.getReturnType().equals(void.class)) {
		if (!"getClass".equals(m.getName())) {
		    return (true);
		}
	    }

	    if (m.getName().matches("^get[A-Z].*") && m.getReturnType().equals(boolean.class)) {
		return (true);
	    }

	    if (m.getName().matches("^is[A-Z].*") && m.getReturnType().equals(Boolean.class)) {
		return (true);
	    }
	}

	return (false);
    }

    public static boolean isSetter(Method m) {
	if (m == null) {
	    return (false);
	}

	if (Modifier.isPublic(m.getModifiers()) && (m.getParameterTypes().length == 1)) {
	    if (m.getName().matches("^set[A-Z].*")) {
		Class[] paramTypes = m.getParameterTypes();
		if (paramTypes[0].isAssignableFrom(Identifier.class) || Identifier.class.isAssignableFrom(paramTypes[0])) {
		    return (false);
		} else {
		    return (true);
		}
	    }

	}

	return (false);
    }

    public static String getFullPropertiesPath(String propertiesFileName) {
	return "/opt/bvc/controller/configuration/" + propertiesFileName;
    }
    
    //This is called when mapping a valid java enumeration back to the yang model value
    public static String mapEnumeratedValue(String propertyName, String propertyValue) {
        LOG.info("mapEnumeratedValue called with propertyName=" + propertyName + " and value=" + propertyValue);
        String mappingKey = "yang." + propertyName + "." + propertyValue;
        if (yangMappingProperties.containsKey(mappingKey)) {
            return (yangMappingProperties.getProperty(mappingKey));
        } else {
            LOG.info("yangMappingProperties did not contain the key " + mappingKey + " returning the original value.");
            return propertyValue;
        }
    }

}
