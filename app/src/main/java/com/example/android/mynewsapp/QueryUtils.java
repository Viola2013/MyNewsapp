package com.example.android.mynewsapp;

import android.text.TextUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mervi on 1.7.2017.
 */

final class QueryUtils {
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();
    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECTION_TIMEOUT = 15000;
    private static final String REQUEST_METHOD = "GET";
    private static final String CHARSET_ENCODING = "UTF-8";
    private static final String NULL_STRING = "";

    private QueryUtils() {
    }

    public static List<NewsListing> fetchData(String queryUrl) {
        URL url = generateURL(queryUrl);
        String jsonResponse = NULL_STRING;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }
        return parseJSON(jsonResponse);
    }

    private static URL generateURL(String queryString) {
        URL url = null;
        try {
            url = new URL(queryString);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error creating url", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = NULL_STRING;

        if (url == null) return jsonResponse;

        HttpURLConnection urlConnection = null;
        InputStream iStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setRequestMethod(REQUEST_METHOD);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                iStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(iStream);
            } else {
                Log.e(LOG_TAG, "Connection error, response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem Retrieving JSON result", e);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
            if (iStream != null) iStream.close();
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream iStream) throws IOException {
        StringBuilder out = new StringBuilder();
        if (iStream != null) {
            InputStreamReader iStreamReader =
                    new InputStreamReader(iStream, Charset.forName(CHARSET_ENCODING));
            BufferedReader buffer = new BufferedReader(iStreamReader);
            String line = buffer.readLine();
            while (line != null) {
                out.append(line);
                line = buffer.readLine();
            }
        }
        return out.toString();
    }

    private static List<NewsListing> parseJSON(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) return null;
        List<NewsListing> out = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray results = root.optJSONObject("response").optJSONArray("results");
            if (results == null) return null;
            for (int i = 0; i < results.length(); ++i) {
                JSONObject currNewslist = results.getJSONObject(i);
                final String title = currNewslist.optString("webTitle");
                final String webUrl = currNewslist.optString("webUrl");
                final String sectionName = currNewslist.optString("sectionName");
                final String date = currNewslist.optString("webPublicationDate");
                JSONObject fields = currNewslist.optJSONObject("fields");
                NewsListing tmpNewslist = new NewsListing(title, webUrl, sectionName, date);

                if (fields != null) {
                    tmpNewslist.setAuthor(fields.optString("byline"));
                    tmpNewslist.setTrailText(fields.optString("trailText"));
                }
                out.add(tmpNewslist);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "problem parsing json", e);
        }
        return out;
    }
}