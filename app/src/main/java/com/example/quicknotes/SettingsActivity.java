package com.example.quicknotes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private ImageView bgPreview;

    private final ActivityResultLauncher<String> exportLauncher =
        registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri == null) return;
            java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    NoteDatabase db = NoteDatabase.getInstance(this);
                    List<Note> notes = db.noteDao().getAllNotes();
                    List<ShoppingItem> items = db.shoppingItemDao().getAll();
                    String json = BackupManager.exportToJson(notes, items);
                    java.io.OutputStream os = getContentResolver().openOutputStream(uri);
                    os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    os.close();
                    runOnUiThread(() -> android.widget.Toast.makeText(this, "Exported " + notes.size() + " notes", android.widget.Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    runOnUiThread(() -> android.widget.Toast.makeText(this, "Export failed", android.widget.Toast.LENGTH_SHORT).show());
                }
            });
        });

    private final ActivityResultLauncher<String[]> importLauncher =
        registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri == null) return;
            java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    java.io.InputStream is = getContentResolver().openInputStream(uri);
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = is.read(buffer)) != -1) baos.write(buffer, 0, n);
                    is.close();
                    String json = baos.toString("UTF-8");
                    BackupManager.BackupData data = BackupManager.importFromJson(json);
                    if (data == null || data.notes == null) {
                        runOnUiThread(() -> android.widget.Toast.makeText(this, "Invalid backup file", android.widget.Toast.LENGTH_SHORT).show());
                        return;
                    }
                    int noteCount = data.notes.size();
                    runOnUiThread(() -> new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Import backup?")
                        .setMessage("This will add " + noteCount + " notes.")
                        .setPositiveButton("Import", (d, w) ->
                            java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                                NoteDatabase db = NoteDatabase.getInstance(this);
                                for (Note note : data.notes) { note.setId(0); db.noteDao().insert(note); }
                                if (data.shoppingItems != null)
                                    for (ShoppingItem s : data.shoppingItems) { s.setId(0); db.shoppingItemDao().insert(s); }
                                runOnUiThread(() -> android.widget.Toast.makeText(this, "Imported " + noteCount + " notes", android.widget.Toast.LENGTH_SHORT).show());
                            }))
                        .setNegativeButton("Cancel", null).show());
                } catch (Exception e) {
                    runOnUiThread(() -> android.widget.Toast.makeText(this, "Import failed", android.widget.Toast.LENGTH_SHORT).show());
                }
            });
        });

    private final ActivityResultLauncher<String[]> imagePicker =
        registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                // Persist permission so URI survives reboots
                try {
                    getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {}
                ThemeManager.setBackgroundUri(this, uri.toString());
                bgPreview.setImageURI(uri);
                bgPreview.setVisibility(android.view.View.VISIBLE);
                Toast.makeText(this, "Background set", Toast.LENGTH_SHORT).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        // Theme radio buttons
        RadioGroup themeGroup = findViewById(R.id.themeRadioGroup);
        int current = ThemeManager.getTheme(this);
        switch (current) {
            case ThemeManager.THEME_DARK:       themeGroup.check(R.id.themeDark); break;
            case ThemeManager.THEME_AMOLED:     themeGroup.check(R.id.themeAmoled); break;
            case ThemeManager.THEME_SEPIA:      themeGroup.check(R.id.themeSepia); break;
            case ThemeManager.THEME_STEEL:      themeGroup.check(R.id.themeSteel); break;
            case ThemeManager.THEME_ROSE:       themeGroup.check(R.id.themeRose); break;
            case ThemeManager.THEME_BUBBLEGUM:  themeGroup.check(R.id.themeBubblegum); break;
            case ThemeManager.THEME_OCEAN:      themeGroup.check(R.id.themeOcean); break;
            default:                             themeGroup.check(R.id.themeLight); break;
        }
        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int theme;
            if (checkedId == R.id.themeDark)            theme = ThemeManager.THEME_DARK;
            else if (checkedId == R.id.themeAmoled)     theme = ThemeManager.THEME_AMOLED;
            else if (checkedId == R.id.themeSepia)      theme = ThemeManager.THEME_SEPIA;
            else if (checkedId == R.id.themeSteel)      theme = ThemeManager.THEME_STEEL;
            else if (checkedId == R.id.themeRose)       theme = ThemeManager.THEME_ROSE;
            else if (checkedId == R.id.themeBubblegum)  theme = ThemeManager.THEME_BUBBLEGUM;
            else if (checkedId == R.id.themeOcean)      theme = ThemeManager.THEME_OCEAN;
            else                                         theme = ThemeManager.THEME_LIGHT;
            ThemeManager.setTheme(this, theme);
            ThemeManager.apply(this);
            recreate();
        });

        // Background image
        bgPreview = findViewById(R.id.bgPreview);
        String existingUri = ThemeManager.getBackgroundUri(this);
        if (existingUri != null) {
            bgPreview.setImageURI(Uri.parse(existingUri));
            bgPreview.setVisibility(android.view.View.VISIBLE);
        }

        findViewById(R.id.chooseBgBtn).setOnClickListener(v ->
            imagePicker.launch(new String[]{"image/*"}));

        findViewById(R.id.removeBgBtn).setOnClickListener(v -> {
            ThemeManager.setBackgroundUri(this, null);
            bgPreview.setImageURI(null);
            bgPreview.setVisibility(android.view.View.GONE);
            Toast.makeText(this, "Background removed", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.exportBtn).setOnClickListener(v -> exportLauncher.launch("quicknotes-backup.json"));
        findViewById(R.id.importBtn).setOnClickListener(v -> importLauncher.launch(new String[]{"application/json", "*/*"}));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
