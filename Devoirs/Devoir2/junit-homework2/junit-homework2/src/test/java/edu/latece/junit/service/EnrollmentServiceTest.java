package edu.latece.junit.service;

import edu.latece.junit.domain.Course;
import edu.latece.junit.domain.Enrollment;
import edu.latece.junit.domain.Student;
import edu.latece.junit.domain.TimeSlot;
import edu.latece.junit.repo.CourseCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnrollmentService - Tests du service d'inscription")
class EnrollmentServiceTest {

    private CourseCatalog catalog;
    private EnrollmentService service;
    private TimeSlot slotMonday9to10;
    private TimeSlot slotMonday10to11;
    private TimeSlot slotTuesday9to10;

    @BeforeEach
    void setUp() {
        catalog = new CourseCatalog();
        service = new EnrollmentService(catalog);
        
        slotMonday9to10 = new TimeSlot(1, 9, 0, 10, 0);
        slotMonday10to11 = new TimeSlot(1, 10, 0, 11, 0);
        slotTuesday9to10 = new TimeSlot(2, 9, 0, 10, 0);
    }

    @Nested
    @DisplayName("Tests d'inscription réussie")
    class SuccessfulEnrollmentTests {

        @BeforeEach
        void setUp() {
            catalog.add(new Course("INF101", 30, slotMonday9to10, Set.of()));
            catalog.add(new Course("INF201", 30, slotMonday10to11, Set.of("INF101")));
        }

        @Test
        @DisplayName("Inscription réussie à un cours sans prérequis")
        void testSuccessfulEnrollmentNoCourse() {
            Student student = new Student("S1", Set.of());
            Enrollment enrollment = service.enroll(student, "INF101");
            
            assertNotNull(enrollment);
            assertEquals("S1", enrollment.studentId());
            assertEquals("INF101", enrollment.courseCode());
            assertNotNull(enrollment.createdAt());
        }

        @Test
        @DisplayName("Inscription réussie à un cours avec prérequis")
        void testSuccessfulEnrollmentWithPrerequisite() {
            Student student = new Student("S2", Set.of("INF101"));
            Enrollment enrollment = service.enroll(student, "INF201");
            
            assertNotNull(enrollment);
            assertEquals("S2", enrollment.studentId());
            assertEquals("INF201", enrollment.courseCode());
        }

        @Test
        @DisplayName("Code de cours normalisé (uppercase et trim)")
        void testCourseCodeNormalized() {
            Student student = new Student("S1", Set.of());
            Enrollment enrollment = service.enroll(student, "  inf101  ");
            
            assertEquals("INF101", enrollment.courseCode());
        }

        @Test
        @DisplayName("Inscrire le même étudiant à plusieurs cours (sans conflit)")
        void testMultipleEnrollmentsSameStudent() {
            Student student = new Student("S1", Set.of("INF101"));
            
            Enrollment enroll1 = service.enroll(student, "INF101");
            Enrollment enroll2 = service.enroll(student, "INF201");
            
            List<Enrollment> studentEnrollments = service.enrollmentsFor("S1");
            assertEquals(2, studentEnrollments.size());
        }

        @Test
        @DisplayName("L'incrément de capacité ne se produit qu'une fois après validation")
        void testCapacityIncrementAfterValidation() {
            catalog.add(new Course("SMALL", 1, slotTuesday9to10, Set.of()));
            
            Student s1 = new Student("S1", Set.of());
            Student s2 = new Student("S2", Set.of());
            
            // S1 s'inscrit (succès)
            service.enroll(s1, "SMALL");
            
            // S2 essaie de s'inscrire (capacité atteinte)
            assertThrows(EnrollmentException.class, () -> {
                service.enroll(s2, "SMALL");
            });
            
            // Vérifier que la capacité est toujours 1
            Course small = catalog.get("SMALL");
            assertEquals(1, small.enrolledCount());
        }
    }

    @Nested
    @DisplayName("Tests d'erreur : Cours inexistant")
    class CourseNotFoundTests {

        @Test
        @DisplayName("Rejeter inscription pour un cours inexistant")
        void testCourseNotFound() {
            Student student = new Student("S1", Set.of());
            
            EnrollmentException exception = assertThrows(EnrollmentException.class, () -> {
                service.enroll(student, "UNKNOWN");
            });
            
            assertTrue(exception.getMessage().contains("not found") || 
                      exception.getMessage().contains("Course not found"));
        }

