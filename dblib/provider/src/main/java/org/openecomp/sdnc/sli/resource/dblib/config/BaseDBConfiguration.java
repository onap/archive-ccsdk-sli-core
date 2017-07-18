/*-
 * ============LICENSE_START=======================================================
 * openecomp
 * ================================================================================
 * Copyright (C) 2016 - 2017 AT&T
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

package org.openecomp.sdnc.sli.resource.dblib.config;

import java.util.Properties;

public abstract class BaseDBConfiguration {
	public static final String DATABASE_TYPE	= "org.openecomp.sdnc.sli.dbtype";
	public static final String DATABASE_URL		= "org.openecomp.sdnc.sli.jdbc.url";
	public static final String DATABASE_NAME	= "org.openecomp.sdnc.sli.jdbc.database";
	public static final String CONNECTION_NAME	= "org.openecomp.sdnc.sli.jdbc.connection.name";
	public static final String DATABASE_USER 	= "org.openecomp.sdnc.sli.jdbc.user";
	public static final String DATABASE_PSSWD	= "org.openecomp.sdnc.sli.jdbc.password";
	public static final String CONNECTION_TIMEOUT="org.openecomp.sdnc.sli.jdbc.connection.timeout";
	public static final String REQUEST_TIMEOUT	= "org.openecomp.sdnc.sli.jdbc.request.timeout";
	public static final String MIN_LIMIT		= "org.openecomp.sdnc.sli.jdbc.limit.min";
	public static final String MAX_LIMIT		= "org.openecomp.sdnc.sli.jdbc.limit.max";
	public static final String INIT_LIMIT		= "org.openecomp.sdnc.sli.jdbc.limit.init";
	public static final String DATABASE_HOSTS   = "org.openecomp.sdnc.sli.jdbc.hosts";

	
	protected final Properties props;

	public BaseDBConfiguration(Properties properties) {
		this.props = properties;
	}

	public int getConnTimeout() {
		try {
			String value = props.getProperty(CONNECTION_TIMEOUT);
			return Integer.parseInt(value);
		} catch(Exception exc) {
			return -1;
		}
	}

	public int getRequestTimeout() {
		try {
			String value = props.getProperty(REQUEST_TIMEOUT);
			if(value == null)
				return -1;
			return Integer.parseInt(value);
		} catch(Exception exc) {
			return -1;
		}
	}

	public String getDbConnectionName() {
		return props.getProperty(CONNECTION_NAME);
	}

	public String getDatabaseName() {
		return props.getProperty(DATABASE_NAME);
	}

	public String getDbUserId() {
		return props.getProperty(DATABASE_USER);
	}

	public String getDbPasswd() {
		return props.getProperty(DATABASE_PSSWD);
	}

	public int getDbMinLimit() {
		String value = props.getProperty(MIN_LIMIT);
		return Integer.parseInt(value);
	}

	public int getDbMaxLimit() {
		String value = props.getProperty(MAX_LIMIT);
		return Integer.parseInt(value);
	}

	public int getDbInitialLimit() {
		String value = props.getProperty(INIT_LIMIT);
		return Integer.parseInt(value);
	}

	public String getDbUrl() {
		return props.getProperty(DATABASE_URL);
	}

	public String getServerGroup() {
		return null;
	}
}
