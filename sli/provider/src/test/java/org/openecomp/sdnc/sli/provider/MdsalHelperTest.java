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

import junit.framework.TestCase;

public class MdsalHelperTest extends TestCase {

    public static final String pathToSdnPropertiesFile = "./src/test/resources/l3sdn.properties";

    public void testSdnProperties() {
        MdsalHelperTesterUtil.loadProperties(pathToSdnPropertiesFile);
        assertEquals("synccomplete", MdsalHelperTesterUtil.mapEnumeratedValue("request-status", "Synccomplete"));
        assertEquals("asynccomplete", MdsalHelperTesterUtil.mapEnumeratedValue("request-status", "asynccomplete"));
        assertEquals("notifycomplete", MdsalHelperTesterUtil.mapEnumeratedValue("request-status", "notifycomplete"));
        assertEquals("service-configuration-operation", MdsalHelperTesterUtil.mapEnumeratedValue("rpc-name",
                "ServiceConfigurationOperation"));
    }

    public void testNegativeSdnProperties() {
        assertNotSame("synccomplete", MdsalHelperTesterUtil.mapEnumeratedValue("request-status", "Synccomplete"));
    }

}
