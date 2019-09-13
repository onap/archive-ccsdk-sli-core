package org.onap.ccsdk.sli.core.api;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.onap.ccsdk.sli.core.api.exceptions.ExitNodeException;

public class ExitNodeExceptionTest {
    @Test
    public void exitNodeExceptionTest() {
        assertNotNull(new ExitNodeException());

    }

    @Test
    public void exitNodeExceptionTestString() {
        assertNotNull(new ExitNodeException("JUnit Test"));

    }

    @Test
    public void exitNodeExceptionTestStringThrowable() {
        assertNotNull(new ExitNodeException("JUnit Test", new Exception("JUnit Test")));

    }
}
