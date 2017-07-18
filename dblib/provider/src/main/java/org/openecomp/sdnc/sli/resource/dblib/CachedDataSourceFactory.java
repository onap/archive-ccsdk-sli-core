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

package org.openecomp.sdnc.sli.resource.dblib;


import org.openecomp.sdnc.sli.resource.dblib.config.BaseDBConfiguration;
import org.openecomp.sdnc.sli.resource.dblib.config.JDBCConfiguration;
import org.openecomp.sdnc.sli.resource.dblib.jdbc.JdbcDBCachedDataSource;
import org.openecomp.sdnc.sli.resource.dblib.jdbc.MySQLCachedDataSource;

/**
 * @version $Revision: 1.1 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki
 */
public class CachedDataSourceFactory {

	public static CachedDataSource createDataSource(BaseDBConfiguration config) {
		if(config instanceof JDBCConfiguration)
			return JdbcDBCachedDataSource.createInstance(config);

		return (CachedDataSource)null;
	}

}
