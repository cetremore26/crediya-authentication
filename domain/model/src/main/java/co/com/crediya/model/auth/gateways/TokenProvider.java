package co.com.crediya.model.auth.gateways;

import co.com.crediya.model.auth.AccessToken;
import co.com.crediya.model.user.User;

public interface TokenProvider {
    AccessToken generate(User user);
}
