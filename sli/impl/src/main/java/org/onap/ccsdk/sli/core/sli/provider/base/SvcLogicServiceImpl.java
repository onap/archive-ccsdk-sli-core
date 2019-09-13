/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 *  Modifications Copyright (C) 2018 IBM.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.onap.ccsdk.sli.core.api.SvcLogicContext;
import org.onap.ccsdk.sli.core.api.SvcLogicGraph;
import org.onap.ccsdk.sli.core.api.SvcLogicNode;
import org.onap.ccsdk.sli.core.api.SvcLogicService;
import org.onap.ccsdk.sli.core.api.exceptions.ExitNodeException;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.api.lang.SvcLogicExpressionParser;
import org.onap.ccsdk.sli.core.api.util.SvcLogicResolver;
import org.onap.ccsdk.sli.core.api.util.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.AbstractSvcLogicNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.BlockNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.BreakNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.CallNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ConfigureNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.DeleteNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ExecuteNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ExistsNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ExitNodeExecutor;
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
import org.slf4j.MDC;

public class SvcLogicServiceImpl implements SvcLogicService {
    protected SvcLogicResolver resolver;
    protected SvcLogicExpressionParser parser;

    protected final Map<String, AbstractSvcLogicNodeExecutor> BUILTIN_NODES =
            new HashMap<String, AbstractSvcLogicNodeExecutor>() {
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
                    put("set", new SetNodeExecutor(parser));
                    put("switch", new SwitchNodeExecutor());
                    put("update", new UpdateNodeExecutor());
                    put("break", new BreakNodeExecutor());
                    put("while", new WhileNodeExecutor());
                    put("exit", new ExitNodeExecutor());
                }
            };

    private static final Logger LOG = LoggerFactory.getLogger(SvcLogicServiceImpl.class);
    protected HashMap<String, AbstractSvcLogicNodeExecutor> nodeExecutors = null;
    protected Properties properties;
    protected SvcLogicStore store;
    protected static final String CURRENT_GRAPH = "currentGraph";

    public SvcLogicServiceImpl(SvcLogicStore store, SvcLogicExpressionParser parser, SvcLogicResolver resolver) {
        this.store = store;
        this.parser = parser;
        this.resolver = resolver;
    }

    protected void registerExecutors() {
        for (String nodeType : BUILTIN_NODES.keySet()) {
            registerExecutor(nodeType, BUILTIN_NODES.get(nodeType));
        }
    }

    public void registerExecutor(String nodeName, AbstractSvcLogicNodeExecutor executor) {
        if (nodeExecutors == null) {
            nodeExecutors = new HashMap<>();
        }
        executor.setResolver(resolver);
        nodeExecutors.put(nodeName, executor);
    }

    public void unregisterExecutor(String nodeName) {
        nodeExecutors.remove(nodeName);
    }

    public SvcLogicContext execute(SvcLogicGraph graph, SvcLogicContext ctx) throws SvcLogicException {
        if (nodeExecutors == null) {
            registerExecutors();
        }

        MDC.put(CURRENT_GRAPH, graph.toString());

        SvcLogicNode curNode = graph.getRootNode();
        LOG.info("About to execute graph {}", graph.toString());
        try {
            while (curNode != null) {
                SvcLogicNode nextNode = executeNode(curNode, ctx);
                curNode = nextNode;
            }
        } catch (ExitNodeException e) {
            LOG.debug("SvcLogicServiceImpl caught ExitNodeException");
        }
        MDC.remove("nodeId");
        MDC.remove(CURRENT_GRAPH);

        return (ctx);
    }

    public SvcLogicNode executeNode(SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException {
        if (node == null) {
            return (null);
        }

        LOG.info("About to execute node #{} {} node in graph {}", node.getNodeId(), node.getNodeType(),
                node.getGraph().toString());

        AbstractSvcLogicNodeExecutor executor = nodeExecutors.get(node.getNodeType());

        if (executor != null) {
            MDC.put("nodeId", node.getNodeId() + " (" + node.getNodeType() + ")");
            return (executor.execute(this, node, ctx));
        } else {
            throw new SvcLogicException("Attempted to execute a node of type " + node.getNodeType()
                    + ", but no executor was registered for this type");
        }
    }

    @Override
    public boolean hasGraph(String module, String rpc, String version, String mode) throws SvcLogicException {
        return (store.hasGraph(module, rpc, version, mode));
    }

    @Override
    public Properties execute(String module, String rpc, String version, String mode, Properties props)
            throws SvcLogicException {
        LOG.info("Fetching service logic from data store");
        SvcLogicGraph graph = store.fetch(module, rpc, version, mode);

        if (graph == null) {
            Properties retProps = new Properties();
            retProps.setProperty("error-code", "401");
            retProps.setProperty("error-message",
                    "No service logic found for [" + module + "," + rpc + "," + version + "," + mode + "]");
            return (retProps);
        }

        SvcLogicContext ctx = new SvcLogicContextImpl(props);
        ctx.setAttribute(CURRENT_GRAPH, graph.toString());
        ctx.setAttribute("X-ECOMP-RequestID", MDC.get("X-ECOMP-RequestID"));
        execute(graph, ctx);
        return (ctx.toProperties());
    }

    @Override
    public SvcLogicStore getStore() throws SvcLogicException {
        return this.store;
    }

}