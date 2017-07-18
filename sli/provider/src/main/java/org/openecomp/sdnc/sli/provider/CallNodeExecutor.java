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
import org.openecomp.sdnc.sli.SvcLogicGraph;
import org.openecomp.sdnc.sli.SvcLogicNode;
import org.openecomp.sdnc.sli.SvcLogicStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallNodeExecutor extends SvcLogicNodeExecutor {

	private static final Logger LOG = LoggerFactory
			.getLogger(CallNodeExecutor.class);
	
	@Override
	public SvcLogicNode execute(SvcLogicServiceImpl svc, SvcLogicNode node, SvcLogicContext ctx)
			throws SvcLogicException {

		String outValue = "not-found";
		
		SvcLogicGraph myGraph = node.getGraph();
		
		if (myGraph == null)
		{
			LOG.debug("execute: getGraph returned null");
		}
		else
		{
			LOG.debug("execute: got SvcLogicGraph");
		}
		
		SvcLogicExpression moduleExpr = null;
		
		String module = null;
		
		moduleExpr = node.getAttribute("module");
		if (moduleExpr != null)
		{
			module  = SvcLogicExpressionResolver.evaluate(moduleExpr, node, ctx);
		}
		
		if ((module == null) || (module.length() == 0))
		{
			if (myGraph != null)
			{
				module = myGraph.getModule();
				LOG.debug("myGraph.getModule() returned "+module);
			}
		}
		
		SvcLogicExpression rpcExpr = null;
		String rpc = null;
		rpcExpr = node.getAttribute("rpc");
		if (rpcExpr != null)
		{
			rpc  = SvcLogicExpressionResolver.evaluate(rpcExpr, node, ctx);
		}
		
		if ((rpc == null) || (rpc.length() == 0))
		{
			if (myGraph != null)
			{
				rpc = myGraph.getRpc();
				LOG.debug("myGraph.getRpc() returned "+rpc);
			}
		}
		
		String mode = null;
		
		moduleExpr = node.getAttribute("mode");
		if (moduleExpr != null)
		{
			mode  = SvcLogicExpressionResolver.evaluate(moduleExpr, node, ctx);
		}

		if ((mode == null) || (mode.length() == 0))
		{
			if (myGraph != null)
			{
				mode = myGraph.getMode();

				LOG.debug("myGraph.getMode() returned "+mode);
			}
		}
		
		String version = null;
		
		moduleExpr = node.getAttribute("version");
		if (moduleExpr != null)
		{
			version  = SvcLogicExpressionResolver.evaluate(moduleExpr, node, ctx);
		}

		String parentGraph = ctx.getAttribute("currentGraph");
        ctx.setAttribute("parentGraph", parentGraph);
		
		SvcLogicStore store = getStore();
		
        if (store != null) {
			SvcLogicGraph calledGraph = store.fetch(module, rpc, version, mode);
            LOG.debug("Parent " + parentGraph + " is calling child " + calledGraph.toString());
            ctx.setAttribute("currentGraph", calledGraph.toString());
            if (calledGraph != null) {
				svc.execute(calledGraph, ctx);
				
				outValue = ctx.getStatus();
            } else {
                LOG.error("Could not find service logic for [" + module + "," + rpc + "," + version + "," + mode + "]");
			}
		}
		else
		{
			LOG.debug("Could not get SvcLogicStore reference");
		}
		
		SvcLogicNode nextNode = node.getOutcomeValue(outValue);
		if (nextNode != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("about to execute " + outValue + " branch");
			}
            ctx.setAttribute("currentGraph", parentGraph);
			return (nextNode);
		}

		nextNode = node.getOutcomeValue("Other");
		if (nextNode != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("about to execute Other branch");
			}
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("no " + outValue + " or Other branch found");
			}
		}
        ctx.setAttribute("currentGraph", parentGraph);
        ctx.setAttribute("parentGraph", null);

		return (nextNode);

	}

}
