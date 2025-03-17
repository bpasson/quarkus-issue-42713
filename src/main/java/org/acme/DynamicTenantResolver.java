package org.acme;

import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.OidcTenantConfigBuilder;
import io.quarkus.oidc.TenantConfigResolver;
import io.quarkus.oidc.runtime.OidcConfig;
import io.quarkus.oidc.runtime.OidcUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@ApplicationScoped
public class DynamicTenantResolver implements TenantConfigResolver {

    private static final Logger log = LoggerFactory.getLogger(DynamicTenantResolver.class);
    public static final String HEADER_TENANT_ID = "X-Tenant-ID";

    @Inject
    OidcConfig oidcConfig;

    @Override
    public Uni<OidcTenantConfig> resolve(RoutingContext context, OidcRequestContext<OidcTenantConfig> requestContext) {
        String resolvedClientId = context.get(OidcUtils.TENANT_ID_ATTRIBUTE);
        String clientId = context.request().headers().get(HEADER_TENANT_ID);

        log.info("resolvedClientId = {}, headerClientId = {}", resolvedClientId, clientId);

        if( resolvedClientId == null && clientId == null ) {
            log.warn("Not tenant resolved and no header provided, defaulting to 'quarkus-app'");
            clientId = "quarkus-app";
        }

        if (clientId != null && !clientId.equals(resolvedClientId)) {
            context.put(OidcUtils.TENANT_ID_ATTRIBUTE, clientId);
            return Uni.createFrom().item(createTenantConfig(clientId));
        }

        // fallback to whatever was resolved earlier
        return Uni.createFrom().item(createTenantConfig(resolvedClientId));
    }

    private Supplier<OidcTenantConfig> createTenantConfig(String clientId) {

        io.quarkus.oidc.runtime.OidcTenantConfig defaultTenant = OidcConfig.getDefaultTenant(oidcConfig);
        OidcTenantConfigBuilder config = new OidcTenantConfigBuilder();

        config.authentication(defaultTenant.authentication());
        defaultTenant.applicationType().ifPresent(config::applicationType);
        defaultTenant.authServerUrl().ifPresent(config::authServerUrl);
        config.allowTokenIntrospectionCache(defaultTenant.allowTokenIntrospectionCache());
        defaultTenant.authorizationPath().ifPresent(config::authorizationPath);
        config.allowUserInfoCache(defaultTenant.allowUserInfoCache());

        config.credentials(defaultTenant.credentials());
        config.clientId(clientId);
        config.certificateChain(defaultTenant.certificateChain());
        config.codeGrant(defaultTenant.codeGrant());
        config.clientName(clientId);
        defaultTenant.cacheUserInfoInIdtoken().ifPresent(config::cacheUserInfoInIdtoken);
        defaultTenant.connectionDelay().ifPresent(config::connectionDelay);
        config.connectionTimeout(defaultTenant.connectionTimeout());

        defaultTenant.discoveryEnabled().ifPresent(config::discoveryEnabled);

        defaultTenant.endSessionPath().ifPresent(config::endSessionPath);

        config.introspectionCredentials(defaultTenant.introspectionCredentials());
        defaultTenant.introspectionPath().ifPresent(config::introspectionPath);

        defaultTenant.jwksPath().ifPresent(config::jwksPath);

        config.logout(defaultTenant.logout());

        defaultTenant.maxPoolSize().ifPresent(config::maxPoolSize);

        defaultTenant.provider().ifPresent(config::provider);
        config.proxy(defaultTenant.proxy().host().orElse(null), defaultTenant.proxy().port(), defaultTenant.proxy().username().orElse(null), defaultTenant.proxy().password().orElse(null));

        defaultTenant.publicKey().ifPresent(config::publicKey);

        config.roles(defaultTenant.roles());
        defaultTenant.revokePath().ifPresent(config::revokePath);

        config.tenantId(clientId);
        config.tenantEnabled(true);
        config.token(defaultTenant.token());
        defaultTenant.tokenPath().ifPresent(config::tokenPath);

        defaultTenant.userInfoPath().ifPresent(config::userInfoPath);

        return config::build;
    }
}
