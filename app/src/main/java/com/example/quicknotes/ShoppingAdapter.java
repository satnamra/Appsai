package com.example.quicknotes;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ViewHolder> {
    private List<ShoppingItem> items = new ArrayList<>();
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onChecked(ShoppingItem item, boolean checked);
        void onDelete(ShoppingItem item);
    }

    public void setListener(OnItemActionListener l) { this.listener = l; }

    public void updateItems(List<ShoppingItem> newItems) {
        items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public ShoppingItem getItemAt(int pos) { return items.get(pos); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        ShoppingItem item = items.get(pos);
        h.checkBox.setText(item.getText());
        h.checkBox.setChecked(item.isChecked());
        // Strikethrough when checked
        h.checkBox.setPaintFlags(item.isChecked()
            ? h.checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            : h.checkBox.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        h.checkBox.setOnCheckedChangeListener(null);
        h.checkBox.setOnCheckedChangeListener((b, checked) -> {
            if (listener != null) listener.onChecked(item, checked);
        });
        h.deleteBtn.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ImageButton deleteBtn;
        ViewHolder(View v) {
            super(v);
            checkBox = v.findViewById(R.id.itemCheckBox);
            deleteBtn = v.findViewById(R.id.itemDeleteBtn);
        }
    }
}
