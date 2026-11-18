package co.com.crediya.model.autenticacion.gateways;

public interface CifradorClaves {
    String cifrar(String claveEnClaro);

    boolean coincide(String claveEnClaro, String claveCifrada);
}
