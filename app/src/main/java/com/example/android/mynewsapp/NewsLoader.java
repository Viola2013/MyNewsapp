package com.example.android.mynewsapp;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Mervi on 1.7.2017.
 */

class NewsLoader extends AsyncTaskLoader<List<NewsListing>> {
    private final String mQueryUrl;

    public NewsLoader(Context context, String queryUrl) {
        super(context);
        mQueryUrl = queryUrl;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public List<NewsListing> loadInBackground() {
        if (mQueryUrl == null) return null;
        if (mQueryUrl.startsWith("mock://yle")) {
            return loadMockYleData();
        }
        if (mQueryUrl.startsWith("mock://")) {
            return loadMockData();
        }
        return QueryUtils.fetchData(mQueryUrl);
    }

    private List<NewsListing> loadMockYleData() {
        try (InputStream is = getContext().getAssets().open("mock_yle.json")) {
            return QueryUtils.parseNewsApiJSON(is);
        } catch (IOException e) {
            Log.e("NewsLoader", "Could not read mock_yle.json", e);
            return null;
        }
    }

    private List<NewsListing> loadMockData() {
        try (InputStream is = getContext().getAssets().open("mock_news.json")) {
            return QueryUtils.parseGuardianJSON(is);
        } catch (IOException e) {
            Log.e("NewsLoader", "Could not read mock_news.json", e);
            return null;
        }
    }
}
