package com.example.android.mynewsapp;


/*
  Created by Mervi on 1.7.2017.
 */
public class NewsListing {

    private final String mTitle;
    private final String mWebUrl;
    private final String mSectionName;
    private final String mDate;
    private String mAuthor = "";
    private String mTrailText = "";


    public NewsListing(String title, String webUrl, String sectionName, String date) {
        mTitle = title;
        mWebUrl = webUrl;
        mSectionName = sectionName;
        mDate = date;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getWebUrl() {
        return mWebUrl;
    }

    public String getSectionName() {
        return mSectionName;
    }

    public String getDate() {
        return mDate;
    }

    public String getTrailText() {

        return mTrailText;
    }

    public void setTrailText(String trailText) {
        mTrailText = trailText;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

}