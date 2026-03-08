package com.example.quicknotes;

import android.text.Editable;
import android.text.TextWatcher;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class NoteActivity extends AppCompatActivity {
    private EditText titleEditText;
    private EditText contentEditText;
    private EditText tagsEditText;
    private NoteDao noteDao;
    private Note currentNote;
    private boolean isNewNote = true;
    private int selectedColor = 0;
    private String originalTitle = "";
    private String originalContent = "";
    private MenuItem favoriteMenuItem;

    private static final int[] NOTE_COLORS = {
        0, 0xFFEF4444, 0xFFF97316, 0xFFEAB308, 0xFF22C55E, 0xFF3B82F6, 0xFFA855F7
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        noteDao = NoteDatabase.getInstance(this).noteDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        tagsEditText = findViewById(R.id.tagsEditText);

        // Character counter
        android.widget.TextView characterCounter = findViewById(R.id.characterCounter);
        contentEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                characterCounter.setText(s.length() + " / 1000");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.saveButton).setOnClickListener(v -> saveNote());
        findViewById(R.id.discardButton).setOnClickListener(v -> handleBack());

        setupColorPicker();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        Intent intent = getIntent();
        if (intent.hasExtra("note_id")) {
            int noteId = intent.getIntExtra("note_id", -1);
            if (noteId != -1) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    currentNote = noteDao.getNoteById(noteId);
                    runOnUiThread(() -> {
                        if (currentNote != null) {
                            titleEditText.setText(currentNote.getTitle());
                            contentEditText.setText(currentNote.getContent());
                            tagsEditText.setText(currentNote.getTags());
                            selectedColor = currentNote.getColor();
                            originalTitle = currentNote.getTitle();
                            originalContent = currentNote.getContent();
                            isNewNote = false;
                            updateColorPickerSelection();
                            updateFavoriteIcon();
                        }
                    });
                });
            }
        }
    }

    private boolean hasChanges() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        if (isNewNote) return !title.isEmpty() || !content.isEmpty();
        return !title.equals(originalTitle) || !content.equals(originalContent);
    }

    private void setupColorPicker() {
        LinearLayout colorPicker = findViewById(R.id.colorPicker);
        if (colorPicker == null) return;
        int dp = (int) (32 * getResources().getDisplayMetrics().density);
        int margin = (int) (6 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < NOTE_COLORS.length; i++) {
            final int color = NOTE_COLORS[i];
            View circle = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp, dp);
            params.setMargins(margin, 0, margin, 0);
            circle.setLayoutParams(params);
            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            d.setColor(color == 0 ? 0xFF9CA3AF : color);
            circle.setBackground(d);
            circle.setOnClickListener(v -> { selectedColor = color; updateColorPickerSelection(); });
            colorPicker.addView(circle);
        }
    }

    private void updateColorPickerSelection() {
        LinearLayout colorPicker = findViewById(R.id.colorPicker);
        if (colorPicker == null) return;
        for (int i = 0; i < colorPicker.getChildCount(); i++) {
            View child = colorPicker.getChildAt(i);
            int color = NOTE_COLORS[i];
            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            d.setColor(color == 0 ? 0xFF9CA3AF : color);
            if (selectedColor == color) {
                d.setStroke((int)(3 * getResources().getDisplayMetrics().density), 0xFF1E293B);
            }
            child.setBackground(d);
        }
    }

    private void updateFavoriteIcon() {
        if (favoriteMenuItem == null) return;
        boolean fav = currentNote != null && currentNote.isFavorite();
        favoriteMenuItem.setIcon(fav ? R.drawable.ic_star : R.drawable.ic_star_outline);
    }

    private void toggleFavorite() {
        if (isNewNote) { saveNoteAndRun(this::toggleFavorite); return; }
        Executors.newSingleThreadExecutor().execute(() -> {
            currentNote.setFavorite(!currentNote.isFavorite());
            noteDao.update(currentNote);
            runOnUiThread(() -> {
                updateFavoriteIcon();
                Toast.makeText(this,
                    currentNote.isFavorite() ? "Added to favorites" : "Removed from favorites",
                    Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showReminderPicker() {
        Calendar cal = Calendar.getInstance();
        if (currentNote != null && currentNote.getReminderTime() > 0) {
            cal.setTimeInMillis(currentNote.getReminderTime());
        }
        new DatePickerDialog(this, (view, year, month, day) ->
            new TimePickerDialog(this, (v, hour, minute) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, day, hour, minute, 0);
                selected.set(Calendar.MILLISECOND, 0);
                if (selected.getTimeInMillis() <= System.currentTimeMillis()) {
                    Toast.makeText(this, "Please pick a future time", Toast.LENGTH_SHORT).show();
                    return;
                }
                scheduleReminder(selected.getTimeInMillis());
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
            .show(),
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            .show();
    }

    private void scheduleReminder(long timeMs) {
        if (isNewNote) {
            saveNoteAndRun(() -> scheduleReminder(timeMs));
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            currentNote.setReminderTime(timeMs);
            noteDao.update(currentNote);
            runOnUiThread(() -> {
                AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, ReminderReceiver.class);
                intent.putExtra(ReminderReceiver.EXTRA_TITLE, currentNote.getTitle());
                intent.putExtra(ReminderReceiver.EXTRA_NOTE_ID, currentNote.getId());
                PendingIntent pi = PendingIntent.getBroadcast(this, currentNote.getId(),
                    intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pi);

                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());
                Toast.makeText(this, "Reminder set for " + sdf.format(timeMs), Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void saveNoteAndRun(Runnable after) {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        if (title.isEmpty()) { titleEditText.setError("Title required"); return; }
        Executors.newSingleThreadExecutor().execute(() -> {
            Note note = new Note(title, content, System.currentTimeMillis());
            note.setColor(selectedColor);
            noteDao.insert(note);
            // Reload as current note
            List<Note> all = noteDao.getAllNotes();
            if (!all.isEmpty()) {
                currentNote = all.get(0);
                originalTitle = currentNote.getTitle();
                originalContent = currentNote.getContent();
                isNewNote = false;
            }
            runOnUiThread(after::run);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_editor, menu);
        favoriteMenuItem = menu.findItem(R.id.action_favorite);
        updateFavoriteIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_favorite) { toggleFavorite(); return true; }
        if (id == R.id.action_reminder) { showReminderPicker(); return true; }
        if (id == R.id.action_share) { shareNote(); return true; }
        if (id == R.id.action_delete) { deleteNote(); return true; }
        if (id == android.R.id.home) { handleBack(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void handleBack() {
        if (!hasChanges()) { finish(); return; }
        new AlertDialog.Builder(this)
            .setTitle("Save changes?")
            .setPositiveButton("Save", (d, w) -> saveNote())
            .setNegativeButton("Discard", (d, w) -> finish())
            .setNeutralButton("Cancel", null)
            .show();
    }

    private void shareNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        String text = title.isEmpty() ? content : title + "\n\n" + content;
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(i, "Share note"));
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        if (title.isEmpty()) { titleEditText.setError("Title is required"); return; }
        Executors.newSingleThreadExecutor().execute(() -> {
            if (isNewNote) {
                Note note = new Note(title, content, System.currentTimeMillis());
                note.setColor(selectedColor);
                note.setTags(tagsEditText.getText().toString().trim());
                noteDao.insert(note);
            } else {
                currentNote.setTitle(title);
                currentNote.setContent(content);
                currentNote.setTimestamp(System.currentTimeMillis());
                currentNote.setColor(selectedColor);
                currentNote.setTags(tagsEditText.getText().toString().trim());
                noteDao.update(currentNote);
            }
            runOnUiThread(this::finish);
        });
    }

    private void deleteNote() {
        if (!isNewNote && currentNote != null) {
            new AlertDialog.Builder(this)
                .setTitle("Delete note?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (d, w) ->
                    Executors.newSingleThreadExecutor().execute(() -> {
                        noteDao.delete(currentNote);
                        runOnUiThread(this::finish);
                    }))
                .setNegativeButton("Cancel", null)
                .show();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        handleBack();
    }

}
