package com.example.quicknotes;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ShoppingListActivity extends AppCompatActivity {
    private ShoppingAdapter adapter;
    private ShoppingItemDao dao;
    private EditText inputField;

    private final ActivityResultLauncher<Intent> sttLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                ArrayList<String> matches = result.getData()
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (matches != null && !matches.isEmpty()) {
                    parseAndAddItems(matches.get(0));
                }
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Shopping List");
        }

        dao = NoteDatabase.getInstance(this).shoppingItemDao();
        adapter = new ShoppingAdapter();
        adapter.setListener(new ShoppingAdapter.OnItemActionListener() {
            @Override
            public void onChecked(ShoppingItem item, boolean checked) {
                item.setChecked(checked);
                Executors.newSingleThreadExecutor().execute(() -> dao.update(item));
            }
            @Override
            public void onDelete(ShoppingItem item) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    dao.delete(item);
                    runOnUiThread(() -> loadItems());
                });
            }
        });

        RecyclerView rv = findViewById(R.id.shoppingRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        inputField = findViewById(R.id.shoppingInput);

        findViewById(R.id.addItemBtn).setOnClickListener(v -> {
            String text = inputField.getText().toString().trim();
            if (!text.isEmpty()) {
                addItem(text);
                inputField.setText("");
            }
        });

        ImageButton micBtn = findViewById(R.id.micButton);
        micBtn.setOnClickListener(v -> startSpeechRecognition());

        loadItems();
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your shopping items...");
        try {
            sttLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseAndAddItems(String text) {
        // Split by comma, "and", semicolon
        String[] parts = text.split("[,;]|\\band\\b");
        int count = 0;
        for (String part : parts) {
            String item = part.trim();
            if (!item.isEmpty()) {
                addItem(item);
                count++;
            }
        }
        Toast.makeText(this, "Added " + count + " item" + (count != 1 ? "s" : ""), Toast.LENGTH_SHORT).show();
    }

    private void addItem(String text) {
        ShoppingItem item = new ShoppingItem(capitalize(text), System.currentTimeMillis());
        Executors.newSingleThreadExecutor().execute(() -> {
            dao.insert(item);
            runOnUiThread(() -> loadItems());
        });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void loadItems() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ShoppingItem> items = dao.getAll();
            runOnUiThread(() -> adapter.updateItems(items));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Clear checked").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, 2, 0, "Clear all").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        if (item.getItemId() == 1) {
            Executors.newSingleThreadExecutor().execute(() -> {
                dao.deleteChecked();
                runOnUiThread(() -> loadItems());
            });
            return true;
        }
        if (item.getItemId() == 2) {
            new AlertDialog.Builder(this)
                .setTitle("Clear all items?")
                .setPositiveButton("Clear", (d, w) ->
                    Executors.newSingleThreadExecutor().execute(() -> {
                        dao.deleteAll();
                        runOnUiThread(() -> loadItems());
                    }))
                .setNegativeButton("Cancel", null).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
