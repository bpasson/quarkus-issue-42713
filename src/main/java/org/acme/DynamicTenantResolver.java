package org.acme;

import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.TenantConfigResolver;
import io.quarkus.oidc.common.runtime.OidcConstants;
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
        if (clientId != null && !clientId.equals(resolvedClientId)) {
            return Uni.createFrom().item(createTenantConfig(clientId));
        }

        // fallback to whatever was resolved earlier or default
        return null;
    }

    private Supplier<OidcTenantConfig> createTenantConfig(String clientId) {

        OidcTenantConfig defaultTenant = oidcConfig.defaultTenant;
        OidcTenantConfig config = new OidcTenantConfig();

        config.setAuthentication(defaultTenant.getAuthentication());
        config.setApplicationType(OidcTenantConfig.ApplicationType.WEB_APP);
        defaultTenant.getAuthServerUrl().ifPresent(config::setAuthServerUrl);
        config.setAllowTokenIntrospectionCache(defaultTenant.isAllowTokenIntrospectionCache());
        defaultTenant.getAuthorizationPath().ifPresent(config::setAuthorizationPath);
        config.setAllowUserInfoCache(defaultTenant.isAllowUserInfoCache());

        config.setCredentials(defaultTenant.getCredentials());
        config.setClientId(clientId);
        config.setCertificateChain(defaultTenant.getCertificateChain());
        config.setCodeGrant(defaultTenant.getCodeGrant());
        config.setClientName(clientId);
        defaultTenant.isCacheUserInfoInIdtoken().ifPresent(config::setCacheUserInfoInIdtoken);
        defaultTenant.getConnectionDelay().ifPresent(config::setConnectionDelay);
        config.setConnectionTimeout(defaultTenant.getConnectionTimeout());

        defaultTenant.isDiscoveryEnabled().ifPresent(config::setDiscoveryEnabled);

        defaultTenant.getEndSessionPath().ifPresent(config::setEndSessionPath);

        config.setIntrospectionCredentials(defaultTenant.getIntrospectionCredentials());
        defaultTenant.getIntrospectionPath().ifPresent(config::setIntrospectionPath);

        defaultTenant.getJwksPath().ifPresent(config::setJwksPath);

        config.setLogout(defaultTenant.getLogout());

        defaultTenant.getMaxPoolSize().ifPresent(config::setMaxPoolSize);

        defaultTenant.getProvider().ifPresent(config::setProvider);
        config.setProxy(defaultTenant.getProxy());
        defaultTenant.getPublicKey().ifPresent(config::setPublicKey);

        config.setRoles(defaultTenant.getRoles());
        defaultTenant.getRevokePath().ifPresent(config::setRevokePath);

        config.setTenantId(clientId);
        config.setTenantEnabled(true);
        config.setToken(defaultTenant.getToken());
        defaultTenant.getTokenPath().ifPresent(config::setTokenPath);

        defaultTenant.getUserInfoPath().ifPresent(config::setUserInfoPath);

        return () -> config;
    }
}
