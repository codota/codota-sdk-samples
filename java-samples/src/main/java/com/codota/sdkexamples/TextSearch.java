package com.codota.sdkexamples;

import com.codota.service.client.CodotaConnectionException;
import com.codota.service.client.CodotaHttpException;
import com.codota.service.client.SearchClient;
import com.codota.service.connector.ApacheServiceConnector;
import com.codota.service.model.TextualMatch;

import java.util.List;

public class TextSearch {

    /**
     * set this to your codepack
     */

    private SearchClient codotaClient;


    public TextSearch(String token, String codePack) {
        codotaClient = SearchClient.client(ApacheServiceConnector.instance());
        assert codotaClient != null;
        codotaClient.setDefaultCodePack(codePack);
        if (token != null) {
            codotaClient.setToken(token);
            validateToken(token);
        }

    }


    public static void main(String[] args) {
        TextSearch instance = new TextSearch(CodotaSDKSettings.VALID_TOKEN, CodotaSDKSettings.CODE_PACK);
        instance.execute();
    }

    /**
     * This example goes through the following steps:
     * (1) creating the client and settings its configuration parameters
     * (2) validating the token
     * (3) printing type-ahead results for a specific example type "BlockingQueue"
     * (4) translating a fully-qualified name to an internal name using the typeahead interface
     * (5) getting all bookmarks (usages) for a given internal class name
     */
    private void execute() {
        /**
         * create the search client and set appropriate client version
         * set configuration parameters
         * - set a valid token (you should set the value in the TokenVault to your own token
         * - set a valid code pack
         */

        // print typeahead results for BlockingQueue search string

        // get bookmarks for a given class key
        try {
            String term = "request";
            List<TextualMatch> matches = textSearch(term, true);
            System.out.println("Found " + matches.size() + " textual matches for " + term);
            System.out.println("==============================================================");

            for (TextualMatch match : matches) {
                System.out.println("Found a match in " + match.filepath + " in artifact " + match.artifactName);
            }
        } catch (CodotaHttpException e) {
            e.printStackTrace();
        } catch (CodotaConnectionException e) {
            e.printStackTrace();
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




    private List<TextualMatch> textSearch(String term, boolean all) throws CodotaHttpException, CodotaConnectionException {
        return codotaClient.textSearch(term, all);
    }




}
