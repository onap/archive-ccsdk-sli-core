package org.onap.ccsdk.sli.core.dblib.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.core.dblib.DBLIBResourceProvider;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;

public class DblibModule extends AbstractLightyModule {

    public DBLIBResourceProvider dbLibResourceProvider;
    public DBResourceManager dbResourceManager;

    @Override
    protected boolean initProcedure() {
        this.dbLibResourceProvider = new DBLIBResourceProvider();
        this.dbResourceManager = new DBResourceManager(this.dbLibResourceProvider);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public DBLIBResourceProvider getDBLIBResourceProvider() {
        return this.dbLibResourceProvider;
    }

    public DBResourceManager getDBResourceManager() {
        return this.dbResourceManager;
    }
}
