package co.com.crediya.model.usuario;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.usuario.exception.UsuarioInvalidoException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    void normalizesEmailDocumentAndDefaultRole() {
        Usuario usuario = usuarioValido().toBuilder()
                .correoElectronico("  Ana@Example.COM ")
                .documentoIdentidad(" 1020304050 ")
                .rol(null)
                .build()
                .normalizado();

        assertEquals("ana@example.com", usuario.getCorreoElectronico());
        assertEquals("1020304050", usuario.getDocumentoIdentidad());
        assertEquals(Rol.CLIENTE, usuario.getRol());
    }

    @Test
    void normalizesBlankDocumentToNull() {
        Usuario usuario = usuarioValido().toBuilder().documentoIdentidad("   ").build().normalizado();

        assertNull(usuario.getDocumentoIdentidad());
    }

    @Test
    void normalizedDoesNotModifyOriginal() {
        Usuario original = usuarioValido().toBuilder().correoElectronico(" ANA@example.com").build();

        original.normalizado();

        assertEquals(" ANA@example.com", original.getCorreoElectronico());
    }

    @Test
    void acceptsValidUser() {
        assertDoesNotThrow(() -> usuarioValido().normalizado().validar());
    }

    @Test
    void rejectsInvalidData() {
        assertThrows(UsuarioInvalidoException.class,
                () -> usuarioValido().toBuilder().nombres(" ").build().normalizado().validar());
        assertThrows(UsuarioInvalidoException.class,
                () -> usuarioValido().toBuilder().correoElectronico("sin-arroba").build().normalizado().validar());
        assertThrows(UsuarioInvalidoException.class,
                () -> usuarioValido().toBuilder().documentoIdentidad("12ab").build().normalizado().validar());
        assertThrows(UsuarioInvalidoException.class,
                () -> usuarioValido().toBuilder().clave("corta").build().normalizado().validar());
        assertThrows(UsuarioInvalidoException.class,
                () -> usuarioValido().toBuilder().salarioBase(new BigDecimal("-1")).build().normalizado().validar());
    }

    @Test
    void requiresDocumentOnlyForClients() {
        assertThrows(UsuarioInvalidoException.class,
                () -> usuarioValido().toBuilder().documentoIdentidad(null).build().normalizado().validar());
        assertDoesNotThrow(() -> usuarioValido().toBuilder()
                .documentoIdentidad(null)
                .rol(Rol.ASESOR)
                .build()
                .normalizado()
                .validar());
    }

    private Usuario usuarioValido() {
        return Usuario.builder()
                .nombres("Ana")
                .apellidos("Pérez")
                .documentoIdentidad("1020304050")
                .correoElectronico("ana@example.com")
                .salarioBase(new BigDecimal("2500000"))
                .clave("Clave1234")
                .build();
    }
}
