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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindUsages {

    /**
     * set this to your codepack
     */

    private SearchClient codotaClient;


    public FindUsages(String token, String codePack) {
        codotaClient = SearchClient.client(ApacheServiceConnector.instance());
        assert codotaClient != null;
        codotaClient.setDefaultCodePack(codePack);
        if (token != null) {
            codotaClient.setToken(token);
            validateToken(token);
        }

    }


    public static void main(String[] args) {
        FindUsages instance = new FindUsages(CodotaSDKSettings.VALID_TOKEN, CodotaSDKSettings.CODE_PACK);
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
        getTypeahead("BlockingQueue");

        // translating qualified name to internal name using the typeahead interface
        String internalName = getInternalName("java.util.concurrent.BlockingQueue");

        // get bookmarks for a given class key
        List<Bookmark> rawBookmarks = getBookmarks(internalName);

        if (CodotaSDKSettings.DECRYPTION_SERVICE_URL != null) {
            try {
                Collection<Bookmark> bookmarks = codotaClient.decryptBatch(CodotaSDKSettings.DECRYPTION_SERVICE_URL,rawBookmarks);
                if (bookmarks != null) {
                    System.out.println("Decrypted Bookmarks " +  bookmarks);
                }
            } catch (CodotaHttpException e) {
                e.printStackTrace();
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


    /**
     * Example for getting the list of typeahead hits for a given prefix String
     */
    private XRefTypeaheadResult getTypeahead(String prefix) {
        try {
            XRefTypeaheadResult xr = codotaClient.xreftypeahead(prefix);
            for (Map.Entry<String, XRefTypeaheadInfo > entry : xr.entrySet()) {
                System.out.println(entry.getKey() + " -> " + entry.getValue());
            }
            return xr;
        } catch (CodotaHttpException e) {
            e.printStackTrace();
        } catch (CodotaConnectionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * translate a fully qualified name into an internal name (JVM representation)
     * @param qualifiedName - fully qualified name of a class
     * @return internal name (JVM style)
     */
    private String getInternalName(String qualifiedName) {
        XRefTypeaheadResult xr = getTypeahead(qualifiedName);
        assert xr != null;
        XRefTypeaheadInfo xi = xr.get(qualifiedName);
        if (xi != null) {
            return xi.key;
        } else {
            return null;
        }
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
    private List<Bookmark> getBookmarks(String classKey) {
        Map<String, String> props = new HashMap<String, String>();
        props.put("all", "false");
        props.put("classKey", classKey);
        props.put("derived", "false");
        props.put("type", "CLASS");
        props.put("codePack", CodotaSDKSettings.CODE_PACK);

        try {
            CrossRefResults cr = codotaClient.searchCrossRef(props);

            for (Bookmark b : cr.bookmarks) {
                //System.out.println("**");
                System.out.println(b);
                if (b.location != null) {
                    System.out.println(b.location + " of class " + b.location.getClass());
                }
            }
            System.out.println("CrossRef:" + cr);
            return cr.bookmarks;
        } catch (CodotaHttpException e) {
            e.printStackTrace();
        } catch (CodotaConnectionException e) {
            e.printStackTrace();
        }
        return null;
    }





}
