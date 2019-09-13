package org.onap.ccsdk.sli.core.api.util;

import java.util.LinkedList;
import org.onap.ccsdk.sli.core.api.SvcLogicGraph;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;


public interface SvcLogicParser {

    LinkedList<SvcLogicGraph> parse(String fileName) throws SvcLogicException;

}
