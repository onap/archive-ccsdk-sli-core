package org.onap.ccsdk.sli.core.api.lang;

import java.util.List;


public interface SvcLogicBinaryExpression extends SvcLogicExpression {

    /* (non-Javadoc)
         * @see org.onap.ccsdk.sli.core.sli.SvcLogicExpression#getOperators()
         */
    List<OperatorType> getOperators();

    /* (non-Javadoc)
         * @see org.onap.ccsdk.sli.core.sli.SvcLogicExpression#addOperator(java.lang.String)
         */
    void addOperator(String operator);

    /* (non-Javadoc)
         * @see org.onap.ccsdk.sli.core.sli.SvcLogicExpression#asParsedExpr()
         */
    String asParsedExpr();

}
