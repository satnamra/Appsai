package com.example.quicknotes;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ShoppingItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ShoppingItem item);

    @Update
    void update(ShoppingItem item);

    @Delete
    void delete(ShoppingItem item);

    @Query("SELECT * FROM shopping_items ORDER BY createdAt ASC")
    List<ShoppingItem> getAll();

    @Query("DELETE FROM shopping_items WHERE isChecked = 1")
    void deleteChecked();

    @Query("DELETE FROM shopping_items")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM shopping_items")
    int getCount();

    @Query("SELECT COUNT(*) FROM shopping_items WHERE isChecked = 0")
    int getUncheckedCount();

    @Query("SELECT MAX(createdAt) FROM shopping_items")
    long getLastModified();
}
