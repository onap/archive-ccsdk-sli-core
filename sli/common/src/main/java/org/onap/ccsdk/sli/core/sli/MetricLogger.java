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

/**
 *
 */
package org.onap.ccsdk.sli.core.sli;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * @author dt5972
 *
 */
public class MetricLogger {

    private static final Logger METRIC = LoggerFactory.getLogger("org.onap.ccsdk.sli.core.filters.metric");
    private static final Marker INVOKE_RETURN = MarkerFactory.getMarker("INVOKE-RETURN");
    private static final Marker INVOKE = MarkerFactory.getMarker("INVOKE");

    private String lastMsg = null;

    public String getRequestID() {
        return MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
    }
    
    public MetricLogger() {

    }

    public static String asIso8601(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SS'+00:00'");
        df.setTimeZone(tz);
        return df.format(date);
    }

    public static String asIso8601(long tsInMillis) {
        return MetricLogger.asIso8601(new Date(tsInMillis));
    }

    @Deprecated
    public void logRequest(String svcInstanceId, String svcName, String partnerName, String targetEntity,
            String targetServiceName, String targetElement, String msg) {
        logRequest(svcInstanceId, targetEntity, targetServiceName, targetElement, msg);
    }

    public void logRequest(String svcInstanceId, String targetEntity, String targetServiceName, String targetElement,
            String msg) {
        long start = System.currentTimeMillis();
        MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP, MetricLogger.asIso8601(start));

        if (svcInstanceId != null) {
            MDC.put(ONAPLogConstants.MDCs.SERVICE_INSTANCE_ID, svcInstanceId);
        }
        if (targetEntity != null) {
            MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, targetEntity);
        }

        if (targetServiceName != null) {
            MDC.put(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME, targetServiceName);
        }

        if (targetElement != null) {
            MDC.put(ONAPLogConstants.MDCs.TARGET_ELEMENT, targetElement);
        }
        this.lastMsg = msg;
        METRIC.info(INVOKE, "Invoke");
    }
    
    public void logResponse(String statusCode, String responseCode, String responseDescription) {
        long start = System.currentTimeMillis();
        MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP, MetricLogger.asIso8601(start));

        if (statusCode != null) {
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, statusCode);
        }
        if (responseCode != null) {
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, responseCode);
        }
        if (responseDescription != null) {
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION, formatString(responseDescription));
        }
        long end = System.currentTimeMillis();
        MDC.put(ONAPLogConstants.MDCs.LOG_TIMESTAMP, MetricLogger.asIso8601(end));
        MDC.put(ONAPLogConstants.MDCs.ELAPSED_TIME, Long.toString(end - start));
        METRIC.info(INVOKE_RETURN, formatString(lastMsg));
        resetContext();
    }

    protected String formatString(String str) {
        if (str != null) {
            str = str.replaceAll("\\R", ""); // this will strip all new line characters
            str = str.replaceAll("\\|", "%7C"); // log records should not contain a pipe, encode the pipe character
        }
        return str;
    }

    public static void resetContext() {
        MDC.remove(ONAPLogConstants.MDCs.TARGET_ENTITY);
        MDC.remove(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME);
        MDC.remove(ONAPLogConstants.MDCs.TARGET_ELEMENT);
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE);
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_CODE);
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION);
    }
}