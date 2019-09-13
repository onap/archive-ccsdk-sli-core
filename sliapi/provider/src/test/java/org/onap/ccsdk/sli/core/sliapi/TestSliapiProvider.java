/**
 *
 */
package org.onap.ccsdk.sli.core.sliapi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.api.SvcLogicGraph;
import org.onap.ccsdk.sli.core.api.util.SvcLogicParser;
import org.onap.ccsdk.sli.core.api.util.SvcLogicStore;
import org.onap.ccsdk.sli.core.parser.SvcLogicExpressionParserImpl;
import org.onap.ccsdk.sli.core.parser.SvcLogicParserImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.HashMapResolver;
import org.onap.ccsdk.sli.core.sli.provider.base.InMemorySvcLogicStore;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.AbstractSvcLogicNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.BlockNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.CallNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ConfigureNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.DeleteNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ExecuteNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ExistsNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ForNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.GetResourceNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.IsAvailableNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.NotifyNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.RecordNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ReleaseNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ReserveNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.ReturnNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.SaveNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.SetNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.SwitchNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.UpdateNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.executors.WhileNodeExecutor;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.HealthcheckInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.VlbcheckInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterBuilder;

/**
 * @author dt5972
 *
 */
public class TestSliapiProvider {
    private SliapiProvider provider;
    private static final String HEALTHCHECK_DG = "sli_healthcheck.xml";
    private static final Map<String, AbstractSvcLogicNodeExecutor> BUILTIN_NODES = new HashMap<String, AbstractSvcLogicNodeExecutor>() {
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
                    put("set", new SetNodeExecutor(new SvcLogicExpressionParserImpl()));
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
        SvcLogicStore store = new InMemorySvcLogicStore();
        assertNotNull(store);

        // Load the DG for the healthcheck api
        URL testCaseUrl = TestSliapiProvider.class.getClassLoader().getResource(HEALTHCHECK_DG);
        if (testCaseUrl == null) {
            fail("Cannot find " + HEALTHCHECK_DG);
        }

        SvcLogicParser parser = new SvcLogicParserImpl();
        LinkedList<SvcLogicGraph> graphs = parser.parse(testCaseUrl.getPath());
        for (SvcLogicGraph graph : graphs) {
            store.store(graph);
        }

        store.activate("sli", "healthcheck", "1.0.0", "sync");

        // Create a ServiceLogicService and initialize it
        SvcLogicServiceImpl svc = new SvcLogicServiceImpl(new InMemorySvcLogicStore(),
                new SvcLogicExpressionParserImpl(), new HashMapResolver());

        for (String nodeType : BUILTIN_NODES.keySet()) {
            svc.registerExecutor(nodeType, BUILTIN_NODES.get(nodeType));
        }

        // Finally ready to create SliapiProvider
        provider = new SliapiProvider(svc);
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
     * {@link SliapiProvider#executeGraph(org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput)}.
     */
    @Test
    public void testExecuteGraph() {
        ExecuteGraphInputBuilder inputBuilder = new ExecuteGraphInputBuilder();

        // Valid test - graph exists
        inputBuilder.setMode(ExecuteGraphInput.Mode.Sync);
        inputBuilder.setModuleName("sli");
        inputBuilder.setRpcName("healthcheck");
        List<SliParameter> pList = new LinkedList<>();
        SliParameterBuilder pBuilder = new SliParameterBuilder();
        pBuilder.setParameterName("int-parameter");
        pBuilder.setIntValue(1);
        pList.add(pBuilder.build());
        pBuilder.setParameterName("bool-parameter");
        pBuilder.setIntValue(null);
        pBuilder.setBooleanValue(true);
        pList.add(pBuilder.build());
        pBuilder.setParameterName("str-parameter");
        pBuilder.setBooleanValue(null);
        pBuilder.setStringValue("value");
        pList.add(pBuilder.build());
        inputBuilder.setSliParameter(pList);
        provider.executeGraph(inputBuilder.build());
    
        
        // Invalid test - graph does not exist
        inputBuilder.setMode(ExecuteGraphInput.Mode.Sync);
        inputBuilder.setModuleName("sli");
        inputBuilder.setRpcName("no-such-graph");
        pList = new LinkedList<>();
        pBuilder = new SliParameterBuilder();
        pBuilder.setParameterName("int-parameter");
        pBuilder.setIntValue(1);
        pList.add(pBuilder.build());
        pBuilder.setParameterName("bool-parameter");
        pBuilder.setIntValue(null);
        pBuilder.setBooleanValue(true);
        pList.add(pBuilder.build());
        pBuilder.setParameterName("str-parameter");
        pBuilder.setBooleanValue(null);
        pBuilder.setStringValue("value");
        pList.add(pBuilder.build());
        inputBuilder.setSliParameter(pList);
        provider.executeGraph(inputBuilder.build());
        
        assertTrue(provider.vlbcheck(mock(VlbcheckInput.class)) instanceof Future<?>);
    }

    /**
     * Test method for
     * {@link SliapiProvider#healthcheck()}.
     */
    @Test
    public void testHealthcheck() {
        provider.healthcheck(mock(HealthcheckInput.class));
    }

}
