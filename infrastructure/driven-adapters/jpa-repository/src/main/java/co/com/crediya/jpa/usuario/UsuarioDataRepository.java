package co.com.crediya.jpa.usuario;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioDataRepository
        extends JpaRepository<UsuarioEntity, UUID> {

    boolean existsByCorreoElectronicoIgnoreCase(String correoElectronico);

    Optional<UsuarioEntity> findByCorreoElectronicoIgnoreCase(String correoElectronico);

    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    Optional<UsuarioEntity> findByDocumentoIdentidad(String documentoIdentidad);
}
