package co.com.crediya.jpa.user;

import co.com.crediya.jpa.helper.AdapterOperations;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import co.com.crediya.model.user.gateways.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserRepositoryAdapter
        extends AdapterOperations<User, UserEntity, UUID, UserDataRepository>
        implements UserRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryAdapter.class);

    public UserRepositoryAdapter(UserDataRepository repository, ObjectMapper mapper) {
        super(repository, mapper, UserRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByCorreoElectronico(String correoElectronico) {
        return repository.existsByCorreoElectronicoIgnoreCase(correoElectronico);
    }

    @Override
    public Optional<User> findByCorreoElectronico(String correoElectronico) {
        return repository.findByCorreoElectronicoIgnoreCase(correoElectronico).map(this::toEntity);
    }

    @Override
    public boolean existsByDocumentoIdentidad(String documentoIdentidad) {
        return repository.existsByDocumentoIdentidad(documentoIdentidad);
    }

    /** Lookup for other services: the password hash never leaves this adapter here. */
    @Override
    public Optional<User> findByDocumentoIdentidad(String documentoIdentidad) {
        return repository.findByDocumentoIdentidad(documentoIdentidad)
                .map(this::toEntity)
                .map(user -> user.toBuilder().clave(null).build());
    }

    /**
     * Transactional: the insert is atomic and is rolled back on any error. The data is flushed
     * inside the method (see {@link #saveData}) so that a unique violation is detected here and
     * translated to {@link UserAlreadyExistsException} instead of failing later at commit time.
     */
    @Override
    @Transactional
    public User save(User user) {
        UUID id = user.getId() == null ? UUID.randomUUID() : user.getId();

        User saved;
        try {
            saved = super.save(user.toBuilder().id(id).build());
        } catch (DataIntegrityViolationException exception) {
            if (!isUniqueViolation(exception)) {
                throw exception;
            }
            LOGGER.warn("Violación de unicidad al guardar usuario.");
            throw new UserAlreadyExistsException();
        }

        LOGGER.info("Usuario guardado con id {}.", id);
        return saved;
    }

    @Override
    protected UserEntity saveData(UserEntity data) {
        return repository.saveAndFlush(data);
    }

    private static boolean isUniqueViolation(DataIntegrityViolationException exception) {
        return exception instanceof DuplicateKeyException
                || exception.getCause() instanceof ConstraintViolationException violation
                        && violation.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE;
    }

    /** Explicit mapping: the domain model is built with Lombok's builder and has no JPA annotations. */
    @Override
    protected UserEntity toData(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .documentoIdentidad(user.getDocumentoIdentidad())
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .fechaNacimiento(user.getFechaNacimiento())
                .direccion(user.getDireccion())
                .telefono(user.getTelefono())
                .correoElectronico(user.getCorreoElectronico())
                .salarioBase(user.getSalarioBase())
                .clave(user.getClave())
                .rol(user.getRol())
                .build();
    }

    private static User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .documentoIdentidad(entity.getDocumentoIdentidad())
                .nombres(entity.getNombres())
                .apellidos(entity.getApellidos())
                .fechaNacimiento(entity.getFechaNacimiento())
                .direccion(entity.getDireccion())
                .telefono(entity.getTelefono())
                .correoElectronico(entity.getCorreoElectronico())
                .salarioBase(entity.getSalarioBase())
                .clave(entity.getClave())
                .rol(entity.getRol())
                .build();
    }
}
