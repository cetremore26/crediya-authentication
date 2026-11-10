package co.com.crediya.jpa.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.com.crediya.jpa.JpaTestApplication;
import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(classes = JpaTestApplication.class)
class UserRepositoryAdapterTest {

    @Autowired
    private UserRepositoryAdapter userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM usuarios");
    }

    @Test
    void savesUserWithGeneratedIdAndCanFindEmailWithoutCaseSensitivity() {
        User saved = userRepository.save(user("ana@example.com"));

        assertNotNull(saved.getId());
        assertEquals("ana@example.com", saved.getCorreoElectronico());
        assertTrue(userRepository.existsByCorreoElectronico("ANA@EXAMPLE.COM"));
    }

    @Test
    void rejectsDuplicateEmail() {
        userRepository.save(user("ana@example.com"));

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userRepository.save(user("ana@example.com")));
    }

    @Test
    void doesNotReportOtherIntegrityErrorsAsDuplicates() {
        User withoutName = user("ana@example.com").toBuilder().nombres(null).build();

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(withoutName));
    }

    @Test
    void findsUserByDocumentAndRejectsDuplicateDocument() {
        userRepository.save(user("ana@example.com").toBuilder()
                .documentoIdentidad("1020304050")
                .clave("hash")
                .build());

        assertTrue(userRepository.existsByDocumentoIdentidad("1020304050"));
        User found = userRepository.findByDocumentoIdentidad("1020304050").orElseThrow();
        assertEquals("Ana", found.getNombres());
        assertEquals("hash", found.getClave());
        assertTrue(userRepository.findByDocumentoIdentidad("999999").isEmpty());
        assertThrows(
                UserAlreadyExistsException.class,
                () -> userRepository.save(user("otra@example.com").toBuilder()
                        .documentoIdentidad("1020304050").build()));
    }

    @Test
    void findsUserByEmailWithPasswordHashAndRole() {
        userRepository.save(user("ana@example.com").toBuilder()
                .clave("hash")
                .rol(Rol.ADMIN)
                .build());

        User found = userRepository.findByCorreoElectronico("ANA@example.com").orElseThrow();

        assertEquals("hash", found.getClave());
        assertEquals(Rol.ADMIN, found.getRol());
        assertTrue(userRepository.findByCorreoElectronico("nadie@example.com").isEmpty());
    }

    @Test
    void preservesUserDataWhenSaving() {
        User user = user("ana@example.com");

        User saved = userRepository.save(user);

        assertEquals(user.getNombres(), saved.getNombres());
        assertEquals(user.getApellidos(), saved.getApellidos());
        assertEquals(0, user.getSalarioBase().compareTo(saved.getSalarioBase()));
    }

    private User user(String email) {
        return User.builder()
                .nombres("Ana")
                .apellidos("Pérez")
                .correoElectronico(email)
                .salarioBase(new BigDecimal("2500000"))
                .build();
    }
}
