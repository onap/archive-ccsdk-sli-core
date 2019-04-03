package org.onap.ccsdk.sli.core.sliapi.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import io.lighty.core.controller.api.LightyModule;
import org.onap.ccsdk.sli.core.sliapi.sliapiProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;

public class SliApiModule extends AbstractLightyModule implements LightyModule {

    private final sliapiProvider sliapiProvider;

    public SliApiModule (final DataBroker dataBroker,
                         final NotificationPublishService notificationPublishService,
                         final RpcProviderRegistry rpcRegistry) {
        this.sliapiProvider = new sliapiProvider(dataBroker, notificationPublishService, rpcRegistry);
    }

    public sliapiProvider getSliapiProvider() {
        return this.sliapiProvider;
    }

    @Override
    protected boolean initProcedure() {
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }
}