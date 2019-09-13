package org.onap.ccsdk.sli.core.api.lang;

public interface SvcLogicVariableTerm {

    String getName();

    SvcLogicExpression getSubscript();

    String asParsedExpr();

}
