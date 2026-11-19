package co.com.crediya.jpa.usuario;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioDataRepository
        extends JpaRepository<UsuarioEntity, UUID> {

    boolean existsByCorreoElectronico(String correoElectronico);

    Optional<UsuarioEntity> findByCorreoElectronico(String correoElectronico);

    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    Optional<UsuarioEntity> findByDocumentoIdentidad(String documentoIdentidad);

    List<UsuarioEntity> findByDocumentoIdentidadIn(Collection<String> documentosIdentidad);
}