        @Test
        @DisplayName("Rejeter null comme code de cours")
        void testNullCourseCode() {
            Student student = new Student("S1", Set.of());
            
            assertThrows(IllegalArgumentException.class, () -> {
                service.enroll(student, null);
            });
        }

        @Test
        @DisplayName("Rejeter code de cours vide")
        void testEmptyCourseCode() {
            Student student = new Student("S1", Set.of());
            
            assertThrows(IllegalArgumentException.class, () -> {
                service.enroll(student, "");
            });
        }

        @Test
        @DisplayName("Rejeter code de cours avec seulement des espaces")
        void testBlankCourseCode() {
            Student student = new Student("S1", Set.of());
            
            assertThrows(IllegalArgumentException.class, () -> {
                service.enroll(student, "   ");
            });
        }
    }

    @Nested
    @DisplayName("Tests d'erreur : Capacité atteinte")
    class CapacityTests {

        @BeforeEach
        void setUp() {
            catalog.add(new Course("LIMITED", 2, slotMonday9to10, Set.of()));
        }

        @Test
        @DisplayName("Rejeter inscription quand la capacité est atteinte")
        void testCapacityReached() {
            Student s1 = new Student("S1", Set.of());
            Student s2 = new Student("S2", Set.of());
            Student s3 = new Student("S3", Set.of());
            
            service.enroll(s1, "LIMITED");
            service.enroll(s2, "LIMITED");
            
            EnrollmentException exception = assertThrows(EnrollmentException.class, () -> {
                service.enroll(s3, "LIMITED");
            });
            
            assertTrue(exception.getMessage().contains("capacity"));
        }

        @Test
        @DisplayName("Vérifier le message d'erreur de capacité")
        void testCapacityErrorMessage() {
            Student s1 = new Student("S1", Set.of());
            Student s2 = new Student("S2", Set.of());
            Student s3 = new Student("S3", Set.of());
            
            service.enroll(s1, "LIMITED");
            service.enroll(s2, "LIMITED");
            
            EnrollmentException exception = assertThrows(EnrollmentException.class, () -> {
                service.enroll(s3, "LIMITED");
            });
            
            assertTrue(exception.getMessage().contains("LIMITED"));
        }
    }

    @Nested
    @DisplayName("Tests d'erreur : Prérequis manquants")
    class PrerequisiteTests {

        @BeforeEach
        void setUp() {
            catalog.add(new Course("INF101", 30, slotMonday9to10, Set.of()));
            catalog.add(new Course("INF201", 30, slotMonday10to11, Set.of("INF101")));
            catalog.add(new Course("INF301", 30, slotTuesday9to10, Set.of("INF101", "INF201")));
        }

        @Test
        @DisplayName("Rejeter inscription sans prérequis manquant")
        void testMissingPrerequisite() {
            Student student = new Student("S1", Set.of());
            
            EnrollmentException exception = assertThrows(EnrollmentException.class, () -> {
                service.enroll(student, "INF201");
            });
            
            assertTrue(exception.getMessage().contains("prerequisite"));
        }

        @Test
        @DisplayName("Vérifier le message du prérequis manquant")
        void testMissingPrerequisiteMessage() {
            Student student = new Student("S1", Set.of());
            
            EnrollmentException exception = assertThrows(EnrollmentException.class, () -> {
                service.enroll(student, "INF201");
            });
            
            assertTrue(exception.getMessage().contains("INF101"));
        }

        @Test
        @DisplayName("Accepter inscription avec tous les prérequis")
        void testAllPrerequisitesMet() {
            Student student = new Student("S1", Set.of("INF101", "INF201"));
            
            Enrollment enrollment = service.enroll(student, "INF301");
            assertNotNull(enrollment);
        }

        @Test
        @DisplayName("Rejeter si un seul prérequis manque sur plusieurs")
        void testOneMissingOutOfMultiplePrerequisites() {
            Student student = new Student("S1", Set.of("INF101")); // INF201 manque
            
            EnrollmentException exception = assertThrows(EnrollmentException.class, () -> {
                service.enroll(student, "INF301");
            });
            
            assertTrue(exception.getMessage().contains("INF201"));
        }

