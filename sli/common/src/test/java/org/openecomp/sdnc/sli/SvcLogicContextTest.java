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

package org.openecomp.sdnc.sli;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openecomp.sdnc.sli.SvcLogicContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import junit.framework.TestCase;

public class SvcLogicContextTest extends TestCase {
	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicContext.class);
	
	public void testMerge() {
		
		try {
			InputStream testStr = getClass().getResourceAsStream("/mergetest.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document theDocument = db.parse(testStr);
			SvcLogicContext ctx = new SvcLogicContext();
			ctx.mergeDocument("test-merge", theDocument);
			Properties props = ctx.toProperties();
			LOG.info("SvcLogicContext contains the following : ");
			for (Enumeration e = props.propertyNames(); e.hasMoreElements() ; ) {
				String propName = (String) e.nextElement();
				LOG.info(propName+" = "+props.getProperty(propName));
				
			}
		} catch (Exception e) {
			LOG.error("Caught exception trying to merge", e);
			fail("Caught exception trying to merge");
		}
		
	}

}
