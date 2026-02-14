package com.mealplanner.service;

import com.mealplanner.model.MealPlan;
import com.mealplanner.model.MealPlanEntry;
import com.mealplanner.model.MealType;
import com.mealplanner.repository.MealPlanRepository;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrmnlDisplayService {

    static final int WIDTH = 800;
    static final int HEIGHT = 480;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEE d MMM");

    private final MealPlanRepository mealPlanRepository;

    public TrmnlDisplayService(MealPlanRepository mealPlanRepository) {
        this.mealPlanRepository = mealPlanRepository;
    }

    /**
     * Renders a black-and-white 800x480 PNG image showing today's and tomorrow's meal plans.
     */
    public byte[] renderDisplayImage(LocalDate today) throws IOException {
        LocalDate tomorrow = today.plusDays(1);

        List<MealPlanEntry> todayEntries = getEntriesForDate(today);
        List<MealPlanEntry> tomorrowEntries = getEntriesForDate(tomorrow);

        Map<String, String> todayNotes = getNotesForDate(today);
        Map<String, String> tomorrowNotes = getNotesForDate(tomorrow);

        String todayDayKey = today.getDayOfWeek().name();
        String tomorrowDayKey = tomorrow.getDayOfWeek().name();

        String todayNote = todayNotes.getOrDefault(todayDayKey, null);
        String tomorrowNote = tomorrowNotes.getOrDefault(tomorrowDayKey, null);

        return renderImage(today, todayEntries, todayNote, tomorrow, tomorrowEntries, tomorrowNote);
    }

    List<MealPlanEntry> getEntriesForDate(LocalDate date) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
        String dayOfWeek = date.getDayOfWeek().name();

        return mealPlanRepository.findByWeekStartDate(weekStart)
                .map(plan -> plan.getEntries().stream()
                        .filter(e -> dayOfWeek.equals(e.getDayOfWeek()))
                        .sorted(Comparator.comparingInt(MealPlanEntry::getDisplayOrder))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private Map<String, String> getNotesForDate(LocalDate date) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
        return mealPlanRepository.findByWeekStartDate(weekStart)
                .map(MealPlan::getDayNotes)
                .orElse(Collections.emptyMap());
    }

    byte[] renderImage(LocalDate today, List<MealPlanEntry> todayEntries, String todayNote,
                       LocalDate tomorrow, List<MealPlanEntry> tomorrowEntries, String tomorrowNote)
            throws IOException {

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = image.createGraphics();

        // Enable anti-aliasing for text
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // White background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.BLACK);

        // Header
        Font headerFont = new Font(Font.SANS_SERIF, Font.BOLD, 22);
        Font dayFont = new Font(Font.SANS_SERIF, Font.BOLD, 20);
        Font mealTypeFont = new Font(Font.SANS_SERIF, Font.BOLD, 15);
        Font mealFont = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
        Font noteFont = new Font(Font.SANS_SERIF, Font.ITALIC, 13);
        Font emptyFont = new Font(Font.SANS_SERIF, Font.ITALIC, 15);

        // Title bar
        g.setFont(headerFont);
        g.fillRect(0, 0, WIDTH, 36);
        g.setColor(Color.WHITE);
        g.drawString("Meal Planner", 16, 26);
        g.setColor(Color.BLACK);

        // Divider line down the middle
        int midX = WIDTH / 2;
        g.drawLine(midX, 36, midX, HEIGHT);

        // Draw each day column
        int columnWidth = midX - 1;
        drawDayColumn(g, 0, 40, columnWidth, today, todayEntries, todayNote,
                dayFont, mealTypeFont, mealFont, noteFont, emptyFont, true);
        drawDayColumn(g, midX + 1, 40, columnWidth, tomorrow, tomorrowEntries, tomorrowNote,
                dayFont, mealTypeFont, mealFont, noteFont, emptyFont, false);

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private void drawDayColumn(Graphics2D g, int x, int startY, int width,
                               LocalDate date, List<MealPlanEntry> entries, String note,
                               Font dayFont, Font mealTypeFont, Font mealFont, Font noteFont,
                               Font emptyFont, boolean isToday) {
        int padding = 14;
        int y = startY + 10;

        // Day label: "Today - Sat 14 Feb" or "Tomorrow - Sun 15 Feb"
        g.setFont(dayFont);
        String prefix = isToday ? "Today" : "Tomorrow";
        String label = prefix + " — " + date.format(DATE_FMT);
        g.drawString(label, x + padding, y + 20);
        y += 32;

        // Underline
        g.drawLine(x + padding, y, x + width - padding, y);
        y += 12;

        if (entries.isEmpty()) {
            g.setFont(emptyFont);
            g.drawString("No meals planned", x + padding, y + 16);
            y += 30;
        } else {
            // Group by meal type, preserving order: BREAKFAST, LUNCH, DINNER, then null
            Map<MealType, List<MealPlanEntry>> grouped = new LinkedHashMap<>();
            for (MealType type : MealType.values()) {
                List<MealPlanEntry> matching = entries.stream()
                        .filter(e -> type.equals(e.getMealType()))
                        .collect(Collectors.toList());
                if (!matching.isEmpty()) {
                    grouped.put(type, matching);
                }
            }
            // Entries without a meal type
            List<MealPlanEntry> untyped = entries.stream()
                    .filter(e -> e.getMealType() == null)
                    .collect(Collectors.toList());
            if (!untyped.isEmpty()) {
                grouped.put(null, untyped);
            }

            for (Map.Entry<MealType, List<MealPlanEntry>> group : grouped.entrySet()) {
                MealType type = group.getKey();
                if (type != null) {
                    g.setFont(mealTypeFont);
                    String typeLabel = capitalize(type.name());
                    g.drawString(typeLabel, x + padding, y + 15);
                    y += 22;
                }

                for (MealPlanEntry entry : group.getValue()) {
                    g.setFont(mealFont);
                    String mealTitle = entry.getMeal() != null ? entry.getMeal().getTitle() : "Unknown meal";
                    String cookName = entry.getAssignedCook() != null
                            ? " (" + entry.getAssignedCook().getName() + ")"
                            : "";
                    String line = "• " + mealTitle + cookName;

                    // Truncate if too long for column
                    FontMetrics fm = g.getFontMetrics();
                    int maxWidth = width - 2 * padding;
                    if (fm.stringWidth(line) > maxWidth) {
                        while (fm.stringWidth(line + "…") > maxWidth && line.length() > 1) {
                            line = line.substring(0, line.length() - 1);
                        }
                        line += "…";
                    }

                    g.drawString(line, x + padding + 6, y + 16);
                    y += 22;
                }
                y += 4;
            }
        }

        // Draw note if present
        if (note != null && !note.isBlank()) {
            y += 4;
            g.setFont(noteFont);
            g.drawString("Note: " + truncateForWidth(g, "Note: " + note, width - 2 * padding), x + padding, y + 13);
        }
    }

    private String truncateForWidth(Graphics2D g, String text, int maxWidth) {
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        while (fm.stringWidth(text + "…") > maxWidth && text.length() > 1) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "…";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
