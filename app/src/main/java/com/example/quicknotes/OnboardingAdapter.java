package com.example.quicknotes;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.PageViewHolder> {

    static final class Page {
        final String emoji;
        final int bgColor;     // circle background (semi-transparent)
        final String title;
        final String subtitle;
        final String[] featEmojis;
        final String[] featTexts;

        Page(String emoji, int bgColor, String title, String subtitle,
             String[] featEmojis, String[] featTexts) {
            this.emoji = emoji;
            this.bgColor = bgColor;
            this.title = title;
            this.subtitle = subtitle;
            this.featEmojis = featEmojis;
            this.featTexts = featTexts;
        }
    }

    private static final Page[] PAGES = {
        new Page(
            "✨", 0x226750A4,
            "Welcome to QuickNotes",
            "Your all-in-one notes companion.\nSmart, beautiful, and always ready.",
            new String[]{"⚡", "🌙", "🎨"},
            new String[]{
                "Instant capture — never lose an idea",
                "Works offline, zero account needed",
                "8 themes including dark & AMOLED"
            }
        ),
        new Page(
            "✍️", 0x22E91E8C,
            "Write in Style",
            "Full Markdown editor with live preview\nand voice dictation.",
            new String[]{"**B**", "🎤", "📋"},
            new String[]{
                "Bold, italic, headers, lists, checkboxes",
                "Voice dictation — just tap and speak",
                "4 ready-made templates to start fast"
            }
        ),
        new Page(
            "🗂️", 0x220097A7,
            "Organize Everything",
            "Tags, pins, favorites and lightning-fast\nsearch keep you in control.",
            new String[]{"📌", "⭐", "🔍"},
            new String[]{
                "Pin important notes to the top",
                "Star favorites & filter with one tap",
                "Search by title, content, or tag"
            }
        ),
        new Page(
            "🛒", 0x2243A047,
            "Shopping & Reminders",
            "Built-in shopping list with voice input\nand timed reminders for every note.",
            new String[]{"🛒", "⏰", "🎨"},
            new String[]{
                "Smart shopping list with voice input",
                "Set exact reminders — never miss a task",
                "Color-label notes for visual grouping"
            }
        ),
        new Page(
            "🔒", 0x22F44336,
            "Private & Secure",
            "Lock sensitive notes with PIN or\nbiometrics. Back up everything.",
            new String[]{"🔐", "👆", "💾"},
            new String[]{
                "Lock any note with a custom PIN",
                "Fingerprint & face unlock support",
                "Export/import JSON backup anytime"
            }
        ),
        new Page(
            "🎨", 0x22FF9800,
            "Make It Yours",
            "Choose from 8 themes and set a\ncustom photo background.",
            new String[]{"🌑", "🌹", "🌊"},
            new String[]{
                "Dark, AMOLED, Sepia, Steel themes",
                "Rose, Bubblegum & Ocean themes",
                "Your own photo as background"
            }
        )
    };

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.onboarding_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder h, int position) {
        Page p = PAGES[position];

        h.emoji.setText(p.emoji);

        // Colored circle behind emoji
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(p.bgColor);
        h.iconBg.setBackground(circle);

        h.title.setText(p.title);
        h.subtitle.setText(p.subtitle);

        h.feat1emoji.setText(p.featEmojis[0]);
        h.feat1text.setText(p.featTexts[0]);
        h.feat2emoji.setText(p.featEmojis[1]);
        h.feat2text.setText(p.featTexts[1]);
        h.feat3emoji.setText(p.featEmojis[2]);
        h.feat3text.setText(p.featTexts[2]);

        // Round corners on features card
        GradientDrawable card = new GradientDrawable();
        card.setShape(GradientDrawable.RECTANGLE);
        int cornerPx = (int) (16 * h.itemView.getContext().getResources().getDisplayMetrics().density);
        card.setCornerRadius(cornerPx);
        android.util.TypedValue tv = new android.util.TypedValue();
        h.itemView.getContext().getTheme().resolveAttribute(
            com.google.android.material.R.attr.colorSurfaceVariant, tv, true);
        card.setColor(tv.data);
        h.featuresLayout.setBackground(card);
    }

    @Override
    public int getItemCount() {
        return PAGES.length;
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        View iconBg;
        TextView emoji, title, subtitle;
        LinearLayout featuresLayout;
        TextView feat1emoji, feat1text;
        TextView feat2emoji, feat2text;
        TextView feat3emoji, feat3text;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            iconBg = itemView.findViewById(R.id.iconBg);
            emoji = itemView.findViewById(R.id.pageEmoji);
            title = itemView.findViewById(R.id.pageTitle);
            subtitle = itemView.findViewById(R.id.pageSubtitle);
            featuresLayout = itemView.findViewById(R.id.featuresLayout);
            feat1emoji = itemView.findViewById(R.id.feat1emoji);
            feat1text = itemView.findViewById(R.id.feat1text);
            feat2emoji = itemView.findViewById(R.id.feat2emoji);
            feat2text = itemView.findViewById(R.id.feat2text);
            feat3emoji = itemView.findViewById(R.id.feat3emoji);
            feat3text = itemView.findViewById(R.id.feat3text);
        }
    }
}
