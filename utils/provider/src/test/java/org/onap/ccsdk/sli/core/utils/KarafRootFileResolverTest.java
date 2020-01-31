package org.onap.ccsdk.sli.core.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class KarafRootFileResolverTest {
    @Test
    public void getSuccessfulResolutionMessage() throws Exception {
        final PropertiesFileResolver resolver = new KarafRootFileResolver(SvcLogicConstants.SUCCESS, null);
        assertEquals(SvcLogicConstants.SUCCESS, resolver.getSuccessfulResolutionMessage());
    }

}