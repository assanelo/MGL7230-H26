package edu.latece.junit.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Course - Tests essentiels")
class CourseTestSimple {

    @Test
    @DisplayName("testValidCourse - Le cours est créé correctement avec les valeurs fournies")
    void testValidCourse() {
        TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
        Course course = new Course("INF101", 30, slot, Set.of("PRE101"));
        
        assertEquals("INF101", course.code());
        assertEquals(30, course.capacity());
        assertEquals(slot, course.timeSlot());
        assertEquals(Set.of("PRE101"), course.prerequisites());
        assertEquals(0, course.enrolledCount());
    }

    @Test
    @DisplayName("testCodeNormalized - Le code de cours est normalisé (trim + majuscules)")
    void testCodeNormalized() {
        TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
        Course course = new Course("  inf101  ", 30, slot, Set.of());
        
        assertEquals("INF101", course.code());
    }

    @Test
    @DisplayName("testIncrementEnrollmentWhenFull - Exception levée si capacité déjà atteinte")
    void testIncrementEnrollmentWhenFull() {
        TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
        Course course = new Course("INF101", 2, slot, Set.of());
        
        // Remplir la capacité
        course.incrementEnrollment();
        course.incrementEnrollment();
        
        // Tenter d'ajouter un troisième étudiant devrait lever une exception
        assertThrows(IllegalStateException.class, () -> {
            course.incrementEnrollment();
        });
    }

    @Test
    @DisplayName("testSeatAvailabilityTransition - hasSeatAvailable passe de true à false")
    void testSeatAvailabilityTransition() {
        TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
        Course course = new Course("INF101", 2, slot, Set.of());
        
        assertTrue(course.hasSeatAvailable(), "Devrait avoir des places disponibles au début");
        
        course.incrementEnrollment();
        assertTrue(course.hasSeatAvailable(), "Devrait encore avoir 1 place disponible");
        
        course.incrementEnrollment();
        assertFalse(course.hasSeatAvailable(), "Ne devrait plus avoir de places disponibles");
    }
}
