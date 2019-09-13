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

package org.onap.ccsdk.sli.core.parser;

import java.net.URL;
import java.util.LinkedList;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.onap.ccsdk.sli.core.api.SvcLogicGraph;
import org.onap.ccsdk.sli.core.api.SvcLogicNode;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.api.lang.SvcLogicExpression;
import org.onap.ccsdk.sli.core.api.lang.SvcLogicExpressionParser;
import org.onap.ccsdk.sli.core.api.util.SvcLogicParser;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicGraphImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicNodeImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.lang.SvcLogicAtomImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.util.CheckSumHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author dt5972
 *
 */
public class SvcLogicParserImpl implements SvcLogicParser {

    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    static final String JAXP_DYNAMIC_VALIDATION = "http://apache.org/xml/features/validation/dynamic";
    static final String JAXP_SCHEMA_VALIDATION = "http://apache.org/xml/features/validation/schema";



    private static final Logger LOGGER = LoggerFactory.getLogger(SvcLogicParserImpl.class);
    private static final String SLI_VALIDATING_PARSER = "org.onap.ccsdk.sli.parser.validate";
    private static final String SVCLOGIC_XSD = "/svclogic.xsd";
    private SAXParser saxParser;

    private class SvcLogicHandler extends DefaultHandler {
        protected SvcLogicExpressionParser parser = new SvcLogicExpressionParserImpl();
        private Locator locator = null;
        private String module = null;
        private String version = null;
        private LinkedList<SvcLogicGraph> graphs = null;
        private SvcLogicGraphImpl curGraph = null;
        private SvcLogicNode curNode = null;
        private LinkedList<SvcLogicNode> nodeStack = null;
        private int curNodeId = 0;
        private String outcomeValue = null;
        private LinkedList<String> outcomeStack = null;

        public SvcLogicHandler(LinkedList<SvcLogicGraph> graphs) {
            this.graphs = graphs;
            this.curNode = null;
            this.nodeStack = new LinkedList<>();
            this.outcomeStack = new LinkedList<>();
            this.curNodeId = 1;
            this.outcomeValue = null;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {

            // Handle service-logic (graph) tag
            if ("service-logic".equalsIgnoreCase(qName)) {

                module = attributes.getValue("module");
                if (module == null || module.length() == 0) {
                    throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                            + "Missing 'module' attribute from service-logic tag");
                }

                version = attributes.getValue("version");
                if (version == null || version.length() == 0) {
                    throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                            + "Missing 'version' attribute from service-logic tag");
                }

                return;
            }

            if ("method".equalsIgnoreCase(qName)) {

                if (curGraph != null) {
                    throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                            + "Cannot nest module tags");
                }

                curGraph = new SvcLogicGraphImpl();
                curGraph.setModule(module);
                curGraph.setVersion(version);
                this.curNodeId = 1;

                String attrValue = attributes.getValue("rpc");
                if (attrValue == null || attrValue.length() == 0) {
                    throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                            + "Missing 'rpc' attribute for method tag");
                }
                curGraph.setRpc(attrValue);

