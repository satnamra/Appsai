package com.example.quicknotes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private TextView weatherText;
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
            if (note.isLocked()) {
                showBiometricForNote(note);
            } else {
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                intent.putExtra("note_id", note.getId());
                startActivity(intent);
            }
        });
        adapter.setOnShoppingClickListener(() ->
            startActivity(new Intent(this, ShoppingListActivity.class)));
        adapter.setOnShoppingDeleteListener(() ->
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete shopping list?")
                .setMessage("All items will be removed.")
                .setPositiveButton("Delete", (d, w) ->
                    java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                        database.shoppingItemDao().deleteAll();
                        runOnUiThread(() -> loadNotes());
                    }))
                .setNegativeButton("Cancel", null)
                .show());
        recyclerView.setAdapter(adapter);
        notesCounter = findViewById(R.id.notesCounter);
        weatherText = findViewById(R.id.weatherText);
        tagFilterChips = findViewById(R.id.tagFilterChips);
        fetchWeather();

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
        fab.setOnClickListener(v -> showNewItemSheet());

        ImageButton favoritesBtn = findViewById(R.id.favoritesBtn);
        favoritesBtn.setOnClickListener(v -> {
            showFavoritesOnly = !showFavoritesOnly;
            activeTag = null;
            updateFavoritesBtn(favoritesBtn);
            loadNotes();
        });

        ImageButton settingsBtn = findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        loadNotes();
    }

    private void fetchWeather() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL("https://wttr.in/?format=j1");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "QuickNotes/1.0");
                if (conn.getResponseCode() != 200) return;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();
                String json = sb.toString();
                // Parse temp_C
                String tempC = extractJsonString(json, "temp_C");
                // Parse moon_phase from weather[0].astronomy[0]
                String moonPhase = extractJsonString(json, "moon_phase");
                if (tempC == null) return;
                String moonEmoji = moonPhaseEmoji(moonPhase != null ? moonPhase : "");
                String display = tempC + "°C  " + moonEmoji;
                runOnUiThread(() -> {
                    weatherText.setText(display);
                    weatherText.setVisibility(View.VISIBLE);
                });
            } catch (Exception ignored) {}
        });
    }

    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int start = idx + search.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private String moonPhaseEmoji(String phase) {
        String p = phase.toLowerCase();
        if (p.contains("new")) return "\uD83C\uDF11";
        if (p.contains("waxing crescent")) return "\uD83C\uDF12";
        if (p.contains("first quarter")) return "\uD83C\uDF13";
        if (p.contains("waxing gibbous")) return "\uD83C\uDF14";
        if (p.contains("full")) return "\uD83C\uDF15";
        if (p.contains("waning gibbous")) return "\uD83C\uDF16";
        if (p.contains("last quarter") || p.contains("third quarter")) return "\uD83C\uDF17";
        if (p.contains("waning crescent")) return "\uD83C\uDF18";
        return "\uD83C\uDF19";
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
            int shoppingTotal = database.shoppingItemDao().getCount();
            int shoppingUnchecked = database.shoppingItemDao().getUncheckedCount();
            long shoppingLastModified = database.shoppingItemDao().getLastModified();
            boolean shoppingFavorited = getSharedPreferences("quicknotes_prefs", MODE_PRIVATE)
                    .getBoolean("shopping_favorited", false);
            // When filtering favorites, only show shopping card if it's favorited
            int displayShoppingTotal = (!showFavoritesOnly || shoppingFavorited) ? shoppingTotal : 0;
            runOnUiThread(() -> {
                adapter.updateNotes(notes);
                adapter.setShoppingCounts(displayShoppingTotal, shoppingUnchecked, shoppingLastModified);
                updateCounter();
                updateTagChips(allNotes);
            });
        });
    }

    private void updateCounter() {
        notesCounter.setText(String.valueOf(adapter.getItemCount()));
    }

    private void updateFavoritesBtn(ImageButton btn) {
        if (showFavoritesOnly) {
            btn.setImageResource(R.drawable.ic_star);
            btn.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFFFC107));
        } else {
            btn.setImageResource(R.drawable.ic_star_outline);
            android.util.TypedValue tv = new android.util.TypedValue();
            getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, tv, true);
            btn.setImageTintList(android.content.res.ColorStateList.valueOf(tv.data));
        }
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

    private void showNewItemSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        sheet.setContentView(R.layout.bottom_sheet_new_item);
        sheet.findViewById(R.id.cardNote).setOnClickListener(v -> {
            sheet.dismiss();
            startActivity(new Intent(this, NoteActivity.class));
        });
        sheet.findViewById(R.id.cardShopping).setOnClickListener(v -> {
            sheet.dismiss();
            startActivity(new Intent(this, ShoppingListActivity.class));
        });
        sheet.findViewById(R.id.cardTemplate).setOnClickListener(v -> {
            sheet.dismiss();
            showTemplateSheet();
        });
        sheet.show();
    }

    private void showTemplateSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        sheet.setContentView(R.layout.bottom_sheet_templates);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault());
        String today = sdf.format(new java.util.Date());
        String[][] templates = {
            {"Meeting Notes", "## Meeting Notes\n**Date:** " + today + "\n**Attendees:**\n\n### Agenda\n- \n\n### Discussion\n\n### Action Items\n- [ ] \n- [ ] "},
            {"Daily Journal - " + today, "## " + today + "\n\n**Mood:** \uD83D\uDE0A\n\n### What happened today\n\n### Grateful for\n1. \n2. \n3. \n\n### Tomorrow's focus\n- [ ] "},
            {"Todo List", "## Tasks\n\n### Today\n- [ ] \n- [ ] \n- [ ] \n\n### This Week\n- [ ] \n\n### Done\n- [x] "},
            {"Brainstorm", "## Topic: \n\n### Ideas\n- \n- \n- \n\n### Wild Ideas\n- \n\n### Next Steps\n- [ ] \n- [ ] "}
        };
        int[] ids = {R.id.templateMeeting, R.id.templateJournal, R.id.templateTodo, R.id.templateBrainstorm};
        for (int i = 0; i < ids.length; i++) {
            final String title = templates[i][0];
            final String content = templates[i][1];
            sheet.findViewById(ids[i]).setOnClickListener(v -> {
                sheet.dismiss();
                Intent intent = new Intent(this, NoteActivity.class);
                intent.putExtra("template_title", title);
                intent.putExtra("template_content", content);
                startActivity(intent);
            });
        }
        sheet.show();
    }

    private void showBiometricForNote(Note note) {
        androidx.biometric.BiometricPrompt.PromptInfo promptInfo =
            new androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                .setTitle("Locked Note")
                .setSubtitle("Authenticate to open")
                .setAllowedAuthenticators(
                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK |
                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
        new androidx.biometric.BiometricPrompt(this,
            androidx.core.content.ContextCompat.getMainExecutor(this),
            new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(androidx.biometric.BiometricPrompt.AuthenticationResult r) {
                    Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                    intent.putExtra("note_id", note.getId());
                    intent.putExtra("bypass_lock", true);
                    startActivity(intent);
                }
                @Override
                public void onAuthenticationError(int code, CharSequence msg) {
                    showPinUnlockDialog(note);
                }
                @Override public void onAuthenticationFailed() {}
            }).authenticate(promptInfo);
    }

    private void showPinUnlockDialog(Note note) {
        android.widget.EditText pinInput = new android.widget.EditText(this);
        pinInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Enter PIN").setView(pinInput)
            .setPositiveButton("Open", (d, w) -> {
                if (pinInput.getText().toString().equals(note.getLockPin())) {
                    Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                    intent.putExtra("note_id", note.getId());
                    intent.putExtra("bypass_lock", true);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Wrong PIN", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
        ThemeManager.applyBackground(this, findViewById(R.id.backgroundImage));
    }
}
