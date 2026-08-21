package com.aiqa.externalapi;

import java.security.Principal;
import java.util.Set;
import java.util.UUID;

/** Authenticated machine principal carrying tenant and scope identity. */
public record ExternalApiPrincipal(String clientId, UUID apiClientId, UUID companyId, Set<ApiScope> scopes) implements Principal {
    @Override
    public String getName() {
        return clientId;
    }

    public boolean hasScope(ApiScope scope) {
        return scopes != null && scopes.contains(scope);
    }
}
