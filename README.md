# OIDC Dynamic Client Issue

This is a reproducer for Quarkus issue [#42713](https://github.com/quarkusio/quarkus/issues/42713) and demonstrates three problems with OIDC and dynamic clients.

## Dev Services won't start when default tenant disabled
The dev-services won't start when the default tenant is disabled. But they should start as normal as it could be the
case you want only dynamic configurations and no default configuration. A realm import for dev services could supply
all needed dynamic clients.

I worked around this by disabling the default tenant on boot in this reproducer, see the `DummyTenantIdHeaderFilter`

## Resolved Tenant ID not backed by actual OIDC client
The `OidcUtils.TENANT_ID_ATTRIBUTE` offered through the `RoutingContext` in the `public Uni<OidcTenantConfig> resolve(RoutingContext context, OidcRequestContext<OidcTenantConfig> requestContext)` method of the `TenantConfigResolver` is
used by Quarkus to notify us of an earlier resolved tenant-id. It fails however to verify if the OidcTenantConfig for
that specific tenant-id is still present. Use the following steps to reproduce the issue:

1. Start the application in dev-mode 
2. Clear all cookies for http://localhost:8080
3. Open http://localhost:8080
4. Log in with bob/bob
5. Restart the application in dev-mode
6. Reload http://localhost:8080
7. You now get a status 401, where you would expect to get a login screen.

The only way to fix it is to fully restart Quarkus in dev-mode.

## Hot Code Reload in Dev Mode breaks dynamic configuration
If you alter code e.g. change the string in `GreetingResource` and reload the page dev-mode will perform a hot code
reload and you end up with a `ID token verification has failed: Client is closed` log message and a status 401. Use
the following steps to reproduce the issue:

1. Start, (restart if still running) the application in dev-mode.
2. Clear all cookies for http://localhost:8080
3. Go to http://localhost:8080
4. Login with bob/bob
5. Alter the test in `GreetingResource`
6. Reload http://localhost:8080
7. You now get a status 401 and the `ID token verification has failed: Client is closed` message in the logs.

The only way to fix it is to fully restart Quarkus in dev-mode.