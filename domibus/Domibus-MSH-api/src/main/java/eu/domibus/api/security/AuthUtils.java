package eu.domibus.api.security;

import eu.domibus.api.security.functions.AuthenticatedFunction;
import eu.domibus.api.security.functions.AuthenticatedProcedure;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface AuthUtils {
    /**
     * Returns the original user passed via the security context OR
     * null when the user has the role ROLE_ADMIN or unsecured authorizations is allowed
     */
    String getOriginalUserFromSecurityContext() throws AuthenticationException;

    String getAuthenticatedUser();

    boolean isUnsecureLoginAllowed();

    boolean isSuperAdmin();

    boolean isAdmin();

    void hasUserOrAdminRole();

    void hasAdminRole();

    void hasUserRole();

    void setAuthenticationToSecurityContext(String user, String password);

    void setAuthenticationToSecurityContext(String user, String password, AuthRole authRole);

    boolean isAdminMultiAware();

    /**
     * Clear spring security context from thread
     */
    void clearSecurityContext();

    /**
     * Method execute function given in function parameter.
     * If method isUnsecureLoginAllowed returns false, then
     * the spring security context with user Authentication and role AuthRole.ROLE_ADMIN is set before invoking the function.
     * After the method is executed the security context is removed.
     *
     * @param function - method to wrap
     * @param user     - Authentication: username
     * @param password - Authentication: username
     */
    void runWithSecurityContext(AuthenticatedProcedure function, String user, String password);

    /**
     * Method execute function given in function parameter.
     * If method isUnsecureLoginAllowed returns false, then
     * the spring security context with user Authentication and role 'authRole; is set before invoking the function.
     * After the method is executed the security context is removed.
     *
     * @param function - method to wrap
     * @param user     - Authentication: username
     * @param password - Authentication: username
     * @param authRole - Authentication: role
     */
    void runWithSecurityContext(AuthenticatedProcedure function, String user, String password, AuthRole authRole);

    /**
     * Method execute function given in function parameter.
     * If method isUnsecureLoginAllowed returns false or parameter forceSecurityContext is set to true, then
     * the spring security context with user Authentication and role 'authRole; is set before invoking the function.
     * After the method is executed the security context is removed.
     *
     * @param function             - method to wrap
     * @param user                 - Authentication: username
     * @param password             - Authentication: username
     * @param authRole             - Authentication: role
     * @param forceSecurityContext - force security context even if unsecured login is allowed
     */
    void runWithSecurityContext(AuthenticatedProcedure function, String user, String password, AuthRole authRole, boolean forceSecurityContext);

    /**
     * Method execute function given in function parameter.
     * If method isUnsecureLoginAllowed returns false, then
     * the spring security context with user Authentication and role 'authRole; is set before invoking the function.
     * After the method is executed the security context is removed.
     *
     * @param function - method to wrap
     * @param user     - Authentication: username
     * @param password - Authentication: username
     * @param authRole - Authentication: role
     *                 return function result
     */
    <R> R runFunctionWithSecurityContext(AuthenticatedFunction function, String user, String password, AuthRole authRole);

    /**
     * Method execute function given in function parameter.
     * If method isUnsecureLoginAllowed returns false, then
     * the spring security context with user Authentication and role 'authRole; is set before invoking the function.
     * After the method is executed the security context is removed.
     *
     * @param function             - method to wrap
     * @param user                 - Authentication: username
     * @param password             - Authentication: username
     * @param authRole             - Authentication: role
     * @param forceSecurityContext - force security context even if unsecured login is allowed
     *                             return function result
     */
    <R> R runFunctionWithSecurityContext(AuthenticatedFunction function, String user, String password, AuthRole authRole, boolean forceSecurityContext);

    void runWithDomibusSecurityContext(AuthenticatedProcedure method, AuthRole authRole);

    void runWithDomibusSecurityContext(AuthenticatedProcedure method, AuthRole authRole, boolean forceSecurityContext);

    <R> R runFunctionWithDomibusSecurityContext(AuthenticatedFunction function, AuthRole authRole, boolean forceSecurityContext);

}

