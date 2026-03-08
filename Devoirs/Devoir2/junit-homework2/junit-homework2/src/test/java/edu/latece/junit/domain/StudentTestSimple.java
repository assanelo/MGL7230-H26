package edu.latece.junit.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Student - Tests essentiels")
class StudentTestSimple {

    @Test
    @DisplayName("testValidStudentCreation - L'étudiant est créé avec id/nom valides")
    void testValidStudentCreation() {
        Student student = new Student("S12345", Set.of("INF101", "INF201"));
        
        assertEquals("S12345", student.id());
        assertTrue(student.hasCompleted("INF101"));
        assertTrue(student.hasCompleted("INF201"));
        assertEquals(2, student.completedCourses().size());
    }

    @Test
    @DisplayName("testIdTrimmed - L'identifiant de l'étudiant est trimé")
    void testIdTrimmed() {
        Student student = new Student("  S12345  ", Set.of());
        
        assertEquals("S12345", student.id());
    }

    @Test
    @DisplayName("testHasCompletedCaseInsensitive - hasCompleted fonctionne sans sensibilité à la casse")
    void testHasCompletedCaseInsensitive() {
        Student student = new Student("S001", Set.of("INF101", "MAT201"));
        
        assertTrue(student.hasCompleted("INF101"));
        assertTrue(student.hasCompleted("inf101"));
        assertTrue(student.hasCompleted("Inf101"));
        assertTrue(student.hasCompleted("MAT201"));
        assertTrue(student.hasCompleted("mat201"));
    }

    @Test
    @DisplayName("testCompletedCoursesImmutable - La collection retournée est immuable")
    void testCompletedCoursesImmutable() {
        Student student = new Student("S001", Set.of("INF101"));
        Set<String> completed = student.completedCourses();
        
        // Tenter de modifier la collection retournée devrait lever une exception
        assertThrows(UnsupportedOperationException.class, () -> {
            completed.add("INF201");
        });
    }
}
