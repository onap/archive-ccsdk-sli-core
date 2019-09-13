package org.onap.ccsdk.sli.core.api.lang;

public interface SvcLogicVariableTerm extends SvcLogicExpression {

    String getName();

    SvcLogicExpression getSubscript();

    String asParsedExpr();

}
