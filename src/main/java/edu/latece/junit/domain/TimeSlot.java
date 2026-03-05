package edu.latece.junit.domain;

import java.util.Objects;

/**
 * Simple weekly timeslot.
 * Day is 1..7 (1=Mon, 7=Sun).
 * Hours are in 24h format: 0..23.
 * Minutes: 0..59.
 */
public final class TimeSlot {
    private final int dayOfWeek; // 1..7
    private final int startMinutes; // minutes since 00:00
    private final int endMinutes;   // minutes since 00:00 (exclusive)

    public TimeSlot(int dayOfWeek, int startHour, int startMinute, int endHour, int endMinute) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be 1..7");
        }
        validateTime(startHour, startMinute);
        validateTime(endHour, endMinute);

        int start = startHour * 60 + startMinute;
        int end = endHour * 60 + endMinute;

        if (end <= start) {
            throw new IllegalArgumentException("end time must be after start time");
        }

        this.dayOfWeek = dayOfWeek;
        this.startMinutes = start;
        this.endMinutes = end;
    }

    private static void validateTime(int hour, int minute) {
        if (hour < 0 || hour > 23) throw new IllegalArgumentException("hour must be 0..23");
        if (minute < 0 || minute > 59) throw new IllegalArgumentException("minute must be 0..59");
    }

    public int dayOfWeek() {
        return dayOfWeek;
    }

    public int startMinutes() {
        return startMinutes;
    }

    public int endMinutes() {
        return endMinutes;
    }

    /**
     * Overlap is true if the time intervals intersect on the same day.
     * Touching endpoints is NOT an overlap: [10:00,11:00) and [11:00,12:00) do not overlap.
     */
    public boolean overlapsWith(TimeSlot other) {
        Objects.requireNonNull(other, "other");
        if (this.dayOfWeek != other.dayOfWeek) return false;

        // [a,b) overlaps [c,d) if max(a,c) < min(b,d)
        int maxStart = Math.max(this.startMinutes, other.startMinutes);
        int minEnd = Math.min(this.endMinutes, other.endMinutes);
        return maxStart < minEnd;
    }

    @Override
    public String toString() {
        return "Day " + dayOfWeek + " [" + startMinutes + "," + endMinutes + ")";
    }
}
