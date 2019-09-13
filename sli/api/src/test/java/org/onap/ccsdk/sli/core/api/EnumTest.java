package org.onap.ccsdk.sli.core.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.onap.ccsdk.sli.core.api.extensions.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.api.extensions.SvcLogicResource;
import org.onap.ccsdk.sli.core.api.lang.AtomType;
import org.onap.ccsdk.sli.core.api.lang.OperatorType;

public class EnumTest {

    @Test
    public void queryStatus() {
        assertNotNull(SvcLogicResource.QueryStatus.FAILURE);
        assertNotNull(SvcLogicResource.QueryStatus.NOT_FOUND);
        assertNotNull(SvcLogicResource.QueryStatus.SUCCESS);
    }

    @Test
    public void configStatus() {
        assertNotNull(SvcLogicAdaptor.ConfigStatus.ALREADY_ACTIVE);
        assertNotNull(SvcLogicAdaptor.ConfigStatus.FAILURE);
        assertNotNull(SvcLogicAdaptor.ConfigStatus.NOT_FOUND);
        assertNotNull(SvcLogicAdaptor.ConfigStatus.NOT_READY);
        assertNotNull(SvcLogicAdaptor.ConfigStatus.SUCCESS);
    }

    @Test
    public void atomType() {
        assertNotNull(AtomType.CONTEXT_VAR);
        assertNotNull(AtomType.IDENTIFIER);
        assertNotNull(AtomType.NUMBER);
        assertNotNull(AtomType.STRING);
    }

    @Test
    public void operatorType() {
        for (OperatorType op : OperatorType.values()) {
            OperatorType newOp = OperatorType.fromString(op.toString());
            assertNotNull(newOp);
            assertNotNull(newOp.getText());
            assertNotNull(newOp.toString());
        }
    }

    @Test
    public void nullOperatorType() {
        OperatorType nullOperator = OperatorType.fromString(null);
        assertNull(nullOperator);
    }

}
