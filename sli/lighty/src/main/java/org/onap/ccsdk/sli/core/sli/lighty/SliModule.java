/*
 * ============LICENSE_START==========================================
 * Copyright (c) 2019 PANTHEON.tech s.r.o.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 *
 */
package org.onap.ccsdk.sli.core.sli.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicClassResolver;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicPropertiesProviderImpl;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicServiceImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that manages and provides services from
 * the sli-provider artifact.
 */
public class SliModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(SliModule.class);

    private final DbLibService dbLibService;
    SvcLogicService svcLogicService;
    SvcLogicPropertiesProvider svcLogicPropertiesProvider;
    SvcLogicClassResolver svcLogicClassResolver;

    public SliModule(DbLibService dbLibService) {
        this.dbLibService = dbLibService;
    }

    @Override
    protected boolean initProcedure() {
        svcLogicPropertiesProvider = new SvcLogicPropertiesProviderImpl();
        svcLogicClassResolver = new SvcLogicClassResolver();
        try {
            svcLogicService = new SvcLogicServiceImpl(svcLogicPropertiesProvider, dbLibService, svcLogicClassResolver);
        } catch (SvcLogicException e) {
            LOG.error("Unable to start {} in {}!", SvcLogicService.class, SliModule.class, e);
            return false;
        }
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public SvcLogicService getSvcLogicServiceImpl() {
        return this.svcLogicService;
    }
}
