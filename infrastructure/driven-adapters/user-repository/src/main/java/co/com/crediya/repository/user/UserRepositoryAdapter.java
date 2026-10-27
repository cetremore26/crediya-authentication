package co.com.crediya.repository.user;

import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import co.com.crediya.model.user.gateways.UserRepository;
import java.sql.PreparedStatement;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserRepositoryAdapter implements UserRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryAdapter.class);
    private static final String INSERT_USER = """
            INSERT INTO usuarios (
                id, nombres, apellidos, fecha_nacimiento, direccion, telefono,
                correo_electronico, salario_base, documento_identidad, clave, rol
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
    public Optional<User> findByCorreoElectronico(String correoElectronico) {
        return jdbcTemplate.query(
                        "SELECT id, documento_identidad, nombres, apellidos, correo_electronico, "
                                + "salario_base, clave, rol FROM usuarios "
                                + "WHERE LOWER(correo_electronico) = LOWER(?)",
                        (rs, rowNum) -> {
                            String rol = rs.getString("rol");
                            return User.builder()
                                    .id(rs.getObject("id", UUID.class))
                                    .documentoIdentidad(rs.getString("documento_identidad"))
                                    .nombres(rs.getString("nombres"))
                                    .apellidos(rs.getString("apellidos"))
                                    .correoElectronico(rs.getString("correo_electronico"))
                                    .salarioBase(rs.getBigDecimal("salario_base"))
                                    .clave(rs.getString("clave"))
                                    .rol(rol == null ? null : Rol.valueOf(rol))
                                    .build();
                        },
                        correoElectronico)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsByDocumentoIdentidad(String documentoIdentidad) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM usuarios WHERE documento_identidad = ?)",
                Boolean.class,
                documentoIdentidad);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Optional<User> findByDocumentoIdentidad(String documentoIdentidad) {
        return jdbcTemplate.query(
                        "SELECT id, documento_identidad, nombres, apellidos, correo_electronico, "
                                + "salario_base FROM usuarios WHERE documento_identidad = ?",
                        (rs, rowNum) -> User.builder()
                                .id(rs.getObject("id", UUID.class))
                                .documentoIdentidad(rs.getString("documento_identidad"))
                                .nombres(rs.getString("nombres"))
                                .apellidos(rs.getString("apellidos"))
                                .correoElectronico(rs.getString("correo_electronico"))
                                .salarioBase(rs.getBigDecimal("salario_base"))
                                .build(),
                        documentoIdentidad)
                .stream()
                .findFirst();
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
                statement.setString(9, user.getDocumentoIdentidad());
                statement.setString(10, user.getClave());
                statement.setString(11, user.getRol() == null ? null : user.getRol().name());
                return statement;
            });
        } catch (DuplicateKeyException exception) {
            LOGGER.warn("Violación de unicidad de correo al guardar usuario.");
            throw new UserAlreadyExistsException();
        }

        LOGGER.info("Usuario guardado con id {}.", id);
        return user.toBuilder().id(id).correoElectronico(email).build();
    }
}
