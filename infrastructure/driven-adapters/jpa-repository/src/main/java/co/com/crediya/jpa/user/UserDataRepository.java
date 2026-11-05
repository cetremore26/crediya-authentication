package co.com.crediya.jpa.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDataRepository
        extends JpaRepository<UserEntity, UUID> {

    boolean existsByCorreoElectronicoIgnoreCase(String correoElectronico);

    Optional<UserEntity> findByCorreoElectronicoIgnoreCase(String correoElectronico);

    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    Optional<UserEntity> findByDocumentoIdentidad(String documentoIdentidad);
}
