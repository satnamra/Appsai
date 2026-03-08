package com.example.quicknotes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.PageViewHolder> {

    private final int[] icons = {
        R.drawable.ic_add,
        R.drawable.ic_empty_notes,
        R.drawable.ic_save
    };

    private final String[] titles = {
        "Capture Instantly",
        "Stay Organized",
        "Ready to Go!"
    };

    private final String[] subtitles = {
        "Jot down ideas, tasks, and thoughts\nbefore they slip away",
        "All your notes in one beautiful place,\nalways at your fingertips",
        "Start writing your first note\nand never lose a thought again"
    };

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.onboarding_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        holder.icon.setImageResource(icons[position]);
        holder.icon.setColorFilter(
            holder.itemView.getContext().getColor(R.color.md_theme_light_primary)
        );
        holder.title.setText(titles[position]);
        holder.subtitle.setText(subtitles[position]);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, subtitle;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.pageIcon);
            title = itemView.findViewById(R.id.pageTitle);
            subtitle = itemView.findViewById(R.id.pageSubtitle);
        }
    }
}
