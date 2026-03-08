package com.example.quicknotes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private List<Note> notes;
    private List<Note> allNotes;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public NotesAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes != null ? new ArrayList<>(notes) : new ArrayList<>();
        this.allNotes = new ArrayList<>(this.notes);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.titleTextView.setText(note.getTitle());
        holder.contentTextView.setText(note.getContent());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.dateTextView.setText(sdf.format(note.getTimestamp()));

        int wordCount = note.getContent().trim().isEmpty() ? 0 :
            note.getContent().trim().split("\\s+").length;
        holder.wordCountText.setText(wordCount + " words");

        // Show tags
        if (holder.tagsText != null) {
            String tags = note.getTags();
            if (tags != null && !tags.trim().isEmpty()) {
                holder.tagsText.setText(tags);
                holder.tagsText.setVisibility(View.VISIBLE);
            } else {
                holder.tagsText.setVisibility(View.GONE);
            }
        }

        // Apply color to accent border
        if (note.getColor() != 0) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(note.getColor());
            drawable.setCornerRadius(4f);
            holder.accentBorder.setBackground(drawable);
        } else {
            holder.accentBorder.setBackgroundResource(R.drawable.note_accent_gradient);
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public Note getNoteAt(int position) {
        return notes.get(position);
    }

    public void updateNotes(List<Note> newNotes) {
        this.allNotes = newNotes != null ? new ArrayList<>(newNotes) : new ArrayList<>();
        this.notes = new ArrayList<>(this.allNotes);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            notes = new ArrayList<>(allNotes);
        } else {
            String lower = query.toLowerCase(Locale.getDefault());
            List<Note> filtered = new ArrayList<>();
            for (Note note : allNotes) {
                if (note.getTitle().toLowerCase(Locale.getDefault()).contains(lower) ||
                    note.getContent().toLowerCase(Locale.getDefault()).contains(lower)) {
                    filtered.add(note);
                }
            }
            notes = filtered;
        }
        notifyDataSetChanged();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, dateTextView, wordCountText, tagsText;
        View accentBorder;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleText);
            contentTextView = itemView.findViewById(R.id.contentText);
            dateTextView = itemView.findViewById(R.id.dateText);
            wordCountText = itemView.findViewById(R.id.wordCountText);
            tagsText = itemView.findViewById(R.id.tagsText);
            accentBorder = itemView.findViewById(R.id.accentBorder);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(notes.get(position));
                    }
                }
            });
        }
    }
}
