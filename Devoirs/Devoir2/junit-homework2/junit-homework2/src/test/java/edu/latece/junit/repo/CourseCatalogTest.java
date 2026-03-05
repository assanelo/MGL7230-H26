package edu.latece.junit.repo;

import edu.latece.junit.domain.Course;
import edu.latece.junit.domain.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CourseCatalog - Tests du catalogue de cours")
class CourseCatalogTest {

    private TimeSlot defaultSlot;

    @BeforeEach
    void setUp() {
        defaultSlot = new TimeSlot(1, 9, 0, 10, 30);
    }

    @Nested
    @DisplayName("Construction et opérations basiques")
    class BasicOperationsTests {

        @Test
        @DisplayName("Créer un catalogue vide")
        void testEmptyCatalog() {
            CourseCatalog catalog = new CourseCatalog();
            assertEquals(0, catalog.size());
        }

        @Test
        @DisplayName("Ajouter un cours au catalogue")
        void testAddCourse() {
            CourseCatalog catalog = new CourseCatalog();
            Course course = new Course("INF101", 30, defaultSlot, Set.of());
            
            catalog.add(course);
            assertEquals(1, catalog.size());
        }

        @Test
        @DisplayName("Ajouter plusieurs cours au catalogue")
        void testAddMultipleCourses() {
            CourseCatalog catalog = new CourseCatalog();
            catalog.add(new Course("INF101", 30, defaultSlot, Set.of()));
            catalog.add(new Course("INF201", 25, defaultSlot, Set.of()));
            catalog.add(new Course("INF301", 20, defaultSlot, Set.of()));
            
            assertEquals(3, catalog.size());
        }

        @Test
        @DisplayName("Rejeter null comme cours")
        void testAddNullCourse() {
            CourseCatalog catalog = new CourseCatalog();
            assertThrows(NullPointerException.class, () -> {
                catalog.add(null);
            });
        }

        @Test
        @DisplayName("Rejeter un cours avec un code dupliqué")
        void testDuplicateCourseCode() {
            CourseCatalog catalog = new CourseCatalog();
            Course course1 = new Course("INF101", 30, defaultSlot, Set.of());
            Course course2 = new Course("INF101", 25, defaultSlot, Set.of());
            
            catalog.add(course1);
            assertThrows(IllegalArgumentException.class, () -> {
                catalog.add(course2);
            });
        }

        @Test
        @DisplayName("Rejeter un cours avec un code dupliqué (casse différente)")
        void testDuplicateCourseCodeDifferentCase() {
            CourseCatalog catalog = new CourseCatalog();
            Course course1 = new Course("inf101", 30, defaultSlot, Set.of());
            Course course2 = new Course("INF101", 25, defaultSlot, Set.of());
            
            catalog.add(course1);
            assertThrows(IllegalArgumentException.class, () -> {
                catalog.add(course2);
            });
        }
    }

    @Nested
    @DisplayName("Recherche de cours (get)")
    class GetTests {

        private CourseCatalog catalog;

        @BeforeEach
        void setUp() {
            catalog = new CourseCatalog();
            catalog.add(new Course("INF101", 30, defaultSlot, Set.of()));
            catalog.add(new Course("INF201", 25, defaultSlot, Set.of()));
        }

        @Test
        @DisplayName("Récupérer un cours existant par son code exact")
        void testGetExistingCourse() {
            Course course = catalog.get("INF101");
            assertNotNull(course);
            assertEquals("INF101", course.code());
        }

        @Test
        @DisplayName("Récupérer un cours avec casse différente (normalized)")
        void testGetCourseNormalizedCase() {
            Course course1 = catalog.get("inf101");
            Course course2 = catalog.get("INF101");
            Course course3 = catalog.get("Inf101");
            
            assertNotNull(course1);
            assertNotNull(course2);
            assertNotNull(course3);
            assertEquals(course1.code(), course2.code());
            assertEquals(course2.code(), course3.code());
        }

        @Test
        @DisplayName("Récupérer un cours avec espaces (trim)")
        void testGetCourseWithWhitespace() {
            Course course1 = catalog.get("  INF101  ");
            Course course2 = catalog.get("\tINF201\t");
            Course course3 = catalog.get("INF101");
            
            assertNotNull(course1);
            assertNotNull(course2);
            assertEquals(course1.code(), course3.code());
        }

        @Test
        @DisplayName("Retourner null pour un cours non existant")
        void testGetNonExistentCourse() {
            Course course = catalog.get("INF999");
            assertNull(course);
        }

        @Test
        @DisplayName("Retourner null pour null comme input")
        void testGetNull() {
            Course course = catalog.get(null);
            assertNull(course);
        }

        @Test
        @DisplayName("Retourner null pour une chaîne vide")
        void testGetEmptyString() {
            Course course = catalog.get("");
            assertNull(course);
        }

