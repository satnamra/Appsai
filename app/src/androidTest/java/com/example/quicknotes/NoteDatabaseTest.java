package com.example.quicknotes;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented tests for Room database — runs on device/emulator.
 */
@RunWith(AndroidJUnit4.class)
public class NoteDatabaseTest {

    private NoteDatabase db;
    private NoteDao noteDao;
    private ShoppingItemDao shoppingDao;

    @Before
    public void createDb() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, NoteDatabase.class).build();
        noteDao = db.noteDao();
        shoppingDao = db.shoppingItemDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    // ── Note CRUD ─────────────────────────────────────────────

    @Test
    public void insertAndGetNote() {
        Note note = new Note("Test", "Body", 1000L);
        noteDao.insert(note);
        List<Note> notes = noteDao.getAllNotes();
        assertEquals(1, notes.size());
        assertEquals("Test", notes.get(0).getTitle());
    }

    @Test
    public void updateNote() {
        Note note = new Note("Old", "Body", 1000L);
        noteDao.insert(note);
        Note saved = noteDao.getAllNotes().get(0);
        saved.setTitle("New");
        noteDao.update(saved);
        Note updated = noteDao.getNoteById(saved.getId());
        assertEquals("New", updated.getTitle());
    }

    @Test
    public void deleteNote() {
        Note note = new Note("Del", "Body", 1000L);
        noteDao.insert(note);
        Note saved = noteDao.getAllNotes().get(0);
        noteDao.delete(saved);
        assertTrue(noteDao.getAllNotes().isEmpty());
    }

    @Test
    public void searchNotes_byTitle() {
        noteDao.insert(new Note("Meeting notes", "content", 1000L));
        noteDao.insert(new Note("Shopping list", "content", 1001L));
        List<Note> results = noteDao.searchNotes("Meeting");
        assertEquals(1, results.size());
        assertEquals("Meeting notes", results.get(0).getTitle());
    }

    @Test
    public void searchNotes_byContent() {
        Note n = new Note("Title", "important body text", 1000L);
        noteDao.insert(n);
        List<Note> results = noteDao.searchNotes("important");
        assertEquals(1, results.size());
    }

    @Test
    public void searchNotes_byTag() {
        Note n = new Note("Title", "body", 1000L);
        n.setTags("work, urgent");
        noteDao.insert(n);
        List<Note> results = noteDao.searchNotes("urgent");
        assertEquals(1, results.size());
    }

    @Test
    public void getFavorites_onlyReturnsFavorites() {
        Note fav = new Note("Fav", "body", 1000L);
        fav.setFavorite(true);
        noteDao.insert(fav);
        Note normal = new Note("Normal", "body", 1001L);
        noteDao.insert(normal);

        List<Note> favs = noteDao.getFavorites();
        assertEquals(1, favs.size());
        assertEquals("Fav", favs.get(0).getTitle());
    }

    @Test
    public void favorites_sortedFirst() {
        Note normal = new Note("Normal", "body", 1000L);
        noteDao.insert(normal);
        Note fav = new Note("Fav", "body", 999L);
        fav.setFavorite(true);
        noteDao.insert(fav);

        List<Note> all = noteDao.getAllNotes();
        assertEquals("Fav", all.get(0).getTitle()); // favorites first
    }

    @Test
    public void getNotesByTag() {
        Note work = new Note("Work", "body", 1000L);
        work.setTags("work, project");
        noteDao.insert(work);
        Note personal = new Note("Personal", "body", 1001L);
        personal.setTags("personal");
        noteDao.insert(personal);

        List<Note> workNotes = noteDao.getNotesByTag("work");
        assertEquals(1, workNotes.size());
        assertEquals("Work", workNotes.get(0).getTitle());
    }

    @Test
    public void noteColor_persistedCorrectly() {
        Note n = new Note("Colored", "body", 1000L);
        n.setColor(0xFFEF4444);
        noteDao.insert(n);
        Note saved = noteDao.getAllNotes().get(0);
        assertEquals(0xFFEF4444, saved.getColor());
    }

    @Test
    public void reminderTime_persistedCorrectly() {
        Note n = new Note("Reminder", "body", 1000L);
        n.setReminderTime(9876543210L);
        noteDao.insert(n);
        Note saved = noteDao.getAllNotes().get(0);
        assertEquals(9876543210L, saved.getReminderTime());
    }

    // ── Shopping CRUD ──────────────────────────────────────────

    @Test
    public void shoppingItem_insertAndGet() {
        ShoppingItem item = new ShoppingItem("Milk", 1000L);
        shoppingDao.insert(item);
        List<ShoppingItem> items = shoppingDao.getAll();
        assertEquals(1, items.size());
        assertEquals("Milk", items.get(0).getText());
        assertFalse(items.get(0).isChecked());
    }

    @Test
    public void shoppingItem_update() {
        ShoppingItem item = new ShoppingItem("Bread", 1000L);
        shoppingDao.insert(item);
        ShoppingItem saved = shoppingDao.getAll().get(0);
        saved.setChecked(true);
        shoppingDao.update(saved);
        assertTrue(shoppingDao.getAll().get(0).isChecked());
    }

    @Test
    public void shoppingItem_delete() {
        ShoppingItem item = new ShoppingItem("Eggs", 1000L);
        shoppingDao.insert(item);
        ShoppingItem saved = shoppingDao.getAll().get(0);
        shoppingDao.delete(saved);
        assertTrue(shoppingDao.getAll().isEmpty());
    }

    @Test
    public void shoppingItem_deleteChecked() {
        shoppingDao.insert(new ShoppingItem("Done1", 1000L));
        shoppingDao.insert(new ShoppingItem("Done2", 1001L));
        shoppingDao.insert(new ShoppingItem("Todo", 1002L));

        // Mark first two as checked
        List<ShoppingItem> all = shoppingDao.getAll();
        for (ShoppingItem i : all) {
            if (!i.getText().equals("Todo")) {
                i.setChecked(true);
                shoppingDao.update(i);
            }
        }
        shoppingDao.deleteChecked();

        List<ShoppingItem> remaining = shoppingDao.getAll();
        assertEquals(1, remaining.size());
        assertEquals("Todo", remaining.get(0).getText());
    }

    @Test
    public void shoppingItem_deleteAll() {
        shoppingDao.insert(new ShoppingItem("A", 1000L));
        shoppingDao.insert(new ShoppingItem("B", 1001L));
        shoppingDao.insert(new ShoppingItem("C", 1002L));
        shoppingDao.deleteAll();
        assertTrue(shoppingDao.getAll().isEmpty());
    }

    @Test
    public void shoppingItem_orderedByCreatedAt() {
        ShoppingItem first = new ShoppingItem("First", 1000L);
        first.setChecked(true);
        shoppingDao.insert(first);
        shoppingDao.insert(new ShoppingItem("Second", 1001L));

        List<ShoppingItem> items = shoppingDao.getAll();
        // Ordered by createdAt ASC: first inserted item comes first regardless of checked state
        assertEquals("First", items.get(0).getText());
        assertEquals("Second", items.get(1).getText());
    }
}
