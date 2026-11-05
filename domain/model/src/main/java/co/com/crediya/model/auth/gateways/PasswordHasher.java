package co.com.crediya.model.auth.gateways;

public interface PasswordHasher {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
