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
import org.onap.ccsdk.sli.core.api.extensions.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicExpressionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReleaseNodeExecutor extends AbstractSvcLogicNodeExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(ReleaseNodeExecutor.class);

    @Override
    public SvcLogicNode execute(SvcLogicService svc, SvcLogicNode node, SvcLogicContext ctx)
            throws SvcLogicException {

        String plugin = SvcLogicExpressionResolver.evaluate(node.getAttribute("plugin"), node, ctx);
        String resourceType = SvcLogicExpressionResolver.evaluate(node.getAttribute("resource"), node, ctx);
        String key = SvcLogicExpressionResolver.evaluateAsKey(node.getAttribute("key"), node, ctx);

        String outValue = "failure";

        if (LOG.isDebugEnabled()) {
            LOG.debug("release node encountered - looking for resource class " + plugin);
        }

        SvcLogicResource resourcePlugin = getSvcLogicResource(plugin);
        if (resourcePlugin != null) {
            try {
                switch (resourcePlugin.release(resourceType, key, ctx)) {
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
            LOG.warn("Could not find SvcLogicResource object for plugin " + plugin);
        }
        return (getNextNode(node, outValue));
    }

}