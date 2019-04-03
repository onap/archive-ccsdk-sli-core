package org.onap.ccsdk.sli.core.slipluginutils.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import io.lighty.core.controller.api.LightyModule;
import org.onap.ccsdk.sli.core.slipluginutils.SliPluginUtils;
import org.onap.ccsdk.sli.core.slipluginutils.SliStringUtils;

public class SliPluginUtilsModule extends AbstractLightyModule implements LightyModule {

    private final SliPluginUtils sliPluginUtils;
    private final SliStringUtils sliStringUtils;

    public SliPluginUtilsModule () {
        this.sliPluginUtils = new SliPluginUtils();
        this.sliStringUtils = new SliStringUtils();
    }

    public SliPluginUtils getSliPluginUtils() {
        return this.sliPluginUtils;
    }

    public SliStringUtils getSliStringUtils() {
        return sliStringUtils;
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
