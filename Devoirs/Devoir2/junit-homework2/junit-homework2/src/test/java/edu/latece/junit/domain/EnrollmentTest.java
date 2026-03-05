package edu.latece.junit.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Enrollment - Tests d'inscription")
class EnrollmentTest {

    @Nested
    @DisplayName("Construction et accès aux données")
    class ConstructorTests {

        @Test
        @DisplayName("Créer une inscription avec données valides")
        void testValidEnrollment() {
            Enrollment enrollment = new Enrollment("S1", "INF101");
            
            assertEquals("S1", enrollment.studentId());
            assertEquals("INF101", enrollment.courseCode());
            assertNotNull(enrollment.createdAt());
        }

        @Test
        @DisplayName("Rejeter un ID étudiant null")
        void testNullStudentId() {
            assertThrows(NullPointerException.class, () -> {
                new Enrollment(null, "INF101");
            });
        }

        @Test
        @DisplayName("Rejeter un code de cours null")
        void testNullCourseCode() {
            assertThrows(NullPointerException.class, () -> {
                new Enrollment("S1", null);
            });
        }

        @Test
        @DisplayName("Rejeter les deux paramètres null")
        void testBothNull() {
            assertThrows(NullPointerException.class, () -> {
                new Enrollment(null, null);
            });
        }
    }

    @Nested
    @DisplayName("Tests de timestamp de création")
    class CreatedAtTests {

        @Test
        @DisplayName("L'horodatage est défini au moment de la création")
        void testCreatedAtIsSet() {
            Instant before = Instant.now();
            Enrollment enrollment = new Enrollment("S1", "INF101");
            Instant after = Instant.now();
            
            assertNotNull(enrollment.createdAt());
            assertFalse(enrollment.createdAt().isBefore(before));
            assertFalse(enrollment.createdAt().isAfter(after.plusSeconds(1)));
        }

        @Test
        @DisplayName("Deux inscriptions créées à des moments différents ont des timestamps différents")
        void testDifferentTimestamps() {
            Enrollment e1 = new Enrollment("S1", "INF101");
            
            // Petite pause
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
            
            Enrollment e2 = new Enrollment("S2", "INF201");
            
            // Généralement e2 doit être plus tard, mais on ne peut pas le garantir sur une si courte période
            assertNotEquals(e1.createdAt(), e2.createdAt());
        }

        @Test
        @DisplayName("Multiple inscriptions créées rapidement")
        void testMultipleEnrollmentTimestamps() {
            Enrollment e1 = new Enrollment("S1", "INF101");
            Enrollment e2 = new Enrollment("S1", "INF201");
            Enrollment e3 = new Enrollment("S1", "INF301");
            
            assertNotNull(e1.createdAt());
            assertNotNull(e2.createdAt());
            assertNotNull(e3.createdAt());
            
            // e3 doit être créé après e1
            assertTrue(e3.createdAt().isAfter(e1.createdAt()) || 
                      e3.createdAt().equals(e1.createdAt()));
        }
    }

    @Nested
    @DisplayName("Tests d'immuabilité")
    class ImmutabilityTests {

        @Test
        @DisplayName("Les données de l'inscription ne peuvent pas être modifiées")
        void testEnrollmentImmutable() {
            Enrollment enrollment = new Enrollment("S1", "INF101");
            
            // Les accesseurs ne retournent que les valeurs, pas de setters
            assertEquals("S1", enrollment.studentId());
            assertEquals("INF101", enrollment.courseCode());
            
            // Pas de setters disponibles
            assertThrows(NoSuchMethodException.class, () -> {
                Enrollment.class.getMethod("setStudentId", String.class);
            });
        }
    }

    @Nested
    @DisplayName("Tests avec différentes données")
    class VariousDataTests {

        @Test
        @DisplayName("Inscription avec ID étudiant court")
        void testShortStudentId() {
            Enrollment enrollment = new Enrollment("S", "INF101");
            assertEquals("S", enrollment.studentId());
        }

        @Test
        @DisplayName("Inscription avec ID étudiant long")
        void testLongStudentId() {
            Enrollment enrollment = new Enrollment("STUDENT_WITH_VERY_LONG_ID_12345", "INF101");
            assertEquals("STUDENT_WITH_VERY_LONG_ID_12345", enrollment.studentId());
        }

        @Test
        @DisplayName("Inscription avec code de cours court")
        void testShortCourseCode() {
            Enrollment enrollment = new Enrollment("S1", "CS");
            assertEquals("CS", enrollment.courseCode());
        }

