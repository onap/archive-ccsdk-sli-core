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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicExpression;
import org.openecomp.sdnc.sli.SvcLogicNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReturnNodeExecutor extends SvcLogicNodeExecutor {

	private static final Logger LOG = LoggerFactory
			.getLogger(ReturnNodeExecutor.class);
	
	@Override
	public SvcLogicNode execute(SvcLogicServiceImpl svc, SvcLogicNode node,
			SvcLogicContext ctx) throws SvcLogicException {

		String status = SvcLogicExpressionResolver.evaluate(
				node.getAttribute("status"), node, ctx);

		if (status != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Returning status " + status);
			}
			ctx.setStatus(status);
		} else {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Return node has no status attribute set");
			}
		}

		Set<Map.Entry<String, SvcLogicExpression>> parameterSet = node
				.getParameterSet();

		for (Iterator<Map.Entry<String, SvcLogicExpression>> iter = parameterSet
				.iterator(); iter.hasNext();) {
			Map.Entry<String, SvcLogicExpression> curEnt = iter.next();
			String curName = curEnt.getKey();
			String curValue = SvcLogicExpressionResolver.evaluate(
					curEnt.getValue(), node, ctx);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Setting context attribute " + curName + " to "
						+ curValue);
			}
			ctx.setAttribute(curName, curValue);
		}
		return null;
	}


}
