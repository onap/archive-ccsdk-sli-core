/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
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

package org.onap.ccsdk.sli.core.api.util;

public class ActivationEntry {
    public String getModule() {
        return module;
    }

    public String getRpc() {
        return rpc;
    }

    public String getVersion() {
        return version;
    }

    public String getMode() {
        return mode;
    }

    private String module;
    private String rpc;
    private String version;
    private String mode;

    public ActivationEntry(String module, String rpc, String version, String mode) {
        this.module = module;
        this.rpc = rpc;
        this.version = version;
        this.mode = mode;
    }
    
    @Override
    public String toString() {
        return "ActivationEntry [module=" + module + ", rpc=" + rpc + ", version=" + version + ", mode=" + mode + "]";
    }
}
