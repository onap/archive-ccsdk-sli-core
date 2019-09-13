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

package org.onap.ccsdk.sli.core.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.sli.core.api.SvcLogicContext;
import org.onap.ccsdk.sli.core.api.SvcLogicGraph;
import org.onap.ccsdk.sli.core.api.util.SvcLogicParser;
import org.onap.ccsdk.sli.core.api.util.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.provider.base.HashMapResolver;
import org.onap.ccsdk.sli.core.sli.provider.base.InMemorySvcLogicStore;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicContextImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.AbstractSvcLogicNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.BlockNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.BreakNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.CallNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ConfigureNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.DeleteNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ExecuteNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ExistsNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ForNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.GetResourceNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.IsAvailableNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.NotifyNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.RecordNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ReleaseNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ReserveNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ReturnNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.SaveNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.SetNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.SwitchNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.UpdateNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.WhileNodeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ITCaseSvcLogicGraphExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(ITCaseSvcLogicGraphExecutor.class);
    protected static SvcLogicStore store;
    protected static SvcLogicParser parser;
    protected static SvcLogicServiceImpl svc;

    private static final Map<String, AbstractSvcLogicNodeExecutor> BUILTIN_NODES = new HashMap<String, AbstractSvcLogicNodeExecutor>() {
        {
            put("block", new BlockNodeExecutor());
            put("break", new BreakNodeExecutor());
            put("call", new CallNodeExecutor());
            put("configure", new ConfigureNodeExecutor());
            put("delete", new DeleteNodeExecutor());
            put("execute", new ExecuteNodeExecutor());
            put("exists", new ExistsNodeExecutor());
            put("for", new ForNodeExecutor());
            put("get-resource", new GetResourceNodeExecutor());
            put("is-available", new IsAvailableNodeExecutor());
            put("notify", new NotifyNodeExecutor());
            put("record", new RecordNodeExecutor());
            put("release", new ReleaseNodeExecutor());
            put("reserve", new ReserveNodeExecutor());
            put("return", new ReturnNodeExecutor());
            put("save", new SaveNodeExecutor());
            put("set", new SetNodeExecutor());
            put("switch", new SwitchNodeExecutor());
            put("update", new UpdateNodeExecutor());
            put("while", new WhileNodeExecutor());

        }
    };

    private static HashMapResolver svcLogicClassResolver;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        store = new InMemorySvcLogicStore();
        parser = new SvcLogicParserImpl();
        svc = new SvcLogicServiceImpl(store, new HashMapResolver());

        for (String nodeType : BUILTIN_NODES.keySet()) {
            svc.registerExecutor(nodeType, BUILTIN_NODES.get(nodeType));
        }




    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        LOG.info("after class");
    }

    @Before
    public void setUp() throws Exception {
        LOG.info("before");
    }

    @After
    public void tearDown() throws Exception {
        LOG.info("after");
    }

    @Test
    public void testExecute() {

        try {
            InputStream testStr = getClass().getResourceAsStream("/executor.tests");
            BufferedReader testsReader = new BufferedReader(new InputStreamReader(testStr));
            String testCaseLine = null;
            while ((testCaseLine = testsReader.readLine()) != null) {

                String[] testCaseFields = testCaseLine.split(":");
                String testCaseFile = testCaseFields[0];
                String testCaseMethod = testCaseFields[1];
                String testCaseParameters = null;

                if (testCaseFields.length > 2) {
                    testCaseParameters = testCaseFields[2];
                }

                SvcLogicContext ctx = new SvcLogicContextImpl();
                if (testCaseParameters != null) {
                    String[] testCaseParameterSettings = testCaseParameters.split(",");

                    for (int i = 0; i < testCaseParameterSettings.length; i++) {
                        String[] nameValue = testCaseParameterSettings[i].split("=");
                        if (nameValue != null) {
                            String name = nameValue[0];
                            String value = "";
                            if (nameValue.length > 1) {
                                value = nameValue[1];
                            }

                            ctx.setAttribute(name, value);
                        }
                    }
                }

                testCaseFile = testCaseFile.trim();

                if (testCaseFile.length() > 0) {
                    if (!testCaseFile.startsWith("/")) {
                        testCaseFile = "/" + testCaseFile;
                    }
                    URL testCaseUrl = getClass().getResource(testCaseFile);
                    if (testCaseUrl == null) {
                        fail("Could not resolve test case file " + testCaseFile);
                    }


                    LinkedList<SvcLogicGraph> graphs = parser.parse(testCaseUrl.getPath());

                    assertNotNull(graphs);
                    // Load grqphs into db to support call node
                    for (SvcLogicGraph graph : graphs) {
                        store.store(graph);
                    }
                    // Load grqphs into db to support call node
                    store.activate("neutron", "canCreateNetwork", "1.0.0", "sync");
                    store.activate("neutron", "switchTester", "1.0.0", "sync");
                    store.activate("neutron", "forRecordTester", "1.0.0", "sync");
                    store.activate("neutron", "whileNodeTester", "1.0.0", "sync");
                    store.activate("neutron", "resourceTester", "1.0.0", "sync");
                    store.activate("neutron", "configureTester", "1.0.0", "sync");
                    store.activate("neutron", "javaPluginTester", "1.0.0", "sync");
                    store.activate("neutron", "allNodesTester", "1.0.0", "sync");
                    store.activate("neutron", "networkCreated", "1.0.0", "sync");

                    for (SvcLogicGraph graph : graphs) {
                        if (graph.getRpc().equals(testCaseMethod)) {
                            Properties props = ctx.toProperties();
                            LOG.info("SvcLogicContext before executing {}:", testCaseMethod);
                            for (Enumeration e1 = props.propertyNames(); e1.hasMoreElements(); ) {
                                String propName = (String) e1.nextElement();
                                LOG.info(propName + " = " + props.getProperty(propName));
                            }

                            svc.execute(graph, ctx);

                            props = ctx.toProperties();
                            LOG.info("SvcLogicContext after executing {}:", testCaseMethod);
                            for (Enumeration e2 = props.propertyNames(); e2.hasMoreElements(); ) {
                                String propName = (String) e2.nextElement();
                                LOG.info(propName + " = " + props.getProperty(propName));
                            }
                        }
                    }

                }
            }

        } catch (Exception e) {
            LOG.error("Caught exception executing directed graphs", e);
            fail("Exception executing graphs");
        }
    }
}
