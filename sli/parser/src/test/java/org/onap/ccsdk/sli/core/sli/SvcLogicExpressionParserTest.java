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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.Test;
import org.onap.ccsdk.sli.core.api.lang.SvcLogicExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import junit.framework.TestCase;

public class SvcLogicExpressionParserTest extends TestCase {

	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicExprListener.class);
    /*
     * # $uni-circuit-id = abc123 # $uni-cir-units = 10 # value = 1 # $arg1 = 2 # $network.name = vCE0001.in #
     * $network.segment[0].provider-segmentation-id = 1212 # $network.segment[1].provider-segmentation-id = 1213 #
     * $availability-zone = mtsnj-esx-az01 length($uni-circuit-id) > 0 # true $uni-cir-units * 1000 * 100 / 100 # 10000
     * $uni-cir-units / 1000 # 0 $uni-cir-units - 100 # -90 $uni-cir-units + 100 # 110 ($value * 3 - $arg1 > 0) and
     * (length($uni-circuit-id) == 0) # false 'pg-'+$network.name # pg-vCE0001.in
     * $network.segment[0].provider-segmentation-id # 1212 toUpperCase($network.name) # VCE0001.IN
     * toLowerCase($network.name) # vce0001.in toUpperCase(substr($availability-zone, 0, 5)) # MTSNJ convertBase(1234,
     * 10) # 1234 convertBase(10, 16, 10) # 16 convertBase(ZZ, 36, 10) # 1295 convertBase(10, 10, 36) # a (0 - 1) *
     * $arg1 # -2
     * 
     */
    SvcLogicExpressionParserImpl parser = new SvcLogicExpressionParserImpl();

    @Test
    public void simpleValue() {

    }
	public void testParse() {
		try
		{
			InputStream testStr = getClass().getResourceAsStream("/expression.tests");
			BufferedReader testsReader = new BufferedReader(new InputStreamReader(testStr));
			String testExpr = null;
			while ((testExpr = testsReader.readLine()) != null) {
				
                SvcLogicExpression parsedExpr = parser.parse(testExpr);
				if (parsedExpr == null)
				{
					fail("parse("+testExpr+") returned null");
				}
				else
				{
					LOG.info("test expression = ["+testExpr+"] ; parsed expression = ["+parsedExpr.asParsedExpr()+"]");
					
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Caught exception processing test cases");
		}
	}

}
