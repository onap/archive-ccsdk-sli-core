package org.onap.ccsdk.sli.core.dblib.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import io.lighty.core.controller.api.LightyModule;
import org.onap.ccsdk.sli.core.dblib.DBLIBResourceProvider;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;

public class DblibModule extends AbstractLightyModule implements LightyModule {

    public DBLIBResourceProvider dbLibResourceProvider;
    public DBResourceManager dbResourceManager;

    public DblibModule () {
        this.dbLibResourceProvider = new DBLIBResourceProvider();
        this.dbResourceManager = new DBResourceManager(this.dbLibResourceProvider);
    }

    public DBLIBResourceProvider getDBLIBResourceProvider() {
        return this.dbLibResourceProvider;
    }

    public DBResourceManager getDBResourceManager() {
        return this.dbResourceManager;
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