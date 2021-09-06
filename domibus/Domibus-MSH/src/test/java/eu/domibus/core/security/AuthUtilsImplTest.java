package eu.domibus.core.security;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.security.functions.AuthenticatedProcedure;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_AUTH_UNSECURE_LOGIN_ALLOWED;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author François Gautier
 * @since 4.2
 */
@SuppressWarnings("AccessStaticViaInstance")
@RunWith(JMockit.class)
public class AuthUtilsImplTest {

    public static final String STRING = "TEST";
    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Tested
    private AuthUtilsImpl authUtilsImpl;

    @Test
    public void getOriginalUserFromSecurityContext_user(
            @Mocked SecurityContextHolder securityContextHolder,
            @Mocked Authentication authentication) {
        new Expectations(authUtilsImpl) {{
            authUtilsImpl.isUnsecureLoginAllowed();
            result = false;

            SecurityContextHolder.getContext().getAuthentication();
            result = authentication;

            authentication.getAuthorities();
            result = Collections.singletonList(new SimpleGrantedAuthority(AuthRole.ROLE_USER.name()));

            authentication.getPrincipal();
            result = STRING;

            authentication.getName();
            result = "authenticationName";
        }};
        assertEquals(STRING, authUtilsImpl.getOriginalUserFromSecurityContext());
        new FullVerifications() {
        };
    }

    @Test
    public void getOriginalUserFromSecurityContext_superAdmin(
            @Mocked SecurityContextHolder securityContextHolder,
            @Mocked Authentication authentication) {
        new Expectations(authUtilsImpl) {{
            authUtilsImpl.isUnsecureLoginAllowed();
            result = false;

            SecurityContextHolder.getContext().getAuthentication();
            result = authentication;

            authentication.getAuthorities();
            result = Collections.singletonList(new SimpleGrantedAuthority(AuthRole.ROLE_AP_ADMIN.name()));

            authentication.getName();
            result = "authenticationName";
        }};
        assertNull(authUtilsImpl.getOriginalUserFromSecurityContext());
        new FullVerifications() {
        };
    }

    @Test
    public void getOriginalUserFromSecurityContext_admin(
            @Mocked SecurityContextHolder securityContextHolder,
            @Mocked Authentication authentication) {
        new Expectations(authUtilsImpl) {{
            authUtilsImpl.isUnsecureLoginAllowed();
            result = false;

            SecurityContextHolder.getContext().getAuthentication();
            result = authentication;

            authentication.getAuthorities();
            result = Collections.singletonList(new SimpleGrantedAuthority(AuthRole.ROLE_ADMIN.name()));

            authentication.getName();
            result = "authenticationName";
        }};
        assertNull(authUtilsImpl.getOriginalUserFromSecurityContext());
        new FullVerifications() {
        };
    }

    @Test(expected = AuthenticationException.class)
    public void getOriginalUserFromSecurityContext_noAuth(
            @Mocked SecurityContextHolder securityContextHolder) {
        new Expectations(authUtilsImpl) {{
            authUtilsImpl.isUnsecureLoginAllowed();
            result = false;

            SecurityContextHolder.getContext().getAuthentication();
            result = null;
        }};
        authUtilsImpl.getOriginalUserFromSecurityContext();
        new FullVerifications() {
        };
    }

    @Test(expected = AuthenticationException.class)
    public void getOriginalUserFromSecurityContext_noContext(
            @Mocked SecurityContextHolder securityContextHolder) {
        new Expectations(authUtilsImpl) {{
            authUtilsImpl.isUnsecureLoginAllowed();
            result = false;

            SecurityContextHolder.getContext();
            result = null;
        }};
        authUtilsImpl.getOriginalUserFromSecurityContext();
        new FullVerifications() {
        };
    }

    @Test
    public void getOriginalUserFromSecurityContext_unsecureLoginAllowed() {
        new Expectations(authUtilsImpl) {{
            authUtilsImpl.isUnsecureLoginAllowed();
            result = true;
        }};
        assertNull(authUtilsImpl.getOriginalUserFromSecurityContext());
        new FullVerifications() {
        };
    }

