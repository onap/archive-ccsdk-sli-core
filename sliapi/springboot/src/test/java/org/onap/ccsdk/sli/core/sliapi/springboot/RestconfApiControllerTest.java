package org.onap.ccsdk.sli.core.sliapi.springboot;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.sli.core.sliapi.model.ExecuteGraphInput;
import org.onap.ccsdk.sli.core.sliapi.model.ExecutegraphinputInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@WebMvcTest(RestconfApiController.class)
public class RestconfApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RestconfApiController restconfApiController;

    @Test
    public void testHealthcheck() throws Exception {
        String url = "/restconf/operations/SLI-API:healthcheck";

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content("")).andReturn();

        assertEquals(200, mvcResult.getResponse().getStatus());
    }

    @Test
    public void testVlbcheck() throws Exception {
        String url = "/restconf/operations/SLI-API:vlbcheck";

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content("")).andReturn();

        assertEquals(200, mvcResult.getResponse().getStatus());
    }

    @Test
    public void testExecuteHealthcheck() throws Exception {
        String url = "/restconf/operations/SLI-API:execute-graph";

        ExecuteGraphInput executeGraphInput = new ExecuteGraphInput();
        ExecutegraphinputInput executeGraphData = new ExecutegraphinputInput();

        executeGraphData.setModuleName("sli");
        executeGraphData.setRpcName("healthcheck");
        executeGraphData.setMode("sync");
        executeGraphInput.setInput(executeGraphData);

        String jsonString = mapToJson(executeGraphInput);

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(jsonString)).andReturn();

        assertEquals(200, mvcResult.getResponse().getStatus());

    }

    private String mapToJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }

}
