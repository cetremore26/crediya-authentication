package co.com.crediya.jpa.user;

import co.com.crediya.model.user.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "documento_identidad", length = 15, unique = true)
    private String documentoIdentidad;

    @Column(name = "nombres", nullable = false, length = 150)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 150)
    private String apellidos;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "direccion", length = 300)
    private String direccion;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "correo_electronico", nullable = false, length = 320, unique = true)
    private String correoElectronico;

    @Column(name = "salario_base", nullable = false, precision = 17, scale = 2)
    private BigDecimal salarioBase;

    @Column(name = "clave", length = 100)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", length = 20)
    private Rol rol;
}
