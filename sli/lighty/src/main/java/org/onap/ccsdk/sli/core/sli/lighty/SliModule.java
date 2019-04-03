package org.onap.ccsdk.sli.core.sli.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicPropertiesProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SliModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(SliModule.class);


    private final DbLibService dbLibService;
    private final SvcLogicResource svcLogicResource;
    private final SvcLogicRecorder svcLogicRecorder;
    private final SvcLogicJavaPlugin svcLogicJavaPlugin;
    private final SvcLogicAdaptor svcLogicAdaptor;

    private SvcLogicPropertiesProviderImpl svcLogicPropertiesImpl;
    private SvcLogicServiceImplLighty svcLogicImpl;
    private SvcLogicClassResolverLighty svcLogicClassResolver;

    public SliModule(DbLibService dbLibService, SvcLogicResource svcLogicResource, SvcLogicRecorder svcLogicRecorder,
            SvcLogicJavaPlugin svcLogicJavaPlugin, SvcLogicAdaptor svcLogicAdaptor) {
        this.dbLibService = dbLibService;
        this.svcLogicResource = svcLogicResource;
        this.svcLogicRecorder = svcLogicRecorder;
        this.svcLogicJavaPlugin = svcLogicJavaPlugin;
        this.svcLogicAdaptor = svcLogicAdaptor;
    }

    @Override
    protected boolean initProcedure() {
        this.svcLogicPropertiesImpl = new SvcLogicPropertiesProviderImpl();
        this.svcLogicClassResolver = new SvcLogicClassResolverLighty(svcLogicResource, svcLogicRecorder,
                svcLogicJavaPlugin, svcLogicAdaptor);
        this.svcLogicImpl = new SvcLogicServiceImplLighty(this.svcLogicPropertiesImpl, dbLibService,
                svcLogicClassResolver);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public SvcLogicPropertiesProviderImpl getSvcLogicPropertiesProviderImpl() {
        return this.svcLogicPropertiesImpl;
    }

    public SvcLogicServiceImplLighty getSvcLogicServiceImpl() {
        return this.svcLogicImpl;
    }
}
