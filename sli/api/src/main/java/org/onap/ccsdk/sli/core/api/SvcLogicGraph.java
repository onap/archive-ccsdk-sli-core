/*-
x * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 * Modifications copyright (C) 2017 AT&T Intellectual Property. All rights
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

package org.onap.ccsdk.sli.core.api;

import java.io.PrintStream;
import java.io.Serializable;
import org.onap.ccsdk.sli.core.api.exceptions.DuplicateValueException;

public interface SvcLogicGraph {
    String getMd5sum();

    void setMd5sum(String md5sum);

    String getModule();

    void setModule(String module);

    String getRpc();

    void setRpc(String rpc);

    String getMode();

    void setMode(String mode);

    String getVersion();

    void setVersion(String version);

    void setRootNode(SvcLogicNode rootNode);

    SvcLogicNode getRootNode();

    Serializable getAttribute(String name);

    void setAttribute(String name, Serializable value) throws DuplicateValueException;

    SvcLogicNode getNamedNode(String nodeName);

    void setNamedNode(String nodeName, SvcLogicNode node) throws DuplicateValueException;

    void printAsXml(PrintStream out);

    void printAsGv(PrintStream out);
}
