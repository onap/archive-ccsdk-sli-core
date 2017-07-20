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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import org.openecomp.sdnc.sli.MetricLogger;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicGraph;
import org.openecomp.sdnc.sli.SvcLogicNode;
import org.openecomp.sdnc.sli.SvcLogicParser;
import org.openecomp.sdnc.sli.SvcLogicStore;
import org.openecomp.sdnc.sli.SvcLogicStoreFactory;
import org.openecomp.sdnc.sli.provider.BlockNodeExecutor;
import org.openecomp.sdnc.sli.provider.CallNodeExecutor;
import org.openecomp.sdnc.sli.provider.ConfigureNodeExecutor;
import org.openecomp.sdnc.sli.provider.DeleteNodeExecutor;
import org.openecomp.sdnc.sli.provider.ExecuteNodeExecutor;
import org.openecomp.sdnc.sli.provider.ExistsNodeExecutor;
import org.openecomp.sdnc.sli.provider.ForNodeExecutor;
import org.openecomp.sdnc.sli.provider.GetResourceNodeExecutor;
import org.openecomp.sdnc.sli.provider.IsAvailableNodeExecutor;
import org.openecomp.sdnc.sli.provider.NotifyNodeExecutor;
import org.openecomp.sdnc.sli.provider.RecordNodeExecutor;
import org.openecomp.sdnc.sli.provider.ReleaseNodeExecutor;
import org.openecomp.sdnc.sli.provider.ReserveNodeExecutor;
import org.openecomp.sdnc.sli.provider.ReturnNodeExecutor;
import org.openecomp.sdnc.sli.provider.SaveNodeExecutor;
import org.openecomp.sdnc.sli.provider.SetNodeExecutor;
import org.openecomp.sdnc.sli.provider.SvcLogicNodeExecutor;
import org.openecomp.sdnc.sli.provider.SvcLogicServiceImpl;
import org.openecomp.sdnc.sli.provider.SwitchNodeExecutor;
import org.openecomp.sdnc.sli.provider.UpdateNodeExecutor;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class SvcLogicGraphExecutorTest extends TestCase {
	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicGraph.class);
	
	private static final Map<String, SvcLogicNodeExecutor> BUILTIN_NODES = new HashMap<String, SvcLogicNodeExecutor>() {
		{
			put("block", new BlockNodeExecutor());
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

		}
	};
	
	public void testExecute() {
		
		try {
			InputStream testStr = getClass().getResourceAsStream("/executor.tests");
			BufferedReader testsReader = new BufferedReader(new InputStreamReader(testStr));
			
			InputStream propStr = getClass().getResourceAsStream("/svclogic.properties");
			
			SvcLogicStore store = SvcLogicStoreFactory.getSvcLogicStore(propStr);
			
			assertNotNull(store);
			
			store.registerNodeType("switch");
			store.registerNodeType("block");
			store.registerNodeType("get-resource");
			store.registerNodeType("reserve");
			store.registerNodeType("is-available");
			store.registerNodeType("exists");
			store.registerNodeType("configure");
			store.registerNodeType("return");
			store.registerNodeType("record");
			store.registerNodeType("allocate");
			store.registerNodeType("release");
			store.registerNodeType("for");
			store.registerNodeType("set");
			SvcLogicParser parser = new SvcLogicParser(store);
			
			// Loop through executor tests

			SvcLogicServiceImpl svc = new SvcLogicServiceImpl();
			
			for (String nodeType : BUILTIN_NODES.keySet()) {

				LOG.info("SLI - registering node executor for node type "+nodeType);
				
				svc.registerExecutor(nodeType, BUILTIN_NODES.get(nodeType));

			}
			String testCaseLine = null;
			while ((testCaseLine = testsReader.readLine()) != null) {
				
				String[] testCaseFields = testCaseLine.split(":");
				String testCaseFile = testCaseFields[0];
				String testCaseMethod = testCaseFields[1];
				String testCaseParameters = null;
				
				if (testCaseFields.length > 2) {
					testCaseParameters = testCaseFields[2];
				}

				SvcLogicContext ctx = new SvcLogicContext();
				if (testCaseParameters != null) {
					String[] testCaseParameterSettings = testCaseParameters.split(",");
					
					for (int i = 0 ; i < testCaseParameterSettings.length ; i++) {
						String[] nameValue = testCaseParameterSettings[i].split("=");
						if (nameValue != null) {
							String name = nameValue[0];
							String value = "";
							if (nameValue.length > 1) {
								value = nameValue[1];
							}
							
							ctx.setAttribute(name,  value);
						}
					}
				}
				
				testCaseFile = testCaseFile.trim();
				
				if (testCaseFile.length() > 0) {
					if (!testCaseFile.startsWith("/")) {
						testCaseFile = "/"+testCaseFile;
					}
					URL testCaseUrl = getClass().getResource(testCaseFile);
					if (testCaseUrl == null) {
						fail("Could not resolve test case file "+testCaseFile);
					}
					
					LinkedList<SvcLogicGraph> graphs = parser.parse(testCaseUrl.getPath());
					

					assertNotNull(graphs);
					
					for (SvcLogicGraph graph: graphs) {
						if (graph.getRpc().equals(testCaseMethod)) {
							Properties props = ctx.toProperties();
							LOG.info("SvcLogicContext before executing "+testCaseMethod+":");
							for (Enumeration e1 = props.propertyNames(); e1.hasMoreElements() ; ) {
								String propName = (String) e1.nextElement();
								LOG.info(propName+" = "+props.getProperty(propName));
							}
							
							svc.execute(graph, ctx);
							
							props = ctx.toProperties();
							LOG.info("SvcLogicContext after executing "+testCaseMethod+":");
							for (Enumeration e2 = props.propertyNames(); e2.hasMoreElements() ; ) {
								String propName = (String) e2.nextElement();
								LOG.info(propName+" = "+props.getProperty(propName));
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