        @Test
        @DisplayName("Récupérer le bon cours parmi plusieurs")
        void testGetCorrectCourseFromMultiple() {
            Course inf201 = catalog.get("INF201");
            assertNotNull(inf201);
            assertEquals(25, inf201.capacity());
        }
    }

    @Nested
    @DisplayName("Tests de normalisation (cas fix)")
    class NormalizationTests {

        @Test
        @DisplayName("Normalisation multipl: trim + uppercase")
        void testMultipleNormalizations() {
            CourseCatalog catalog = new CourseCatalog();
            catalog.add(new Course("  inf101  ", 30, defaultSlot, Set.of()));
            
            // Tous ces codes doivent trouver le même cours
            assertNotNull(catalog.get("INF101"));
            assertNotNull(catalog.get("inf101"));
            assertNotNull(catalog.get("  INF101  "));
            assertNotNull(catalog.get("\tInF101\r\n"));
        }

        @Test
        @DisplayName("Pas de faux positifs avec codes similaires")
        void testNoFalsePositives() {
            CourseCatalog catalog = new CourseCatalog();
            catalog.add(new Course("INF101", 30, defaultSlot, Set.of()));
            catalog.add(new Course("INF102", 25, defaultSlot, Set.of()));
            
            assertNotNull(catalog.get("INF101"));
            assertNotNull(catalog.get("INF102"));
            assertNull(catalog.get("INF103"));
        }

        @Test
        @DisplayName("Codes avec caractères spéciaux rester distincts")
        void testSpecialCharacters() {
            CourseCatalog catalog = new CourseCatalog();
            Course course = new Course("CS-101", 30, defaultSlot, Set.of());
            catalog.add(course);
            
            assertNotNull(catalog.get("CS-101"));
            assertNull(catalog.get("CS101")); // CS-101 != CS101
        }
    }

    @Nested
    @DisplayName("Tests de catalogue avec nombreux cours")
    class LargeCatalogTests {

        @Test
        @DisplayName("Catalogue avec 10 cours")
        void testCatalogWithTenCourses() {
            CourseCatalog catalog = new CourseCatalog();
            
            for (int i = 1; i <= 10; i++) {
                String code = "INF" + (100 + i);
                catalog.add(new Course(code, 30, defaultSlot, Set.of()));
            }
            
            assertEquals(10, catalog.size());
            assertNotNull(catalog.get("INF101"));
            assertNotNull(catalog.get("INF110"));
            assertNull(catalog.get("INF111"));
        }

        @Test
        @DisplayName("Récupération efficace dans un grand catalogue")
        void testLargeCatalogRetrieval() {
            CourseCatalog catalog = new CourseCatalog();
            
            // Ajouter 100 cours
            for (int i = 1; i <= 100; i++) {
                String code = String.format("CS%03d", i);
                catalog.add(new Course(code, 30, defaultSlot, Set.of()));
            }
            
            // Vérifier quelques accès
            assertNotNull(catalog.get("CS001"));
            assertNotNull(catalog.get("CS050"));
            assertNotNull(catalog.get("CS100"));
            assertNull(catalog.get("CS101"));
        }
    }

    @Nested
    @DisplayName("Tests de cas limites et erreurs")
    class EdgeCasesTests {

        @Test
        @DisplayName("Message d'erreur pour code dupliqué")
        void testDuplicateCodeErrorMessage() {
            CourseCatalog catalog = new CourseCatalog();
            Course course1 = new Course("INF101", 30, defaultSlot, Set.of());
            catalog.add(course1);
            
            Course course2 = new Course("INF101", 25, defaultSlot, Set.of());
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                catalog.add(course2);
            });
            
            assertTrue(exception.getMessage().contains("Duplicate"));
        }

        @Test
        @DisplayName("Catalogue indépendant de l'ordre d'ajout")
        void testIndependentOfAdditionOrder() {
            CourseCatalog catalog1 = new CourseCatalog();
            catalog1.add(new Course("INF101", 30, defaultSlot, Set.of()));
            catalog1.add(new Course("INF201", 25, defaultSlot, Set.of()));
            
            CourseCatalog catalog2 = new CourseCatalog();
            catalog2.add(new Course("INF201", 25, defaultSlot, Set.of()));
            catalog2.add(new Course("INF101", 30, defaultSlot, Set.of()));
            
            assertEquals(catalog1.size(), catalog2.size());
            assertEquals(catalog1.get("INF101").code(), catalog2.get("INF101").code());
        }

        @Test
        @DisplayName("Codes avec seulement des chiffres")
        void testNumericOnlyCodes() {
            CourseCatalog catalog = new CourseCatalog();
            Course course = new Course("101", 30, defaultSlot, Set.of());
            catalog.add(course);
            
            assertNotNull(catalog.get("101"));
        }
    }
}
