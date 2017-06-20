package com.codota.sdkexamples;

import com.codota.service.client.CodotaConnectionException;
import com.codota.service.client.CodotaHttpException;
import com.codota.service.client.SearchClient;
import com.codota.service.connector.ApacheServiceConnector;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shaia on 20/06/2017.
 */
public class ManualDependenciesGetter {

    /**
     * set this to your codepack
     */

    private SearchClient codotaClient;


    public ManualDependenciesGetter(String token) {
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


    public List<String> getManualDependencies(String codePack, String artifactName, String filePath) {
        try {
            return this.codotaClient.getManualDependencies(codePack, artifactName, filePath);
        } catch (CodotaHttpException e) {
            e.printStackTrace();
        } catch (CodotaConnectionException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static void main(String[] args) {
        ManualDependenciesGetter instance = new ManualDependenciesGetter(CodotaSDKSettings.VALID_TOKEN);
        List<String> manualDependencies = instance.getManualDependencies(
                "github_square_okhttp_560dae058b9d0b03006e7e97",
                "com.squareup.okhttp.mockwebserver",
                "com/squareup/okhttp/internal/framed/FramedServer.java");
        System.out.println("Manual Dependencies: " + manualDependencies);
    }
}
