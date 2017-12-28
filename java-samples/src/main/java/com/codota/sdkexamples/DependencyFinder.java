/*
 * Copyright (C) 2016 Codota
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codota.sdkexamples;

import com.codota.service.client.CodotaConnectionException;
import com.codota.service.client.CodotaHttpException;
import com.codota.service.client.SearchClient;
import com.codota.service.connector.ApacheServiceConnector;
import com.codota.service.connector.ConnectorSettings;
import com.codota.service.model.DependencyInfo;

import java.util.Collection;

public class DependencyFinder {

    /**
     * set this to your codepack
     */

    private SearchClient codotaClient;


    public DependencyFinder(String token, String codePack) {
        ConnectorSettings.setHost(ConnectorSettings.Host.GATEWAY);
        codotaClient = SearchClient.client(ApacheServiceConnector.instance());
        assert codotaClient != null;
        codotaClient.setDefaultCodePack(codePack);
        if (token != null) {
            codotaClient.setToken(token);
            validateToken(token);
        }

    }

    /**
     * check if the given authentication token is valid
     * @param token - token
     */
    private void validateToken(String token) {
        boolean result = codotaClient.isTokenValid(token);
        System.out.println("====== Is token valid? = " + result + " ======");
    }


    public void processArtifact(String artifactName) {
        try {
            for (String filepath : codotaClient.allFilesForArtifact(artifactName)) {
                System.out.println("\n****** Processing file " + filepath);
                try {
                    DependencyInfo dependencies = codotaClient.getDependencies(filepath, artifactName);
                    for (DependencyInfo.InternalDependency dep : dependencies.getInternalDeps()) {
                        System.out.println("Internal dependency on " + dep.getFilepath() + " in artifacts " + dep.getArtifactNames());
                    }
                    for (Collection<DependencyInfo.OptionalInternalDependency> group: dependencies.getInternalDepsExtended()) {
                        System.out.println("Internal dependency on group " + group);
                    }

                    for (String extDep : dependencies.getExternalDeps()) {
                        System.out.println("External dependency on " + extDep);
                    }
                    for (String extDep : dependencies.getExternalImportOnlyDeps()) {
                        System.out.println("External Import-only dependency on " + extDep);
                    }

                } catch (CodotaHttpException e) {

                    // We may get an 404 if the artifact contains non-source files
                    System.err.println("Could not find dependencies for " + filepath + " - HTTP Response " + e.toString());
                }
            }
        } catch (CodotaHttpException e) {
            e.printStackTrace();
        } catch (CodotaConnectionException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DependencyFinder instance = new DependencyFinder(CodotaSDKSettings.VALID_TOKEN, CodotaSDKSettings.CODE_PACK);
        instance.processArtifact("com.squareup.okhttp.mockwebserver");
    }

}
