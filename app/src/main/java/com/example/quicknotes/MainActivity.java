package com.example.quicknotes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private NoteDatabase database;
    private NoteDao noteDao;
    private View rootView;
    private TextView notesCounter;
    private ChipGroup tagFilterChips;
    private String activeTag = null;
    private boolean showFavoritesOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootView = findViewById(android.R.id.content);
        database = NoteDatabase.getInstance(this);
        noteDao = database.noteDao();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotesAdapter(this, null);
        adapter.setOnItemClickListener(note -> {
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            intent.putExtra("note_id", note.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        notesCounter = findViewById(R.id.notesCounter);
        tagFilterChips = findViewById(R.id.tagFilterChips);

        // Greeting
        TextView greeting = findViewById(R.id.greetingText);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) greeting.setText("Good morning \u2600\ufe0f");
        else if (hour < 18) greeting.setText("Good afternoon \uD83C\uDF24\uFE0F");
        else greeting.setText("Good evening \uD83C\uDF19");

        // Search
        TextInputEditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateCounter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) { return false; }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Note deleted = adapter.getNoteAt(viewHolder.getAdapterPosition());
                Executors.newSingleThreadExecutor().execute(() -> noteDao.delete(deleted));
                loadNotes();
                Snackbar.make(rootView, "Note deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo", v -> Executors.newSingleThreadExecutor().execute(() -> {
                        noteDao.insert(deleted);
                        runOnUiThread(() -> loadNotes());
                    })).show();
            }
        }).attachToRecyclerView(recyclerView);

        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NoteActivity.class)));

        ImageButton shoppingBtn = findViewById(R.id.shoppingBtn);
        shoppingBtn.setOnClickListener(v -> startActivity(new Intent(this, ShoppingListActivity.class)));

        ImageButton settingsBtn = findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        loadNotes();
    }

    private void loadNotes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Note> allNotes = noteDao.getAllNotes();
            List<Note> notes;
            if (showFavoritesOnly) {
                notes = noteDao.getFavorites();
            } else if (activeTag != null) {
                notes = noteDao.getNotesByTag(activeTag);
            } else {
                notes = allNotes;
            }
            runOnUiThread(() -> {
                adapter.updateNotes(notes);
                updateCounter();
                updateTagChips(allNotes);
            });
        });
    }

    private void updateCounter() {
        notesCounter.setText(String.valueOf(adapter.getItemCount()));
    }

    private void updateTagChips(List<Note> notes) {
        // Collect all unique tags
        Set<String> tags = new LinkedHashSet<>();
        for (Note note : notes) {
            if (note.getTags() != null && !note.getTags().isEmpty()) {
                for (String tag : note.getTags().split(",")) {
                    String t = tag.trim();
                    if (!t.isEmpty()) tags.add(t);
                }
            }
        }
        // Rebuild chips
        tagFilterChips.removeAllViews();
        if (tags.isEmpty()) return;

        // "All" chip
        Chip allChip = new Chip(this);
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(activeTag == null && !showFavoritesOnly);
        allChip.setOnClickListener(v -> { activeTag = null; showFavoritesOnly = false; loadNotes(); });
        tagFilterChips.addView(allChip);

        // Favorites chip
        boolean hasFavorites = false;
        for (Note n : notes) { if (n.isFavorite()) { hasFavorites = true; break; } }
        if (hasFavorites) {
            Chip favChip = new Chip(this);
            favChip.setText("⭐ Favorites");
            favChip.setCheckable(true);
            favChip.setChecked(showFavoritesOnly);
            favChip.setOnClickListener(v -> { showFavoritesOnly = !showFavoritesOnly; activeTag = null; loadNotes(); });
            tagFilterChips.addView(favChip);
        }

        for (String tag : tags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(true);
            chip.setChecked(tag.equals(activeTag));
            chip.setOnClickListener(v -> {
                activeTag = tag.equals(activeTag) ? null : tag;
                loadNotes();
            });
            tagFilterChips.addView(chip);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
        ThemeManager.applyBackground(this, findViewById(R.id.backgroundImage));
    }
}
