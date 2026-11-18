package co.com.crediya.api.usuario;

public record UsuarioResponse(String mensaje) {
    public static UsuarioResponse registrado() {
        return new UsuarioResponse("Usuario registrado exitosamente.");
    }
}
