package org.onap.ccsdk.sli.core.odlsli;

import java.io.IOException;
import org.junit.Test;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.api.util.SvcLogicLoader;
import org.onap.ccsdk.sli.core.parser.SvcLogicParserImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.InMemorySvcLogicStore;
import org.onap.ccsdk.sli.core.sli.provider.base.util.SvcLogicLoaderImpl;

public class TestSvcLogicLoader {

    @Test
    public void testLoadAndActivate() throws IOException, SvcLogicException {
        SvcLogicLoader loader = new SvcLogicLoaderImpl(new InMemorySvcLogicStore(), new SvcLogicParserImpl());
        loader.loadAndActivate("src/test/resources");
    }
}
