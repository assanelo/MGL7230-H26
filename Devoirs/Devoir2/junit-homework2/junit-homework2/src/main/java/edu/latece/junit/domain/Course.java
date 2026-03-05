package edu.latece.junit.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Course {
    private final String code;
    private final int capacity;
    private final TimeSlot timeSlot;
    private final Set<String> prerequisites;

    private int enrolledCount;

    public Course(String code, int capacity, TimeSlot timeSlot, Set<String> prerequisites) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("course code must not be blank");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        this.code = code.trim().toUpperCase();
        this.capacity = capacity;
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");

        Set<String> prereq = new HashSet<>();
        if (prerequisites != null) {
            for (String p : prerequisites) {
                if (p != null && !p.isBlank()) prereq.add(p.trim().toUpperCase());
            }
        }
        this.prerequisites = Collections.unmodifiableSet(prereq);
        this.enrolledCount = 0;
    }

    public String code() {
        return code;
    }

    public int capacity() {
        return capacity;
    }

    public int enrolledCount() {
        return enrolledCount;
    }

    public TimeSlot timeSlot() {
        return timeSlot;
    }

    public Set<String> prerequisites() {
        return prerequisites;
    }

    public boolean hasSeatAvailable() {
        return enrolledCount < capacity;
    }

    public void incrementEnrollment() {
        if (!hasSeatAvailable()) {
            throw new IllegalStateException("No seats available");
        }
        enrolledCount++;
    }
}
