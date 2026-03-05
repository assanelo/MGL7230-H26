package edu.latece.junit.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Student - Tests d'étudiant")
class StudentTest {

    @Nested
    @DisplayName("Construction et validation de Student")
    class ConstructorTests {

        @Test
        @DisplayName("Créer un étudiant avec ID valide et cours complétés")
        void testValidStudentCreation() {
            Student student = new Student("S123", Set.of("INF101", "INF201"));
            assertEquals("S123", student.id());
            assertTrue(student.hasCompleted("INF101"));
            assertTrue(student.hasCompleted("INF201"));
        }

        @Test
        @DisplayName("Créer un étudiant sans cours complétés")
        void testStudentWithoutCompletedCourses() {
            Student student = new Student("S456", Set.of());
            assertEquals("S456", student.id());
            assertEquals(0, student.completedCourses().size());
        }

        @Test
        @DisplayName("Créer un étudiant avec null comme cours complétés")
        void testStudentWithNullCourses() {
            Student student = new Student("S789", null);
            assertEquals("S789", student.id());
            assertEquals(0, student.completedCourses().size());
        }

        @Test
        @DisplayName("Rejeter un ID null")
        void testNullId() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Student(null, Set.of());
            });
        }

        @Test
        @DisplayName("Rejeter un ID vide")
        void testEmptyId() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Student("", Set.of());
            });
        }

        @Test
        @DisplayName("Rejeter un ID avec seulement des espaces")
        void testBlankId() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Student("   ", Set.of());
            });
        }

        @Test
        @DisplayName("Normaliser l'ID (trim)")
        void testIdTrimmed() {
            Student student = new Student("  S123  ", Set.of());
            assertEquals("S123", student.id());
        }

        @Test
        @DisplayName("Normaliser les codes de cours (uppercase et trim)")
        void testCourseCodesNormalized() {
            Student student = new Student("S1", Set.of(" inf101 ", "INF201", "Inf301"));
            assertTrue(student.hasCompleted("INF101"));
            assertTrue(student.hasCompleted("inf101"));
            assertTrue(student.hasCompleted("INF201"));
            assertTrue(student.hasCompleted("INF301"));
        }

        @Test
        @DisplayName("Ignorer les codes de cours null ou vides")
        void testIgnoreNullOrBlankCourses() {
            Student student = new Student("S1", Set.of("INF101", null, "", "  ", "INF201"));
            assertEquals(2, student.completedCourses().size());
            assertTrue(student.hasCompleted("INF101"));
            assertTrue(student.hasCompleted("INF201"));
        }
    }

    @Nested
    @DisplayName("Tests de hasCompleted()")
    class HasCompletedTests {

        private Student student;

        @BeforeEach
        void setUp() {
            student = new Student("S1", Set.of("INF101", "INF201"));
        }

        @Test
        @DisplayName("Vérifier qu'un cours complété est reconnu")
        void testHasCompletedExistingCourse() {
            assertTrue(student.hasCompleted("INF101"));
        }

        @Test
        @DisplayName("Vérifier qu'un cours non complété n'est pas reconnu")
        void testHasCompletedNonExistingCourse() {
            assertFalse(student.hasCompleted("INF301"));
        }

        @Test
        @DisplayName("Vérifier hasCompleted avec casse différente")
        void testHasCompletedCaseInsensitive() {
            assertTrue(student.hasCompleted("inf101"));
            assertTrue(student.hasCompleted("Inf101"));
            assertTrue(student.hasCompleted("INF101"));
        }

        @Test
        @DisplayName("Vérifier hasCompleted avec espaces")
        void testHasCompletedWithWhitespace() {
            assertTrue(student.hasCompleted("  INF101  "));
            assertTrue(student.hasCompleted("\tINF201\t"));
        }

        @Test
        @DisplayName("Rejeter null dans hasCompleted()")
        void testHasCompletedNull() {
            assertThrows(NullPointerException.class, () -> {
                student.hasCompleted(null);
            });
        }
    }

    @Nested
    @DisplayName("Tests d'immuabilité")
    class ImmutabilityTests {

        @Test
        @DisplayName("Les cours complétés ne peuvent pas être modifiés")
        void testCompletedCoursesImmutable() {
            Set<String> courses = Set.of("INF101");
            Student student = new Student("S1", courses);
            
            assertThrows(UnsupportedOperationException.class, () -> {
                student.completedCourses().add("INF201");
            });
        }

        @Test
        @DisplayName("Accès répété à completedCourses() retourne le même ensemble")
        void testCompletedCoursesConsistency() {
            Student student = new Student("S1", Set.of("INF101", "INF201"));
            Set<String> courses1 = student.completedCourses();
            Set<String> courses2 = student.completedCourses();
            
            assertEquals(courses1, courses2);
            assertEquals(2, courses1.size());
        }
    }

    @Nested
    @DisplayName("Tests de cas limites")
    class EdgeCaseTests {

        @Test
        @DisplayName("Étudiant avec nombreux cours complétés")
        void testStudentWithManyCourses() {
            Set<String> courses = Set.of("INF101", "INF201", "INF301", "INF401", 
                                         "INF501", "INF601", "INF701", "INF801");
            Student student = new Student("S1", courses);
            
            assertEquals(8, student.completedCourses().size());
            assertTrue(student.hasCompleted("INF501"));
        }

        @Test
        @DisplayName("ID avec caractères spéciaux")
        void testIdWithSpecialCharacters() {
            Student student = new Student("S-123-456", Set.of());
            assertEquals("S-123-456", student.id());
        }

        @Test
        @DisplayName("Codes de cours avec chiffres")
        void testCourseCodesWithNumbers() {
            Student student = new Student("S1", Set.of("INF2023", "CS4801"));
            assertTrue(student.hasCompleted("INF2023"));
            assertTrue(student.hasCompleted("CS4801"));
        }
    }
}
