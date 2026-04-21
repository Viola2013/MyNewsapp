package com.example.android.mynewsapp;

import android.text.TextUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
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
        Log.i(LOG_TAG, "Fetching data from: " + queryUrl);
        URL url = generateURL(queryUrl);
        String jsonResponse = NULL_STRING;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        List<NewsListing> newsListings = new ArrayList<>();
        if (queryUrl.contains("guardianapis.com")) {
            List<NewsListing> guardianNews = parseGuardianJSON(jsonResponse);
            if (guardianNews != null) newsListings.addAll(guardianNews);
        } else if (queryUrl.contains("newsapi.org") || queryUrl.contains("yle.fi")) {
            List<NewsListing> newsApiNews = parseNewsApiJSON(jsonResponse);
            if (newsApiNews != null) newsListings.addAll(newsApiNews);
        } else if (queryUrl.contains("newsdata.io")) {
            List<NewsListing> newsDataNews = parseNewsDataJSON(jsonResponse);
            if (newsDataNews != null) newsListings.addAll(newsDataNews);
        } else if (queryUrl.contains("thenewsapi.com")) {
            List<NewsListing> theNewsApiNews = parseTheNewsApiJSON(jsonResponse);
            if (theNewsApiNews != null) newsListings.addAll(theNewsApiNews);
        }
        return newsListings;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setRequestMethod(REQUEST_METHOD);
            // Re-enabling User-Agent as it was not the cause of 403 (it was 429 rate limit)
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                InputStream errorStream = urlConnection.getErrorStream();
                if (errorStream != null) {
                    String errorResponse = readFromStream(errorStream);
                    Log.e(LOG_TAG, "Error response: " + errorResponse);
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static URL generateURL(String queryUrl) {
        URL url = null;
        try {
            url = new URL(queryUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName(CHARSET_ENCODING));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    public static List<NewsListing> parseGuardianJSON(InputStream inputStream) {
        if (inputStream == null) return null;
        try {
            String jsonResponse = readFromStream(inputStream);
            return parseGuardianJSON(jsonResponse);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem parsing guardian json from stream", e);
            return null;
        }
    }

    public static List<NewsListing> parseNewsApiJSON(InputStream inputStream) {
        if (inputStream == null) return null;
        try {
            String jsonResponse = readFromStream(inputStream);
            return parseNewsApiJSON(jsonResponse);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem parsing NewsApi json from stream", e);
            return null;
        }
    }

    public static List<NewsListing> parseGuardianJSON(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) return null;
        return parseGuardianJSON(new JSONTokener(jsonString));
    }

    private static List<NewsListing> parseGuardianJSON(JSONTokener tokener) {
        List<NewsListing> out = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(tokener);
            JSONObject response = root.optJSONObject("response");
            if (response == null) return null;
            JSONArray results = response.optJSONArray("results");
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
            Log.e(LOG_TAG, "problem parsing guardian json", e);
        }
        return out;
    }

    private static List<NewsListing> parseNewsDataJSON(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) return null;
        List<NewsListing> out = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray results = root.optJSONArray("results");
            if (results == null) return null;
            for (int i = 0; i < results.length(); ++i) {
                JSONObject article = results.getJSONObject(i);
                final String title = article.optString("title");
                final String webUrl = article.optString("link");
                final String date = article.optString("pubDate");
                
                JSONArray creators = article.optJSONArray("creator");
                String author = "";
                if (creators != null && creators.length() > 0) {
                    author = creators.optString(0);
                }
                
                final String trailText = article.optString("description");
                final String sectionName = article.optString("source_id");

                NewsListing tmpNewslist = new NewsListing(title, webUrl, sectionName, formatDateForAdapter(date));
                tmpNewslist.setAuthor(author);
                tmpNewslist.setTrailText(trailText);
                out.add(tmpNewslist);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "problem parsing newsdata.io json", e);
        }
        return out;
    }

    /**
     * Helper to format various date strings to ISO-8601 (or close enough for ZonedDateTime.parse)
     * NewsData.io uses "yyyy-MM-dd HH:mm:ss"
     */
    private static String formatDateForAdapter(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "";
        // If it already looks like ISO (contains T or Z), return it
        if (rawDate.contains("T") || rawDate.contains("Z")) return rawDate;
        // NewsData format "2024-05-24 12:34:56" -> "2024-05-24T12:34:56Z"
        if (rawDate.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
            return rawDate.replace(" ", "T") + "Z";
        }
        return rawDate;
    }

    private static List<NewsListing> parseTheNewsApiJSON(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) return null;
        List<NewsListing> out = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray data = root.optJSONArray("data");
            if (data == null) return null;
            for (int i = 0; i < data.length(); ++i) {
                JSONObject article = data.getJSONObject(i);
                final String title = article.optString("title");
                final String webUrl = article.optString("url");
                final String date = article.optString("published_at");
                final String author = article.optString("source");
                final String trailText = article.optString("description");
                
                JSONArray categories = article.optJSONArray("categories");
                String sectionName = "";
                if (categories != null && categories.length() > 0) {
                    sectionName = categories.optString(0);
                }

                NewsListing tmpNewslist = new NewsListing(title, webUrl, sectionName, date);
                tmpNewslist.setAuthor(author);
                tmpNewslist.setTrailText(trailText);
                out.add(tmpNewslist);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "problem parsing thenewsapi.com json", e);
        }
        return out;
    }

    private static List<NewsListing> parseNewsApiJSON(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) return null;
        List<NewsListing> out = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray articles = root.optJSONArray("articles");
            if (articles == null) return null;
            for (int i = 0; i < articles.length(); ++i) {
                JSONObject article = articles.getJSONObject(i);
                final String title = article.optString("title");
                final String webUrl = article.optString("url");
                final String date = article.optString("publishedAt");
                final String author = article.optString("author");
                final String trailText = article.optString("description");
                
                JSONObject source = article.optJSONObject("source");
                String sectionName = "";
                if (source != null) {
                    sectionName = source.optString("name");
                }

                NewsListing tmpNewslist = new NewsListing(title, webUrl, sectionName, date);
                tmpNewslist.setAuthor(author);
                tmpNewslist.setTrailText(trailText);
                out.add(tmpNewslist);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "problem parsing newsapi json", e);
        }
        return out;
    }
}