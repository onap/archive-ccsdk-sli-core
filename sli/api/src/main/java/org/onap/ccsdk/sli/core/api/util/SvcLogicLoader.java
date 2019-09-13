package org.onap.ccsdk.sli.core.api.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;


public interface SvcLogicLoader {

    void loadAndActivate(String directoryRoot) throws IOException;

    void loadGraphs(List<Path> graphPaths, String directoryRoot, SvcLogicStore store);

    void bulkActivate(String directoryRoot);

}
