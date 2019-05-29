/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

/**
 * 
 */
package org.onap.ccsdk.sli.core.sli;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

public interface SvcLogicNode {
	public int getNodeId();
	
	public String getNodeName();
	
	public String getNodeType();
	
	public SvcLogicGraph getGraph();
	
	public int getNumOutcomes();
	
	public SvcLogicExpression getAttribute(String name);
	
	public void setAttribute(String name, String value) throws SvcLogicException;
	
	public void setAttribute(String name, SvcLogicExpression value) throws SvcLogicException;
	
	public void mapParameter(String name, String value) throws SvcLogicException;
	
	public SvcLogicExpression getParameter(String name);
	
	public boolean isVisited();

	public void setVisited(boolean visited, boolean recursive);
	
	public void addOutcome(String outcomeValue, SvcLogicNode node) throws SvcLogicException;
	
	public Set<Map.Entry<String, SvcLogicNode>> getOutcomeSet();
	//TODO : this was static check for impact
	public Set<Map.Entry<String, SvcLogicExpression>> getParameterSet();
	//TODO : review if this is needed
	public void printAsGv(PrintStream pstr);
	// TODO : review if this is needed
	public void printAsXml(PrintStream pstr, int indentLvl);

	public SvcLogicNode getOutcomeValue(String value);
}
