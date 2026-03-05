package edu.latece.junit.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Student {
    private final String id;
    private final Set<String> completedCourses; // course codes completed

    public Student(String id, Set<String> completedCourses) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("student id must not be blank");
        this.id = id.trim();

        Set<String> completed = new HashSet<>();
        if (completedCourses != null) {
            for (String c : completedCourses) {
                if (c != null && !c.isBlank()) completed.add(c.trim().toUpperCase());
            }
        }
        this.completedCourses = Collections.unmodifiableSet(completed);
    }

    public String id() {
        return id;
    }

    public boolean hasCompleted(String courseCode) {
        Objects.requireNonNull(courseCode, "courseCode");
        return completedCourses.contains(courseCode.trim().toUpperCase());
    }

    public Set<String> completedCourses() {
        return completedCourses;
    }
}
