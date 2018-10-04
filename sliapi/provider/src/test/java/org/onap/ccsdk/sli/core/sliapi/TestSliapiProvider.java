/**
 *
 */
package org.onap.ccsdk.sli.core.sliapi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicParser;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;
import org.onap.ccsdk.sli.core.sli.provider.BlockNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.CallNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.ConfigureNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.DeleteNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.ExecuteNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.ExistsNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.ForNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.GetResourceNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.IsAvailableNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.NotifyNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.RecordNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.ReleaseNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.ReserveNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.ReturnNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.SaveNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.SetNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicPropertiesProviderImpl;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicServiceImpl;
import org.onap.ccsdk.sli.core.sli.provider.SwitchNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.UpdateNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.WhileNodeExecutor;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.SLIAPIService;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterBuilder;

/**
 * @author dt5972
 *
 */
public class TestSliapiProvider {

    private sliapiProvider provider;

    private static final String HEALTHCHECK_DG = "sli_healthcheck.xml";

    private static final Map<String, SvcLogicNodeExecutor> BUILTIN_NODES = new HashMap<String, SvcLogicNodeExecutor>() {
        {
            put("block", new BlockNodeExecutor());
            put("call", new CallNodeExecutor());
            put("configure", new ConfigureNodeExecutor());
            put("delete", new DeleteNodeExecutor());
            put("execute", new ExecuteNodeExecutor());
            put("exists", new ExistsNodeExecutor());
            put("for", new ForNodeExecutor());
            put("get-resource", new GetResourceNodeExecutor());
            put("is-available", new IsAvailableNodeExecutor());
            put("notify", new NotifyNodeExecutor());
            put("record", new RecordNodeExecutor());
            put("release", new ReleaseNodeExecutor());
            put("reserve", new ReserveNodeExecutor());
            put("return", new ReturnNodeExecutor());
            put("save", new SaveNodeExecutor());
            put("set", new SetNodeExecutor());
            put("switch", new SwitchNodeExecutor());
            put("update", new UpdateNodeExecutor());
            put("while", new WhileNodeExecutor());

        }
    };

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        DataBroker dataBroker = mock(DataBroker.class);
        NotificationPublishService notifyService = mock(NotificationPublishService.class);
        RpcProviderRegistry rpcRegistry = mock(RpcProviderRegistry.class);
        BindingAwareBroker.RpcRegistration<SLIAPIService> rpcRegistration = (BindingAwareBroker.RpcRegistration<SLIAPIService>) mock(
                BindingAwareBroker.RpcRegistration.class);
        when(rpcRegistry.addRpcImplementation(any(Class.class), any(SLIAPIService.class))).thenReturn(rpcRegistration);

        // Load svclogic.properties and get a SvcLogicStore
        InputStream propStr = TestSliapiProvider.class.getResourceAsStream("/svclogic.properties");
        Properties svcprops = new Properties();
        svcprops.load(propStr);

        SvcLogicStore store = SvcLogicStoreFactory.getSvcLogicStore(svcprops);

        assertNotNull(store);

        // Load the DG for the healthcheck api
        URL testCaseUrl = TestSliapiProvider.class.getClassLoader().getResource(HEALTHCHECK_DG);
        if (testCaseUrl == null) {
            fail("Cannot find " + HEALTHCHECK_DG);
        }
        SvcLogicParser.load(testCaseUrl.getPath(), store);
        SvcLogicParser.activate("sli", "healthcheck", "1.0.0", "sync", store);

        // Create a ServiceLogicService and initialize it
        SvcLogicServiceImpl svc = new SvcLogicServiceImpl(new SvcLogicPropertiesProviderImpl());
        for (String nodeType : BUILTIN_NODES.keySet()) {
            svc.registerExecutor(nodeType, BUILTIN_NODES.get(nodeType));
        }

        // Finally ready to create sliapiProvider
        provider = new sliapiProvider(dataBroker, notifyService, rpcRegistry, svc);
        provider.setDataBroker(dataBroker);
        provider.setNotificationService(notifyService);
        provider.setRpcRegistry(rpcRegistry);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        provider.close();
    }

    /**
     * Test method for
     * {@link org.onap.ccsdk.sli.core.sliapi.sliapiProvider#executeGraph(org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput)}.
     */
    @Test
    public void testExecuteGraph() {
        ExecuteGraphInputBuilder inputBuilder = new ExecuteGraphInputBuilder();

        inputBuilder.setMode(ExecuteGraphInput.Mode.Sync);
        inputBuilder.setModuleName("sli");
        inputBuilder.setRpcName("healthcheck");
        List<SliParameter> pList = new LinkedList<>();
        SliParameterBuilder pBuilder = new SliParameterBuilder();
        pBuilder.setParameterName("int-parameter");
        pBuilder.setIntValue(1);
        pList.add(pBuilder.build());
        inputBuilder.setSliParameter(pList);

        provider.executeGraph(inputBuilder.build());
        assertTrue(provider.vlbcheck() instanceof Future<?>);
    }

    /**
     * Test method for
     * {@link org.onap.ccsdk.sli.core.sliapi.sliapiProvider#healthcheck()}.
     */
    @Test
    public void testHealthcheck() {
        provider.healthcheck();
    }

}
