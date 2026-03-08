package com.example.quicknotes;

import org.junit.Test;
import static org.junit.Assert.*;

public class ShoppingItemTest {

    @Test
    public void shoppingItem_defaults() {
        ShoppingItem item = new ShoppingItem("Milk", 1000L);
        assertEquals("Milk", item.getText());
        assertEquals(1000L, item.getCreatedAt());
        assertFalse(item.isChecked());
    }

    @Test
    public void shoppingItem_check() {
        ShoppingItem item = new ShoppingItem("Bread", 0);
        item.setChecked(true);
        assertTrue(item.isChecked());
        item.setChecked(false);
        assertFalse(item.isChecked());
    }

    @Test
    public void shoppingItem_setText() {
        ShoppingItem item = new ShoppingItem("Eggs", 0);
        item.setText("Organic Eggs");
        assertEquals("Organic Eggs", item.getText());
    }
}
