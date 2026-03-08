package com.example.quicknotes;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes ORDER BY isFavorite DESC, timestamp DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :id")
    Note getNoteById(int id);

    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') ORDER BY isFavorite DESC, timestamp DESC")
    List<Note> searchNotes(String query);

    @Query("SELECT * FROM notes WHERE isFavorite = 1 ORDER BY timestamp DESC")
    List<Note> getFavorites();

    @Query("SELECT * FROM notes WHERE tags LIKE '%' || :tag || '%' ORDER BY isFavorite DESC, timestamp DESC")
    List<Note> getNotesByTag(String tag);
}
