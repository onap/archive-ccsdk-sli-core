package org.onap.ccsdk.sli.core.api.lang;

import java.util.List;


public interface SvcLogicExpression {

    void addOperand(SvcLogicExpression expr);

    List<SvcLogicExpression> getOperands();

    int numOperands();

    String asParsedExpr();

}
