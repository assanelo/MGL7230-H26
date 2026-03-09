package edu.latece.junit.repo;

import edu.latece.junit.domain.Course;
import edu.latece.junit.domain.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CourseCatalog - Tests de détection bugs")
class CourseCatalogTest {

    private CourseCatalog catalog;
    private TimeSlot defaultSlot;

    @BeforeEach
    void setUp() {
        catalog = new CourseCatalog();
        defaultSlot = new TimeSlot(1, 9, 0, 10, 30);
    }

    @Test
    @DisplayName("testGetCourseNormalizedCase - get() devrait normaliser la casse")
    void testGetCourseNormalizedCase() {
        catalog.add(new Course("INF101", 30, defaultSlot, Set.of()));
        
        // Devrait trouver le cours même avec casse différente
        assertNotNull(catalog.get("inf101"), 
            "get() devrait normaliser en majuscules");
        assertNotNull(catalog.get("Inf101"), 
            "get() devrait être insensible à la casse");
        assertNotNull(catalog.get("INF101"), 
            "get() devrait fonctionner avec majuscules");
    }

    @Test
    @DisplayName("testGetCourseWithWhitespace - get() devrait gérer les espaces")
    void testGetCourseWithWhitespace() {
        catalog.add(new Course("INF101", 30, defaultSlot, Set.of()));
        
        // Devrait trouver le cours même avec espaces
        assertNotNull(catalog.get("  INF101  "), 
            "get() devrait trim les espaces");
        assertNotNull(catalog.get("\tINF101\t"), 
            "get() devrait gérer les caractères blancs");
    }

    @Test
    @DisplayName("testGetNonExistentCourse - get() retourne null si cours inexistant")
    void testGetNonExistentCourse() {
        catalog.add(new Course("INF101", 30, defaultSlot, Set.of()));
        
        assertNull(catalog.get("INF999"), 
            "get() devrait retourner null pour un cours inexistant");
    }

    @Test
    @DisplayName("testAddAndSize - Ajout de cours et taille du catalogue")
    void testAddAndSize() {
        assertEquals(0, catalog.size());
        
        catalog.add(new Course("INF101", 30, defaultSlot, Set.of()));
        assertEquals(1, catalog.size());
        
        catalog.add(new Course("INF201", 25, defaultSlot, Set.of()));
        assertEquals(2, catalog.size());
    }
}
