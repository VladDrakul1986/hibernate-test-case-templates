package org.hibernate.search.bugs;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.wildfly.common.annotation.NotNull;

import java.util.Objects;

public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    private static final String defaultTenant = "default";
    private static  final String adminTenant = "root";


    @NotNull
    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getCurrentTenant().isEmpty() ? defaultTenant :  TenantContext.getCurrentTenant();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
    @Override
    public boolean isRoot(String tenantID){
        return Objects.equals(TenantContext.getCurrentTenant(), adminTenant);
    }
}
