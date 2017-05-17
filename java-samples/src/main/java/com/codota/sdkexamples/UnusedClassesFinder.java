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
import com.codota.service.client.CrossRefResults;
import com.codota.service.client.SearchClient;
import com.codota.service.connector.ApacheServiceConnector;
import com.codota.service.connector.ConnectorSettings;
import com.codota.service.model.Bookmark;
import com.codota.service.model.XRefTypeaheadInfo;
import com.codota.service.model.XRefTypeaheadResult;

import java.util.*;

public class UnusedClassesFinder {

    /**
     * set this to your codepack
     */

    private SearchClient codotaClient;


    public UnusedClassesFinder(String token, String codePack) {
        codotaClient = SearchClient.client(ApacheServiceConnector.instance());
        assert codotaClient != null;
        codotaClient.setDefaultCodePack(codePack);
        if (token != null) {
            codotaClient.setToken(token);
            validateToken(token);
        }

    }


    public static void main(String[] args) {
        UnusedClassesFinder instance = new UnusedClassesFinder(CodotaSDKSettings.VALID_TOKEN, CodotaSDKSettings.CODE_PACK);
        try {
            instance.findUnusedClasses("com.squareup.okhttp.mockwebserver");
        } catch (CodotaHttpException e) {
            e.printStackTrace();
        } catch (CodotaConnectionException e) {
            e.printStackTrace();
        }
    }

    public void findUnusedClasses(String artifactName) throws CodotaHttpException, CodotaConnectionException {
        List<String> classes = codotaClient.allClassesForArtifact(artifactName);
        System.out.printf("Found %d classes for artifact %s\n", classes.size(), artifactName);
        int cnt = 0;
        for (String klass : classes) {
            cnt++;
            System.out.println("Checking class " + klass);
            if (cnt % 100 == 0) {
                System.out.printf("Checking class %d/%d\n", cnt, classes.size());
            }
            boolean isUsed = isUsedByOtherClasses(klass);
            if (!isUsed) {
                System.out.printf("Potentially unused: %s\n",  klass);
            }
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




    private boolean isUsedByOtherClasses(String className) throws CodotaHttpException, CodotaConnectionException {
        String classKey = "L" + className + ";";
        for (Bookmark ref : getReferences(classKey)) {

            // Checking whether this reference is from a different class
            if (!className.equals(ref.resourceFullyQualifiedName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * get bookmarks for a given class key
     * @param classKey - the fully qualified name of the class in JVM internal name form
     * @return list of (typically encrypted) bookmarks
     *
     * This method takes a name of a class as a query and returns the list of bookmarks for this class.
     * The classKey parameter should be an internal class name, for example: "Ljava/util/concurrent/BlockingQueue;"
     * You can obtain the internal class name for a class using the xreftypeahead API.
     */
    private List<Bookmark> getReferences(String classKey) throws CodotaHttpException, CodotaConnectionException {
        Map<String, String> props = new HashMap<String, String>();
        props.put("all", "false");
        props.put("classKey", classKey);
        props.put("derived", "false");
        props.put("type", "CLASS");
        props.put("codePack", CodotaSDKSettings.CODE_PACK);

        CrossRefResults cr = codotaClient.searchCrossRef(props);
        return cr.bookmarks;
    }





}
