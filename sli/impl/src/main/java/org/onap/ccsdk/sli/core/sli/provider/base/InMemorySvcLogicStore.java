package org.onap.ccsdk.sli.core.sli.provider.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.onap.ccsdk.sli.core.api.SvcLogicGraph;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.api.util.SvcLogicStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemorySvcLogicStore implements SvcLogicStore {
    private static final Logger logger = LoggerFactory.getLogger(InMemorySvcLogicStore.class);
    public Map<String, SvcLogicGraph> graphStore;

    protected List<String> nodeTypes;

    public InMemorySvcLogicStore() {
        nodeTypes = new ArrayList<String>();
        defaultNodes(nodeTypes);
        graphStore = new HashMap<String, SvcLogicGraph>();
    }

    private void defaultNodes(List<String> nodeTypes2) {
        nodeTypes.add("block");
        nodeTypes.add("is-available");
        nodeTypes.add("exists");
        nodeTypes.add("reserve");
        nodeTypes.add("release");
        nodeTypes.add("allocate");
        nodeTypes.add("get-resource");
        nodeTypes.add("configure");
        nodeTypes.add("return");
        nodeTypes.add("switch");
        nodeTypes.add("record");
        nodeTypes.add("save");
        nodeTypes.add("for");
        nodeTypes.add("set");
        nodeTypes.add("execute");
        nodeTypes.add("delete");
        nodeTypes.add("update");
        nodeTypes.add("call");
        nodeTypes.add("notify");
        nodeTypes.add("break");
    }

    @Override
    public void init(Properties props) throws SvcLogicException {

    }

    @Override
    public boolean hasGraph(String module, String rpc, String version, String mode) throws SvcLogicException {
        String storeId = new String(module + ":" + rpc);
        return graphStore.containsKey(storeId);
    }

    @Override
    public SvcLogicGraph fetch(String module, String rpc, String version, String mode) throws SvcLogicException {
        String storeId = new String(module + ":" + rpc);
        return graphStore.get(storeId);
    }

    @Override
    public void store(SvcLogicGraph graph) throws SvcLogicException {
        if (graph != null) {
            String storeId = new String(graph.getModule() + ":" + graph.getRpc());
            graphStore.put(storeId, graph);
            logger.info(graph.toString() + " stored in InMemorySvcLogicStore.");
        }
    }

    @Override
    public void delete(String module, String rpc, String version, String mode) throws SvcLogicException {
        String storeId = new String(module + ":" + rpc);
        if (graphStore.containsKey(storeId)) {
            graphStore.remove(storeId);
        }
    }

    @Override
    public void activate(SvcLogicGraph graph) throws SvcLogicException {
        //Do nothing
    }

    @Override
    public void activate(String arg0, String arg1, String arg2, String arg3) throws SvcLogicException {
        //Do nothing
    }

}
