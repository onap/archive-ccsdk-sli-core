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

package org.onap.ccsdk.sli.core.sli.provider.base;

import org.onap.ccsdk.sli.core.api.SvcLogicContext;
import org.onap.ccsdk.sli.core.api.SvcLogicNode;
import org.onap.ccsdk.sli.core.api.SvcLogicServiceBase;
import org.onap.ccsdk.sli.core.api.exceptions.ExitNodeException;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExitNodeExecutor extends AbstractSvcLogicNodeExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(ExitNodeExecutor.class);

    @Override
    public SvcLogicNode execute(SvcLogicServiceBase svc, SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException {
        String message = "ExitNodeExecutor encountered exit with nodeId " + node.getNodeId();
        LOG.debug(message);
        throw new ExitNodeException(message);
    }

}
