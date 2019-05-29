package org.onap.ccsdk.sli.core.sli;

import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SvcLogicHandler extends DefaultHandler {
        private Locator locator = null;
        private String module = null;
        private String version = null;
        private LinkedList<SvcLogicGraph> graphs = null;
        private SvcLogicGraph curGraph = null;
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

                curGraph = new SvcLogicGraph();
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

                        curNode.mapParameter(parmName, parmValue);
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
                            attrValue = SvcLogicExpressionFactory.parse(evalExpr);

                        } else {
                            if (Character.isDigit(attrValueStr.charAt(0))) {
                                attrValue = new SvcLogicAtom("NUMBER", attrValueStr);
                            } else {
                                attrValue = new SvcLogicAtom("STRING", attrValueStr);
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