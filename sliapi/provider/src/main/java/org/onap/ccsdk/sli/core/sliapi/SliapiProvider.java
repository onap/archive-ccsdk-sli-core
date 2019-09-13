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

package org.onap.ccsdk.sli.core.sliapi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;
import org.onap.ccsdk.sli.core.api.SvcLogicService;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput.Mode;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.HealthcheckInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.HealthcheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.HealthcheckOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.SLIAPIService;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.VlbcheckInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.VlbcheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.VlbcheckOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResult;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResultBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class SliapiProvider implements AutoCloseable, SLIAPIService {

	private static final Logger LOG = LoggerFactory.getLogger(SliapiProvider.class);
	private static final String appName = "slitester";

    private SvcLogicService svcLogic;

	private static String SLIAPI_NAMESPACE = "org:onap:ccsdk:sli:core:sliapi";
	private static String SLIAPI_REVISION = "2016-11-10";
	private static String SDNC_STATUS_FILE = "SDNC_STATUS_FILE";
	private static String sdncStatusFile = null;

	private static QName TEST_RESULTS_QNAME = null;
	private static QName TEST_RESULT_QNAME = null;
	private static QName TEST_ID_QNAME = null;
	private static QName RESULTS_QNAME = null;

	static {
		TEST_RESULTS_QNAME = QName.create(SLIAPI_NAMESPACE, SLIAPI_REVISION, "test-results");
		TEST_RESULT_QNAME = QName.create(TEST_RESULTS_QNAME, "test-result");
		TEST_ID_QNAME = QName.create(TEST_RESULT_QNAME, "test-identifier");
		RESULTS_QNAME = QName.create(TEST_RESULT_QNAME, "results");
	}

    public SliapiProvider(SvcLogicService svcLogic) {
		this.LOG.info("Creating provider for " + appName);
		this.svcLogic = svcLogic;
		initialize();
	}

	public void initialize() {
		LOG.info("Initializing provider for " + appName);
		// initialization code goes here.
		sdncStatusFile = System.getenv(SDNC_STATUS_FILE);
		LOG.info("SDNC STATUS FILE = " + sdncStatusFile);
		LOG.info("Initialization complete for " + appName);
	}

	protected void initializeChild() {
		// Override if you have custom initialization intelligence
	}

	@Override
	public void close() throws Exception {
		LOG.info("Closing provider for " + appName);
		// closing code goes here
		LOG.info("Successfully closed provider for " + appName);
	}


	@Override
	public ListenableFuture<RpcResult<ExecuteGraphOutput>> executeGraph(ExecuteGraphInput input) {
		RpcResult<ExecuteGraphOutput> rpcResult = null;

		ExecuteGraphOutputBuilder respBuilder = new ExecuteGraphOutputBuilder();

		String calledModule = input.getModuleName();
		String calledRpc = input.getRpcName();
		Mode calledMode = input.getMode();
		String modeStr = "sync";

		if (calledMode == Mode.Async) {
			modeStr = "async";
		}

		if (svcLogic == null) {
			respBuilder.setResponseCode("500");
			respBuilder.setResponseMessage("Could not locate OSGi SvcLogicService service");
			respBuilder.setAckFinalIndicator("Y");

			rpcResult = RpcResultBuilder.<ExecuteGraphOutput>status(true).withResult(respBuilder.build()).build();
			return (Futures.immediateFuture(rpcResult));
		}

		try {
			if (!svcLogic.hasGraph(calledModule, calledRpc, null, modeStr)) {
				respBuilder.setResponseCode("404");
				respBuilder.setResponseMessage(
						"Directed graph for " + calledModule + "/" + calledRpc + "/" + modeStr + " not found");
				respBuilder.setAckFinalIndicator("Y");

				rpcResult = RpcResultBuilder.<ExecuteGraphOutput>status(true).withResult(respBuilder.build()).build();
				return (Futures.immediateFuture(rpcResult));
			}
		} catch (Exception e) {
			LOG.error(
					"Caught exception looking for directed graph for " + calledModule + "/" + calledRpc + "/" + modeStr,
					e);

			respBuilder.setResponseCode("500");
			respBuilder.setResponseMessage("Internal error : could not determine if target graph exists");
			respBuilder.setAckFinalIndicator("Y");

			rpcResult = RpcResultBuilder.<ExecuteGraphOutput>status(true).withResult(respBuilder.build()).build();
			return (Futures.immediateFuture(rpcResult));
		}

		// Load properties
		Properties parms = new Properties();

		// Pass properties using names from sli-parameters
		for (SliParameter sliParm : input.getSliParameter()) {

			String propValue = "";

			Boolean boolval = sliParm.isBooleanValue();

			if (boolval != null) {
				propValue = boolval.toString();
			} else {
				Integer intval = sliParm.getIntValue();
				if (intval != null) {
					propValue = intval.toString();
				} else {
					propValue = sliParm.getStringValue();
					if (propValue == null) {
						propValue = "";
					}
				}
			}
			parms.setProperty(sliParm.getParameterName(), propValue);
		}

		// Also, pass "meta" properties (i.e. pass SliParameter objects themselves)
		ExecuteGraphInputBuilder inputBuilder = new ExecuteGraphInputBuilder(input);

		SliapiHelper.toProperties(parms, "input", inputBuilder);

		try {
			LOG.info("Calling directed graph for " + calledModule + "/" + calledRpc + "/" + modeStr);

			if (LOG.isTraceEnabled()) {
				StringBuffer argList = new StringBuffer();
				argList.append("Parameters : {");
				Enumeration e = parms.propertyNames();
				while (e.hasMoreElements()) {
					String propName = (String) e.nextElement();
					argList.append(" (" + propName + "," + parms.getProperty(propName) + ") ");
				}
				argList.append("}");
				LOG.trace(argList.toString());
				argList = null;
			}

            Properties respProps = svcLogic.execute(calledModule, calledRpc, null, modeStr, parms);

			StringBuilder sb = new StringBuilder("{");

			for (Object key : respProps.keySet()) {
				String keyValue = (String) key;
				if (keyValue != null && !"".equals(keyValue) && !keyValue.contains("input.sli-parameter")) {
					sb.append("\"").append(keyValue).append("\": \"").append(respProps.getProperty(keyValue))
							.append("\",");
				}
			}

			sb.setLength(sb.length() - 1);
			sb.append("}");

			respBuilder.setResponseCode(respProps.getProperty("error-code", "0"));
			respBuilder.setResponseMessage(respProps.getProperty("error-message", ""));// TODO change response-text to
																						// response-message to match
																						// other BVC APIs
			respBuilder.setAckFinalIndicator(respProps.getProperty("ack-final", "Y"));
			respBuilder.setContextMemoryJson(sb.toString());

			TestResultBuilder testResultBuilder = new TestResultBuilder();

			SliapiHelper.toBuilder(respProps, testResultBuilder);



		} catch (Exception e) {
			LOG.error("Caught exception executing directed graph for" + calledModule + ":" + calledRpc + "," + modeStr
					+ ">", e);

			respBuilder.setResponseCode("500");
			respBuilder.setResponseMessage("Internal error : caught exception executing directed graph " + calledModule
					+ "/" + calledRpc + "/" + modeStr);
			respBuilder.setAckFinalIndicator("Y");

		}

		rpcResult = RpcResultBuilder.<ExecuteGraphOutput>status(true).withResult(respBuilder.build()).build();
		return (Futures.immediateFuture(rpcResult));
	}

	@Override
	public ListenableFuture<RpcResult<HealthcheckOutput>> healthcheck(HealthcheckInput healthcheckInput) {

		RpcResult<HealthcheckOutput> rpcResult = null;

		HealthcheckOutputBuilder respBuilder = new HealthcheckOutputBuilder();

		String calledModule = "sli";
		String calledRpc = "healthcheck";
		String modeStr = "sync";

		if (svcLogic == null) {
			respBuilder.setResponseCode("500");
			respBuilder.setResponseMessage("Could not locate OSGi SvcLogicService service");
			respBuilder.setAckFinalIndicator("Y");

			rpcResult = RpcResultBuilder.<HealthcheckOutput>failed().withResult(respBuilder.build()).build();
			return (Futures.immediateFuture(rpcResult));
		}

		try {
			if (!svcLogic.hasGraph(calledModule, calledRpc, null, modeStr)) {
				respBuilder.setResponseCode("404");
				respBuilder.setResponseMessage(
						"Directed graph for " + calledModule + "/" + calledRpc + "/" + modeStr + " not found");

				respBuilder.setAckFinalIndicator("Y");

				rpcResult = RpcResultBuilder.<HealthcheckOutput>status(true).withResult(respBuilder.build()).build();
				return (Futures.immediateFuture(rpcResult));
			}
		} catch (Exception e) {
			LOG.error(
					"Caught exception looking for directed graph for " + calledModule + "/" + calledRpc + "/" + modeStr,
					e);

			respBuilder.setResponseCode("500");
			respBuilder.setResponseMessage("Internal error : could not determine if target graph exists");
			respBuilder.setAckFinalIndicator("Y");

			rpcResult = RpcResultBuilder.<HealthcheckOutput>failed().withResult(respBuilder.build()).build();
			return (Futures.immediateFuture(rpcResult));
		}

		try {
			LOG.info("Calling directed graph for " + calledModule + "/" + calledRpc + "/" + modeStr);

			Properties parms = new Properties();

			Properties respProps = svcLogic.execute(calledModule, calledRpc, null, modeStr, parms);

			respBuilder.setResponseCode(respProps.getProperty("error-code", "0"));
			respBuilder.setResponseMessage(respProps.getProperty("error-message", ""));
			respBuilder.setAckFinalIndicator(respProps.getProperty("ack-final", "Y"));

		} catch (Exception e) {
			LOG.error("Caught exception executing directed graph for" + calledModule + ":" + calledRpc + "," + modeStr
					+ ">", e);

			respBuilder.setResponseCode("500");
			respBuilder.setResponseMessage("Internal error : caught exception executing directed graph " + calledModule
					+ "/" + calledRpc + "/" + modeStr);
			respBuilder.setAckFinalIndicator("Y");

		}

		rpcResult = RpcResultBuilder.<HealthcheckOutput>status(true).withResult(respBuilder.build()).build();
		return (Futures.immediateFuture(rpcResult));
	}

	public ListenableFuture<RpcResult<VlbcheckOutput>> vlbcheck(VlbcheckInput vlbInput) {

		RpcResult<VlbcheckOutput> rpcResult = null;

		VlbcheckOutputBuilder respBuilder = new VlbcheckOutputBuilder();

		String calledModule = "sli";
		String calledRpc = "vlbcheck";
		String modeStr = "sync";

		if (svcLogic == null) {
			respBuilder.setResponseCode("500");
			respBuilder.setResponseMessage("Could not locate OSGi SvcLogicService service");
			respBuilder.setAckFinalIndicator("Y");

			rpcResult = RpcResultBuilder.<VlbcheckOutput>failed().withResult(respBuilder.build()).build();
			return (Futures.immediateFuture(rpcResult));
		}

		boolean dgExists = true;
		try {
			if (!svcLogic.hasGraph(calledModule, calledRpc, null, modeStr)) {
				dgExists = false;
			}
		} catch (Exception e) {
			LOG.warn(
					"Caught exception looking for directed graph for " + calledModule + "/" + calledRpc + "/" + modeStr,
					e);

			dgExists = false;
		}

		if (dgExists) {
			try {
				LOG.info("Calling directed graph for " + calledModule + "/" + calledRpc + "/" + modeStr);

				Properties parms = new Properties();

				Properties respProps = svcLogic.execute(calledModule, calledRpc, null, modeStr, parms);

				respBuilder.setResponseCode(respProps.getProperty("error-code", "0"));
				respBuilder.setResponseMessage(respProps.getProperty("error-message", ""));
				respBuilder.setAckFinalIndicator(respProps.getProperty("ack-final", "Y"));

			} catch (Exception e) {
				LOG.error("Caught exception executing directed graph for" + calledModule + ":" + calledRpc + ","
						+ modeStr + ">", e);

				respBuilder.setResponseCode("500");
				respBuilder.setResponseMessage("Internal error : caught exception executing directed graph "
						+ calledModule + "/" + calledRpc + "/" + modeStr);
				respBuilder.setAckFinalIndicator("Y");

			}

			rpcResult = RpcResultBuilder.<VlbcheckOutput>status(true).withResult(respBuilder.build()).build();
			return (Futures.immediateFuture(rpcResult));
		} else {
			// check the state based on the config file

			boolean suspended = false;
			BufferedReader br = null;
			String line = "";

			if (sdncStatusFile != null) {
				try {
					br = new BufferedReader(new FileReader(sdncStatusFile));
					while ((line = br.readLine()) != null) {
						if ("ODL_STATE=SUSPENDED".equals(line)) {
							suspended = true;
							LOG.debug("vlbcheck: server is suspended");
						}
					}
					br.close();
				} catch (FileNotFoundException e) {
					LOG.trace("Caught File not found exception " + sdncStatusFile + "\n", e);
				} catch (Exception e) {
					LOG.trace("Failed to read status file " + sdncStatusFile + "\n", e);
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {
							LOG.warn("Failed to close status file " + sdncStatusFile + "\n", e);
						}
					}
				}
			}

			if (suspended) {
				rpcResult = RpcResultBuilder.<VlbcheckOutput>failed()
						.withError(ErrorType.APPLICATION, "resource-denied", "Server Suspended").build();
			} else {
				respBuilder.setResponseMessage("server is normal");
				rpcResult = RpcResultBuilder.<VlbcheckOutput>status(true).withResult(respBuilder.build()).build();
			}
			return (Futures.immediateFuture(rpcResult));
		}
	}

	private MapEntryNode toMapEntryNode(TestResult testResult) {

		YangInstanceIdentifier testResultId = YangInstanceIdentifier.builder().node(TEST_RESULTS_QNAME)
				.node(TEST_RESULT_QNAME).build();

		// Construct results list
		LinkedList<LeafSetEntryNode<Object>> entryList = new LinkedList<>();
		for (String result : testResult.getResults()) {
			LeafSetEntryNode<Object> leafSetEntryNode = ImmutableLeafSetEntryNodeBuilder.create()
					.withNodeIdentifier(new NodeWithValue(RESULTS_QNAME, result)).withValue(result).build();
			entryList.add(leafSetEntryNode);
		}
		// Construct results LeafSetNode
		LeafSetNode<?> resultsNode = ImmutableLeafSetNodeBuilder.create()
				.withNodeIdentifier(new NodeIdentifier(RESULTS_QNAME)).withValue(entryList).build();

		// Construct test result ContainerNode with 2 children - test-identifier leaf
		// and results leaf-set
		MapEntryNode testResultNode = ImmutableNodes.mapEntryBuilder()
				.withNodeIdentifier(new NodeIdentifierWithPredicates(TEST_RESULT_QNAME, TEST_ID_QNAME,
						testResult.getTestIdentifier()))
				.withChild(ImmutableNodes.leafNode(TEST_ID_QNAME, testResult.getTestIdentifier()))
				.withChild(resultsNode).build();

		return (testResultNode);

	}

}
