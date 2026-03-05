package edu.latece.junit;

import edu.latece.junit.domain.Course;
import edu.latece.junit.domain.Enrollment;
import edu.latece.junit.domain.Student;
import edu.latece.junit.domain.TimeSlot;
import edu.latece.junit.repo.CourseCatalog;
import edu.latece.junit.service.EnrollmentException;
import edu.latece.junit.service.EnrollmentService;

import java.util.Set;

/**
 * Petit programme de démonstration pour exécuter la logique métier (domaine)
 * sans passer par les tests.
 */
public class App {

    public static void main(String[] args) {
        CourseCatalog catalog = new CourseCatalog();

        // Cours
        catalog.add(new Course("INF101", 2, new TimeSlot(1, 9, 0, 10, 30), Set.of()));
        catalog.add(new Course("INF201", 1, new TimeSlot(1, 10, 30, 12, 0), Set.of("INF101")));
        catalog.add(new Course("INF301", 2, new TimeSlot(1, 9, 30, 11, 0), Set.of())); // chevauche INF101

        EnrollmentService service = new EnrollmentService(catalog);

        Student alice = new Student("S1", Set.of("INF101"));
        Student bob = new Student("S2", Set.of()); // n’a pas complété INF101

        System.out.println("=== Scénario 1 : Inscription réussie (Alice -> INF201) ===");
        tryEnroll(service, alice, "INF201");
        System.out.println();

        System.out.println("=== Scénario 2 : Capacité atteinte (Bob -> INF201) ===");
        // INF201 a une capacité de 1, et Alice l’a déjà pris.
        tryEnroll(service, bob, "INF201");
        System.out.println();

        System.out.println("=== Scénario 3 : Prérequis manquant (Bob -> INF201) ===");
        // Nouveau service + nouveau catalogue pour que l’échec provienne du prérequis (et non de la capacité)
        catalog = new CourseCatalog();
        catalog.add(new Course("INF101", 2, new TimeSlot(1, 9, 0, 10, 30), Set.of()));
        catalog.add(new Course("INF201", 1, new TimeSlot(1, 10, 30, 12, 0), Set.of("INF101")));
        service = new EnrollmentService(catalog);

        tryEnroll(service, bob, "INF201");
        System.out.println();

        System.out.println("=== Scénario 4 : Conflit d’horaire (Alice -> INF101 puis INF301) ===");
        catalog = new CourseCatalog();
        catalog.add(new Course("INF101", 2, new TimeSlot(1, 9, 0, 10, 30), Set.of()));
        catalog.add(new Course("INF301", 2, new TimeSlot(1, 9, 30, 11, 0), Set.of())); // chevauche INF101
        service = new EnrollmentService(catalog);

        tryEnroll(service, alice, "INF101"); // ok
        tryEnroll(service, alice, "INF301"); // échec attendu : chevauchement
    }

    private static void tryEnroll(EnrollmentService service, Student student, String courseCode) {
        try {
            Enrollment e = service.enroll(student, courseCode);
            System.out.println("✅ Inscription OK : étudiant=" + e.studentId() + " cours=" + e.courseCode());
            System.out.println("Inscriptions actuelles pour " + student.id() + " : " + service.enrollmentsFor(student.id()).size());
        } catch (EnrollmentException ex) {
            System.out.println("❌ Inscription refusée : " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("❌ Erreur inattendue : " + ex);
        }
    }
}
