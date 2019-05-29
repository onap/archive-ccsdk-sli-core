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

package org.onap.ccsdk.sli.core.sli;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public interface SvcLogicContextApi {
	public String getAttribute(String name);

	public void setAttribute(String name, String value);

	public Set<String> getAttributeKeySet();

	public String getStatus();

	public void setStatus(String status);

	public Properties toProperties();

	public void mergeDocument(String pfx, Document doc);

	public void mergeElement(String pfx, Element element, Map<String, Integer> nodeMap);

	public String resolve(String ctxVarName);

}
