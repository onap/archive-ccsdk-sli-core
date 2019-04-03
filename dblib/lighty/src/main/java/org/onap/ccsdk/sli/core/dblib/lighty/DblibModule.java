package org.onap.ccsdk.sli.core.dblib.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.core.dblib.DBResourceManagerLighty;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;

public class DblibModule extends AbstractLightyModule {

    private final AAAEncryptionService aaaEncryptionService;

    private DBLIBResourceProviderLighty dbLibResourceProvider;
    private DBResourceManagerLighty dbResourceManager;

    public DblibModule(AAAEncryptionService aaaEncryptionService) {
        this.aaaEncryptionService = aaaEncryptionService;
    }

    @Override
    protected boolean initProcedure() {
        this.dbLibResourceProvider = new DBLIBResourceProviderLighty(aaaEncryptionService);
        this.dbResourceManager = new DBResourceManagerLighty(this.dbLibResourceProvider);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public DBLIBResourceProviderLighty getDBLIBResourceProvider() {
        return this.dbLibResourceProvider;
    }

    public DBResourceManagerLighty getDBResourceManager() {
        return this.dbResourceManager;
    }
}
