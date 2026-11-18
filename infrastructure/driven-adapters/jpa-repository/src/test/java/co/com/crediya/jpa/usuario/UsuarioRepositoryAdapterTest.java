package co.com.crediya.jpa.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.com.crediya.jpa.JpaTestApplication;
import co.com.crediya.model.usuario.Rol;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.exception.UsuarioYaRegistradoException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(classes = JpaTestApplication.class)
class UsuarioRepositoryAdapterTest {

    @Autowired
    private UsuarioRepositoryAdapter usuarioRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM usuarios");
    }

    @Test
    void savesUserWithGeneratedIdAndCanFindEmailWithoutCaseSensitivity() {
        Usuario saved = usuarioRepository.guardar(usuario("ana@example.com"));

        assertNotNull(saved.getId());
        assertEquals("ana@example.com", saved.getCorreoElectronico());
        assertTrue(usuarioRepository.existePorCorreoElectronico("ANA@EXAMPLE.COM"));
    }

    @Test
    void rejectsDuplicateEmail() {
        usuarioRepository.guardar(usuario("ana@example.com"));

        assertThrows(
                UsuarioYaRegistradoException.class,
                () -> usuarioRepository.guardar(usuario("ana@example.com")));
    }

    @Test
    void doesNotReportOtherIntegrityErrorsAsDuplicates() {
        Usuario withoutName = usuario("ana@example.com").toBuilder().nombres(null).build();

        assertThrows(DataIntegrityViolationException.class, () -> usuarioRepository.guardar(withoutName));
    }

    @Test
    void findsUserByDocumentAndRejectsDuplicateDocument() {
        usuarioRepository.guardar(usuario("ana@example.com").toBuilder()
                .documentoIdentidad("1020304050")
                .clave("hash")
                .build());

        assertTrue(usuarioRepository.existePorDocumentoIdentidad("1020304050"));
        Usuario found = usuarioRepository.buscarPorDocumentoIdentidad("1020304050").orElseThrow();
        assertEquals("Ana", found.getNombres());
        assertEquals("hash", found.getClave());
        assertTrue(usuarioRepository.buscarPorDocumentoIdentidad("999999").isEmpty());
        assertThrows(
                UsuarioYaRegistradoException.class,
                () -> usuarioRepository.guardar(usuario("otra@example.com").toBuilder()
                        .documentoIdentidad("1020304050").build()));
    }

    @Test
    void findsUserByEmailWithPasswordHashAndRole() {
        usuarioRepository.guardar(usuario("ana@example.com").toBuilder()
                .clave("hash")
                .rol(Rol.ADMIN)
                .build());

        Usuario found = usuarioRepository.buscarPorCorreoElectronico("ANA@example.com").orElseThrow();

        assertEquals("hash", found.getClave());
        assertEquals(Rol.ADMIN, found.getRol());
        assertTrue(usuarioRepository.buscarPorCorreoElectronico("nadie@example.com").isEmpty());
    }

    @Test
    void preservesUserDataWhenSaving() {
        Usuario usuario = usuario("ana@example.com");

        Usuario saved = usuarioRepository.guardar(usuario);

        assertEquals(usuario.getNombres(), saved.getNombres());
        assertEquals(usuario.getApellidos(), saved.getApellidos());
        assertEquals(0, usuario.getSalarioBase().compareTo(saved.getSalarioBase()));
    }

    private Usuario usuario(String email) {
        return Usuario.builder()
                .nombres("Ana")
                .apellidos("Pérez")
                .correoElectronico(email)
                .salarioBase(new BigDecimal("2500000"))
                .rol(Rol.CLIENTE)
                .build();
    }
}
