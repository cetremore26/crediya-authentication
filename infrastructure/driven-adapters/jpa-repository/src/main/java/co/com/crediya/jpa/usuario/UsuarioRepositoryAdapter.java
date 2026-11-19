package co.com.crediya.jpa.usuario;

import co.com.crediya.jpa.helper.AdapterOperations;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.exception.DocumentoYaRegistradoException;
import co.com.crediya.model.usuario.exception.UsuarioYaRegistradoException;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import java.util.Locale;
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
public class UsuarioRepositoryAdapter
        extends AdapterOperations<Usuario, UsuarioEntity, UUID, UsuarioDataRepository>
        implements UsuarioRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioRepositoryAdapter.class);
    /** Columna del índice único {@code uk_usuarios_documento_identidad} (ver migración V1). */
    private static final String COLUMNA_DOCUMENTO = "documento_identidad";

    public UsuarioRepositoryAdapter(UsuarioDataRepository repository, ObjectMapper mapper) {
        super(repository, mapper, UsuarioRepositoryAdapter::aDominio);
    }

    @Override
    public boolean existePorCorreoElectronico(String correoElectronico) {
        return repository.existsByCorreoElectronicoIgnoreCase(correoElectronico);
    }

    @Override
    public Optional<Usuario> buscarPorCorreoElectronico(String correoElectronico) {
        return repository.findByCorreoElectronicoIgnoreCase(correoElectronico).map(this::toEntity);
    }

    @Override
    public boolean existePorDocumentoIdentidad(String documentoIdentidad) {
        return repository.existsByDocumentoIdentidad(documentoIdentidad);
    }

    @Override
    public Optional<Usuario> buscarPorDocumentoIdentidad(String documentoIdentidad) {
        return repository.findByDocumentoIdentidad(documentoIdentidad).map(this::toEntity);
    }

    /**
     * Transaccional: la inserción es atómica y se revierte ante cualquier error. Los datos se
     * escriben dentro del método (ver {@link #saveData}) para detectar aquí una violación de
     * unicidad y traducirla a {@link DocumentoYaRegistradoException} si fue el documento o a
     * {@link UsuarioYaRegistradoException} si fue el correo, en vez de fallar al confirmar.
     */
    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        UUID id = usuario.getId() == null ? UUID.randomUUID() : usuario.getId();

        Usuario saved;
        try {
            saved = super.save(usuario.toBuilder().id(id).build());
        } catch (DataIntegrityViolationException exception) {
            if (!esViolacionDeUnicidad(exception)) {
                throw exception;
            }
            if (violaUnicidadDelDocumento(exception)) {
                LOGGER.warn("Documento duplicado al guardar usuario.");
                throw new DocumentoYaRegistradoException();
            }
            LOGGER.warn("Correo duplicado al guardar usuario.");
            throw new UsuarioYaRegistradoException();
        }

        LOGGER.info("Usuario guardado con id {}.", id);
        return saved;
    }

    @Override
    protected UsuarioEntity saveData(UsuarioEntity data) {
        return repository.saveAndFlush(data);
    }

    private static boolean esViolacionDeUnicidad(DataIntegrityViolationException exception) {
        return exception instanceof DuplicateKeyException
                || exception.getCause() instanceof ConstraintViolationException violation
                        && violation.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE;
    }

    /**
     * Si la restricción violada es la del documento, según el nombre que extrae Hibernate. No se
     * mira el mensaje del driver porque puede incluir la sentencia con todas las columnas.
     */
    private static boolean violaUnicidadDelDocumento(DataIntegrityViolationException exception) {
        return exception.getCause() instanceof ConstraintViolationException violation
                && violation.getConstraintName() != null
                && violation.getConstraintName().toLowerCase(Locale.ROOT).contains(COLUMNA_DOCUMENTO);
    }

    /** Mapeo explícito: el modelo de dominio se construye con el builder de Lombok y no tiene anotaciones JPA. */
    @Override
    protected UsuarioEntity toData(Usuario usuario) {
        return UsuarioEntity.builder()
                .id(usuario.getId())
                .documentoIdentidad(usuario.getDocumentoIdentidad())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .direccion(usuario.getDireccion())
                .telefono(usuario.getTelefono())
                .correoElectronico(usuario.getCorreoElectronico())
                .salarioBase(usuario.getSalarioBase())
                .clave(usuario.getClave())
                .rol(usuario.getRol())
                .build();
    }

    private static Usuario aDominio(UsuarioEntity entity) {
        return Usuario.builder()
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
