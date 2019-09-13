package org.onap.ccsdk.sli.core.api;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.onap.ccsdk.sli.core.api.exceptions.BreakNodeException;

public class BreakNodeExceptionTest {
    @Test
    public void breakNodeExceptionTest() {
        assertNotNull(new BreakNodeException());
    }

    @Test
    public void breakNodeExceptionTestString() {
        assertNotNull(new BreakNodeException("JUnit Test"));
    }

    @Test
    public void breakNodeExceptionTestStringThrowable() {
        assertNotNull(new BreakNodeException("JUnit Test", new Exception("JUnit Test")));
    }
}
