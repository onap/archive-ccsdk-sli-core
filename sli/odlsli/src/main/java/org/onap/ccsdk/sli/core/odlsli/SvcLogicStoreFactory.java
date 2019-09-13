/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
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

package org.onap.ccsdk.sli.core.odlsli;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.onap.ccsdk.sli.core.api.exceptions.ConfigurationException;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.api.util.SvcLogicStore;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvcLogicStoreFactory {

	private static final Logger LOG = LoggerFactory.getLogger(SvcLogicStoreFactory.class);

	public static SvcLogicStore getSvcLogicStore(String propfile)
			throws SvcLogicException {
		File propFile = new File(propfile);
		if (!propFile.canRead()) {
			throw new ConfigurationException("Cannot read property file "
					+ propfile);

		}

		try {
			return getSvcLogicStore(new FileInputStream(propFile));
		} catch (Exception e) {
			throw new ConfigurationException(
					"Could load service store from properties file " + propfile,
					e);
		}

	}

	public static SvcLogicStore getSvcLogicStore(InputStream inStr) throws SvcLogicException
	{
		Properties props = new Properties();

		try {
			props.load(inStr);
		} catch (Exception e) {
			throw new ConfigurationException("Could not get load properties from input stream", e);
		}

		return getSvcLogicStore(props);
	}

	public static SvcLogicStore getSvcLogicStore(Properties props)
            throws SvcLogicException {
        String storeType = props.getProperty("org.onap.ccsdk.sli.dbtype");
        if ((storeType == null) || (storeType.length() == 0)) {
            throw new ConfigurationException("property org.onap.ccsdk.sli.dbtype unset");

        }

        LOG.debug("Using org.onap.ccsdk.sli.dbtype={}", storeType);

        if ("jdbc".equalsIgnoreCase(storeType)) {
            return new SvcLogicJdbcStore(props);
        } else if ("dblib".equalsIgnoreCase(storeType)) {
            return new SvcLogicDblibStore(new DBResourceManager(props));
        } else {
            throw new ConfigurationException("unsupported dbtype (" + storeType + ")");
        }
	}

}