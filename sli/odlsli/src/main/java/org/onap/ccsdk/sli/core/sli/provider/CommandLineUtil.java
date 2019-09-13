package org.onap.ccsdk.sli.core.sli.provider;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import org.onap.ccsdk.sli.core.api.SvcLogicGraph;
import org.onap.ccsdk.sli.core.api.exceptions.ConfigurationException;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.api.util.SvcLogicLoader;
import org.onap.ccsdk.sli.core.api.util.SvcLogicParser;
import org.onap.ccsdk.sli.core.api.util.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicParserImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.util.SvcLogicLoaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineUtil.class);
    private static final String LOAD_MESSAGE = "Getting SvcLogicGraph from database - {}";
    private static final String LOAD_ERROR_MESSAGE = "SvcLogicGraph not found - {}";
    private static final String ACTIVATION_ERROR_MESSAGE = "Could not activate SvcLogicGraph - {}";
    private static final String PRINT_ERROR_MESSAGE = "Could not print SvcLogicGraph - {}";
    private static final String SLI_VALIDATING_PARSER = "org.onap.ccsdk.sli.parser.validate";
    private static final String SVC_LOGIC_STORE_ERROR = "Could not get service logic store";

    public static void main(String argv[]) {

        if (argv.length == 0) {
            usage();
        }

        if ("load".equalsIgnoreCase(argv[0])) {
            if (argv.length == 3) {
                String xmlfile = argv[1];
                String propfile = argv[2];

                SvcLogicStore store = getStore(propfile);
                try {
                    load(xmlfile, store);
                } catch (Exception e) {
                    LOGGER.error("Load failed ", e);
                }
            } else {
                usage();
            }
        } else if ("print".equalsIgnoreCase(argv[0])) {
            String version = null;
            String propfile = null;

            switch (argv.length) {
                case 6:
                    version = argv[4];
                    propfile = argv[5];
                case 5:
                    if (propfile == null) {
                        propfile = argv[4];
                    }
                    SvcLogicStore store = getStore(propfile);
                    print(argv[1], argv[2], argv[3], version, store);
                    break;
                default:
                    usage();
            }
        } else if ("get-source".equalsIgnoreCase(argv[0])) {

            if (argv.length == 6) {
                SvcLogicStore store = getStore(argv[5]);
                getSource(argv[1], argv[2], argv[3], argv[4], store);
            } else {
                usage();
            }
        } else if ("activate".equalsIgnoreCase(argv[0])) {
            if (argv.length == 6) {
                SvcLogicStore store = getStore(argv[5]);
                activate(argv[1], argv[2], argv[3], argv[4], store);
            } else {
                usage();
            }
        } else if ("validate".equalsIgnoreCase(argv[0])) {
            if (argv.length == 3) {
                String xmlfile = argv[1];
                String propfile = argv[2];

                System.setProperty(SLI_VALIDATING_PARSER, "true");
                SvcLogicStore store = getStore(propfile);
                try {
                    validate(xmlfile, store);
                } catch (Exception e) {
                    LOGGER.error("Validate failed", e);
                }
            } else {
                usage();
            }
        } else if ("install".equalsIgnoreCase(argv[0])) {
            if (argv.length == 3) {
                SvcLogicStore store = getStore(argv[2]);
                SvcLogicLoader loader = new SvcLogicLoaderImpl(store, new SvcLogicParserImpl());
                try {
                    loader.loadAndActivate(argv[1]);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                usage();
            }
        } else if ("bulkActivate".equalsIgnoreCase(argv[0])) {
            if (argv.length == 3) {
                SvcLogicStore store = getStore(argv[2]);
                SvcLogicLoader loader = new SvcLogicLoaderImpl(store, new SvcLogicParserImpl());
                try {
                    loader.bulkActivate(argv[1]);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                usage();
            }
        }

        System.exit(0);
    }

    protected static SvcLogicStore getStore(String propfile) {

        SvcLogicStore store = null;

        try {
            store = SvcLogicStoreFactory.getSvcLogicStore(propfile);
        } catch (Exception e) {
            LOGGER.error(SVC_LOGIC_STORE_ERROR, e);
            System.exit(1);
        }

        return store;

    }


    public static void load(String xmlfile, SvcLogicStore store) throws SvcLogicException {
        File xmlFile = new File(xmlfile);
        if (!xmlFile.canRead()) {
            throw new ConfigurationException("Cannot read xml file (" + xmlfile + ")");
        }

        SvcLogicParser parser = new SvcLogicParserImpl();
        LinkedList<SvcLogicGraph> graphs;
        try {
            LOGGER.info("Loading {}", xmlfile);
            graphs = parser.parse(xmlfile);
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage(), e);
        }

        if (graphs == null) {
            throw new SvcLogicException("Could not parse " + xmlfile);
        }

        for (SvcLogicGraph graph : graphs) {

            try {
                LOGGER.info("Saving " + graph.toString() + " to database.");
                store.store(graph);
            } catch (Exception e) {
                throw new SvcLogicException(e.getMessage(), e);
            }

        }

    }

    public static void validate(String xmlfile, SvcLogicStore store) throws SvcLogicException {
        File xmlFile = new File(xmlfile);
        if (!xmlFile.canRead()) {
            throw new ConfigurationException("Cannot read xml file (" + xmlfile + ")");
        }

        SvcLogicParser parser = new SvcLogicParserImpl();
        LinkedList<SvcLogicGraph> graphs;
        try {
            LOGGER.info("Validating {}", xmlfile);
            graphs = parser.parse(xmlfile);
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage(), e);
        }

        if (graphs == null) {
            throw new SvcLogicException("Could not parse " + xmlfile);
        } else {
            LOGGER.info("Compilation successful for {}", xmlfile);
        }

    }

    private static void print(String module, String rpc, String mode, String version, SvcLogicStore store) {
        String details = "(module:" + module + ", rpc:" + rpc + ", version:" + version + ", mode:" + mode + ")";

        try {
            LOGGER.info(LOAD_MESSAGE, details);

            SvcLogicGraph graph = store.fetch(module, rpc, version, mode);
            if (graph == null) {
                LOGGER.error(LOAD_ERROR_MESSAGE, details);
                System.exit(1);
            }
            graph.printAsGv(System.out);
        } catch (Exception e) {
            LOGGER.error(PRINT_ERROR_MESSAGE, details, e);
            System.exit(1);
        }

    }

    private static void getSource(String module, String rpc, String mode, String version, SvcLogicStore store) {
        String details = "(module:" + module + ", rpc:" + rpc + ", version:" + version + ", mode:" + mode + ")";

        try {
            LOGGER.info(LOAD_MESSAGE, details);

            SvcLogicGraph graph = store.fetch(module, rpc, version, mode);
            if (graph == null) {
                LOGGER.error(LOAD_ERROR_MESSAGE, details);
                System.exit(1);
            }
            graph.printAsXml(System.out);
        } catch (Exception e) {
            LOGGER.error(PRINT_ERROR_MESSAGE, details, e);
            System.exit(1);
        }

    }

    public static void activate(String module, String rpc, String version, String mode, SvcLogicStore store) {
        String details = "(module:" + module + ", rpc:" + rpc + ", version:" + version + ", mode:" + mode + ")";

        try {
            LOGGER.info(LOAD_MESSAGE, details);

            SvcLogicGraph graph = store.fetch(module, rpc, version, mode);
            if (graph == null) {
                LOGGER.error(LOAD_ERROR_MESSAGE, details);
                System.exit(1);
            }
            store.activate(graph);
        } catch (Exception e) {
            LOGGER.error(ACTIVATION_ERROR_MESSAGE, details, e);
            System.exit(1);
        }

    }

    private static void usage() {
        System.err.println("Usage: SvcLogicParser load <xml-file> <prop-file>");
        System.err.println(" OR    SvcLogicParser print <module> <rpc> <mode> [<version>] <prop-file>");
        System.err.println(" OR    SvcLogicParser get-source <module> <rpc> <mode> <version> <prop-file>");
        System.err.println(" OR    SvcLogicParser activate <module> <rpc> <version> <mode>");
        System.err.println(" OR    SvcLogicParser validate <file path to graph> <prop-file>");
        System.err.println(" OR    SvcLogicParser install <service-logic directory path> <prop-file>");
        System.err.println(" OR    SvcLogicParser bulkActivate <path to activation file> <prop-file>");
        System.exit(1);
    }
}
