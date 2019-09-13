package org.onap.ccsdk.sli.core.api.lang;

public interface SvcLogicFunctionCall extends SvcLogicExpression {

    String getFunctionName();

    void setFunctionName(String functionName);

}
