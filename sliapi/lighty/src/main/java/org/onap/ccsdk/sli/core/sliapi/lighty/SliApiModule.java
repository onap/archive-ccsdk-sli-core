package org.onap.ccsdk.sli.core.sliapi.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;

public class SliApiModule extends AbstractLightyModule {

    private final DataBroker dataBroker;
    private final NotificationPublishService notificationPublishService;
    private final RpcProviderRegistry rpcRegistry;
    private final SvcLogicService svcLogic;

    private sliapiProviderLighty sliapiProvider;

    public SliApiModule (DataBroker dataBroker, NotificationPublishService notificationPublishService,
            RpcProviderRegistry rpcRegistry, SvcLogicService svcLogic) {
        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
        this.rpcRegistry = rpcRegistry;
        this.svcLogic = svcLogic;
    }

    @Override
    protected boolean initProcedure() {
        this.sliapiProvider = new sliapiProviderLighty(dataBroker, notificationPublishService, rpcRegistry, svcLogic);
        sliapiProvider.initialize();
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public sliapiProviderLighty getSliapiProvider() {
        return sliapiProvider;
    }
}
