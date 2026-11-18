package co.com.crediya.model.usuario;

public enum Rol {
    ADMIN,
    ASESOR,
    CLIENTE;

    /**
     * Regla de quién puede registrar a quién: un ADMIN asigna cualquier rol, un ASESOR solo
     * registra clientes y un CLIENTE no registra usuarios.
     */
    public boolean puedeAsignar(Rol rolNuevo) {
        return switch (this) {
            case ADMIN -> rolNuevo != null;
            case ASESOR -> rolNuevo == CLIENTE;
            case CLIENTE -> false;
        };
    }
}
