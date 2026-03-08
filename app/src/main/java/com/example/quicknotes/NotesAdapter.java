package com.example.quicknotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import io.noties.markwon.Markwon;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SHOPPING = 0;
    private static final int TYPE_NOTE = 1;

    private List<Note> notes;
    private List<Note> allNotes;
    private Context context;
    private OnItemClickListener listener;
    private OnShoppingClickListener shoppingListener;
    private OnShoppingDeleteListener shoppingDeleteListener;

    private int shoppingTotal = 0;
    private int shoppingUnchecked = 0;
    private long shoppingLastModified = 0;
    private Markwon markwon;

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public interface OnShoppingClickListener {
        void onShoppingClick();
    }

    public interface OnShoppingDeleteListener {
        void onShoppingDelete();
    }

    public NotesAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes != null ? new ArrayList<>(notes) : new ArrayList<>();
        this.allNotes = new ArrayList<>(this.notes);
        markwon = Markwon.builder(context)
            .usePlugin(io.noties.markwon.ext.tasklist.TaskListPlugin.create(context))
            .usePlugin(io.noties.markwon.ext.strikethrough.StrikethroughPlugin.create())
            .build();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnShoppingClickListener(OnShoppingClickListener listener) {
        this.shoppingListener = listener;
    }

    public void setOnShoppingDeleteListener(OnShoppingDeleteListener listener) {
        this.shoppingDeleteListener = listener;
    }

    public void setShoppingCounts(int total, int unchecked, long lastModified) {
        this.shoppingTotal = total;
        this.shoppingUnchecked = unchecked;
        this.shoppingLastModified = lastModified;
        notifyDataSetChanged();
    }

    private boolean hasShoppingCard() {
        return shoppingTotal > 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (hasShoppingCard() && position == 0) return TYPE_SHOPPING;
        return TYPE_NOTE;
    }

    @Override
    public int getItemCount() {
        return notes.size() + (hasShoppingCard() ? 1 : 0);
    }

    private Note noteAt(int position) {
        return notes.get(hasShoppingCard() ? position - 1 : position);
    }

    public Note getNoteAt(int position) {
        return noteAt(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_SHOPPING) {
            View view = inflater.inflate(R.layout.item_shopping_list, parent, false);
            return new ShoppingViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_SHOPPING) {
            ShoppingViewHolder sh = (ShoppingViewHolder) holder;
            sh.subtitle.setText(shoppingUnchecked + " items remaining");
            sh.countBadge.setText(shoppingTotal + " total · " + (shoppingTotal - shoppingUnchecked) + " done");
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            sh.dateText.setText(sdf.format(new Date(shoppingLastModified)));
            sh.itemView.setOnClickListener(v -> {
                if (shoppingListener != null) shoppingListener.onShoppingClick();
            });
            sh.itemView.setOnLongClickListener(v -> {
                if (shoppingDeleteListener != null) shoppingDeleteListener.onShoppingDelete();
                return true;
            });
            // Favorite button
            SharedPreferences prefs = context.getSharedPreferences("quicknotes_prefs", Context.MODE_PRIVATE);
            boolean favored = prefs.getBoolean("shopping_favorited", false);
            applyFavBtn(sh.favoriteBtn, favored);
            sh.favoriteBtn.setOnClickListener(v -> {
                boolean newFav = !prefs.getBoolean("shopping_favorited", false);
                prefs.edit().putBoolean("shopping_favorited", newFav).apply();
                applyFavBtn(sh.favoriteBtn, newFav);
            });
            return;
        }

        NoteViewHolder noteHolder = (NoteViewHolder) holder;
        Note note = noteAt(position);
        noteHolder.titleTextView.setText(note.getTitle());
        markwon.setMarkdown(noteHolder.contentTextView, note.getContent());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        noteHolder.dateTextView.setText(sdf.format(note.getTimestamp()));

        int wordCount = note.getContent().trim().isEmpty() ? 0 :
            note.getContent().trim().split("\\s+").length;
        noteHolder.wordCountText.setText(wordCount + " words");

        if (noteHolder.tagsText != null) {
            String tags = note.getTags();
            if (tags != null && !tags.trim().isEmpty()) {
                noteHolder.tagsText.setText(tags);
                noteHolder.tagsText.setVisibility(View.VISIBLE);
            } else {
                noteHolder.tagsText.setVisibility(View.GONE);
            }
        }

        if (noteHolder.pinIndicator != null)
            noteHolder.pinIndicator.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);
        if (noteHolder.lockIndicator != null)
            noteHolder.lockIndicator.setVisibility(note.isLocked() ? View.VISIBLE : View.GONE);

        if (note.getColor() != 0) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(note.getColor());
            drawable.setCornerRadius(4f);
            noteHolder.accentBorder.setBackground(drawable);
        } else {
            noteHolder.accentBorder.setBackgroundResource(R.drawable.note_accent_gradient);
        }
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

    private void applyFavBtn(ImageButton btn, boolean favored) {
        if (favored) {
            btn.setImageResource(R.drawable.ic_star);
            btn.setImageTintList(ColorStateList.valueOf(0xFFFFC107));
        } else {
            btn.setImageResource(R.drawable.ic_star_outline);
            android.util.TypedValue tv = new android.util.TypedValue();
            context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, tv, true);
            btn.setImageTintList(ColorStateList.valueOf(tv.data));
        }
    }

    static class ShoppingViewHolder extends RecyclerView.ViewHolder {
        TextView subtitle, countBadge, dateText;
        ImageButton favoriteBtn;

        ShoppingViewHolder(@NonNull View itemView) {
            super(itemView);
            subtitle = itemView.findViewById(R.id.shoppingSubtitle);
            countBadge = itemView.findViewById(R.id.shoppingCountBadge);
            dateText = itemView.findViewById(R.id.shoppingDateText);
            favoriteBtn = itemView.findViewById(R.id.shoppingFavoriteBtn);
        }
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, dateTextView, wordCountText, tagsText;
        View accentBorder;
        TextView pinIndicator;
        android.widget.ImageView lockIndicator;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleText);
            contentTextView = itemView.findViewById(R.id.contentText);
            dateTextView = itemView.findViewById(R.id.dateText);
            wordCountText = itemView.findViewById(R.id.wordCountText);
            tagsText = itemView.findViewById(R.id.tagsText);
            accentBorder = itemView.findViewById(R.id.accentBorder);
            pinIndicator = itemView.findViewById(R.id.pinIndicator);
            lockIndicator = itemView.findViewById(R.id.lockIndicator);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(noteAt(position));
                    }
                }
            });
        }
    }
}
