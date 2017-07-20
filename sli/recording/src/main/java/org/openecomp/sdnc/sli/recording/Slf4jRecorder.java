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

package org.openecomp.sdnc.sli.recording;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.openecomp.sdnc.sli.ConfigurationException;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jRecorder implements SvcLogicRecorder {
	
	
	public enum Level {
		ERROR,
		WARN,
		INFO,
		DEBUG,
		TRACE
	}

	@Override
	public void record(Map<String, String> parmMap) throws SvcLogicException {
		String loggerName = parmMap.get("logger");
		if (loggerName == null) {
			loggerName = "Log4jRecorder";
		}
		
		String lvl = parmMap.get("level");
		if (lvl == null) {
			lvl = "INFO";
		}

		Level level = Level.INFO;
		
		try {
			level = Level.valueOf(lvl.toUpperCase());
		} catch (Exception e) {}
		
		
		
		String record = parmMap.get("record");
		if (record == null)
		{
			String delimiter = parmMap.get("delimiter");
			if (delimiter == null)
			{
				delimiter = "|";
			}
			
			int idx = 1;
			boolean moreFields = true;
			while (moreFields)
			{
				String curField = parmMap.get("field"+idx++);
				if (curField == null)
				{
					moreFields = false;
				}
				else
				{
					if (record == null)
					{
						record = delimiter;
					}
					record = record + curField + delimiter;
				}
			}
		}
		
		if (record == null)
		{
			throw new ConfigurationException("No record/fields passed in record node");
		}
		
		Logger logger = LoggerFactory.getLogger(loggerName);

		Date now = new Date();
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat dateFmt = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss:SS'+00:00'");
		dateFmt.setTimeZone(tz);
		if (record.indexOf("__TIMESTAMP__") != -1)
		{
			record = record.replaceFirst("__TIMESTAMP__", dateFmt.format(now));
		}
		
		switch (level) {
		case ERROR:
			logger.error(record);
			break;
		case WARN:
			logger.warn(record);
			break;
		case INFO:
			logger.info(record);
			break;
		case DEBUG:
			logger.debug(record);
			break;
		case TRACE:
			logger.trace(record);
		}
	}

}
