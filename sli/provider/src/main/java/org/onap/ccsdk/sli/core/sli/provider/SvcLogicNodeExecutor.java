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

package org.onap.ccsdk.sli.core.sli.provider;

import org.onap.ccsdk.sli.core.sli.MetricLogger;
import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SvcLogicNodeExecutor {

    public abstract SvcLogicNode execute(SvcLogicServiceImpl svc, SvcLogicNode node, SvcLogicContext ctx)
            throws SvcLogicException;

    private static final Logger LOG = LoggerFactory.getLogger(SvcLogicNodeExecutor.class);

    protected String evaluateNodeTest(SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException {
        if (node == null) {
            return null;
        }

        return (SvcLogicExpressionResolver.evaluate(node.getAttribute("test"), node, ctx));

    }

    protected SvcLogicStore getStore() throws SvcLogicException {
        return SvcLogicActivator.getStore();
    }

    protected SvcLogicAdaptor getAdaptor(String adaptorName) {
        return SvcLogicAdaptorFactory.getInstance(adaptorName);
    }

    protected SvcLogicResource getSvcLogicResource(String plugin) {
        BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

        ServiceReference sref = bctx.getServiceReference(plugin);
        if (sref != null) {
            SvcLogicResource resourcePlugin = (SvcLogicResource) bctx.getService(sref);
            return resourcePlugin;
        } else {
            LOG.warn("Could not find service reference object for plugin " + plugin);
            return null;
        }
    }

    protected SvcLogicRecorder getSvcLogicRecorder(String plugin) {
        BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

        ServiceReference sref = bctx.getServiceReference(plugin);
        if (sref != null) {
            SvcLogicRecorder resourcePlugin = (SvcLogicRecorder) bctx.getService(sref);
            return resourcePlugin;
        } else {
            return null;
        }
    }

    protected SvcLogicJavaPlugin getSvcLogicJavaPlugin(String pluginName) {
        BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

        ServiceReference sref = bctx.getServiceReference(pluginName);

        if (sref == null) {
            LOG.warn("Could not find service reference object for plugin " + pluginName);
            return null;
        } else {
            SvcLogicJavaPlugin plugin = (SvcLogicJavaPlugin) bctx.getService(sref);
            return plugin;
        }
    }

    protected SvcLogicNode getNextNode(SvcLogicNode node, String outValue) {
        MetricLogger.resetContext();
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
                LOG.debug("no " + outValue + " or Other branch found");
            }
        }
        return (nextNode);
    }

}
