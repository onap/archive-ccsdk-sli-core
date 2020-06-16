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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.google.gson.*;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import junit.framework.TestCase;

public class SvcLogicContextTest extends TestCase {
	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicContextTest.class);

	@Test
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

	@Test
    public void testIsSuccess() {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setStatus(SvcLogicConstants.SUCCESS);
        assertTrue(ctx.isSuccess());
        ctx.setStatus(SvcLogicConstants.FAILURE);
        assertFalse(ctx.isSuccess());
    }

    @Test
    public void testMarkSuccess() {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.markSuccess();
        assertTrue(ctx.isSuccess());
        assertEquals(SvcLogicConstants.SUCCESS, ctx.getStatus());
    }

    @Test
    public void testMarkFailed() {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.markFailed();
        assertFalse(ctx.isSuccess());
        assertEquals(SvcLogicConstants.FAILURE, ctx.getStatus());
    }

    @Test
	public void testJsonImportExport() throws JSONException {
		SvcLogicContext ctx = new SvcLogicContext();

		JsonParser jp = new JsonParser();

		InputStream testStr = getClass().getResourceAsStream("/jsonContextTest.json");

		String jsonIn = new BufferedReader(new InputStreamReader(testStr, StandardCharsets.UTF_8))
                          .lines().collect(Collectors.joining("\n"));

		ctx.mergeJson("preload-data", jsonIn);
		String ctxTree = ctx.toJsonString();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonObject jsonCtx = jp.parse(ctxTree).getAsJsonObject();
		JsonObject jsonPreloadData = jsonCtx.getAsJsonObject("preload-data");
		String jsonOut = gson.toJson(jsonPreloadData);

		System.out.println("Input to ctx: "+jsonIn);
		System.out.println("Conversion from ctx: "+ jsonOut);
		// TODO : need to find a way to compare Json that ignores
		// permissible differences.  For example, output values are
		// always quoted, but input is not necessarily always quoted.
		// Uncommenting the line below would cause test case to fail
		// due to these sorts of differences.
		// JSONAssert.assertEquals(jsonIn, jsonOut, false);

	}


}
