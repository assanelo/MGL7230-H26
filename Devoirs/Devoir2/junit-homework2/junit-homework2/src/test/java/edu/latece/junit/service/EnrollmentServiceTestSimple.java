package edu.latece.junit.service;

import edu.latece.junit.domain.Course;
import edu.latece.junit.domain.Enrollment;
import edu.latece.junit.domain.Student;
import edu.latece.junit.domain.TimeSlot;
import edu.latece.junit.repo.CourseCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegistrationSystem (EnrollmentService) - Tests essentiels")
class EnrollmentServiceTestSimple {

    private CourseCatalog catalog;
    private EnrollmentService service;

    @BeforeEach
    void setUp() {
        catalog = new CourseCatalog();
        service = new EnrollmentService(catalog);
    }

    @Test
    @DisplayName("testSuccessfulEnrollmentNoCourse - Inscription réussie si toutes les validations passent")
    void testSuccessfulEnrollmentNoCourse() {
        // Cours sans prérequis
        TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
        catalog.add(new Course("INF101", 30, slot, Set.of()));
        
        Student student = new Student("S001", Set.of());
        Enrollment enrollment = service.enroll(student, "INF101");
        
        assertNotNull(enrollment);
        assertEquals("S001", enrollment.studentId());
        assertEquals("INF101", enrollment.courseCode());
        assertNotNull(enrollment.createdAt());
    }

    @Test
    @DisplayName("testCourseNotFound - Exception si le cours n'existe pas dans le catalogue")
    void testCourseNotFound() {
        Student student = new Student("S001", Set.of());
        
        EnrollmentException exception = assertThrows(EnrollmentException.class, () -> {
            service.enroll(student, "INF999");
        });
        
        assertTrue(exception.getMessage().contains("Course not found"));
    }

    @Test
    @DisplayName("testMissingPrerequisite - Exception si un prérequis est manquant")
    void testMissingPrerequisite() {
        TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
        catalog.add(new Course("INF201", 30, slot, Set.of("INF101")));
        
        // Étudiant n'a pas complété INF101
        Student student = new Student("S001", Set.of());
        
        EnrollmentException exception = assertThrows(EnrollmentException.class, () -> {
            service.enroll(student, "INF201");
        });
        
        assertTrue(exception.getMessage().contains("missing prerequisite"));
    }

    @Test
    @DisplayName("testScheduleConflict - Exception si conflit d'horaire avec une inscription existante")
    void testScheduleConflict() {
        // Deux cours qui se chevauchent (même jour, heures qui se croisent)
        TimeSlot slot1 = new TimeSlot(1, 9, 0, 10, 30);
        TimeSlot slot2 = new TimeSlot(1, 10, 0, 11, 30); // Chevauche slot1 (10:00-10:30)
        
        catalog.add(new Course("INF101", 30, slot1, Set.of()));
        catalog.add(new Course("INF102", 30, slot2, Set.of()));
        
        Student student = new Student("S001", Set.of());
        
        // Première inscription réussie
        service.enroll(student, "INF101");
        
        // Deuxième inscription devrait échouer (conflit d'horaire)
        EnrollmentException exception = assertThrows(EnrollmentException.class, () -> {
            service.enroll(student, "INF102");
        });
        
        assertTrue(exception.getMessage().contains("schedule conflict"));
    }

    @Test
    @DisplayName("testTouchingTimeSlotsNoConflict - Aucune exception pour des horaires adjacents (sans chevauchement)")
    void testTouchingTimeSlotsNoConflict() {
        // Deux cours adjacents mais qui ne se chevauchent pas
        TimeSlot slot1 = new TimeSlot(1, 9, 0, 10, 30);  // [9:00, 10:30)
        TimeSlot slot2 = new TimeSlot(1, 10, 30, 12, 0); // [10:30, 12:00) - Touche mais ne chevauche pas
        
        catalog.add(new Course("INF101", 30, slot1, Set.of()));
        catalog.add(new Course("INF102", 30, slot2, Set.of()));
        
        Student student = new Student("S001", Set.of());
        
        // Les deux inscriptions devraient réussir (pas de chevauchement)
        Enrollment enroll1 = service.enroll(student, "INF101");
        Enrollment enroll2 = service.enroll(student, "INF102");
        
        assertNotNull(enroll1);
        assertNotNull(enroll2);
    }

    @Test
    @DisplayName("testCapacityIncrementAfterValidation - La capacité est incrémentée seulement après validations réussies")
    void testCapacityIncrementAfterValidation() {
        TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
        Course course = new Course("INF101", 1, slot, Set.of("PRE101"));
        catalog.add(course);
        
        assertEquals(0, course.enrolledCount(), "Capacité initiale devrait être 0");
        
        // Étudiant sans le prérequis - inscription échoue
        Student student1 = new Student("S001", Set.of());
        assertThrows(EnrollmentException.class, () -> {
            service.enroll(student1, "INF101");
        });
        
        assertEquals(0, course.enrolledCount(), "Capacité ne devrait pas changer après échec");
        
        // Étudiant avec le prérequis - inscription réussit
        Student student2 = new Student("S002", Set.of("PRE101"));
        service.enroll(student2, "INF101");
        
        assertEquals(1, course.enrolledCount(), "Capacité devrait être incrémentée après succès");
    }

    @Test
    @DisplayName("testGetStudentEnrollments - Retourne les inscriptions du bon étudiant uniquement")
    void testGetStudentEnrollments() {
        TimeSlot slot1 = new TimeSlot(1, 9, 0, 10, 30);
        TimeSlot slot2 = new TimeSlot(2, 9, 0, 10, 30);
        TimeSlot slot3 = new TimeSlot(3, 9, 0, 10, 30);
        
        catalog.add(new Course("INF101", 30, slot1, Set.of()));
        catalog.add(new Course("INF102", 30, slot2, Set.of()));
        catalog.add(new Course("INF103", 30, slot3, Set.of()));
        
        Student student1 = new Student("S001", Set.of());
        Student student2 = new Student("S002", Set.of());
        
        // S001 s'inscrit à 2 cours
        service.enroll(student1, "INF101");
        service.enroll(student1, "INF102");
        
        // S002 s'inscrit à 1 cours
        service.enroll(student2, "INF103");
        
        // Vérifier les inscriptions de S001
        List<Enrollment> enrollmentsS001 = service.enrollmentsFor("S001");
        assertEquals(2, enrollmentsS001.size());
        assertTrue(enrollmentsS001.stream().anyMatch(e -> e.courseCode().equals("INF101")));
        assertTrue(enrollmentsS001.stream().anyMatch(e -> e.courseCode().equals("INF102")));
        
        // Vérifier les inscriptions de S002
        List<Enrollment> enrollmentsS002 = service.enrollmentsFor("S002");
        assertEquals(1, enrollmentsS002.size());
        assertEquals("INF103", enrollmentsS002.get(0).courseCode());
    }

    @Test
    @DisplayName("testReturnedEnrollmentsImmutable - La liste renvoyée par enrollmentsFor est immuable")
    void testReturnedEnrollmentsImmutable() {
        TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
        catalog.add(new Course("INF101", 30, slot, Set.of()));
        
        Student student = new Student("S001", Set.of());
        service.enroll(student, "INF101");
        
        List<Enrollment> enrollments = service.enrollmentsFor("S001");
        
        // Tenter de modifier la liste retournée devrait lever une exception
        assertThrows(UnsupportedOperationException.class, () -> {
            enrollments.add(new Enrollment("S999", "INF999"));
        });
        
        assertThrows(UnsupportedOperationException.class, () -> {
            enrollments.remove(0);
        });
    }
}