                attrValue = attributes.getValue("mode");
                if (attrValue == null || attrValue.length() == 0) {
                    throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                            + "Missing 'mode' attribute for method tag");
                }
                curGraph.setMode(attrValue);

                return;
            }

            // Handle outcome (edge) tag
            if ("outcome".equalsIgnoreCase(qName)) {
                String refValue = attributes.getValue("ref");

                if (refValue != null) {
                    SvcLogicNode refNode = curGraph.getNamedNode(refValue);

                    if (refNode != null) {
                        try {
                            curNode.addOutcome(attributes.getValue("value"), refNode);
                        } catch (SvcLogicException e) {
                            throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber()
                                    + " " + "Cannot add outcome", e);
                        }
                    } else {
                        throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                                + "ref to unknown node " + refValue);
                    }
                    return;
                }

                if (outcomeValue != null) {
                    outcomeStack.push(outcomeValue);
                }
                outcomeValue = attributes.getValue("value");

                return;
            }

            // Handle parameter tag
            if ("parameter".equalsIgnoreCase(qName)) {
                String parmName = attributes.getValue("name");
                String parmValue = attributes.getValue("value");

                if (parmName != null && parmName.length() > 0 && parmValue != null) {
                    try {

                        curNode.mapParameter(parmName, parmValue, parser);
                    } catch (Exception e) {
                        throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                                + " cannot set parameter " + parmName + " to " + parmValue + " [" + e.getMessage()
                                + "]");
                    }
                }

                return;
            }

            // Handle node tags
            String nodeName = attributes.getValue("name");
            SvcLogicNode thisNode;


            try {
                if (nodeName != null && nodeName.length() > 0) {
                    thisNode = new SvcLogicNodeImpl(curNodeId++, qName, nodeName, curGraph);
                } else {
                    thisNode = new SvcLogicNodeImpl(curNodeId++, qName, curGraph);
                }

                if (curGraph.getRootNode() == null) {
                    curGraph.setRootNode(thisNode);
                }
            } catch (SvcLogicException e) {
                throw new SAXException(
                        "line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " " + e.getMessage());

            }

            int numAttributes = attributes.getLength();

            for (int i = 0; i < numAttributes; i++) {
                String attrName = attributes.getQName(i);
                if (!"name".equalsIgnoreCase(attrName)) {
                    try {

                        String attrValueStr = attributes.getValue(i);
                        SvcLogicExpression attrValue;
                        if (attrValueStr.trim().startsWith("`")) {
                            int lastParen = attrValueStr.lastIndexOf('`');
                            String evalExpr = attrValueStr.trim().substring(1, lastParen);
                            attrValue = parser.parse(evalExpr);

                        } else {
                            if (Character.isDigit(attrValueStr.charAt(0))) {
                                attrValue = new SvcLogicAtomImpl("NUMBER", attrValueStr);
                            } else {
                                attrValue = new SvcLogicAtomImpl("STRING", attrValueStr);
                            }
                        }
                        thisNode.setAttribute(attrName, attrValue);
                    } catch (Exception e) {
                        throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                                + "Cannot set attribute " + attrName, e);
                    }
                }
            }

            if (curNode != null) {
                try {
                    if ("block".equalsIgnoreCase(curNode.getNodeType()) || "for".equalsIgnoreCase(curNode.getNodeType())
                            || "while".equalsIgnoreCase(curNode.getNodeType())) {
                        curNode.addOutcome(Integer.toString(curNode.getNumOutcomes() + 1), thisNode);
                    } else {
                        if (outcomeValue == null) {
                            throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber()
                                    + " " + curNode.getNodeType() + " node expects outcome, instead found "
                                    + thisNode.getNodeType());
                        }
                        curNode.addOutcome(outcomeValue, thisNode);
                    }
                } catch (SvcLogicException e) {
                    throw new SAXException(
                            "line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " " + e.getMessage());
                }
                nodeStack.push(curNode);
            }
            curNode = thisNode;

        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            // Handle close of service-logic tag
            if ("service-logic".equalsIgnoreCase(qName)) {
                // Nothing more to do
                return;
            }

            // Handle close of method tag
            if ("method".equalsIgnoreCase(qName)) {
                graphs.add(curGraph);
                curGraph = null;
                return;
            }

            // Handle close of outcome tag
            if ("outcome".equalsIgnoreCase(qName)) {
                // Finished this outcome - pop the outcome stack
                if (outcomeStack.isEmpty()) {
                    outcomeValue = null;
                } else {
                    outcomeValue = outcomeStack.pop();
                }
                return;
            }

            // Handle close of parameter tag - do nothing
            if ("parameter".equalsIgnoreCase(qName)) {
                return;
            }

            // Handle close of a node tag
            if (nodeStack.isEmpty()) {
                curNode = null;
            } else {
                curNode = nodeStack.pop();
            }
        }

        @Override
        public void error(SAXParseException arg0) throws SAXException {
            throw new SAXException(
                    "line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " " + arg0.getMessage());
        }

    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicParser#parse(java.lang.String)
     */
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