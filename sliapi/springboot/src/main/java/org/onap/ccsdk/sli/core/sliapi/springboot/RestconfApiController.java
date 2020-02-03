package org.onap.ccsdk.sli.core.sliapi.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicLoader;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicClassResolver;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicPropertiesProviderImpl;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicServiceImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.InMemorySvcLogicStore;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicResolver;
import org.onap.ccsdk.sli.core.sliapi.model.ExecuteGraphInput;
import org.onap.ccsdk.sli.core.sliapi.model.HealthcheckInput;
import org.onap.ccsdk.sli.core.sliapi.model.ResponseFields;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-02-20T12:50:11.207-05:00")

@Controller
public class RestconfApiController implements RestconfApi {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private static SvcLogicService svc;

    @org.springframework.beans.factory.annotation.Autowired
    public RestconfApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;

        InMemorySvcLogicStore store = new InMemorySvcLogicStore();
        String serviceLogicDirectory = System.getProperty("serviceLogicDirectory");
        System.out.println("serviceLogicDirectory is "+serviceLogicDirectory);
        SvcLogicLoader loader = new SvcLogicLoader(serviceLogicDirectory, store);

        try {
            loader.loadAndActivate();
        } catch (IOException e) {
            log.error("Cannot load directed graphs", e);
        }
        SvcLogicResolver resolver = new SvcLogicClassResolver();


        try {
            svc = new SvcLogicServiceImpl(new SvcLogicPropertiesProviderImpl(), resolver);
        } catch (SvcLogicException e) {
            log.error("Cannot execute directed graph", e);
        }
    }

    @Override
    public ResponseEntity<ResponseFields> healthcheck(@Valid HealthcheckInput healthcheckInput) {
        SvcLogicContext ctx = new SvcLogicContext();
        ResponseFields resp = new ResponseFields();
        try {
            Properties respProps = svc.execute("SLI-API", "healthcheck" , null, "sync", null);


            resp.setAckFinalIndicator(respProps.getProperty("ack-final-indicator", "true"));
            resp.setResponseCode(respProps.getProperty("response-code", "200"));
            resp.setResponseMessage(respProps.getProperty("response-message", "Success"));
            return(new ResponseEntity<>(resp, HttpStatus.OK));
        }
        catch (Exception e) {
            resp.setAckFinalIndicator("true");
            resp.setResponseCode("500");
            resp.setResponseMessage(e.getMessage());
            log.error("Error calling healthcheck directed graph", e);

        }

        return(new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR));

    }

    @Override
    public Optional<ObjectMapper> getObjectMapper() {
        return Optional.ofNullable(objectMapper);
    }

    @Override
    public Optional<HttpServletRequest> getRequest() {
        return Optional.ofNullable(request);
    }





}
