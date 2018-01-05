/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights
 *                         reserved.
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

package org.onap.ccsdk.sli.core.utils.common;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.onap.ccsdk.sli.core.utils.DefaultFileResolver;

/**
 * Resolve properties file location based on the default directory name.
 */
public class CoreDefaultFileResolver extends DefaultFileResolver {

    /**
     * Default path to look for the configuration directory
     */
    private static final Path DEFAULT_DBLIB_PROP_DIR = Paths.get("/opt", "sdnc", "data", "properties");

    public CoreDefaultFileResolver(final String successMessage) {
        super(successMessage, DEFAULT_DBLIB_PROP_DIR);
    }
}
