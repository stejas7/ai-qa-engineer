# Platform Admin Bootstrap

AI UAT Engineer does not ship with a hard-coded super-admin username or password.

The first `PLATFORM_ADMIN` can be created at application startup by supplying both environment-backed Spring properties:

```text
AI_UAT_PLATFORM_ADMIN_EMAIL=<platform-owner-email>
AI_UAT_PLATFORM_ADMIN_PASSWORD=<strong-password-at-least-12-characters>
```

Spring Boot maps these variables to:

```text
ai-uat.platform-admin.email
ai-uat.platform-admin.password
```

## Safety rules

- No account is created when both values are absent.
- Partial configuration fails startup rather than creating an unusable privileged account.
- Passwords shorter than 12 characters are rejected.
- The password is BCrypt encoded before persistence and is never returned by an API.
- If a `PLATFORM_ADMIN` already exists, bootstrap is a no-op.
- If the configured email already belongs to a non-platform account, bootstrap fails rather than silently escalating that account.
- The platform owner uses a dedicated internal platform tenant sentinel and is not treated as a customer-company user.
- `/api/platform/**` remains restricted to `PLATFORM_ADMIN` and exposes read-only company/product/user reporting only.

## Login

After the environment variables are supplied for the first successful deployment, sign in through the normal AI UAT Engineer login page using the configured email and password. The frontend detects `PLATFORM_ADMIN` and shows the Platform Owner Console.

After the platform account exists, the bootstrap variables can be removed from the runtime environment on a later controlled deployment because subsequent startups do not need them.
