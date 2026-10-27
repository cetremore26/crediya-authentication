package co.com.crediya.api.user;

import co.com.crediya.model.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDocumentResponse(
        @JsonProperty("documento_identidad") String documentoIdentidad,
        String nombres,
        String apellidos) {

    public static UserDocumentResponse from(User user) {
        return new UserDocumentResponse(
                user.getDocumentoIdentidad(), user.getNombres(), user.getApellidos());
    }
}
