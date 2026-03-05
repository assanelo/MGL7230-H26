package edu.latece.junit.repo;

import edu.latece.junit.domain.Course;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CourseCatalog {
    private final Map<String, Course> courses = new HashMap<>();

    public void add(Course course) {
        Objects.requireNonNull(course, "course");
        String code = course.code();
        if (courses.containsKey(code)) {
            throw new IllegalArgumentException("Duplicate course code: " + code);
        }
        courses.put(code, course);
    }

    public Course get(String courseCode) {
        if (courseCode == null) return null;
        String normalized = courseCode.trim().toUpperCase();
        return courses.get(normalized);
    }

    public int size() {
        return courses.size();
    }
}
