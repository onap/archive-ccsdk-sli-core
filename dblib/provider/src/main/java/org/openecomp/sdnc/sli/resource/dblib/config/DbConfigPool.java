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

import java.util.ArrayList;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbConfigPool {
	private static Logger LOGGER = LoggerFactory.getLogger(DbConfigPool.class);

	private final String type;

	private ArrayList<BaseDBConfiguration> configurations = new ArrayList<BaseDBConfiguration>();

	public DbConfigPool(Properties properties) {
		LOGGER.debug("Initializing DbConfigType");
		type = properties.getProperty(BaseDBConfiguration.DATABASE_TYPE, "JDBC").toUpperCase();
	}

	public int getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getType() {
		return type;
	}

	public JDBCConfiguration[] getJDBCbSourceArray() {
		return configurations.toArray(new JDBCConfiguration[configurations.size()]);
	}

	public void addConfiguration(BaseDBConfiguration config) {
		configurations.add(config);
	}
}
