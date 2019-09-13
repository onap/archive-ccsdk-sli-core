package org.onap.ccsdk.sli.core.odlsli;


import java.util.HashMap;
import org.onap.ccsdk.sli.core.api.extensions.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.api.extensions.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.api.extensions.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.api.extensions.SvcLogicResource;
import org.onap.ccsdk.sli.core.api.util.SvcLogicResolver;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsgiSvcLogicClassResolver implements SvcLogicResolver {
    private static final Logger LOG = LoggerFactory.getLogger(OsgiSvcLogicClassResolver.class);
    private static HashMap<String, SvcLogicAdaptor> adaptorMap = new HashMap<>();

    public void registerAdaptor(SvcLogicAdaptor adaptor) {
        String name = adaptor.getClass().getName();
        LOG.info("Registering adaptor " + name);
        adaptorMap.put(name, adaptor);
    }

    public void unregisterAdaptor(String name) {
        if (adaptorMap.containsKey(name)) {
            LOG.info("Unregistering " + name);
            adaptorMap.remove(name);
        }
    }

    private SvcLogicAdaptor getAdaptorInstance(String name) {
        if (adaptorMap.containsKey(name)) {
            return adaptorMap.get(name);
        } else {

            SvcLogicAdaptor adaptor = (SvcLogicAdaptor) resolve(name);

            if (adaptor != null) {
                registerAdaptor(adaptor);
            }

            return adaptor;
        }
    }

    private Object resolve(String className) {

        Bundle bundle = FrameworkUtil.getBundle(OsgiSvcLogicClassResolver.class);

        if (bundle == null) {
            // Running outside OSGi container (e.g. jUnit). Use Reflection
            // to resolve class
            try {
                return (Class.forName(className).newInstance());
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {

                LOG.error("Could not resolve class " + className, e);
                return null;
            }

        } else {
            BundleContext bctx = bundle.getBundleContext();
            ServiceReference sref = bctx.getServiceReference(className);
            if (sref != null) {
                return bctx.getService(sref);
            } else {

                LOG.warn("Could not find service reference object for class " + className);
                return null;
            }
        }
    }

    @Override
    public SvcLogicResource getSvcLogicResource(String resourceName) {
        return (SvcLogicResource) resolve(resourceName);
    }

    @Override
    public SvcLogicRecorder getSvcLogicRecorder(String recorderName) {
        return (SvcLogicRecorder) resolve(recorderName);
    }

    @Override
    public SvcLogicJavaPlugin getSvcLogicJavaPlugin(String pluginName) {
        return (SvcLogicJavaPlugin) resolve(pluginName);
    }

    @Override
    public SvcLogicAdaptor getSvcLogicAdaptor(String adaptorName) {
        return getAdaptorInstance(adaptorName);
    }

}
