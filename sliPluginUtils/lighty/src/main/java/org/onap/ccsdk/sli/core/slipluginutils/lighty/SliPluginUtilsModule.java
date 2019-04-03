package org.onap.ccsdk.sli.core.slipluginutils.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import io.lighty.core.controller.api.LightyModule;
import org.onap.ccsdk.sli.core.slipluginutils.DME2;
import org.onap.ccsdk.sli.core.slipluginutils.Dme2Factory;
import org.onap.ccsdk.sli.core.slipluginutils.SliPluginUtils;
import org.onap.ccsdk.sli.core.slipluginutils.SliStringUtils;

public class SliPluginUtilsModule extends AbstractLightyModule implements LightyModule {

    private final SliPluginUtils sliPluginUtils;
    private final SliStringUtils sliStringUtils;
    private final Dme2Factory dme2Factory;
    private final DME2 dme2;

    public SliPluginUtilsModule () {
        this.sliPluginUtils = new SliPluginUtils();
        this.sliStringUtils = new SliStringUtils();
        this.dme2Factory = new Dme2Factory();
        this.dme2 = this.dme2Factory.createDme2();
    }

    public SliPluginUtils getSliPluginUtils() {
        return this.sliPluginUtils;
    }

    public SliStringUtils getSliStringUtils() {
        return sliStringUtils;
    }

    public Dme2Factory getDme2Factory() {
        return dme2Factory;
    }

    public DME2 getDme2() {
        return dme2;
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