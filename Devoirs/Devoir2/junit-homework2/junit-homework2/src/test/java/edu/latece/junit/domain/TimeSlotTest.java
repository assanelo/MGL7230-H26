package edu.latece.junit.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimeSlot - Tests de créneau horaire")
class TimeSlotTest {

    @Nested
    @DisplayName("Construction et validation du TimeSlot")
    class ConstructorTests {

        @Test
        @DisplayName("Créer un créneau horaire valide")
        void testValidTimeSlot() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 30);
            assertEquals(1, slot.dayOfWeek());
            assertEquals(540, slot.startMinutes()); // 9*60
            assertEquals(630, slot.endMinutes()); // 10*60 + 30
        }

        @Test
        @DisplayName("Rejeter créneau avec jour invalide < 1")
        void testInvalidDayTooSmall() {
            assertThrows(IllegalArgumentException.class, () -> {
                new TimeSlot(0, 9, 0, 10, 0);
            });
        }

        @Test
        @DisplayName("Rejeter créneau avec jour invalide > 7")
        void testInvalidDayTooLarge() {
            assertThrows(IllegalArgumentException.class, () -> {
                new TimeSlot(8, 9, 0, 10, 0);
            });
        }

        @Test
        @DisplayName("Rejeter heure de début invalide (< 0)")
        void testInvalidStartHourNegative() {
            assertThrows(IllegalArgumentException.class, () -> {
                new TimeSlot(1, -1, 0, 10, 0);
            });
        }

        @Test
        @DisplayName("Rejeter heure de début invalide (> 23)")
        void testInvalidStartHourTooLarge() {
            assertThrows(IllegalArgumentException.class, () -> {
                new TimeSlot(1, 24, 0, 25, 0);
            });
        }

        @Test
        @DisplayName("Rejeter minute de début invalide (< 0)")
        void testInvalidStartMinuteNegative() {
            assertThrows(IllegalArgumentException.class, () -> {
                new TimeSlot(1, 9, -1, 10, 0);
            });
        }

        @Test
        @DisplayName("Rejeter minute de début invalide (> 59)")
        void testInvalidStartMinuteTooLarge() {
            assertThrows(IllegalArgumentException.class, () -> {
                new TimeSlot(1, 9, 60, 10, 0);
            });
        }

        @Test
        @DisplayName("Rejeter heure de fin invalide (< 0)")
        void testInvalidEndHourNegative() {
            assertThrows(IllegalArgumentException.class, () -> {
                new TimeSlot(1, 9, 0, -1, 0);
            });
        }

        @Test
        @DisplayName("Rejeter heure de fin invalide (> 23)")
        void testInvalidEndHourTooLarge() {
            assertThrows(IllegalArgumentException.class, () -> {
                new TimeSlot(1, 9, 0, 24, 0);
            });
        }

        @Test
        @DisplayName("Rejeter minute de fin invalide (> 59)")
        void testInvalidEndMinuteTooLarge() {
            assertThrows(IllegalArgumentException.class, () -> {
                new TimeSlot(1, 9, 0, 10, 60);
            });
        }

        @Test
        @DisplayName("Rejeter créneau où la fin = début")
        void testEndTimeEqualsStartTime() {
            assertThrows(IllegalArgumentException.class, () -> {
                new TimeSlot(1, 9, 0, 9, 0);
            });
        }

        @Test
        @DisplayName("Rejeter créneau où la fin < début")
        void testEndTimeBeforeStartTime() {
            assertThrows(IllegalArgumentException.class, () -> {
                new TimeSlot(1, 10, 0, 9, 0);
            });
        }
    }

    @Nested
    @DisplayName("Tests de chevauchement de créneaux horaires")
    class OverlapTests {

        @Test
        @DisplayName("Deux créneaux identiques se chevauchent")
        void testIdenticalSlotsOverlap() {
            TimeSlot slot1 = new TimeSlot(1, 9, 0, 10, 0);
            TimeSlot slot2 = new TimeSlot(1, 9, 0, 10, 0);
            assertTrue(slot1.overlapsWith(slot2));
            assertTrue(slot2.overlapsWith(slot1));
        }

        @Test
        @DisplayName("Créneaux partiellement chevauchants")
        void testPartialOverlap() {
            TimeSlot slot1 = new TimeSlot(1, 9, 0, 10, 0);
            TimeSlot slot2 = new TimeSlot(1, 9, 30, 10, 30);
            assertTrue(slot1.overlapsWith(slot2));
            assertTrue(slot2.overlapsWith(slot1));
        }

        @Test
        @DisplayName("Un créneau contient l'autre")
        void testContainment() {
            TimeSlot slot1 = new TimeSlot(1, 9, 0, 11, 0);
            TimeSlot slot2 = new TimeSlot(1, 9, 30, 10, 30);
            assertTrue(slot1.overlapsWith(slot2));
            assertTrue(slot2.overlapsWith(slot1));
        }

        @Test
        @DisplayName("Créneaux qui se touchent n'ont PAS de chevauchement (endpoints exclusifs)")
        void testTouchingEndpointsNoOverlap() {
            TimeSlot slot1 = new TimeSlot(1, 9, 0, 10, 0);
            TimeSlot slot2 = new TimeSlot(1, 10, 0, 11, 0);
            assertFalse(slot1.overlapsWith(slot2));
            assertFalse(slot2.overlapsWith(slot1));
        }

        @Test
        @DisplayName("Créneaux complètement séparés")
        void testCompletelyDisjoint() {
            TimeSlot slot1 = new TimeSlot(1, 9, 0, 10, 0);
            TimeSlot slot2 = new TimeSlot(1, 11, 0, 12, 0);
            assertFalse(slot1.overlapsWith(slot2));
            assertFalse(slot2.overlapsWith(slot1));
        }

        @Test
        @DisplayName("Créneaux sur des jours différents ne se chevauchent pas")
        void testDifferentDaysNoOverlap() {
            TimeSlot slot1 = new TimeSlot(1, 9, 0, 10, 0);
            TimeSlot slot2 = new TimeSlot(2, 9, 0, 10, 0);
            assertFalse(slot1.overlapsWith(slot2));
            assertFalse(slot2.overlapsWith(slot1));
        }

        @Test
        @DisplayName("Créneaux aux extrémités de la semaine")
        void testMondayAndSunday() {
            TimeSlot monday = new TimeSlot(1, 14, 0, 15, 0);
            TimeSlot sunday = new TimeSlot(7, 14, 0, 15, 0);
            assertFalse(monday.overlapsWith(sunday));
        }

        @Test
        @DisplayName("Un créneau se chevauche avec lui-même")
        void testOverlapWithSelf() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 0);
            assertTrue(slot.overlapsWith(slot));
        }

        @Test
        @DisplayName("Null argument doit lancer NullPointerException")
        void testNullArgument() {
            TimeSlot slot = new TimeSlot(1, 9, 0, 10, 0);
            assertThrows(NullPointerException.class, () -> {
                slot.overlapsWith(null);
            });
        }

        @Test
        @DisplayName("Cas limite : fin de slot1 = début de slot2 + 1 minute")
        void testAlmostTouchingEndpoints() {
            TimeSlot slot1 = new TimeSlot(1, 9, 0, 10, 0);
            TimeSlot slot2 = new TimeSlot(1, 9, 59, 11, 0);
            assertTrue(slot1.overlapsWith(slot2));
        }
    }

    @Nested
    @DisplayName("Tests de conversion de minutes")
    class MinuteConversionTests {

        @Test
        @DisplayName("Minuit (0h0m) = 0 minutes")
        void testMidnight() {
            TimeSlot slot = new TimeSlot(1, 0, 0, 1, 0);
            assertEquals(0, slot.startMinutes());
            assertEquals(60, slot.endMinutes());
        }

        @Test
        @DisplayName("Midi (12h0m) = 720 minutes")
        void testNoon() {
            TimeSlot slot = new TimeSlot(1, 12, 0, 13, 0);
            assertEquals(720, slot.startMinutes());
            assertEquals(780, slot.endMinutes());
        }

        @Test
        @DisplayName("Fin de journée (23h59m) = 1439 minutes")
        void testLateEvening() {
            TimeSlot slot = new TimeSlot(1, 22, 0, 23, 59);
            assertEquals(1320, slot.startMinutes());
            assertEquals(1439, slot.endMinutes());
        }

        @Test
        @DisplayName("Horaire avec minutes décalées")
        void testOffsetMinutes() {
            TimeSlot slot = new TimeSlot(1, 14, 30, 15, 45);
            assertEquals(870, slot.startMinutes()); // 14*60+30
            assertEquals(945, slot.endMinutes()); // 15*60+45
        }
    }
}
