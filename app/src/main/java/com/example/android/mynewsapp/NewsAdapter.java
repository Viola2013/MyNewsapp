package com.example.android.mynewsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Adapter for a RecyclerView to display news articles.
 * Uses ListAdapter for efficient updates via DiffUtil.
 */
public class NewsAdapter extends ListAdapter<NewsListing, NewsAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(NewsListing item);
    }

    private final OnItemClickListener mListener;

    // Modern Date API (Requires desugaring in build.gradle)
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    public static final DateTimeFormatter OUTPUT_FORMATTER;

    static {
        OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy", Locale.getDefault());
    }

    public NewsAdapter(OnItemClickListener listener) {
        super(new NewsDiffCallback());
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsListing item = getItem(position);

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(item);
            }
        });

        // Always set text or visibility to avoid recycling bugs (showing old data in new rows)
        holder.title.setText(item.getTitle());
        holder.sectionName.setText(item.getSectionName());
        holder.author.setText(item.getAuthor());
        holder.trailText.setText(item.getTrailText());

        // Improved Date Parsing
        String dateString = item.getDate();
        if (dateString != null && !dateString.isEmpty()) {
            try {
                // ZonedDateTime requires compileOptions { coreLibraryDesugaringEnabled true }
                ZonedDateTime parsedDate = ZonedDateTime.parse(dateString, INPUT_FORMATTER);
                holder.date.setText(parsedDate.format(OUTPUT_FORMATTER));
            } catch (Exception e) {
                holder.date.setText(dateString); // Fallback to raw string
            }
        } else {
            holder.date.setText("");
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title, sectionName, date, trailText, author;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.article_title);
            sectionName = view.findViewById(R.id.section);
            date = view.findViewById(R.id.date);
            author = view.findViewById(R.id.author);
            trailText = view.findViewById(R.id.trail_text);
        }
    }

    static class NewsDiffCallback extends DiffUtil.ItemCallback<NewsListing> {
        @Override
        public boolean areItemsTheSame(@NonNull NewsListing oldItem, @NonNull NewsListing newItem) {
            // Using Web URL as a unique ID
            return oldItem.getWebUrl().equals(newItem.getWebUrl());
        }

        @Override
        public boolean areContentsTheSame(@NonNull NewsListing oldItem, @NonNull NewsListing newItem) {
            // Check if visible content has changed
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getDate().equals(newItem.getDate());
        }
    }
}
