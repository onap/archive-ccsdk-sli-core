package org.onap.ccsdk.sli.core.api.lang;

import java.io.IOException;

public interface SvcLogicExpressionParser {
    SvcLogicExpression parse(String exprStr) throws IOException;
}
