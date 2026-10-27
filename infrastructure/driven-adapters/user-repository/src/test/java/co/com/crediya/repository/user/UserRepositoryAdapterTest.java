package co.com.crediya.repository.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

class UserRepositoryAdapterTest {
    private UserRepositoryAdapter userRepository;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);
        userRepository = new UserRepositoryAdapter(new JdbcTemplate(dataSource));
    }

    @Test
    void savesUserWithGeneratedIdAndCanFindEmailWithoutCaseSensitivity() {
        User saved = userRepository.save(user("ana@example.com"));

        assertNotNull(saved.getId());
        assertTrue(userRepository.existsByCorreoElectronico("ANA@EXAMPLE.COM"));
    }

    @Test
    void rejectsDuplicateEmailRegardlessOfCase() {
        userRepository.save(user("ana@example.com"));

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userRepository.save(user("ANA@example.com")));
    }

    @Test
    void findsUserByDocumentAndRejectsDuplicateDocument() {
        userRepository.save(user("ana@example.com").toBuilder().documentoIdentidad("1020304050").build());

        assertTrue(userRepository.existsByDocumentoIdentidad("1020304050"));
        assertEquals("Ana", userRepository.findByDocumentoIdentidad("1020304050").orElseThrow().getNombres());
        assertTrue(userRepository.findByDocumentoIdentidad("999999").isEmpty());
        assertThrows(
                UserAlreadyExistsException.class,
                () -> userRepository.save(user("otra@example.com").toBuilder()
                        .documentoIdentidad("1020304050").build()));
    }

    @Test
    void preservesUserDataWhenSaving() {
        User user = user("ana@example.com");

        User saved = userRepository.save(user);

        assertEquals(user.getNombres(), saved.getNombres());
        assertEquals(user.getApellidos(), saved.getApellidos());
        assertEquals(user.getSalarioBase(), saved.getSalarioBase());
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
