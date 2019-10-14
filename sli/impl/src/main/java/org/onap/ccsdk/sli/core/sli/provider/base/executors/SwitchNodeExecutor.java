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

package org.onap.ccsdk.sli.core.sli.provider.base.executors;

import org.onap.ccsdk.sli.core.api.SvcLogicContext;
import org.onap.ccsdk.sli.core.api.SvcLogicNode;
import org.onap.ccsdk.sli.core.api.SvcLogicService;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwitchNodeExecutor extends AbstractSvcLogicNodeExecutor {

	private static final Logger LOG = LoggerFactory
			.getLogger(SwitchNodeExecutor.class);
	
	@Override

	public SvcLogicNode execute(SvcLogicService svc, SvcLogicNode node,
			SvcLogicContext ctx) throws SvcLogicException {


		String testResult = evaluateNodeTest(node, ctx);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Executing switch node");
			

			LOG.debug("test expression (" + node.getAttribute("test")
					+ ") evaluates to " + testResult);
		}

		SvcLogicNode nextNode = node.getOutcomeValue(testResult);

		if (LOG.isDebugEnabled()) {
			if (nextNode != null) {
                LOG.debug("Next node to execute is node " + nextNode.getNodeId());
			} else {
				LOG.debug("No next node found");
			}
		}
		return (nextNode);

	}
}
