package org.onap.ccsdk.sli.core.slipluginutils.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.core.slipluginutils.SliPluginUtils;
import org.onap.ccsdk.sli.core.slipluginutils.SliStringUtils;

public class SliPluginUtilsModule extends AbstractLightyModule {

    private SliPluginUtils sliPluginUtils;
    private SliStringUtils sliStringUtils;

    @Override
    protected boolean initProcedure() {
        this.sliPluginUtils = new SliPluginUtils();
        this.sliStringUtils = new SliStringUtils();
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public SliPluginUtils getSliPluginUtils() {
        return this.sliPluginUtils;
    }

    public SliStringUtils getSliStringUtils() {
        return sliStringUtils;
    }
}
