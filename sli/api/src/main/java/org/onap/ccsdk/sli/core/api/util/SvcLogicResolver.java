package org.onap.ccsdk.sli.core.api.util;

import org.onap.ccsdk.sli.core.api.extensions.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.api.extensions.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.api.extensions.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.api.extensions.SvcLogicResource;

public interface SvcLogicResolver {
    
    abstract SvcLogicResource getSvcLogicResource(String resourceName);

    abstract SvcLogicRecorder getSvcLogicRecorder(String recorderName);

    abstract SvcLogicJavaPlugin getSvcLogicJavaPlugin(String pluginName);
    
    abstract SvcLogicAdaptor getSvcLogicAdaptor(String adaptorName);
}