    @Test
    public void getAuthenticatedUser_noAuth(
            @Mocked SecurityContextHolder securityContextHolder,
            @Mocked Authentication authentication) {
        new Expectations() {{
            SecurityContextHolder.getContext().getAuthentication();
            result = authentication;

            authentication.getName();
            result = STRING;
        }};
        assertEquals(STRING, authUtilsImpl.getAuthenticatedUser());
        new FullVerifications() {
        };
    }

    @Test
    public void getAuthenticatedUser_noContext(@Mocked SecurityContextHolder securityContextHolder) {
        new Expectations() {{
            SecurityContextHolder.getContext();
            result = null;
        }};
        assertNull(authUtilsImpl.getAuthenticatedUser());
        new FullVerifications() {
        };
    }

    @Test
    public void isUnsecureLoginAllowed() {
        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_AUTH_UNSECURE_LOGIN_ALLOWED);
            result = true;
        }};
        assertTrue(authUtilsImpl.isUnsecureLoginAllowed());
        new FullVerifications() {
        };
    }

    @Test
    public void isUnsecureLoginAllowed_multitenant() {
        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
        }};
        assertFalse(authUtilsImpl.isUnsecureLoginAllowed());
        new FullVerifications() {
        };
    }


    @Test
    public void isSuperAdmin() {
        new Expectations(authUtilsImpl) {{
            authUtilsImpl.checkAdminRights(AuthRole.ROLE_AP_ADMIN);
            result = true;
            times = 1;
        }};
        assertTrue(authUtilsImpl.isSuperAdmin());
        new FullVerifications() {
        };
    }

    @Test
    public void isAdmin() {
        new Expectations(authUtilsImpl) {{
            authUtilsImpl.checkAdminRights(AuthRole.ROLE_ADMIN);
            result = true;
            times = 1;
        }};
        assertTrue(authUtilsImpl.isAdmin());
        new FullVerifications() {
        };
    }

    @Test
    public void hasUserOrAdminRole_ok() {

        new Expectations(authUtilsImpl) {{
            authUtilsImpl.isAdmin();
            result = false;
            authUtilsImpl.isSuperAdmin();
            result = false;
            authUtilsImpl.getOriginalUserFromSecurityContext();
            result = "TEST_USER";
        }};

        authUtilsImpl.hasUserOrAdminRole();

        new FullVerifications() {
        };
    }

    @Test(expected = AuthenticationException.class)
    public void hasUserOrAdminRole_blankUser() {

        new Expectations(authUtilsImpl) {{
            authUtilsImpl.isAdmin();
            result = false;
            authUtilsImpl.isSuperAdmin();
            result = false;
            authUtilsImpl.getOriginalUserFromSecurityContext();
            result = "";
        }};

        authUtilsImpl.hasUserOrAdminRole();

        new FullVerifications() {
        };
    }

    @Test
    public void hasUserOrAdminRole_isSuperAdmin() {

        new Expectations(authUtilsImpl) {{
            authUtilsImpl.isAdmin();
            result = false;
            authUtilsImpl.isSuperAdmin();
            result = true;
        }};

        authUtilsImpl.hasUserOrAdminRole();

        new FullVerifications() {
        };
    }

    @Test
    public void hasUserOrAdminRole_isAdmin() {

        new Expectations(authUtilsImpl) {{
            authUtilsImpl.isAdmin();
            result = true;
        }};

        authUtilsImpl.hasUserOrAdminRole();

        new FullVerifications() {
        };
    }

    @Test
    public void setAuthenticationToSecurityContextWithRole(@Mocked SecurityContextHolder securityContextHolder) {

        authUtilsImpl.setAuthenticationToSecurityContext("user", "pwd", AuthRole.ROLE_USER);

        new FullVerifications() {{
            Authentication authentication;
            securityContextHolder.getContext()
                    .setAuthentication(authentication = withCapture());

            assertThat(authentication.getCredentials(), is("pwd"));
            assertThat(authentication.getPrincipal(), is("user"));

            assertThat(authentication.getAuthorities().size(), is(1));
            SimpleGrantedAuthority simpleGrantedAuthority = (SimpleGrantedAuthority) authentication.getAuthorities().iterator().next();
            assertThat(simpleGrantedAuthority.getAuthority(), is(AuthRole.ROLE_USER.name()));
        }};
    }

    @Test
    public void setAuthenticationToSecurityContext(@Mocked SecurityContextHolder securityContextHolder) {

        authUtilsImpl.setAuthenticationToSecurityContext("user", "pwd");
        new FullVerifications() {{
            Authentication authentication;
            securityContextHolder.getContext()
                    .setAuthentication(authentication = withCapture());

            assertThat(authentication.getCredentials(), is("pwd"));
            assertThat(authentication.getPrincipal(), is("user"));

            assertThat(authentication.getAuthorities().size(), is(1));
            SimpleGrantedAuthority simpleGrantedAuthority = (SimpleGrantedAuthority) authentication.getAuthorities().iterator().next();
            assertThat(simpleGrantedAuthority.getAuthority(), is(AuthRole.ROLE_ADMIN.name()));
        }};
    }

    @Test
    public void checkAdminRights_authIsNull(
            @Mocked SecurityContextHolder securityContextHolder) {

        new Expectations() {{
            securityContextHolder.getContext().getAuthentication();
            result = null;
        }};

        assertFalse(authUtilsImpl.checkAdminRights(AuthRole.ROLE_USER));

        new FullVerifications() {
        };
    }

    @Test
    public void checkAdminRights_noAuthorities(
            @Mocked SecurityContextHolder securityContextHolder,
            @Mocked Authentication authentication) {

        new Expectations() {{
            securityContextHolder.getContext().getAuthentication();
            result = authentication;

            authentication.getAuthorities();
            result = null;
        }};

        assertFalse(authUtilsImpl.checkAdminRights(AuthRole.ROLE_USER));

        new FullVerifications() {
        };
    }

    @Test
    public void checkAdminRights_found(
            @Mocked SecurityContextHolder securityContextHolder,
            @Mocked Authentication authentication) {

        new Expectations() {{
            securityContextHolder.getContext().getAuthentication();
            result = authentication;

            authentication.getAuthorities();
            result = Arrays.asList(
                    new SimpleGrantedAuthority("NOTFOUND"),
                    new SimpleGrantedAuthority(AuthRole.ROLE_USER.name())
            );
            authentication.getName();
            result = "authentication.getName()";
        }};

        assertTrue(authUtilsImpl.checkAdminRights(AuthRole.ROLE_USER));

        new FullVerifications() {
        };
    }

    @Test
    public void checkAdminRights_notFound(
            @Mocked SecurityContextHolder securityContextHolder,
            @Mocked AuthRole authRole,
            @Mocked Authentication authentication) {

        new Expectations() {{
            securityContextHolder.getContext().getAuthentication();
            result = authentication;

            authentication.getAuthorities();
            result = Collections.singletonList(new SimpleGrantedAuthority("NOTFOUND"));
        }};

        assertFalse(authUtilsImpl.checkAdminRights(AuthRole.ROLE_USER));

        new FullVerifications() {
        };
    }

    @Test
    public void isAdminMultiAware_multiTenant() {

        new Expectations(authUtilsImpl) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            authUtilsImpl.isSuperAdmin();
            result = true;
        }};

        assertTrue(authUtilsImpl.isAdminMultiAware());

        new FullVerifications() {
        };
    }

    @Test
    public void isAdminMultiAware_MonoTenant() {

        new Expectations(authUtilsImpl) {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;

            authUtilsImpl.isAdmin();
            result = true;
        }};

        assertTrue(authUtilsImpl.isAdminMultiAware());

        new FullVerifications() {
        };
    }

    @Test
    public void runWithSecurityContext_AddSecurityContext(@Mocked AuthenticatedProcedure expectedFunction) {
        String expectedUsername = UUID.randomUUID().toString();
        String expectedPassword = UUID.randomUUID().toString();
        new Expectations(authUtilsImpl) {{
            domibusConfigurationService.isMultiTenantAware();
            result=true;

            authUtilsImpl.setAuthenticationToSecurityContext(anyString, anyString, (AuthRole)any);
            expectedFunction.invoke();
            authUtilsImpl.clearSecurityContext();
        }};

        authUtilsImpl.runWithSecurityContext(expectedFunction, expectedUsername, expectedPassword, AuthRole.ROLE_ADMIN);

        new FullVerifications() {{
            AuthenticatedProcedure function;
            String username;
            String password;
            AuthRole role;
            authUtilsImpl.runWithSecurityContext(function = withCapture(),
                    username=withCapture(), password=withCapture(), role=withCapture());
            Assert.assertEquals(expectedFunction, function);
            Assert.assertEquals(expectedUsername,username);
            Assert.assertEquals(expectedPassword,password);
            Assert.assertEquals(AuthRole.ROLE_ADMIN,role);
        }};
    }

    @Test
    public void runWithSecurityContext_NoSecurityContext(@Mocked AuthenticatedProcedure expectedFunction) {
        String expectedUsername = UUID.randomUUID().toString();
        String expectedPassword = UUID.randomUUID().toString();
        new Expectations(authUtilsImpl) {{
            domibusConfigurationService.isMultiTenantAware();
            result=false;

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_AUTH_UNSECURE_LOGIN_ALLOWED);
            result=true;

            expectedFunction.invoke();
        }};

        authUtilsImpl.runWithSecurityContext(expectedFunction, expectedUsername, expectedPassword, AuthRole.ROLE_ADMIN, false);

        new FullVerifications(authUtilsImpl) {{
            authUtilsImpl.setAuthenticationToSecurityContext(anyString, anyString, (AuthRole) any);
            times=0;
            expectedFunction.invoke();
            authUtilsImpl.clearSecurityContext();
            times=0;
        }};
    }

    @Test
    public void runWithSecurityContext_NoSecurityContext_Force(@Mocked AuthenticatedProcedure expectedFunction) {
        // same tests as runWithSecurityContext_NoSecurityContext but force the security context
        String expectedUsername = UUID.randomUUID().toString();
        String expectedPassword = UUID.randomUUID().toString();
        new Expectations(authUtilsImpl) {{
            domibusConfigurationService.isMultiTenantAware();
            result=false;

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_AUTH_UNSECURE_LOGIN_ALLOWED);
            result=true;

            expectedFunction.invoke();
        }};

        authUtilsImpl.runWithSecurityContext(expectedFunction, expectedUsername, expectedPassword, AuthRole.ROLE_ADMIN, true);

        new FullVerifications(authUtilsImpl) {{
            authUtilsImpl.setAuthenticationToSecurityContext(anyString, anyString, (AuthRole) any);
            times=1;
            expectedFunction.invoke();
            authUtilsImpl.clearSecurityContext();
            times=1;
        }};
    }

    @Test
    public void runWithSecurityContext_ClearSecurityContextOnThrowException(@Mocked AuthenticatedProcedure runnable) {
        String username = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();
        new Expectations(authUtilsImpl) {{
            runnable.invoke();
            result = new RuntimeException();
        }};

        try {
            authUtilsImpl.runWithSecurityContext(runnable, username, password, AuthRole.ROLE_ADMIN, true);
            fail("Method should throw error");
        } catch (RuntimeException exc) {
            // ignore
        }
        // verify that clearSecurityContext was called
        new FullVerificationsInOrder(authUtilsImpl) {{
            authUtilsImpl.setAuthenticationToSecurityContext(anyString, anyString, (AuthRole) any);
            runnable.invoke();
            authUtilsImpl.clearSecurityContext();
        }};
    }

    @Test
    public void clearSecurityContext(@Mocked SecurityContextHolder securityContextHolder) {

        new Expectations() {{
            securityContextHolder.clearContext();
        }};
        authUtilsImpl.clearSecurityContext();

        new FullVerifications() {
        };
    }
}