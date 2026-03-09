package edu.latece.junit.service;

import edu.latece.junit.domain.Course;
import edu.latece.junit.domain.Enrollment;
import edu.latece.junit.domain.Student;
import edu.latece.junit.domain.TimeSlot;
import edu.latece.junit.repo.CourseCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Enrollment rules:
 * - course must exist
 * - course must have capacity
 * - student must have completed all prerequisites
 * - no schedule conflict with student's existing enrollments
 */
public class EnrollmentService {

    private final CourseCatalog catalog;
    private final List<Enrollment> enrollments = new ArrayList<>();

    public EnrollmentService(CourseCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
    }

    public Enrollment enroll(Student student, String courseCode) {
        Objects.requireNonNull(student, "student");
        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("courseCode must not be blank");
        }

        String normalized = courseCode.trim().toUpperCase();
        Course course = catalog.get(normalized);
        if (course == null) {
            throw new EnrollmentException("Course not found: " + normalized);
        }

        if (!course.hasSeatAvailable()) {
            throw new EnrollmentException("Enrollment failed: capacity reached for " + normalized);
        }

        // prereqs
        for (String prereq : course.prerequisites()) {
            if (!student.hasCompleted(prereq)) {
                throw new EnrollmentException("Enrollment failed: missing prerequisite " + prereq);
            }
        }

        // schedule conflict
        TimeSlot newSlot = course.timeSlot();
        for (Enrollment e : enrollmentsFor(student.id())) {
            Course existing = catalog.get(e.courseCode());
            if (existing != null && existing.timeSlot().overlapsWith(newSlot)) {
                throw new EnrollmentException("Enrollment failed: schedule conflict with " + existing.code());
            }
        }

        // CORRECT: incrementer après toutes les validations
        course.incrementEnrollment();

        // record
        Enrollment enrollment = new Enrollment(student.id(), normalized);
        enrollments.add(enrollment);
        return enrollment;
    }

    public List<Enrollment> enrollmentsFor(String studentId) {
        Objects.requireNonNull(studentId, "studentId");
        String id = studentId.trim();
        List<Enrollment> result = new ArrayList<>();
        for (Enrollment e : enrollments) {
            if (e.studentId().equals(id)) result.add(e);
        }
        return List.copyOf(result);
    }
}