        @Test
        @DisplayName("Cas insensitif pour les codes de prérequis")
        void testPrerequisiteCaseInsensitive() {
            Student student = new Student("S1", Set.of("inf101")); // lowercase
            
            // Devrait accepter car les codes sont normalisés
            Enrollment enrollment = service.enroll(student, "INF201");
            assertNotNull(enrollment);
        }
    }

    @Nested
    @DisplayName("Tests d'erreur : Conflit d'horaire")
    class ScheduleConflictTests {

        @BeforeEach
        void setUp() {
            catalog.add(new Course("INF101", 30, new TimeSlot(1, 9, 0, 10, 30), Set.of()));
            catalog.add(new Course("INF102", 30, new TimeSlot(1, 9, 30, 11, 0), Set.of()));
            catalog.add(new Course("INF201", 30, new TimeSlot(1, 10, 0, 11, 0), Set.of()));
            catalog.add(new Course("INF301", 30, slotTuesday9to10, Set.of()));
        }

        @Test
        @DisplayName("Rejeter inscription avec conflit d'horaire")
        void testScheduleConflict() {
            Student student = new Student("S1", Set.of());
            
            service.enroll(student, "INF101");
            
            EnrollmentException exception = assertThrows(EnrollmentException.class, () -> {
                service.enroll(student, "INF102");
            });
            
            assertTrue(exception.getMessage().contains("conflict"));
        }

        @Test
        @DisplayName("Vérifier le message de conflit d'horaire")
        void testConflictErrorMessage() {
            Student student = new Student("S1", Set.of());
            
            service.enroll(student, "INF101");
            
            EnrollmentException exception = assertThrows(EnrollmentException.class, () -> {
                service.enroll(student, "INF102");
            });
            
            assertTrue(exception.getMessage().contains("INF101"));
        }

        @Test
        @DisplayName("Accepter des cours sans conflit d'horaire")
        void testNoScheduleConflict() {
            Student student = new Student("S1", Set.of());
            
            Enrollment e1 = service.enroll(student, "INF101");
            Enrollment e2 = service.enroll(student, "INF301"); // Different day
            
            assertNotNull(e1);
            assertNotNull(e2);
        }

        @Test
        @DisplayName("Créneaux qui se touchent ne causent PAS de conflit")
        void testTouchingTimeSlotsNoConflict() {
            Student student = new Student("S1", Set.of());
            
            service.enroll(student, "INF101"); // 9:00-10:30
            // INF201 c'est 10:00-11:00 - début = fin de INF101, pas de conflit
            Enrollment e2 = service.enroll(student, "INF201");
            
            assertNotNull(e2);
        }
    }

    @Nested
    @DisplayName("Tests d'énumération d'inscriptions")
    class EnrollmentEnumerationTests {

        @BeforeEach
        void setUp() {
            catalog.add(new Course("INF101", 30, slotMonday9to10, Set.of()));
            catalog.add(new Course("INF201", 30, slotMonday10to11, Set.of("INF101")));
            catalog.add(new Course("INF301", 30, slotTuesday9to10, Set.of()));
        }

        @Test
        @DisplayName("Étudiant sans inscription")
        void testStudentWithNoEnrollments() {
            List<Enrollment> enrollments = service.enrollmentsFor("S1");
            
            assertTrue(enrollments.isEmpty());
        }

        @Test
        @DisplayName("Récupérer les inscriptions d'un étudiant")
        void testGetStudentEnrollments() {
            Student student = new Student("S1", Set.of("INF101"));
            
            service.enroll(student, "INF101");
            service.enroll(student, "INF201");
            service.enroll(student, "INF301");
            
            List<Enrollment> enrollments = service.enrollmentsFor("S1");
            
            assertEquals(3, enrollments.size());
        }

        @Test
        @DisplayName("Inscriptions filtrées par étudiant")
        void testEnrollmentFiltering() {
            Student s1 = new Student("S1", Set.of("INF101"));
            Student s2 = new Student("S2", Set.of("INF101"));
            
            service.enroll(s1, "INF101");
            service.enroll(s2, "INF101");
            service.enroll(s2, "INF301");
            
            List<Enrollment> s1Enrollments = service.enrollmentsFor("S1");
            List<Enrollment> s2Enrollments = service.enrollmentsFor("S2");
            
            assertEquals(1, s1Enrollments.size());
            assertEquals(2, s2Enrollments.size());
        }

