package co.com.crediya.repository.user;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import co.com.crediya.model.user.gateways.UserRepository;
import java.sql.PreparedStatement;
import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserRepositoryAdapter implements UserRepository {
    private static final String INSERT_USER = """
            INSERT INTO usuarios (
                id, nombres, apellidos, fecha_nacimiento, direccion, telefono,
                correo_electronico, salario_base
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public UserRepositoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsByCorreoElectronico(String correoElectronico) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM usuarios WHERE LOWER(correo_electronico) = LOWER(?))",
                Boolean.class,
                correoElectronico);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    @Transactional
    public User save(User user) {
        UUID id = user.getId() == null ? UUID.randomUUID() : user.getId();
        String email = user.getCorreoElectronico().trim().toLowerCase(Locale.ROOT);

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(INSERT_USER);
                statement.setObject(1, id);
                statement.setString(2, user.getNombres());
                statement.setString(3, user.getApellidos());
                statement.setObject(4, user.getFechaNacimiento());
                statement.setString(5, user.getDireccion());
                statement.setString(6, user.getTelefono());
                statement.setString(7, email);
                statement.setBigDecimal(8, user.getSalarioBase());
                return statement;
            });
        } catch (DuplicateKeyException exception) {
            throw new UserAlreadyExistsException();
        }

        return user.toBuilder().id(id).correoElectronico(email).build();
    }
}
