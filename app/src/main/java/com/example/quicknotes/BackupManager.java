package com.example.quicknotes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BackupManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String exportToJson(List<Note> notes, List<ShoppingItem> items) {
        BackupData data = new BackupData();
        data.version = 1;
        data.exportedAt = System.currentTimeMillis();
        data.notes = notes;
        data.shoppingItems = items;
        return gson.toJson(data);
    }

    public static BackupData importFromJson(String json) {
        try {
            return gson.fromJson(json, BackupData.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static class BackupData {
        @SerializedName("version") public int version;
        @SerializedName("exported_at") public long exportedAt;
        @SerializedName("notes") public List<Note> notes;
        @SerializedName("shopping_items") public List<ShoppingItem> shoppingItems;
    }
}
