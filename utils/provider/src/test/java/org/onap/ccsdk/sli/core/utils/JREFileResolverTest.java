package org.onap.ccsdk.sli.core.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class JREFileResolverTest {

    @Test
    public void getSuccessfulResolutionMessage() throws Exception {
        final PropertiesFileResolver resolver = new JREFileResolver(SvcLogicConstants.SUCCESS, JREFileResolverTest.class);
        assertEquals(SvcLogicConstants.SUCCESS, resolver.getSuccessfulResolutionMessage());
    }

}