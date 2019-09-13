/**
 *
 */
package org.onap.ccsdk.sli.core.sli.provider.base;

import static org.junit.Assert.fail;
import java.util.HashMap;
import org.junit.Test;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.base.recording.Slf4jRecorder;

/**
 * @author dt5972
 *
 */
public class TestSlf4jRecorder {

    /**
     * Test method for {@link org.onap.ccsdk.sli.core.sli.provider.base.recording.Slf4jRecorder#record(java.util.Map)}.
     */
    @Test
    public void testRecord() {
        Slf4jRecorder recorder = new Slf4jRecorder();

        HashMap<String,String> parms = new HashMap<>();
        parms.put("field1","hi");
        try {
            recorder.record(parms);
        } catch (SvcLogicException e) {
            fail("Caught SvcLogicException : "+e.getMessage());
        }
    }

}
