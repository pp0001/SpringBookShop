package com.sap.bookshop.config;

import com.sap.bookshop.utils.JwtGenerator;
import com.sap.bookshop.utils.UserInfoWrapper;
import com.sap.xs2.security.commons.SAPOfflineTokenServices;
import com.sap.xs2.security.container.UserInfo;
import com.sap.xs2.security.container.UserInfoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.web.context.WebApplicationContext;


/**
 * This config provides a reconfiguration of the security configation capable of running with a xsuaa instance, for unit tests and local execution.
 * It uses a key pair for the JWT token generation/validation stored in local files.
 * To test using a http client, you can pass one of the predefined Authorization Bearer ... headers found in src/main/resources/jwtTokensForTesting.txt.
 *
 * @author D020038
 */
@Profile("default")
@Configuration
public class LocalSecurityConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFactory.class);
    public static final String XSAPPNAME = "test-xsappname";

    @Bean
    public SAPOfflineTokenServices sapOfflineTokenServices() {
        JwtGenerator jwtGenerator = new JwtGenerator();
        SAPOfflineTokenServices sapOfflineTokenServices = new SAPOfflineTokenServices();
        sapOfflineTokenServices.setTrustedClientId(jwtGenerator.getClientId());
        sapOfflineTokenServices.setTrustedIdentityZone(jwtGenerator.getIdentityZone());
        sapOfflineTokenServices.setVerificationKey(jwtGenerator.getPublicKey());
        sapOfflineTokenServices.afterPropertiesSet();
        LOGGER.info("=========== JWT for admin + user is: " + jwtGenerator.getTokenForAuthorizationHeader("Hans", "openid", LocalSecurityConfig.XSAPPNAME + ".admin", LocalSecurityConfig.XSAPPNAME + ".user"));
        LOGGER.info("=========== JWT for admin is: " + jwtGenerator.getTokenForAuthorizationHeader("Hans", "openid", LocalSecurityConfig.XSAPPNAME + ".admin"));
        LOGGER.info("=========== JWT for user is: " + jwtGenerator.getTokenForAuthorizationHeader("Hans", "openid", LocalSecurityConfig.XSAPPNAME + ".user"));
        LOGGER.info("=========== JWT without scopes is: " + jwtGenerator.getTokenForAuthorizationHeader("Hans"));
        LOGGER.info("=========== JWT with openid scope only is: " + jwtGenerator.getTokenForAuthorizationHeader("Hans", "openid"));
        return sapOfflineTokenServices;
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public UserInfo userInfoBean() {
        try {
            return new UserInfoWrapper(XSAPPNAME);
        } catch (UserInfoException e) {
            return null;
        }
    }
}