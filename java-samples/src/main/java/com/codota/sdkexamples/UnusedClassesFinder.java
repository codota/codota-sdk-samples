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
        instance.findUnusedClasses("okhttp");
    }

    public void findUnusedClasses(String prefix) {
        Map<String, String> classes = getClassesByPrefix(prefix);
        System.out.printf("Found %d classes for prefix %s\n", classes.size(), prefix);
        int cnt = 0;
        for (String classKey : classes.keySet()) {
            cnt++;
            if (cnt % 100 == 0) {
                System.out.printf("Checking class %d/%d\n", cnt, classes.size());
            }
            boolean isUsed = isUsedByOtherClasses(classKey);
            if (!isUsed) {
                System.out.printf("Potentially unused: %s\n",  classes.get(classKey));
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


    /*
     * Get classes (internal names-->nice name) by prefix (in nice name)
     */
    private Map<String, String> getClassesByPrefix(String prefix) {
        try {
            Map<String, String> classes = new HashMap<String, String>();
            XRefTypeaheadResult xr = codotaClient.xreftypeahead(prefix);

            // We're reversing the map returned by the api
            for (Map.Entry<String, XRefTypeaheadInfo > entry : xr.entrySet()) {
                classes.put(entry.getValue().key, entry.getKey());
            }
            return classes;
        } catch (CodotaHttpException e) {
            e.printStackTrace();
        } catch (CodotaConnectionException e) {
            e.printStackTrace();
        }
        return null;
    }


    private boolean isUsedByOtherClasses(String classKey) {

        for (Bookmark ref : getReferences(classKey)) {

            // Checking whether this reference is from a different class
            if (!classKey.equals("L" + ref.resourceFullyQualifiedName + ";")) {
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
    private List<Bookmark> getReferences(String classKey) {
        Map<String, String> props = new HashMap<String, String>();
        props.put("all", "false");
        props.put("classKey", classKey);
        props.put("derived", "false");
        props.put("type", "CLASS");
        props.put("codePack", CodotaSDKSettings.CODE_PACK);

        try {
            CrossRefResults cr = codotaClient.searchCrossRef(props);
            return cr.bookmarks;
        } catch (CodotaHttpException e) {
            e.printStackTrace();
        } catch (CodotaConnectionException e) {
            e.printStackTrace();
        }
        return null;
    }





}
