package co.com.crediya.model.usuario;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RolTest {

    @Test
    void adminAssignsEveryRole() {
        for (Rol rol : Rol.values()) {
            assertTrue(Rol.ADMIN.puedeAsignar(rol));
        }
    }

    @Test
    void advisorOnlyAssignsClient() {
        assertTrue(Rol.ASESOR.puedeAsignar(Rol.CLIENTE));
        assertFalse(Rol.ASESOR.puedeAsignar(Rol.ASESOR));
        assertFalse(Rol.ASESOR.puedeAsignar(Rol.ADMIN));
    }

    @Test
    void clientAssignsNothing() {
        for (Rol rol : Rol.values()) {
            assertFalse(Rol.CLIENTE.puedeAsignar(rol));
        }
    }

    @Test
    void nobodyAssignsAMissingRole() {
        for (Rol rol : Rol.values()) {
            assertFalse(rol.puedeAsignar(null));
        }
    }
}
