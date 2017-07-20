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

import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicExpression;
import org.openecomp.sdnc.sli.SvcLogicNode;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReserveNodeExecutor extends SvcLogicNodeExecutor {

	private static final Logger LOG = LoggerFactory
			.getLogger(ReserveNodeExecutor.class);
	
	@Override
	public SvcLogicNode execute(SvcLogicServiceImpl svc, SvcLogicNode node,
			SvcLogicContext ctx) throws SvcLogicException {

		String plugin = SvcLogicExpressionResolver.evaluate(
				node.getAttribute("plugin"), node, ctx);
		String resourceType = SvcLogicExpressionResolver.evaluate(
				node.getAttribute("resource"), node, ctx);
		String key = SvcLogicExpressionResolver.evaluateAsKey(
				node.getAttribute("key"), node, ctx);
		String pfx = SvcLogicExpressionResolver.evaluate(node.getAttribute("pfx"),node,ctx);

		
        SvcLogicExpression selectExpr = node.getAttribute("select");
        String select = null;

        if (selectExpr != null)
        {
                select = SvcLogicExpressionResolver.evaluateAsKey(selectExpr, node, ctx);
        }

		String outValue = "failure";

		if (LOG.isDebugEnabled()) {
			LOG.debug("reserve node encountered - looking for resource class "
					+ plugin);
		}



        SvcLogicResource resourcePlugin = getSvcLogicResource(plugin);

			if (resourcePlugin != null) {

				try {
					switch (resourcePlugin.reserve(resourceType, select, key, pfx, ctx)) {
					case SUCCESS:
						outValue = "success";
						break;
					case NOT_FOUND:
						outValue = "not-found";
						break;
					case FAILURE:
					default:
						outValue = "failure";
					}
				} catch (SvcLogicException e) {
					LOG.error("Caught exception from resource plugin", e);
					outValue = "failure";
				}
			} else {
				LOG.warn("Could not find SvcLogicResource object for plugin "
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
			if (LOG.isDebugEnabled()) {

				LOG.debug("no "+outValue+" or Other branch found");
			}
		}
		return (nextNode);
	}

}
