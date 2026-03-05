package edu.latece.junit.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Course - Tests de cours")
class CourseTest {

    @Nested
    @DisplayName("Construction et validation de Course")
    class ConstructorTests {

        @Test
        @DisplayName("Créer un cours avec paramètres valides")
        void testValidCourse() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            Course course = new Course("INF101", 30, slot, Set.of("PRE101"));
            
            assertEquals("INF101", course.code());
            assertEquals(30, course.capacity());
            assertEquals(slot, course.timeSlot());
            assertEquals(Set.of("PRE101"), course.prerequisites());
        }

        @Test
        @DisplayName("Normaliser le code de cours (uppercase)")
        void testCodeNormalized() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            Course course = new Course("inf101", 30, slot, Set.of());
            
            assertEquals("INF101", course.code());
        }

        @Test
        @DisplayName("Rejeter un code null")
        void testNullCode() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            assertThrows(IllegalArgumentException.class, () -> {
                new Course(null, 30, slot, Set.of());
            });
        }

        @Test
        @DisplayName("Rejeter un code vide")
        void testEmptyCode() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            assertThrows(IllegalArgumentException.class, () -> {
                new Course("", 30, slot, Set.of());
            });
        }

        @Test
        @DisplayName("Rejeter un code avec seulement des espaces")
        void testBlankCode() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            assertThrows(IllegalArgumentException.class, () -> {
                new Course("   ", 30, slot, Set.of());
            });
        }

        @Test
        @DisplayName("Rejeter une capacité <= 0")
        void testZeroCapacity() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            assertThrows(IllegalArgumentException.class, () -> {
                new Course("INF101", 0, slot, Set.of());
            });
        }

        @Test
        @DisplayName("Rejeter une capacité négative")
        void testNegativeCapacity() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            assertThrows(IllegalArgumentException.class, () -> {
                new Course("INF101", -5, slot, Set.of());
            });
        }

        @Test
        @DisplayName("Rejeter un TimeSlot null")
        void testNullTimeSlot() {
            assertThrows(NullPointerException.class, () -> {
                new Course("INF101", 30, null, Set.of());
            });
        }

        @Test
        @DisplayName("Accepter un ensemble null de prérequis")
        void testNullPrerequisites() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            Course course = new Course("INF101", 30, slot, null);
            
            assertEquals(0, course.prerequisites().size());
        }

        @Test
        @DisplayName("Normaliser les codes de prérequis (uppercase et trim)")
        void testPrerequisitesNormalized() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            Course course = new Course("INF201", 30, slot, 
                Set.of(" inf101 ", "PRE201", "pre301"));
            
            assertTrue(course.prerequisites().contains("INF101"));
            assertTrue(course.prerequisites().contains("PRE201"));
            assertTrue(course.prerequisites().contains("PRE301"));
        }

        @Test
        @DisplayName("Ignorer les codes null ou vides dans les prérequis")
        void testIgnoreNullPrerequisites() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            Course course = new Course("INF201", 30, slot, 
                new java.util.HashSet<>(java.util.Arrays.asList("INF101", null, "", "  ", "INF301")));
            
            assertEquals(2, course.prerequisites().size());
            assertTrue(course.prerequisites().contains("INF101"));
            assertTrue(course.prerequisites().contains("INF301"));
        }

        @Test
        @DisplayName("Créer un cours sans prérequis")
        void testCourseWithoutPrerequisites() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            Course course = new Course("INF101", 30, slot, Set.of());
            
            assertEquals(0, course.prerequisites().size());
        }
    }

    @Nested
    @DisplayName("Tests d'inscription (capacity management)")
    class EnrollmentCapacityTests {

        private Course course;

        @BeforeEach
        void setUp() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            course = new Course("INF101", 2, slot, Set.of());
        }

        @Test
        @DisplayName("Nouveau cours a 0 inscrits")
        void testInitialEnrollmentCount() {
            assertEquals(0, course.enrolledCount());
        }

        @Test
        @DisplayName("hasSeatAvailable() retourne true pour nouveau cours")
        void testHasSeatAvailableInitially() {
            assertTrue(course.hasSeatAvailable());
        }

        @Test
        @DisplayName("Incrémenter les inscriptions une fois")
        void testIncrementEnrollmentOnce() {
            course.incrementEnrollment();
            
            assertEquals(1, course.enrolledCount());
            assertTrue(course.hasSeatAvailable());
        }

        @Test
        @DisplayName("Incrémenter les inscriptions jusqu'à la capacité")
        void testIncrementEnrollmentToCapacity() {
            course.incrementEnrollment();
            course.incrementEnrollment();
            
            assertEquals(2, course.enrolledCount());
            assertFalse(course.hasSeatAvailable());
        }

        @Test
        @DisplayName("Rejeter incrementEnrollment() quand capacité est atteinte")
        void testIncrementEnrollmentWhenFull() {
            course.incrementEnrollment();
            course.incrementEnrollment();
            
            assertThrows(IllegalStateException.class, () -> {
                course.incrementEnrollment();
            });
        }

        @Test
        @DisplayName("Vérifier hasSeatAvailable() avant et après plénitude")
        void testSeatAvailabilityTransition() {
            assertTrue(course.hasSeatAvailable());
            
            course.incrementEnrollment();
            assertTrue(course.hasSeatAvailable());
            
            course.incrementEnrollment();
            assertFalse(course.hasSeatAvailable());
        }
    }

    @Nested
    @DisplayName("Tests avec différentes capacités")
    class CapacityVariationTests {

        @Test
        @DisplayName("Cours de petite capacité (1)")
        void testSmallCapacity() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            Course course = new Course("INF101", 1, slot, Set.of());
            
            assertTrue(course.hasSeatAvailable());
            course.incrementEnrollment();
            assertFalse(course.hasSeatAvailable());
        }

        @Test
        @DisplayName("Cours de grande capacité (100)")
        void testLargeCapacity() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            Course course = new Course("INF101", 100, slot, Set.of());
            
            assertEquals(100, course.capacity());
            assertTrue(course.hasSeatAvailable());
            
            for (int i = 0; i < 99; i++) {
                course.incrementEnrollment();
            }
            
            assertEquals(99, course.enrolledCount());
            assertTrue(course.hasSeatAvailable());
            course.incrementEnrollment();
            assertFalse(course.hasSeatAvailable());
        }
    }

    @Nested
    @DisplayName("Tests d'immuabilité des prérequis")
    class ImmutabilityTests {

        @Test
        @DisplayName("Les prérequis ne peuvent pas être modifiés")
        void testPrerequisitesImmutable() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            Course course = new Course("INF201", 30, slot, Set.of("INF101"));
            
            assertThrows(UnsupportedOperationException.class, () -> {
                course.prerequisites().add("INF301");
            });
        }
    }

    @Nested
    @DisplayName("Tests d'égalité de codes de cours")
    class CodeEqualityTests {

        @Test
        @DisplayName("Deux cours avec le même code (différentes casses) ont le même code normalisé")
        void testCodeNormalizationConsistency() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            Course course1 = new Course("inf101", 30, slot, Set.of());
            Course course2 = new Course("INF101", 30, slot, Set.of());
            
            assertEquals(course1.code(), course2.code());
        }

        @Test
        @DisplayName("Code avec espaces est normalisé")
        void testCodeWithWhitespace() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            Course course = new Course("  INF101  ", 30, slot, Set.of());
            
            assertEquals("INF101", course.code());
        }
    }
}
