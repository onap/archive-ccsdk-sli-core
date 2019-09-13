package org.onap.ccsdk.sli.core.sli.provider;

import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.junit.Test;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.api.util.SvcLogicLoader;
import org.onap.ccsdk.sli.core.api.util.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicParserImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.InMemorySvcLogicStore;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicLoaderImpl;

public class TestSvcLogicLoader {

    @Test
    public void testLoadAndActivate() throws IOException, SvcLogicException {
        URL propUrl = ITCaseSvcLogicParser.class.getResource("/svclogic.properties");

        InputStream propStr = ITCaseSvcLogicParser.class.getResourceAsStream("/svclogic.properties");

        Properties props = new Properties();

        props.load(propStr);

        SvcLogicStore store = new InMemorySvcLogicStore();

        File graphDirectory =  new File(getClass().getClassLoader().getResource("graphs").getFile());

        if (graphDirectory == null) {
            fail("Cannot find graphs directory");
        }
        SvcLogicLoader loader = new SvcLogicLoaderImpl(store, new SvcLogicParserImpl());
        loader.loadAndActivate(graphDirectory.getAbsolutePath());


    }



}
