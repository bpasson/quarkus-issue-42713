package org.acme;

import io.quarkus.oidc.runtime.OidcConfig;
import io.quarkus.vertx.http.runtime.RouteConstants;
import io.quarkus.vertx.http.runtime.filters.Filters;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DummyTenantIdHeaderFilter {

    private static final Logger log = LoggerFactory.getLogger(DummyTenantIdHeaderFilter.class);

    @Inject
    OidcConfig oidcConfig;

    public void init(@Observes Filters filters) {
        filters.register(getHandler(), RouteConstants.ROUTE_ORDER_BEFORE_DEFAULT);

        // disable the default tenant here, dev services wont start with default
        // tenant disabled
        oidcConfig.defaultTenant.setTenantEnabled(false);
    }

    public Handler<RoutingContext> getHandler() {
        return rc -> {
            if (!rc.request().headers().contains(DynamicTenantResolver.HEADER_TENANT_ID)) {
                log.info("{} is empty, assigning 'quarkus-app'", DynamicTenantResolver.HEADER_TENANT_ID);
                rc.request().headers().add(DynamicTenantResolver.HEADER_TENANT_ID, "quarkus-app");
            }
            rc.next();
        };
    }
}
