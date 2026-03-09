package edu.latece.junit.service;

import edu.latece.junit.domain.Course;
import edu.latece.junit.domain.Enrollment;
import edu.latece.junit.domain.Student;
import edu.latece.junit.domain.TimeSlot;
import edu.latece.junit.repo.CourseCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnrollmentService - Tests de détection bugs")
class EnrollmentServiceTest {

    private CourseCatalog catalog;
    private EnrollmentService service;

    @BeforeEach
    void setUp() {
        catalog = new CourseCatalog();
        service = new EnrollmentService(catalog);
    }

    @Test
    @DisplayName("testTouchingTimeSlotsNoConflict - DÉTECTE BUG TimeSlot.overlapsWith()")
    void testTouchingTimeSlotsNoConflict() {
        // BUG: TimeSlot.overlapsWith() utilise <= au lieu de <, donc créneaux adjacents sont rejetés
        TimeSlot slot1 = new TimeSlot(1, 9, 0, 10, 30);  // [9:00, 10:30)
        TimeSlot slot2 = new TimeSlot(1, 10, 30, 12, 0); // [10:30, 12:00) - Adjacent, PAS de chevauchement
        
        catalog.add(new Course("INF101", 30, slot1, Set.of()));
        catalog.add(new Course("INF102", 30, slot2, Set.of()));
        
        Student student = new Student("S001", Set.of());
        
        // Les deux inscriptions devraient réussir (pas de chevauchement réel)
        Enrollment enroll1 = service.enroll(student, "INF101");
        Enrollment enroll2 = service.enroll(student, "INF102");
        
        assertNotNull(enroll1);
        assertNotNull(enroll2);
    }

    @Test
    @DisplayName("testScheduleConflict - DÉTECTE BUG TimeSlot.overlapsWith() chevauchement réel")
    void testScheduleConflict() {
        // Deux cours qui se chevauchent vraiment
        TimeSlot slot1 = new TimeSlot(1, 9, 0, 10, 30);
        TimeSlot slot2 = new TimeSlot(1, 10, 0, 11, 30); // Chevauche slot1 (10:00-10:30)
        
        catalog.add(new Course("INF101", 30, slot1, Set.of()));
        catalog.add(new Course("INF102", 30, slot2, Set.of()));
        
        Student student = new Student("S001", Set.of());
        service.enroll(student, "INF101");
        
        // Deuxième inscription devrait échouer (conflit d'horaire réel)
        EnrollmentException exception = assertThrows(EnrollmentException.class, () -> {
            service.enroll(student, "INF102");
        });
        
        assertTrue(exception.getMessage().contains("schedule conflict"));
    }

    @Test
    @DisplayName("testCourseNotFoundNormalization - DÉTECTE BUG CourseCatalog.get() normalisation")
    void testCourseNotFoundNormalization() {
        // BUG: CourseCatalog.get() ne normalise pas, donc cours en minuscules introuvable
        catalog.add(new Course("INF101", 30, new TimeSlot(1, 9, 0, 10, 30), Set.of()));
        
        Student student = new Student("S001", Set.of());
        
        // Devrait fonctionner même avec casse différente si get() normalise
        Enrollment enrollment = service.enroll(student, "inf101");
        
        assertNotNull(enrollment);
        assertEquals("INF101", enrollment.courseCode());
    }

    @Test
    @DisplayName("testCapacityIncrementAfterValidation - DÉTECTE BUG ordre validation EnrollmentService")
    void testCapacityIncrementAfterValidation() {
        // BUG: incrementEnrollment() appelé trop tôt, avant validation prérequis
        TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
        Course course = new Course("INF101", 1, slot, Set.of("PRE101"));
        catalog.add(course);
        
        assertEquals(0, course.enrolledCount(), "Capacité initiale devrait être 0");
        
        // Étudiant sans prérequis - inscription échoue
        Student student1 = new Student("S001", Set.of());
        assertThrows(EnrollmentException.class, () -> {
            service.enroll(student1, "INF101");
        });
        
        // BUG DÉTECTÉ: si incrementEnrollment() appelé avant validation, capacité = 1 au lieu de 0
        assertEquals(0, course.enrolledCount(), "Capacité ne devrait pas changer après échec");
        
        // Étudiant avec prérequis - inscription réussit
        Student student2 = new Student("S002", Set.of("PRE101"));
        service.enroll(student2, "INF101");
        
        assertEquals(1, course.enrolledCount(), "Capacité devrait être incrémentée après succès");
    }
}
