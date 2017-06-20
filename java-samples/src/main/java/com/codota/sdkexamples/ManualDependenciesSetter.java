package com.codota.sdkexamples;

import com.codota.service.client.CodotaConnectionException;
import com.codota.service.client.CodotaHttpException;
import com.codota.service.client.SearchClient;
import com.codota.service.connector.ApacheServiceConnector;
import com.codota.service.model.DependencyInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by shaia on 20/06/2017.
 */
public class ManualDependenciesSetter {

    /**
     * set this to your codepack
     */

    private SearchClient codotaClient;


    public ManualDependenciesSetter(String token) {
        codotaClient = SearchClient.client(ApacheServiceConnector.instance());
        assert codotaClient != null;
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


    public void putManualDependencies(String codePack, String artifactName, String filePath,
                                      Set<String> manualDependencies) {
        try {
            this.codotaClient.putManualDependencies(codePack, artifactName, filePath, manualDependencies);
        } catch (CodotaHttpException e) {
            e.printStackTrace();
        } catch (CodotaConnectionException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ManualDependenciesSetter instance = new ManualDependenciesSetter(CodotaSDKSettings.VALID_TOKEN);
        Set<String> manualDependencies = new HashSet<String>(2);
        manualDependencies.add("com/mypackage/someDependency");
        manualDependencies.add("com/theirpackage/anotherDependency");
        instance.putManualDependencies("my_private_code_pack","my.artifact",
                "com/my/dependent/file.java", manualDependencies);
    }
}
