package co.com.crediya.jpa.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface UserDataRepository
        extends CrudRepository<UserEntity, UUID>, QueryByExampleExecutor<UserEntity> {

    boolean existsByCorreoElectronicoIgnoreCase(String correoElectronico);

    Optional<UserEntity> findByCorreoElectronicoIgnoreCase(String correoElectronico);

    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    Optional<UserEntity> findByDocumentoIdentidad(String documentoIdentidad);
}
