/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.core.sli;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author dt5972
 *
 */
public class BaseSvcLogicParser {

    protected static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    protected static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    protected static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    protected static final String JAXP_DYNAMIC_VALIDATION = "http://apache.org/xml/features/validation/dynamic";
    protected static final String JAXP_SCHEMA_VALIDATION = "http://apache.org/xml/features/validation/schema";
    protected static final String LOAD_MESSAGE = "Getting SvcLogicGraph from database - {}";
    protected static final String LOAD_ERROR_MESSAGE = "SvcLogicGraph not found - {}";
    protected static final String ACTIVATION_ERROR_MESSAGE = "Could not activate SvcLogicGraph - {}";
    protected static final String PRINT_ERROR_MESSAGE = "Could not print SvcLogicGraph - {}";

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSvcLogicParser.class);
    protected static final String SLI_VALIDATING_PARSER = "org.onap.ccsdk.sli.parser.validate";
    private static final String SVCLOGIC_XSD = "/svclogic.xsd";
    private SAXParser saxParser;

    public LinkedList<SvcLogicGraph> parse(String fileName) throws SvcLogicException {
        LinkedList<SvcLogicGraph> graphs;

        try {
            if (saxParser == null) {
                saxParser = initParser();
            }

            graphs = new LinkedList<>();
            saxParser.parse(fileName, new SvcLogicHandler(graphs));

            try {
                for (SvcLogicGraph graph : graphs) {
                    graph.setMd5sum(CheckSumHelper.md5SumFromFile(fileName));
                }
            } catch (Exception exc) {
                LOGGER.error("Couldn't set md5sum on graphs", exc);
            }
        } catch (Exception e) {
            LOGGER.error("Parsing failed ", e);
            String msg = e.getMessage();
            if (msg != null) {
                throw new SvcLogicException("Compiler error: " + fileName + " @ " + msg);
            } else {
                throw new SvcLogicException("Compiler error: " + fileName, e);
            }
        }
        return graphs;
    }

    public static void load(String xmlfile, SvcLogicStore store) throws SvcLogicException {
        File xmlFile = new File(xmlfile);
        if (!xmlFile.canRead()) {
            throw new ConfigurationException("Cannot read xml file (" + xmlfile + ")");
        }

        BaseSvcLogicParser parser = new BaseSvcLogicParser();
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

        BaseSvcLogicParser parser = new BaseSvcLogicParser();
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

    protected static void print(String module, String rpc, String mode, String version, SvcLogicStore store) {
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

    protected static void getSource(String module, String rpc, String mode, String version, SvcLogicStore store) {
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

    protected SAXParser initParser() throws ParserConfigurationException, SAXException {
        URL xsdUrl = null;
        Schema schema = null;
        String validateSchema = System.getProperty(SLI_VALIDATING_PARSER, "true");

        if ("true".equalsIgnoreCase(validateSchema)) {
            xsdUrl = getClass().getResource(SVCLOGIC_XSD);
        }

        if (xsdUrl != null) {
            try {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                schema = schemaFactory.newSchema(xsdUrl);
                LOGGER.info("Schema path {}", xsdUrl.getPath());
            } catch (Exception e) {
                LOGGER.warn("Could not validate using schema {}", xsdUrl.getPath(), e);
            }
        } else {
            LOGGER.warn("Could not find resource {}", SVCLOGIC_XSD);
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();

        if (schema != null) {
            factory.setNamespaceAware(true);
            factory.setSchema(schema);
        }

        SAXParser saxParser = factory.newSAXParser();
        if (saxParser.isValidating()) {
            LOGGER.info("Parser configured to validate XML {}", (xsdUrl != null ? xsdUrl.getPath() : null));
        }
        return saxParser;
    }


}
