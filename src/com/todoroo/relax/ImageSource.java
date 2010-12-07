package com.todoroo.relax;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.todoroo.andlib.service.HttpRestClient;
import com.todoroo.andlib.service.RestClient;

public class ImageSource {

    public String[] search(String query, int current) throws IOException, JSONException {
        JSONObject results =
            invokeGet("Query", query, "Sources", "Image",
                    "Image.Offset", Integer.toString(current));
        JSONArray images = results.getJSONObject("Image").getJSONArray("Results");
        String[] urls = new String[images.length()];
        for(int i = 0; i < images.length(); i++) {
            urls[i] = images.getJSONObject(i).getString("MediaUrl");
            System.err.println("got " + urls[i]);
        }
        return urls;
    }

    public boolean hasMoreResults(int current) {
        return current < 1000;
    }

    // --- private implementation

    private static final String APP_ID = "E42CB88A39C376C40352BFDC101822FF81070717";

    private static final String BASE = "http://api.bing.net/json.aspx";

    private RestClient restClient = new HttpRestClient();

    /**
     * Invokes API method using HTTP GET
     *
     * @param method
     *          API method to invoke
     * @param getParameters
     *          Name/Value pairs. Values will be URL encoded.
     * @return response object
     */
    private JSONObject invokeGet(String... getParameters)
            throws IOException, JSONException {
        String request = createFetchUrl(getParameters);
        System.err.println(request);
        String response = restClient.get(request);
        System.err.println(response);
        JSONObject wrapper = new JSONObject(response);
        return wrapper.getJSONObject("SearchResponse");
    }


    /**
     * Creates a URL for invoking an HTTP GET/POST on the given method
     * @param method
     * @param getParameters
     * @return
     */
    private String createFetchUrl(String... parameters) {
        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append(BASE).append("?AppId=").append(APP_ID); //$NON-NLS-1$
        for(int i = 0; i < parameters.length; i += 2) {
            final String encoded;
            if(parameters[i+1] == null)
                encoded = "";
            else
                encoded = URLEncoder.encode(parameters[i+1]);
            requestBuilder.append('&').append(parameters[i]).
                append('=').append(encoded);
        }
        return requestBuilder.toString();
    }
}
