package com.example.android.mynewsapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsListing>> {
    private static final String TAG = "MainActivity";
    private static final int LOADER_ID = 1;
    private static final String SEARCH_QUERY_KEY = "query";
    private static final String USE_MOCK_DATA_KEY = "use_mock";
    private TextView mEmptyStateView;
    private NewsAdapter mAdapter;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmptyStateView = findViewById(R.id.empty_view);
        mProgressBar = findViewById(R.id.progress_bar);

        RecyclerView recyclerView = findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new NewsAdapter(item -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getWebUrl()));
            if (browserIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(browserIntent);
            }
        });
        recyclerView.setAdapter(mAdapter);

        if (getIntent() != null) {
            Log.i(TAG, "onCreate: calling handleIntent");
            handleIntent(getIntent());
        } else {
            Log.i(TAG, "onCreate: getIntent() is null, initiating default load");
            lookupNewsLoaders(null);
        }
    }

    private void handleIntent(Intent intent) {
        final String queryAction = intent.getAction();
        Log.i(TAG, "handleIntent: action=" + queryAction);

        // If it's a MAIN action with no extras, it's just a normal app start.
        // We still want to load something (the default "news").
        if (Intent.ACTION_MAIN.equals(queryAction) && (intent.getExtras() == null || intent.getExtras().isEmpty())) {
            Log.i(TAG, "handleIntent: ACTION_MAIN with no extras, loading defaults");
            lookupNewsLoaders(null);
            return;
        }

        Bundle bundle = new Bundle();

        if (intent.getExtras() != null) {
            for (String key : intent.getExtras().keySet()) {
                Log.i(TAG, "Extra from intent: " + key + " = " + intent.getExtras().get(key));
            }
        }

        // Robustly check for use_mock extra which can be boolean or string from ADB
        if (intent.hasExtra(USE_MOCK_DATA_KEY)) {
            Object value = Objects.requireNonNull(intent.getExtras()).get(USE_MOCK_DATA_KEY);
            boolean useMock = false;
            if (value instanceof Boolean) {
                useMock = (Boolean) value;
            } else if (value instanceof String) {
                useMock = Boolean.parseBoolean((String) value);
            }
            Log.i(TAG, "handleIntent: use_mock resolved to=" + useMock);
            bundle.putBoolean(USE_MOCK_DATA_KEY, useMock);
        }

        if (intent.hasExtra(SEARCH_QUERY_KEY)) {
            String query = intent.getStringExtra(SEARCH_QUERY_KEY);
            Log.i(TAG, "handleIntent: query resolved to=" + query);
            bundle.putString(SEARCH_QUERY_KEY, query);
        }

        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(TAG, "handleIntent: ACTION_SEARCH query=" + query);
            bundle.putString(SEARCH_QUERY_KEY, query);
        }

        Log.i(TAG, "handleIntent: final bundle for loader=" + bundle);
        lookupNewsLoaders(bundle);
    }

    private void lookupNewsLoaders(Bundle bundle) {
        boolean useMock = bundle != null && bundle.getBoolean(USE_MOCK_DATA_KEY, false);
        Log.i(TAG, "lookupNewsLoaders: useMock=" + useMock + ", bundle=" + (bundle != null ? bundle.toString() : "null"));
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (useMock || (cm != null && isNetworkConnected())) {
            mProgressBar.setVisibility(View.VISIBLE);
            Log.i(TAG, "lookupNewsLoaders: restarting loader with bundle");
            LoaderManager.getInstance(this).restartLoader(LOADER_ID, bundle, this);
        } else {
            mEmptyStateView.setVisibility(View.VISIBLE);
            mEmptyStateView.setText(R.string.no_internet);
        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private String createUri(Bundle bundle) {
        String queryString = "news";
        if (bundle != null && bundle.containsKey(SEARCH_QUERY_KEY)) {
            queryString = bundle.getString(SEARCH_QUERY_KEY);
            if (queryString == null) queryString = "news";
        }
        queryString = queryString.trim();
        Log.i(TAG, "createUri: queryString=[" + queryString + "]");

        if (bundle != null) {
            if (bundle.getBoolean(USE_MOCK_DATA_KEY, false)) {
                Log.i(TAG, "createUri: returning mock URL for query=" + queryString);
                if ("yle".equalsIgnoreCase(queryString)) {
                    return "mock://yle";
                }
                return "mock://news";
            }
        }

        if ("newsdata".equalsIgnoreCase(queryString)) {
            String url = "https://newsdata.io/api/1/news?apikey=pub_4b0a1d2836cb4857803810cb4d6d3aba&q=Suomi&language=fi";
            Log.i(TAG, "createUri: identified newsdata query, returning: " + url);
            return url;
        }

        final String GUARDIAN_URL = "https://content.guardianapis.com/search";
        Uri baseUri = Uri.parse(GUARDIAN_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("q", queryString);
        uriBuilder.appendQueryParameter("api-key", "test");
        uriBuilder.appendQueryParameter("show-fields", "byline,trailText");
        uriBuilder.appendQueryParameter("page-size", "10");
        
        String finalUrl = uriBuilder.toString();
        Log.i(TAG, "createUri: returning Guardian URL: " + finalUrl);
        return finalUrl;
    }

    @NonNull
    @Override
    public Loader<List<NewsListing>> onCreateLoader(int id, Bundle args) {
        String uri = createUri(args);
        Log.i(TAG, "onCreateLoader: id=" + id + ", uri=" + uri);
        return new NewsLoader(this, uri);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<NewsListing>> loader, List<NewsListing> data) {
        Log.i(TAG, "onLoadFinished: data size=" + (data != null ? data.size() : "null"));
        mProgressBar.setVisibility(View.GONE);

        if (data != null && !data.isEmpty()) {
            mEmptyStateView.setVisibility(View.GONE);
            mAdapter.submitList(data);
        } else {
            mAdapter.submitList(null);
            mEmptyStateView.setVisibility(View.VISIBLE);
            if (data == null) {
                mEmptyStateView.setText(R.string.error_loading);
            } else {
                mEmptyStateView.setText(R.string.no_results);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<NewsListing>> loader) {
        mAdapter.submitList(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Important: Update the activity intent
        handleIntent(intent);
    }
}
