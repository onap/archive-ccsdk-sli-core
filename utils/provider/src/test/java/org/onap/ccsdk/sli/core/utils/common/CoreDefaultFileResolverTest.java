package org.onap.ccsdk.sli.core.utils.common;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Optional;
import org.junit.Test;
import org.onap.ccsdk.sli.core.utils.PropertiesFileResolver;

public class CoreDefaultFileResolverTest {

    @Test
    public void resolveFile() throws Exception {
        final PropertiesFileResolver resolver = new CoreDefaultFileResolver("success");
        final Optional<File> file = resolver.resolveFile("doesnotexist.cfg");
        assertFalse(file.isPresent());
    }

    @Test
    public void getSuccessfulResolutionMessage() throws Exception {
        final PropertiesFileResolver resolver = new CoreDefaultFileResolver("success");
        assertEquals("success", resolver.getSuccessfulResolutionMessage());
    }

}