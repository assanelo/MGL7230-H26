package edu.latece.junit.domain;

import java.time.Instant;
import java.util.Objects;

public final class Enrollment {
    private final String studentId;
    private final String courseCode;
    private final Instant createdAt;

    public Enrollment(String studentId, String courseCode) {
        this.studentId = Objects.requireNonNull(studentId, "studentId");
        this.courseCode = Objects.requireNonNull(courseCode, "courseCode");
        this.createdAt = Instant.now();
    }

    public String studentId() {
        return studentId;
    }

    public String courseCode() {
        return courseCode;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
