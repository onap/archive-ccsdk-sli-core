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

package org.onap.ccsdk.sli.core.utils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
<<<<<<< HEAD
import java.nio.file.Files;
=======
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
>>>>>>> 99f06c7... Update sli/core for Nitrogen
import java.util.Optional;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.LoggerFactory;

/**
 * Resolves project properties files relative to the directory identified by the JRE property
 * <code>dblib.properties</code>.
 */
public class JREFileResolver implements PropertiesFileResolver {

    /**
     * Key for JRE argument representing the configuration directory
     */

    private final String successMessage;
    private final Class<?> clazz;

    public JREFileResolver(final String successMessage, final Class<?> clazz) {
        this.successMessage = successMessage;
        this.clazz = clazz;
    }

    /**
     * Parse a properties file location based on JRE argument
     *
     * @return an Optional File containing the location if it exists, or an empty Optional
     */
    @Override
    public Optional<File> resolveFile(final String filename) {
<<<<<<< HEAD
        final Bundle bundle = FrameworkUtil.getBundle(this.clazz);
        final File dataFile;

=======
        final URL jreArgumentUrl = FrameworkUtil.getBundle(this.clazz)
                .getResource(filename);
>>>>>>> 99f06c7... Update sli/core for Nitrogen
        try {
            if (bundle == null) {
                return Optional.empty();
            }

            URL jreArgumentEntry = bundle.getEntry(filename);
            if (jreArgumentEntry == null) {
                return Optional.empty();
            }
<<<<<<< HEAD

            dataFile = bundle.getDataFile(filename);
            if(dataFile.exists()) {
                dataFile.delete();
            }

            try (InputStream input = jreArgumentEntry.openStream()){
                Files.copy(input, dataFile.toPath());
            } catch(Exception exc) {
                return Optional.empty();
            }

            return Optional.of(dataFile);
        } catch(final Exception e) {
=======
            final Path dblibPath = Paths.get(jreArgumentUrl.toURI());
            return Optional.of(dblibPath.resolve(filename).toFile());
        } catch(final URISyntaxException | FileSystemNotFoundException e) {
            LoggerFactory.getLogger(this.getClass()).error("", e);
>>>>>>> 99f06c7... Update sli/core for Nitrogen
            return Optional.empty();
        }
    }

    @Override
    public String getSuccessfulResolutionMessage() {
        return this.successMessage;
    }
}
