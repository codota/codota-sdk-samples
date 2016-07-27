package com.codota.sdkexamples;

import com.codota.service.client.CodotaConnectionException;
import com.codota.service.client.CodotaHttpException;
import com.codota.service.client.SearchClient;
import com.codota.service.connector.ApacheServiceConnector;
import com.codota.service.model.Artifact;


public class ArtifactReader {

    private SearchClient codotaClient;


    public ArtifactReader(String token, String codePack) {
//        ConnectorSettings.setHost(ConnectorSettings.Host.LOCAL);
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


    public void readArtifactInfo(String artifactName) {

        try {

            Artifact artifactInfo = codotaClient.readArtifact(artifactName);
            System.out.println("Read info for artifact " + artifactInfo.getArtifactName());
            Artifact.ProjectInfo mavenCoordinates = artifactInfo.getProject();
            System.out.printf("\ngroupId: %s\nartifactId: %s\nversion: %s\n\n", mavenCoordinates.getGroupId(),
                    mavenCoordinates.getArtifactId(), mavenCoordinates.getVersion());
            System.out.println("Developers:");
            for (Artifact.DeveloperInfo dev : artifactInfo.getDevelopers()) {
                System.out.printf("***\nName: %s\nEmail: %s\nRoles: %s\n", dev.getName(), dev.getEmail(), dev.getRoles());
            }
        } catch (CodotaHttpException e) {
            e.printStackTrace();
        } catch (CodotaConnectionException e) {
            e.printStackTrace();
        }


    }



    public static void main(String[] args) {
        ArtifactReader instance = new ArtifactReader(CodotaSDKSettings.VALID_TOKEN, CodotaSDKSettings.CODE_PACK);
        instance.readArtifactInfo("joda-time");
    }






}
