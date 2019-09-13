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

package org.onap.ccsdk.sli.core.sli.provider.base;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.sli.core.api.SvcLogicNode;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.api.extensions.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicContextImpl;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraphImpl;
import org.onap.ccsdk.sli.core.sli.SvcLogicNodeImpl;
import junit.framework.TestCase;

public class PluginTest extends TestCase {

    // The existing plugins work just like a VoidDummyPlugin
    // They will return null simply because they are all void
    // The attribute emitsOutcome will not be present, the expected outcome is success when no exception is thrown by the plugin
    public void testOldPlugin() throws Exception {
        ExecuteNodeExecutor executor = new ExecuteNodeExecutor();
        SvcLogicJavaPlugin plugin = new SvcLogicJavaPlugin() {
                public void dummy(Map<String, String> parameters, SvcLogicContextImpl ctx) throws SvcLogicException {
                        return;
                }
        };

        Class pluginClass = plugin.getClass();
        Method pluginMethod = pluginClass.getMethod("dummy", Map.class, SvcLogicContextImpl.class);
        Map<String, String> parmMap = new HashMap<String, String>();
        SvcLogicContextImpl ctx = new SvcLogicContextImpl();
        Object o = pluginMethod.invoke(plugin, parmMap, ctx);

        SvcLogicGraphImpl graph = new SvcLogicGraphImpl();
        SvcLogicNode node = new SvcLogicNodeImpl(1, "return", graph);
        String emitsOutcome = SvcLogicExpressionResolver.evaluate(node.getAttribute("emitsOutcome"),  node, ctx);
        String outValue = executor.mapOutcome(o, emitsOutcome);
        assertEquals("success",outValue);
    }

    //Newer plugins can set the attribute emitsOutcome to true, if so they should return a string
    //The string represents the outcome value
    public void testNewPlugin() throws Exception {
        ExecuteNodeExecutor executor = new ExecuteNodeExecutor();
        SvcLogicJavaPlugin plugin = new LunchSelectorPlugin();

        Class pluginClass = plugin.getClass();
        Method pluginMethod = pluginClass.getMethod("selectLunch", Map.class, SvcLogicContextImpl.class);

        Map<String, String> parmMap = new HashMap<String, String>();
        SvcLogicContextImpl ctx = new SvcLogicContextImpl();

        parmMap.put("day", "monday");
        Object o = pluginMethod.invoke(plugin, parmMap, ctx);
        SvcLogicGraphImpl graph = new SvcLogicGraphImpl();
        SvcLogicNode node = new SvcLogicNodeImpl(1, "return", graph);
        node.setAttribute("emitsOutcome", "true");
        String emitsOutcome = SvcLogicExpressionResolver.evaluate(node.getAttribute("emitsOutcome"),  node, ctx);
        String outValue = executor.mapOutcome(o, emitsOutcome);
        assertEquals("pizza", outValue);

        parmMap.put("day", "tuesday");
        outValue = (String) pluginMethod.invoke(plugin, parmMap, ctx);
        o = pluginMethod.invoke(plugin, parmMap, ctx);
        outValue = executor.mapOutcome(o, emitsOutcome);
        assertEquals("soup",outValue);

    }

    //APPC had some legacy plugins returning objects which should not be treated as outcomes
    //The attribute emitsOutcome will not be set
    //The outcome should be success as it has always been
    public void testObjPlugin() throws Exception{
        ExecuteNodeExecutor executor = new ExecuteNodeExecutor();
        SvcLogicJavaPlugin plugin = new LunchSelectorPlugin();

        Class pluginClass = plugin.getClass();
        Method pluginMethod = pluginClass.getMethod("makeLunch", Map.class, SvcLogicContextImpl.class);

        Map<String, String> parmMap = new HashMap<String, String>();
        SvcLogicContextImpl ctx = new SvcLogicContextImpl();
        Object o = pluginMethod.invoke(plugin, parmMap, ctx);
        SvcLogicGraphImpl graph = new SvcLogicGraphImpl();
        SvcLogicNode node = new SvcLogicNodeImpl(1, "return", graph);
        String emitsOutcome = SvcLogicExpressionResolver.evaluate(node.getAttribute("emitsOutcome"),  node, ctx);
        String outValue = executor.mapOutcome(o, emitsOutcome);
        assertEquals("success",outValue);
    }

}
