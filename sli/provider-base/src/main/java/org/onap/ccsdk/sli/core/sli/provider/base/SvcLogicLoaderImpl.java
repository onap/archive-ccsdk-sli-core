/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.ccsdk.sli.core.sli.provider.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.onap.ccsdk.sli.core.api.SvcLogicGraph;
import org.onap.ccsdk.sli.core.api.exceptions.ConfigurationException;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.api.util.ActivationEntry;
import org.onap.ccsdk.sli.core.api.util.SvcLogicLoader;
import org.onap.ccsdk.sli.core.api.util.SvcLogicParser;
import org.onap.ccsdk.sli.core.api.util.SvcLogicStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvcLogicLoaderImpl implements SvcLogicLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SvcLogicLoaderImpl.class);
    protected SvcLogicStore store;
    protected SvcLogicParser parser;

    public SvcLogicLoaderImpl(SvcLogicStore store, SvcLogicParser parser) {
        this.store = store;
        this.parser = parser;
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicLoader#loadAndActivate(java.lang.String)
     */
    public void loadAndActivate(String directoryRoot) throws IOException {
        SvcLogicCrawler slc = new SvcLogicCrawler();
        Files.walkFileTree(Paths.get(directoryRoot), slc);
        loadGraphs(slc.getGraphPaths(), directoryRoot, store);
        List<ActivationEntry> activationEntries = processActivationFiles(slc.getActivationPaths());
        activateGraphs(activationEntries, store);
    }

    protected List<ActivationEntry> processActivationFiles(List<Path> activationPaths) {
        List<ActivationEntry> activationEntries = new ArrayList<>();
        for (Path activationFile : activationPaths) {
            activationEntries.addAll(getActivationEntries(activationFile));
        }
        return activationEntries;
    }

    protected void activateGraphs(List<ActivationEntry> activationEntries, SvcLogicStore store) {
        for (ActivationEntry entry : activationEntries) {
            try {
                if (store.hasGraph(entry.getModule(), entry.getRpc(), entry.getVersion(), entry.getMode())) {
                    LOGGER.info("Performing active on {}", entry.toString());
                    store.activate(entry.getModule(), entry.getRpc(), entry.getVersion(), entry.getMode());
                } else {
                    LOGGER.error("hasGraph returned false for " + entry.toString());
                }
            } catch (SvcLogicException e) {
                LOGGER.error("Failed to call hasGraph for " + entry.toString(), e);
            }
        }
    }

    protected List<ActivationEntry> getActivationEntries(Path activationFilePath) {
        List<ActivationEntry> activationEntries = new ArrayList<>();
        int lineNumber = 1;
        try (BufferedReader br = Files.newBufferedReader(activationFilePath, StandardCharsets.US_ASCII)) {
            String fileRead = br.readLine();
            while (fileRead != null) {
                String[] fields = fileRead.split("\\s");
                if (fields.length == 4) {
                    activationEntries.add(parseActivationEntry(fields));
                } else {
                    LOGGER.error("Activation entry [" + fileRead + "] is declared at line number " + lineNumber
                            + " in the file " + activationFilePath + " and is invalid.");
                }
                fileRead = br.readLine();
                lineNumber++;
            }
            return activationEntries;
        } catch (IOException ioe) {
            LOGGER.error("Couldn't read the activation file at " + activationFilePath, ioe);
            return new ArrayList<>();
        }
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicLoader#loadGraphs(java.util.List, java.lang.String, org.onap.ccsdk.sli.core.api.util.SvcLogicStore)
     */
    public void loadGraphs(List<Path> graphPaths, String directoryRoot, SvcLogicStore store) {
        for (Path graphPath : graphPaths) {
            try {
                saveGraph(graphPath.toString(), store);
            } catch (Exception e) {
                LOGGER.error("Couldn't load graph at " + graphPath, e);
            }
        }
    }

    protected void saveGraph(String xmlFile, SvcLogicStore store) throws SvcLogicException {
        File f = new File(xmlFile);
        if (!f.canRead()) {
            throw new ConfigurationException("Cannot read xml file (" + xmlFile + ")");
        }

        LinkedList<SvcLogicGraph> graphs = null;

        try {
            graphs = parser.parse(xmlFile);
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage(), e);
        }

        if (graphs == null) {
            throw new SvcLogicException("Could not parse " + xmlFile);
        }

        for (Iterator<SvcLogicGraph> iter = graphs.iterator(); iter.hasNext();) {
            SvcLogicGraph graph = iter.next();
            try {
                LOGGER.info("Saving " + graph.toString() + " to database");
                store.store(graph);
            } catch (Exception e) {
                throw new SvcLogicException(e.getMessage(), e);
            }
        }
    }

    protected ActivationEntry parseActivationEntry(String[] fileInput) {
        return new ActivationEntry(fileInput[0], fileInput[1], fileInput[2], fileInput[3]);
    }
    
    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicLoader#bulkActivate(java.lang.String)
     */
    public void bulkActivate(String directoryRoot) {
        Path activationFile = Paths.get(directoryRoot);
        List<Path> pathList = new ArrayList<>(1);
        pathList.add(activationFile);
        List<ActivationEntry> activationEntries = processActivationFiles(pathList);
        activateGraphs(activationEntries, store);
    }

}
