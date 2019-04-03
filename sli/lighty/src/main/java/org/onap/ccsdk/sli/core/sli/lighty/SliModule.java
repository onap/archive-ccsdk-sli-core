package org.onap.ccsdk.sli.core.sli.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicPropertiesProviderImpl;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SliModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(SliModule.class);


    private final DbLibService dbLibService;

    private SvcLogicPropertiesProviderImpl svcLogicPropertiesImpl;
    private SvcLogicServiceImpl svcLogicImpl;

    public SliModule (DbLibService dbLibService) {
        this.dbLibService = dbLibService;
    }

    @Override
    protected boolean initProcedure() {
        this.svcLogicPropertiesImpl = new SvcLogicPropertiesProviderImpl();
        try {
            this.svcLogicImpl = new SvcLogicServiceImpl(this.svcLogicPropertiesImpl, dbLibService);
        } catch (SvcLogicException e) {
            LOG.error("Exception thrown while initializing {} in {}!", SvcLogicServiceImpl.class, this.getClass(), e);
            return false;
        }
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public SvcLogicPropertiesProviderImpl getSvcLogicPropertiesProviderImpl() {
        return this.svcLogicPropertiesImpl;
    }

    public SvcLogicServiceImpl getSvcLogicServiceImpl() {
        return this.svcLogicImpl;
    }
}