        @Test
        @DisplayName("ID étudiant normalisé (trim)")
        void testStudentIdNormalized() {
            Student student = new Student("  S1  ", Set.of());
            service.enroll(student, "INF101");
            
            List<Enrollment> enrollments1 = service.enrollmentsFor("S1");
            List<Enrollment> enrollments2 = service.enrollmentsFor("  S1  ");
            
            assertEquals(1, enrollments1.size());
            assertEquals(1, enrollments2.size());
        }

        @Test
        @DisplayName("Null comme ID étudiant")
        void testNullStudentId() {
            assertThrows(NullPointerException.class, () -> {
                service.enrollmentsFor(null);
            });
        }

        @Test
        @DisplayName("Les inscriptions retournées sont immuables")
        void testReturnedEnrollmentsImmutable() {
            Student student = new Student("S1", Set.of());
            service.enroll(student, "INF101");
            
            List<Enrollment> enrollments = service.enrollmentsFor("S1");
            
            assertThrows(UnsupportedOperationException.class, () -> {
                enrollments.add(new Enrollment("S2", "INF201"));
            });
        }
    }

    @Nested
    @DisplayName("Tests d'erreurs de paramètres")
    class ParameterValidationTests {

        @BeforeEach
        void setUp() {
            catalog.add(new Course("INF101", 30, slotMonday9to10, Set.of()));
        }

        @Test
        @DisplayName("Rejeter null comme étudiant")
        void testNullStudent() {
            assertThrows(NullPointerException.class, () -> {
                service.enroll(null, "INF101");
            });
        }

        @Test
        @DisplayName("Rejeter null comme catalogue")
        void testNullCatalog() {
            assertThrows(NullPointerException.class, () -> {
                new EnrollmentService(null);
            });
        }
    }

    @Nested
    @DisplayName("Tests d'intégration - scénarios complexes")
    class IntegrationTests {

        @BeforeEach
        void setUp() {
            // Setup: Créer un catalogue réaliste
            catalog.add(new Course("INF101", 2, new TimeSlot(1, 9, 0, 10, 30), Set.of()));
            catalog.add(new Course("INF102", 2, new TimeSlot(1, 11, 0, 12, 30), Set.of()));
            catalog.add(new Course("INF201", 1, new TimeSlot(2, 9, 0, 10, 30), Set.of("INF101")));
            catalog.add(new Course("INF202", 2, new TimeSlot(3, 14, 0, 15, 30), Set.of("INF101", "INF102")));
        }

        @Test
        @DisplayName("Scénario : Étudiant qui s'inscrit progressivement")
        void testGradualEnrollment() {
            Student alice = new Student("ALICE", Set.of());
            
            // Alice s'inscrit à INF101
            Enrollment e1 = service.enroll(alice, "INF101");
            assertEquals("INF101", e1.courseCode());
            
            // Alice s'inscrit à INF102 (pas de conflit horaire)
            Enrollment e2 = service.enroll(alice, "INF102");
            assertEquals("INF102", e2.courseCode());
            
            List<Enrollment> aliceEnrollments = service.enrollmentsFor("ALICE");
            assertEquals(2, aliceEnrollments.size());
        }

        @Test
        @DisplayName("Scénario : Plusieurs étudiants, course remplie")
        void testMultipleStudentsSameClass() {
            Student s1 = new Student("S1", Set.of());
            Student s2 = new Student("S2", Set.of());
            Student s3 = new Student("S3", Set.of());
            
            // INF101 a une capacité de 2
            service.enroll(s1, "INF101");
            service.enroll(s2, "INF101");
            
            // S3 ne peut pas s'inscrire
            assertThrows(EnrollmentException.class, () -> {
                service.enroll(s3, "INF101");
            });
        }

        @Test
        @DisplayName("Scénario : Tous les checks doivent passer")
        void testAllChecksMustPass() {
            Student bob = new Student("BOB", Set.of("INF101"));
            
            // Bob peut s'inscrire à INF201
            Enrollment e = service.enroll(bob, "INF201");
            assertEquals(1, service.enrollmentsFor("BOB").size());
        }
    }
}
