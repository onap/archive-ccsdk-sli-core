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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicExpression;
import org.openecomp.sdnc.sli.SvcLogicNode;
import org.openecomp.sdnc.sli.SvcLogicRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordNodeExecutor extends SvcLogicNodeExecutor {

	private static final Logger LOG = LoggerFactory
			.getLogger(RecordNodeExecutor.class);
	
	@Override
	public SvcLogicNode execute(SvcLogicServiceImpl svc, SvcLogicNode node,
			SvcLogicContext ctx) throws SvcLogicException {

		String plugin = SvcLogicExpressionResolver.evaluate(
				node.getAttribute("plugin"), node, ctx);
		String outValue = "failure";

		if (LOG.isTraceEnabled()) {
			LOG.trace(node.getNodeType()
					+ " node encountered - looking for recorder class "
					+ plugin);
		}

		Map<String, String> parmMap = new HashMap<String, String>();

		Set<Map.Entry<String, SvcLogicExpression>> parmSet = node
				.getParameterSet();
		boolean hasParms = false;

		for (Iterator<Map.Entry<String, SvcLogicExpression>> iter = parmSet
				.iterator(); iter.hasNext();) {
			hasParms = true;
			Map.Entry<String, SvcLogicExpression> curEnt = iter.next();
			String curName = curEnt.getKey();
			SvcLogicExpression curExpr = curEnt.getValue();
			String curExprValue = SvcLogicExpressionResolver.evaluate(curExpr,
					node, ctx);

			if (LOG.isTraceEnabled()) {
				LOG.trace("executeRecordNode : parameter " + curName + " = "
						+ curExpr + " => " + curExprValue);
			}
			parmMap.put(curName, curExprValue);
		}


			SvcLogicRecorder recorder = getSvcLogicRecorder(plugin);

			if (recorder != null) {

				try {
					recorder.record(parmMap);
				} catch (SvcLogicException e) {
					LOG.error("Caught exception from recorder plugin", e);
					outValue = "failure";
				}
			} else {
				LOG.warn("Could not find SvcLogicRecorder object for plugin "
						+ plugin);
			}

		SvcLogicNode nextNode = node.getOutcomeValue(outValue);
		if (nextNode != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("about to execute " + outValue + " branch");
			}
			return (nextNode);
		}

		nextNode = node.getOutcomeValue("Other");
		if (nextNode != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("about to execute Other branch");
			}
		} else {
			if (LOG.isTraceEnabled()) {
				LOG.trace("no failure or Other branch found");
			}
		}
		return (nextNode);
	}





}
