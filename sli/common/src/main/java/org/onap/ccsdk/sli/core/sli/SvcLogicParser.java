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
import java.io.IOException;
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
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author dt5972
 *
 */
public class SvcLogicParser extends BaseSvcLogicParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SvcLogicParser.class);
    private static final String SVC_LOGIC_STORE_ERROR = "Could not get service logic store";

    public static void main(String argv[]) {

        if (argv.length == 0) {
            SvcLogicParser.usage();
        }

        if ("load".equalsIgnoreCase(argv[0])) {
            if (argv.length == 3) {
                String xmlfile = argv[1];
                String propfile = argv[2];

                SvcLogicStore store = SvcLogicParser.getStore(propfile);
                try {
                    SvcLogicParser.load(xmlfile, store);
                } catch (Exception e) {
                    LOGGER.error("Load failed ", e);
                }
            } else {
                SvcLogicParser.usage();
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
                    SvcLogicStore store = SvcLogicParser.getStore(propfile);
                    SvcLogicParser.print(argv[1], argv[2], argv[3], version, store);
                    break;
                default:
                    SvcLogicParser.usage();
            }
        } else if ("get-source".equalsIgnoreCase(argv[0])) {

            if (argv.length == 6) {
                SvcLogicStore store = SvcLogicParser.getStore(argv[5]);
                SvcLogicParser.getSource(argv[1], argv[2], argv[3], argv[4], store);
            } else {
                SvcLogicParser.usage();
            }
        } else if ("activate".equalsIgnoreCase(argv[0])) {
            if (argv.length == 6) {
                SvcLogicStore store = SvcLogicParser.getStore(argv[5]);
                SvcLogicParser.activate(argv[1], argv[2], argv[3], argv[4], store);
            } else {
                SvcLogicParser.usage();
            }
        } else if ("validate".equalsIgnoreCase(argv[0])) {
            if (argv.length == 3) {
                String xmlfile = argv[1];
                String propfile = argv[2];

                System.setProperty(SLI_VALIDATING_PARSER, "true");
                SvcLogicStore store = SvcLogicParser.getStore(propfile);
                try {
                    SvcLogicParser.validate(xmlfile, store);
                } catch (Exception e) {
                    LOGGER.error("Validate failed", e);
                }
            } else {
                SvcLogicParser.usage();
            }
        } else if ("install".equalsIgnoreCase(argv[0])) {
            if (argv.length == 3) {
                String propfile = argv[2];
                SvcLogicStore store = SvcLogicParser.getStore(propfile);
                SvcLogicLoader loader = new SvcLogicLoader(argv[1], store);
                try {
                    loader.loadAndActivate();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                SvcLogicParser.usage();
            }
        } else if ("bulkActivate".equalsIgnoreCase(argv[0])) {
            if (argv.length == 3) {
                String propfile = argv[2];
                SvcLogicStore store = SvcLogicParser.getStore(propfile);
                SvcLogicLoader loader = new SvcLogicLoader(argv[1], store);
                try {
                    loader.bulkActivate();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                SvcLogicParser.usage();
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
