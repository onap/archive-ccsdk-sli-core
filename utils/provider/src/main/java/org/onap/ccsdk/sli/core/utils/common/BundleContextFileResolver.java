/*-
 * ============LICENSE_START=======================================================
 * onap
 * ================================================================================
 * Copyright (C) 2016 - 2017 ONAP
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
import org.osgi.framework.FrameworkUtil;

import com.google.common.base.Strings;

/**
 * Resolves properties files from runtime property value <code>SDNC_CONFIG_DIR</code> defined in the osgi properties.
 */
public class BundleContextFileResolver extends DefaultFileResolver {

    /**
     * Key for osgi variable representing the configuration directory
     */
    private static final String SDNC_CONFIG_DIR_PROP_KEY = "SDNC_CONFIG_DIR";
    /**
     * Default path to look for the configuration directory
     */
    private static final Path DEFAULT_DBLIB_PROP_DIR = Paths.get("/opt", "sdnc", "data", "properties");

    public BundleContextFileResolver(final String successMessage, final Class<?> clazz) {
        super(successMessage, getPropertiesPath(clazz, SDNC_CONFIG_DIR_PROP_KEY));
    }

    private static Path getPropertiesPath(Class<?> clazz, String osgiPropertyKey) {
        if(FrameworkUtil.getBundle(clazz) == null) {
            return DEFAULT_DBLIB_PROP_DIR;
        } else {
            String path = FrameworkUtil.getBundle(clazz).getBundleContext().getProperty(osgiPropertyKey);
            if(Strings.isNullOrEmpty(path)) {
                return DEFAULT_DBLIB_PROP_DIR;
            }
            return Paths.get(path);
        }

    }
}
