package com.example.quicknotes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for Note model — no Android context needed.
 */
public class NoteTest {

    @Test
    public void note_defaultValues() {
        Note note = new Note("Title", "Content", 1000L);
        assertEquals("Title", note.getTitle());
        assertEquals("Content", note.getContent());
        assertEquals(1000L, note.getTimestamp());
        assertEquals(0, note.getColor());
        assertFalse(note.isFavorite());
        assertEquals(0L, note.getReminderTime());
        assertEquals("", note.getTags());
    }

    @Test
    public void note_setters() {
        Note note = new Note("T", "C", 0);
        note.setTitle("New Title");
        note.setContent("New Content");
        note.setColor(0xFFFF0000);
        note.setFavorite(true);
        note.setReminderTime(9999L);
        note.setTags("work, personal");

        assertEquals("New Title", note.getTitle());
        assertEquals("New Content", note.getContent());
        assertEquals(0xFFFF0000, note.getColor());
        assertTrue(note.isFavorite());
        assertEquals(9999L, note.getReminderTime());
        assertEquals("work, personal", note.getTags());
    }

    @Test
    public void note_nullTagsDefaultsToEmpty() {
        Note note = new Note("T", "C", 0);
        note.setTags(null);
        assertEquals("", note.getTags());
    }

    @Test
    public void note_toggleFavorite() {
        Note note = new Note("T", "C", 0);
        assertFalse(note.isFavorite());
        note.setFavorite(true);
        assertTrue(note.isFavorite());
        note.setFavorite(false);
        assertFalse(note.isFavorite());
    }
}
