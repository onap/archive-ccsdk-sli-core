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

import java.util.Map.Entry;

import org.openecomp.sdnc.sli.DuplicateValueException;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicExpression;
import org.openecomp.sdnc.sli.SvcLogicGraph;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;
import org.openecomp.sdnc.sli.SvcLogicNode;

import junit.framework.TestCase;

public class ExecuteNodeExecutorTest extends TestCase {
    public class MockExecuteNodeExecutor extends ExecuteNodeExecutor {

        protected SvcLogicJavaPlugin getSvcLogicJavaPlugin(String pluginName) {
            return (SvcLogicJavaPlugin) new LunchSelectorPlugin();
        }

        protected String evaluate(SvcLogicExpression expr, SvcLogicNode node,
                SvcLogicContext ctx) throws SvcLogicException {
            return "selectLunch";
        }
    }

    public void testBadPlugin() throws DuplicateValueException, SvcLogicException {
        LunchSelectorPlugin p = new LunchSelectorPlugin();
        MockExecuteNodeExecutor execute = new MockExecuteNodeExecutor();
        SvcLogicNode node = new SvcLogicNode(0, "", "", new SvcLogicGraph());
        node.setAttribute("method", "selectLunch");
        execute.execute(new SvcLogicServiceImpl(), new SvcLogicNode(0, "", "", new SvcLogicGraph()), new SvcLogicContext());
    }

}
