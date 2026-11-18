package co.com.crediya.security;

import co.com.crediya.model.autenticacion.gateways.CifradorClaves;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CifradorClavesBCrypt implements CifradorClaves {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String cifrar(String claveEnClaro) {
        return encoder.encode(claveEnClaro);
    }

    @Override
    public boolean coincide(String claveEnClaro, String claveCifrada) {
        return encoder.matches(claveEnClaro, claveCifrada);
    }
}
