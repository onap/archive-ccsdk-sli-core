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

public class StorageConstants {
    protected static final String JDBC_CONN_ERR = "no jdbc connection";
    protected static final String JDBC_STATEMENT_ERR = "could not prepare statement ";
    protected static final String SVCLOGIC_TABLE = ".SVC_LOGIC";
    protected static final String JDBC_SELECT_COUNT = "SELECT count(*) FROM ";
    protected static final String RESULTSET_CLOSE_ERR = "ResultSet close error: ";
    protected static final String JDBC_SELECT_GRAPGH = "SELECT graph FROM ";
    protected static final String JDBC_INSERT = "INSERT INTO ";
    protected static final String JDBC_DELETE = "DELETE FROM ";
    protected static final String JDBC_UPDATE = "UPDATE ";
    protected static final String JDBC_GRAPH_QUERY = " WHERE module = ? AND rpc = ? AND mode = ? AND version = ?";
    protected static final String JDBC_ACTIVE_GRAPH_QUERY =
            " WHERE module = ? AND rpc = ? AND mode = ? AND active = 'Y'";
}
