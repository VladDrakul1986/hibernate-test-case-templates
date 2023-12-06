package org.hibernate.search.bugs;

public class TenantContext {
    private static InheritableThreadLocal<String> currentTenant = new InheritableThreadLocal<>();

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void setCurrentTenant(String currentTenantName) {
        currentTenant.set(currentTenantName);
    }

    public static void clear() {
        currentTenant.remove();}
}
