package co.com.crediya.api.user;

public record UserResponse(String mensaje) {
    public static UserResponse registered() {
        return new UserResponse("Usuario registrado exitosamente.");
    }
}
