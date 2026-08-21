package com.aiqa.externalapi;

/** M21 external API scopes. */
public enum ApiScope {
    UAT_READ,
    UAT_EXECUTE,
    EVIDENCE_READ,
    ADMIN_READ;

    public String authority() {
        return "SCOPE_" + name();
    }
}
