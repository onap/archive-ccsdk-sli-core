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

package org.onap.ccsdk.sli.core.dblib;

import com.google.common.annotations.VisibleForTesting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.Vector;

import org.onap.ccsdk.sli.core.dblib.propertiesfileresolver.DblibDefaultFileResolver;
import org.onap.ccsdk.sli.core.dblib.propertiesfileresolver.DblibEnvVarFileResolver;
import org.onap.ccsdk.sli.core.dblib.propertiesfileresolver.DblibJREFileResolver;
import org.onap.ccsdk.sli.core.dblib.propertiesfileresolver.DblibKarafRootFileResolver;
import org.onap.ccsdk.sli.core.dblib.propertiesfileresolver.DblibPropertiesFileResolver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for determining the properties file to use and instantiating the <code>DBResourceManager</code>
 * Service.  The priority for properties file resolution is as follows:
 *
 * <ol>
 *     <li>A directory identified by the system environment variable <code>SDNC_CONFIG_DIR</code></li>
 *     <li>The default directory <code>DEFAULT_DBLIB_PROP_DIR</code></li>
 *     <li>A directory identified by the JRE argument <code>dblib.properties</code></li>
 *     <li>A <code>dblib.properties</code> file located in the karaf root directory</li>
 * </ol>
 */
public class DBLIBResourceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DBLIBResourceProvider.class);

    /**
     * The name of the properties file for database configuration
     */
    private static final String DBLIB_PROP_FILE_NAME = "dblib.properties";

    /**
     * Stores a reference to the <code>DBResourceManager</code>
     */
    private ServiceRegistration registration;

    /**
     * A prioritized list of strategies for resolving dblib properties files.
     */
    private Vector<DblibPropertiesFileResolver> dblibPropertiesFileResolvers = new Vector();

    /**
     * Set up the prioritized list of strategies for resolving dblib properties files.
     */
    public DBLIBResourceProvider() {
        dblibPropertiesFileResolvers.add(new DblibEnvVarFileResolver(
                "Using property file (1) from environment variable"
        ));
        dblibPropertiesFileResolvers.add(new DblibDefaultFileResolver(
                "Using property file (1) from default directory"
        ));
        dblibPropertiesFileResolvers.add(new DblibJREFileResolver(
                "Using property file (2) from JRE argument"
        ));
        dblibPropertiesFileResolvers.add(new DblibKarafRootFileResolver(
                "Using property file (4) from karaf root", this));
    }

    /**
     * Called via Aries Blueprint.  Determines the properties file to use, then instantiates the
     * <code>DBResourceManager</code> Service using the appropriate properties file.
     */
    public void init() {
        // determines properties file as according to the priority described in the class header comment
        final File propertiesFile = determinePropertiesFile(this);
        if (propertiesFile != null) {
            try {
                final FileInputStream fileInputStream = new FileInputStream(propertiesFile);
                final Properties properties = new Properties();
                properties.load(fileInputStream);
                final DBResourceManager dbLibService = DBResourceManager.create(properties);
                final String dbLibServiceName = dbLibService.getClass().getName();

                // instantiate the Service based on the given properties
                final BundleContext bundleContext = FrameworkUtil.getBundle(DBLIBResourceProvider.class)
                        .getBundleContext();
                LOG.info("Registering DBResourceManager: {}", dbLibServiceName);
                final String [] serviceArguments = new String[] {
                        dbLibServiceName,
                        DbLibService.class.getName(),
                        javax.sql.DataSource.class.getName() };
                registration = bundleContext.registerService(
                        serviceArguments, dbLibService, null);
            } catch(final FileNotFoundException e) {
                LOG.error("Failed to find properties file: {}", propertiesFile.toString(),
                        new DblibConfigurationException("Failed to find properties file: "
                                + propertiesFile.toString(), e));
            } catch(final IOException e) {
                LOG.error("Failed to load properties for file: {}", propertiesFile.toString(),
                        new DblibConfigurationException("Failed to load properties for file: "
                                + propertiesFile.toString(), e));
            } catch(final Exception e) {
                // TODO: remove this case after refactoring DBResourceManager to throw more specific Exeption(s)
                // Catching "Exception" is bad practice and will cause Sonar warnings.
                LOG.error("Failed to create properties for file: {}", propertiesFile.toString(),
                        new DblibConfigurationException("Could not get initialize database: ", e));
            }
        }
    }

    /**
     * Reports the method chosen for properties resolution to the <code>Logger</code>.
     *
     * @param message Some user friendly message
     * @param fileOptional The file location of the chosen properties file
     * @return the file location of the chosen properties file
     */
    private static File reportSuccess(final String message, final Optional<File> fileOptional) {
        final File file = fileOptional.get();
        LOG.info("{} {}", message, file.getPath());
        return file;
    }

    /**
     * Reports fatal errors.  This is the case in which no properties file could be found.
     *
     * @param message An appropriate fatal error message
     * @param dblibConfigurationException An exception describing what went wrong during resolution
     */
    private static void reportFailure(final String message,
                                      final DblibConfigurationException dblibConfigurationException) {

        LOG.error("{}", message, dblibConfigurationException);
    }

    /**
     * Determines the dblib properties file to use based on the following priority:
     * <ol>
     *     <li>A directory identified by the system environment variable <code>SDNC_CONFIG_DIR</code></li>
     *     <li>The default directory <code>DEFAULT_DBLIB_PROP_DIR</code></li>
     *     <li>A directory identified by the JRE argument <code>dblib.properties</code></li>
     *     <li>A <code>dblib.properties</code> file located in the karaf root directory</li>
     * </ol>
     */
    @VisibleForTesting
    File determinePropertiesFile(final DBLIBResourceProvider dblibResourceProvider) {

        for (final DblibPropertiesFileResolver dblibPropertiesFileResolver : dblibPropertiesFileResolvers) {
            final Optional<File> fileOptional = dblibPropertiesFileResolver.resolveFile(DBLIB_PROP_FILE_NAME);
            if (fileOptional.isPresent()) {
                return reportSuccess(dblibPropertiesFileResolver.getSuccessfulResolutionMessage(), fileOptional);
            }
        }

        reportFailure("Missing configuration properties resource(3)",
                new DblibConfigurationException("Missing configuration properties resource(3): "
                        + DBLIB_PROP_FILE_NAME));
        return null;
    }

    /**
     * Called via Aries Blueprint
     */
    public void destroy() {
        final BundleContext bundleContext =
                FrameworkUtil.getBundle(DBLIBResourceProvider.class).getBundleContext();
        if (registration != null) {
            final ServiceReference dblibServiceReference =
                    bundleContext.getServiceReference(DbLibService.class.getName());
            if (dblibServiceReference == null) {
                LOG.warn("Could not find service reference for DBLIB service ({})", DbLibService.class.getName());
            } else {
                try {
                    final DBResourceManager dblibResourceManagerServiceReference =
                            (DBResourceManager) bundleContext.getService(dblibServiceReference);
                    if (dblibResourceManagerServiceReference == null) {
                        LOG.warn("Could not find service reference for DBLIB service ({})",
                                DbLibService.class.getName());
                    } else {
                        dblibResourceManagerServiceReference.cleanUp();
                    }
                } catch (final ClassCastException e) {
                    LOG.warn("Could not find service reference for DBLIB service ({})",
                            DBResourceManager.class.getName());
                }
            }
            registration.unregister();
        }
    }

}
