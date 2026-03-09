package edu.latece.junit.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimeSlot - Tests de détection bugs")
class TimeSlotTest {

    @Test
    @DisplayName("testTouchingEndpointsNoOverlap - Créneaux adjacents ne devraient PAS se chevaucher")
    void testTouchingEndpointsNoOverlap() {
        // [9:00, 10:30) et [10:30, 12:00) sont adjacents mais ne se chevauchent pas
        TimeSlot slot1 = new TimeSlot(1, 9, 0, 10, 30);
        TimeSlot slot2 = new TimeSlot(1, 10, 30, 12, 0);
        
        assertFalse(slot1.overlapsWith(slot2), 
            "Créneaux adjacents (fin de slot1 = début de slot2) ne devraient PAS se chevaucher");
        assertFalse(slot2.overlapsWith(slot1), 
            "La relation devrait être symétrique");
    }

    @Test
    @DisplayName("testActualOverlap - Créneaux qui se chevauchent vraiment")
    void testActualOverlap() {
        // [9:00, 10:30) et [10:00, 11:30) se chevauchent de 10:00 à 10:30
        TimeSlot slot1 = new TimeSlot(1, 9, 0, 10, 30);
        TimeSlot slot2 = new TimeSlot(1, 10, 0, 11, 30);
        
        assertTrue(slot1.overlapsWith(slot2), 
            "Ces créneaux devraient se chevaucher");
        assertTrue(slot2.overlapsWith(slot1), 
            "La relation devrait être symétrique");
    }

    @Test
    @DisplayName("testNoOverlapDifferentDays - Créneaux sur jours différents")
    void testNoOverlapDifferentDays() {
        TimeSlot monday = new TimeSlot(1, 9, 0, 10, 30);
        TimeSlot tuesday = new TimeSlot(2, 9, 0, 10, 30);
        
        assertFalse(monday.overlapsWith(tuesday), 
            "Créneaux sur jours différents ne devraient pas se chevaucher");
    }
}
