package org.onap.ccsdk.sli.core.sliboot;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.onap.ccsdk.sli.core.api.SvcLogicService;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.api.extensions.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.api.util.SvcLogicLoader;
import org.onap.ccsdk.sli.core.parser.SvcLogicParserImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.HashMapResolver;
import org.onap.ccsdk.sli.core.sli.provider.base.InMemorySvcLogicStore;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.util.SvcLogicLoaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Controller
@EnableAutoConfiguration
public class ExecuteGraphController {
    static SvcLogicService svc;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteGraphController.class);

    @RequestMapping(value = "/executeGraph", method = RequestMethod.POST)
    @ResponseBody
    public HashMap<String, String> executeGraph(@RequestBody String input) {
        LOGGER.error("In request");
        LOGGER.error(input);

        HashMap<String, String> hash = new HashMap<String, String>();
        Properties parms = new Properties();

        hash.put("status", "success");
        JsonObject jsonInput = new Gson().fromJson(input, JsonObject.class);
        JsonObject passthroughObj = jsonInput.get("input").getAsJsonObject();

        writeResponseToCtx(passthroughObj.toString(), parms, "input");

        JsonObject inputObject = jsonInput.get("graphDetails").getAsJsonObject();
        try {
            // Any of these can throw a nullpointer exception
            String calledModule = inputObject.get("module").getAsString();
            String calledRpc = inputObject.get("rpc").getAsString();
            String modeStr = inputObject.get("mode").getAsString();
            // execute should only throw a SvcLogicException
            Properties respProps = svc.execute(calledModule, calledRpc, null, modeStr, parms);
            for (Entry<Object, Object> prop : respProps.entrySet()) {
                hash.put((String) prop.getKey(), (String) prop.getValue());
            }
        } catch (NullPointerException npe) {
            HashMap<String, String> errorHash = new HashMap<String, String>();
            errorHash.put("error-message", "check that you populated module, rpc and or mode correctly.");
            return errorHash;
        } catch (SvcLogicException e) {
            HashMap<String, String> errorHash = new HashMap<String, String>();
            errorHash.put("status", "failure");
            errorHash.put("message", e.getMessage());
            return errorHash;
        }
        return hash;
    }

    public static void writeResponseToCtx(String resp, Properties ctx, String prefix) {
        JsonParser jp = new JsonParser();
        JsonElement element = jp.parse(resp);
        writeJsonObject(element.getAsJsonObject(), ctx, prefix + ".");
    }

    public static void writeJsonObject(JsonObject obj, Properties ctx, String root) {
        for (Entry<String, JsonElement> entry : obj.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                writeJsonObject(entry.getValue().getAsJsonObject(), ctx, root + entry.getKey() + ".");
            } else if (entry.getValue().isJsonArray()) {
                JsonArray array = entry.getValue().getAsJsonArray();
                ctx.put(root + entry.getKey() + "_length", String.valueOf(array.size()));
                Integer arrayIdx = 0;
                for (JsonElement element : array) {
                    if (element.isJsonObject()) {
                        writeJsonObject(element.getAsJsonObject(), ctx, root + entry.getKey() + "[" + arrayIdx + "].");
                    }
                    arrayIdx++;
                }
            } else {
                ctx.put(root + entry.getKey(), entry.getValue().getAsString());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        InMemorySvcLogicStore store = new InMemorySvcLogicStore();
        SvcLogicLoader loader = new SvcLogicLoaderImpl(store, new SvcLogicParserImpl());

        loader.loadAndActivate(System.getProperty("serviceLogicDirectory"));
        HashMapResolver resolver = new HashMapResolver();
        resolver.addSvcLogicRecorder("org.onap.ccsdk.sli.core.sli.recording.Slf4jRecorder", new SvcLogicRecorder() {
            @Override
            public void record(Map<String, String> parmMap) throws SvcLogicException {
                for (Entry<String, String> entry : parmMap.entrySet()) {
                    LOGGER.error(entry.getKey() + " = " + entry.getValue());
                }

            }
        });
        svc = new SvcLogicServiceImpl(store,
                resolver);
        SpringApplication.run(ExecuteGraphController.class, args);
    }
}
