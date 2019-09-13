/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                         reserved.
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

package org.onap.ccsdk.sli.core.sli.provider;

import java.util.Properties;
import org.onap.ccsdk.sli.core.api.SvcLogicGraph;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.api.lang.SvcLogicExpressionParser;
import org.onap.ccsdk.sli.core.api.util.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicContextImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.AbstractSvcLogicNodeExecutor;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class OsgiSvcLogicServiceImpl extends SvcLogicServiceImpl implements MdsalSvcLogicService {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiSvcLogicServiceImpl.class);
    protected BundleContext bctx = null;

    public OsgiSvcLogicServiceImpl(SvcLogicStore store, SvcLogicExpressionParser parser, OsgiSvcLogicClassResolver resolver) {
        super(store, parser, resolver);
        this.resolver = resolver;
        this.store = store;
    }

    public void registerExecutor(ServiceReference sr) {
        String nodeName = (String) sr.getProperty("nodeType");
        if (nodeName != null) {
            AbstractSvcLogicNodeExecutor executor;
            try {
                executor = (AbstractSvcLogicNodeExecutor) bctx.getService(sr);
            } catch (Exception e) {
                LOG.error("Cannot get service executor for {}", nodeName, e);
                return;
            }
            registerExecutor(nodeName, executor);
        }
    }

    public void unregisterExecutor(ServiceReference sr) {
        String nodeName = (String) sr.getProperty("nodeType");

        if (nodeName != null) {
            unregisterExecutor(nodeName);
        }
    }

    @Override
    public Properties execute(String module, String rpc, String version, String mode, Properties props)
            throws SvcLogicException {
        return (execute(module, rpc, version, mode, props, null));
    }

    @Override
    public Properties execute(String module, String rpc, String version, String mode, Properties props,
            DOMDataBroker domDataBroker) throws SvcLogicException {
        SvcLogicGraph graph = store.fetch(module, rpc, version, mode);

        if (graph == null) {
            Properties retProps = new Properties();
            retProps.setProperty("error-code", "401");
            retProps.setProperty("error-message",
                    "No service logic found for [" + module + "," + rpc + "," + version + "," + mode + "]");
            return (retProps);
        }

        SvcLogicContextImpl ctx = new SvcLogicContextImpl(props);
        ctx.setAttribute(CURRENT_GRAPH, graph.toString());
        ctx.setAttribute("X-ECOMP-RequestID", MDC.get("X-ECOMP-RequestID"));
        // TODO : decide best way to handle this
        // ctx.setDomDataBroker(domDataBroker);
        execute(graph, ctx);
        return (ctx.toProperties());
    }

    @Override
    public SvcLogicStore getStore() throws SvcLogicException {
        return this.store;
    }

}
