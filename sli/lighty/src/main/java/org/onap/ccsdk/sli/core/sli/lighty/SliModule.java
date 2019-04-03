package org.onap.ccsdk.sli.core.sli.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import io.lighty.core.controller.api.LightyModule;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicPropertiesProviderImpl;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicServiceImpl;

public class SliModule extends AbstractLightyModule implements LightyModule {

    private final SvcLogicPropertiesProviderImpl svcLogicPropertiesImpl;
    private final SvcLogicServiceImpl svcLogicImpl;

    public SliModule (final DbLibService dbLibService) throws SvcLogicException {
        this.svcLogicPropertiesImpl = new SvcLogicPropertiesProviderImpl();
        this.svcLogicImpl = new SvcLogicServiceImpl(this.svcLogicPropertiesImpl, dbLibService);
    }

    public SvcLogicPropertiesProviderImpl getSvcLogicPropertiesProviderImpl() {
        return this.svcLogicPropertiesImpl;
    }

    public SvcLogicServiceImpl getSvcLogicServiceImpl() {
        return this.svcLogicImpl;
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