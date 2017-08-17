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

import com.google.common.base.Strings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBLIBResourceProvider {

	private static final Logger LOG = LoggerFactory.getLogger(DBLIBResourceProvider.class);

	private static final String SDNC_CONFIG_DIR_PROP_KEY = "SDNC_CONFIG_DIR";

	private static final String DBLIB_JRE_PROPERTY_KEY = "dblib.properties";

	private static final Path DEFAULT_DBLIB_PROP_DIR = Paths.get("opt", "sdnc", "data", "properties");

	private static final String DBLIB_PROP_PATH = "dblib.properties";

	private ServiceRegistration registration;

	public void init() {
		final File propertiesFile = determinePropertiesFile(this);
		if (propertiesFile != null) {
			try {
				final FileInputStream fileInputStream = new FileInputStream(propertiesFile);
				final Properties properties = new Properties();
				properties.load(fileInputStream);
				final DBResourceManager dbLibService = DBResourceManager.create(properties);
				final String dbLibServiceName = dbLibService.getClass().getName();

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
				LOG.error("Failed to create properties for file: {}", propertiesFile.toString(),
						new DblibConfigurationException("Could not get initialize database: ", e));
			}
		}
	}

	private static Optional<File> fileFromJreArgument() {
		final URL jreArgumentUrl = FrameworkUtil.getBundle(DBLIBResourceProvider.class)
				.getResource(DBLIB_JRE_PROPERTY_KEY);
		try {
			if (jreArgumentUrl == null) {
				return Optional.empty();
			}
			final Path dblibPath = Paths.get(jreArgumentUrl.toURI());
			return Optional.of(dblibPath.resolve(DBLIB_PROP_PATH).toFile());
		} catch(final URISyntaxException e) {
			return Optional.empty();
		}
	}


	private static Optional<File> fileFromEnvVariable() {
		// attempt to resolve the property directory from the corresponding environment variable
		final String propDirectoryFromEnvVariable = System.getenv(SDNC_CONFIG_DIR_PROP_KEY);
		final File fileFromEnvVariable;
		if (!Strings.isNullOrEmpty(propDirectoryFromEnvVariable)) {
			fileFromEnvVariable = Paths.get(propDirectoryFromEnvVariable).resolve(DBLIB_PROP_PATH).toFile();
			if(fileFromEnvVariable.exists()) {
				return Optional.of(fileFromEnvVariable);
			}
		}
		return Optional.empty();
	}

	private static Optional<File> fileFromDefaultDir() {
		final File fileFromDefaultDblibDir = DEFAULT_DBLIB_PROP_DIR.resolve(DBLIB_PROP_PATH).toFile();
		if (fileFromDefaultDblibDir.exists()) {
			Optional.of(fileFromDefaultDblibDir);
		}
		return Optional.empty();
	}

	private static Optional<File> fromKarafRootDir(final DBLIBResourceProvider dblibResourceProvider) {
		final URL fromKarafRoot = dblibResourceProvider.getClass().getResource(DBLIB_PROP_PATH);
		if (fromKarafRoot != null) {
			final File propertiesFile = new File(fromKarafRoot.getFile());
			if (propertiesFile.exists()) {
				return Optional.of(propertiesFile);
			}
			return Optional.empty();
		}
		return Optional.empty();
	}

	private static File reportSuccess(final String message, final Optional<File> fileOptional) {
		final File file = fileOptional.get();
		LOG.info("{} {}", message, file.getPath());
		return file;
	}

	private static void reportFailure(final String message,
									  final DblibConfigurationException dblibConfigurationException) {

		LOG.error("{}", message, dblibConfigurationException);
	}

	/**
	 * <ol>
	 *     <li>System Environment Variable</li>
	 *     <li>DEFAULT_DBLIB_PROP_DIR</li>
	 *     <li>JRE argument identified by <code>dblib.properties</code></li>
	 * </ol>
	 */
	private static File determinePropertiesFile(final DBLIBResourceProvider dblibResourceProvider) {

		final Optional<File> fromEnvVariableOptional = fileFromEnvVariable();
		if (fromEnvVariableOptional.isPresent()) {
			return reportSuccess("Using property file (1) from environment variable", fromEnvVariableOptional);
		}

		final Optional<File> fromDefaultDirOptional = fileFromDefaultDir();
		if (fromDefaultDirOptional.isPresent()) {
			return reportSuccess("Using property file (1) from default directory", fromDefaultDirOptional);
		}

		final Optional<File> fromJreArgumentOptional = fileFromJreArgument();
		if (fromJreArgumentOptional.isPresent()) {
			return reportSuccess("Using property file (2) from JRE argument", fromJreArgumentOptional);
		}

		final Optional<File> fromKarafRootOptional = fromKarafRootDir(dblibResourceProvider);
		if (fromKarafRootOptional.isPresent()) {
			return reportSuccess("Using property file (4) from karaf root", fromKarafRootOptional);
		}

		reportFailure("Missing configuration properties resource(3)",
				new DblibConfigurationException("Missing configuration properties resource(3): "
						+ DBLIB_PROP_PATH));
		return null;
	}

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
