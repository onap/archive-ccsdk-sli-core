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

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicContextImpl;
import org.w3c.dom.Document;
import junit.framework.TestCase;

public class SvcLogicContextTest extends TestCase {
	public void testMerge() {
		
		try {
			InputStream testStr = getClass().getResourceAsStream("/mergetest.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document theDocument = db.parse(testStr);
			SvcLogicContextImpl ctx = new SvcLogicContextImpl();
			ctx.mergeDocument("test-merge", theDocument);
			Properties props = ctx.toProperties();
			for (Enumeration e = props.propertyNames(); e.hasMoreElements() ; ) {
				String propName = (String) e.nextElement();
                System.out.println(propName + " = " + props.getProperty(propName));
				
			}
		} catch (Exception e) {
            e.printStackTrace();
			fail("Caught exception trying to merge");
		}
		
	}

    public void testIsSuccess() {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setStatus(SvcLogicConstants.SUCCESS);
        assertTrue(ctx.isSuccess());
        ctx.setStatus(SvcLogicConstants.FAILURE);
        assertFalse(ctx.isSuccess());
    }

    public void testMarkSuccess() {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.markSuccess();
        assertTrue(ctx.isSuccess());
        assertEquals(SvcLogicConstants.SUCCESS, ctx.getStatus());
    }

    public void testMarkFailed() {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.markFailed();
        assertFalse(ctx.isSuccess());
        assertEquals(SvcLogicConstants.FAILURE, ctx.getStatus());
    }

}
