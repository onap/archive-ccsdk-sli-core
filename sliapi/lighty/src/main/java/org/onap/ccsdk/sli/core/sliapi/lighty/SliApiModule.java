package org.onap.ccsdk.sli.core.sliapi.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.core.sliapi.sliapiProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;

public class SliApiModule extends AbstractLightyModule {

    private final DataBroker dataBroker;
    private final NotificationPublishService notificationPublishService;
    private final RpcProviderRegistry rpcRegistry;

    private sliapiProvider sliapiProvider;

    public SliApiModule (DataBroker dataBroker, NotificationPublishService notificationPublishService,
            RpcProviderRegistry rpcRegistry) {
        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
        this.rpcRegistry = rpcRegistry;
    }

    @Override
    protected boolean initProcedure() {
        this.sliapiProvider = new sliapiProvider(dataBroker, notificationPublishService, rpcRegistry);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public sliapiProvider getSliapiProvider() {
        return this.sliapiProvider;
    }
}