        @Test
        @DisplayName("Inscription avec code de cours long")
        void testLongCourseCode() {
            Enrollment enrollment = new Enrollment("S1", "COMPUTER_SCIENCE_2024");
            assertEquals("COMPUTER_SCIENCE_2024", enrollment.courseCode());
        }

        @Test
        @DisplayName("Inscription avec caractères spéciaux")
        void testSpecialCharacters() {
            Enrollment enrollment = new Enrollment("S-2024-01", "CS-101-A");
            assertEquals("S-2024-01", enrollment.studentId());
            assertEquals("CS-101-A", enrollment.courseCode());
        }

        @Test
        @DisplayName("Inscription avec espaces")
        void testWhitespacePreserved() {
            Enrollment enrollment = new Enrollment("S 1", "INF 101");
            assertEquals("S 1", enrollment.studentId());
            assertEquals("INF 101", enrollment.courseCode());
        }
    }

    @Nested
    @DisplayName("Tests de comparaison")
    class ComparisonTests {

        @Test
        @DisplayName("Deux inscriptions identiques ne sont pas nécessairement égales (No equals override)")
        void testNoEqualsOverride() {
            Enrollment e1 = new Enrollment("S1", "INF101");
            Enrollment e2 = new Enrollment("S1", "INF101");
            
            // Par défaut, ils ne sont égaux que si c'est le même objet
            assertNotEquals(e1, e2);
        }

        @Test
        @DisplayName("Une inscription est égale à elle-même")
        void testSelfEquality() {
            Enrollment enrollment = new Enrollment("S1", "INF101");
            assertEquals(enrollment, enrollment);
        }

        @Test
        @DisplayName("Deux inscriptions différentes ne sont pas égales")
        void testDifferentEnrollmentsNotEqual() {
            Enrollment e1 = new Enrollment("S1", "INF101");
            Enrollment e2 = new Enrollment("S2", "INF101");
            Enrollment e3 = new Enrollment("S1", "INF201");
            
            assertNotEquals(e1, e2);
            assertNotEquals(e1, e3);
            assertNotEquals(e2, e3);
        }
    }

    @Nested
    @DisplayName("Tests d'intégration avec d'autres classes")
    class IntegrationTests {

        @Test
        @DisplayName("Créer une inscription avec les données du Student")
        void testEnrollmentWithStudentData() {
            Student student = new Student("ALICE", Set.of());
            Enrollment enrollment = new Enrollment(student.id(), "INF101");
            
            assertEquals(student.id(), enrollment.studentId());
        }

        @Test
        @DisplayName("Créer une inscription avec les données du Course")
        void testEnrollmentWithCourseData() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            Course course = new Course("INF101", 30, slot, Set.of());
            Enrollment enrollment = new Enrollment("S1", course.code());
            
            assertEquals(course.code(), enrollment.courseCode());
        }

        @Test
        @DisplayName("Créer plusieurs inscriptions pour le même cours")
        void testMultipleEnrollmentsForSameCourse() {
            Enrollment e1 = new Enrollment("S1", "INF101");
            Enrollment e2 = new Enrollment("S2", "INF101");
            Enrollment e3 = new Enrollment("S3", "INF101");
            
            assertEquals("INF101", e1.courseCode());
            assertEquals("INF101", e2.courseCode());
            assertEquals("INF101", e3.courseCode());
        }

        @Test
        @DisplayName("Créer plusieurs inscriptions pour le même étudiant")
        void testMultipleEnrollmentsForSameStudent() {
            Enrollment e1 = new Enrollment("S1", "INF101");
            Enrollment e2 = new Enrollment("S1", "INF201");
            Enrollment e3 = new Enrollment("S1", "INF301");
            
            assertEquals("S1", e1.studentId());
            assertEquals("S1", e2.studentId());
            assertEquals("S1", e3.studentId());
        }
    }

    @Nested
    @DisplayName("Tests d'accesseurs")
    class AccessorTests {

        @Test
        @DisplayName("studentId() retourne la valeur correcte")
        void testStudentIdAccessor() {
            Enrollment enrollment = new Enrollment("STUDENT123", "INF101");
            assertEquals("STUDENT123", enrollment.studentId());
        }

        @Test
        @DisplayName("courseCode() retourne la valeur correcte")
        void testCourseCodeAccessor() {
            Enrollment enrollment = new Enrollment("S1", "COURSE456");
            assertEquals("COURSE456", enrollment.courseCode());
        }

        @Test
        @DisplayName("createdAt() retourne un Instant valide")
        void testCreatedAtAccessor() {
            Enrollment enrollment = new Enrollment("S1", "INF101");
            Instant createdAt = enrollment.createdAt();
            
            assertNotNull(createdAt);
            assertFalse(createdAt.isAfter(Instant.now()));
        }
    }
}
