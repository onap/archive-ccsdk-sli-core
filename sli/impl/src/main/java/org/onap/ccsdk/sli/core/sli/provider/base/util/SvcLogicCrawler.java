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

package org.onap.ccsdk.sli.core.sli.provider.base.util;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class SvcLogicCrawler extends SimpleFileVisitor<Path> {

    private List<Path> xmlGraphPathList;
    private List<Path> activationFilePathList;

    public SvcLogicCrawler() {
        xmlGraphPathList = new ArrayList<>();
        activationFilePathList = new ArrayList<>();
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        if (attr.isRegularFile()) {
            String fileName = file.getFileName().toString();
            if (!file.toString().contains(".git") && !fileName.equals("pom.xml") && !fileName.equals("assemble_zip.xml") && !fileName.equals("assemble_zip_less_config.xml") && !fileName.equals("descriptor.xml")) {
                if (fileName.endsWith(".xml")) {
                    xmlGraphPathList.add(file);
                } 
                else if (fileName.endsWith(".versions")) {
                    activationFilePathList.add(file);
                }
            }
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.println("Couldn't visitFile");
        System.err.println(exc.getMessage());
        return CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        String[] skipDirectories = {".git"};
        for (String str : skipDirectories) {
            if (dir.endsWith(str)) {
                return FileVisitResult.SKIP_SUBTREE;
            }
        }
        return CONTINUE;
    }

    public List<Path> getGraphPaths() {
        return this.xmlGraphPathList;
    }

    public List<Path> getActivationPaths() {
        return this.activationFilePathList;
    }
}