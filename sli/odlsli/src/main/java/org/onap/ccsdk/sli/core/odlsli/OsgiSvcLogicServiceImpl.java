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

package org.onap.ccsdk.sli.core.odlsli;

import java.util.Properties;
import org.onap.ccsdk.sli.core.api.SvcLogicGraph;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.api.util.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicContextImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceImpl;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsgiSvcLogicServiceImpl extends SvcLogicServiceImpl implements MdsalSvcLogicService {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiSvcLogicServiceImpl.class);
    protected BundleContext bctx = null;

    public OsgiSvcLogicServiceImpl(SvcLogicStore store,
            OsgiSvcLogicClassResolver resolver) {
        super(store, resolver);
        this.resolver = resolver;
        this.store = store;
    }

    @Override
    @Deprecated
    // DomDataBroker is not being used, this should be removed eventually
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
