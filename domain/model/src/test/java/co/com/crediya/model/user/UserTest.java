package co.com.crediya.model.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.user.exception.UserValidationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void normalizesEmailDocumentAndDefaultRole() {
        User user = validUser().toBuilder()
                .correoElectronico("  Ana@Example.COM ")
                .documentoIdentidad(" 1020304050 ")
                .rol(null)
                .build()
                .normalized();

        assertEquals("ana@example.com", user.getCorreoElectronico());
        assertEquals("1020304050", user.getDocumentoIdentidad());
        assertEquals(Rol.CLIENTE, user.getRol());
    }

    @Test
    void normalizesBlankDocumentToNull() {
        User user = validUser().toBuilder().documentoIdentidad("   ").build().normalized();

        assertNull(user.getDocumentoIdentidad());
    }

    @Test
    void normalizedDoesNotModifyOriginal() {
        User original = validUser().toBuilder().correoElectronico(" ANA@example.com").build();

        original.normalized();

        assertEquals(" ANA@example.com", original.getCorreoElectronico());
    }

    @Test
    void acceptsValidUser() {
        assertDoesNotThrow(() -> validUser().normalized().validate());
    }

    @Test
    void rejectsInvalidData() {
        assertThrows(UserValidationException.class,
                () -> validUser().toBuilder().nombres(" ").build().normalized().validate());
        assertThrows(UserValidationException.class,
                () -> validUser().toBuilder().correoElectronico("sin-arroba").build().normalized().validate());
        assertThrows(UserValidationException.class,
                () -> validUser().toBuilder().documentoIdentidad("12ab").build().normalized().validate());
        assertThrows(UserValidationException.class,
                () -> validUser().toBuilder().clave("corta").build().normalized().validate());
        assertThrows(UserValidationException.class,
                () -> validUser().toBuilder().salarioBase(new BigDecimal("-1")).build().normalized().validate());
    }

    @Test
    void requiresDocumentOnlyForClients() {
        assertThrows(UserValidationException.class,
                () -> validUser().toBuilder().documentoIdentidad(null).build().normalized().validate());
        assertDoesNotThrow(() -> validUser().toBuilder()
                .documentoIdentidad(null)
                .rol(Rol.ASESOR)
                .build()
                .normalized()
                .validate());
    }

    private User validUser() {
        return User.builder()
                .nombres("Ana")
                .apellidos("Pérez")
                .documentoIdentidad("1020304050")
                .correoElectronico("ana@example.com")
                .salarioBase(new BigDecimal("2500000"))
                .clave("Clave1234")
                .build();
    }
}
