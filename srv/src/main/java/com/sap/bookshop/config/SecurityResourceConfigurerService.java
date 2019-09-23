package com.sap.bookshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the generic part of security configuration, relevant for both local
 * and cloud usage.
 *
 * @author D020038
 * @version 18.01.2017
 */
@Configuration
@EnableWebSecurity
@EnableResourceServer
public class SecurityResourceConfigurerService extends ResourceServerConfigurerAdapter {


    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.NEVER); //session is created by approuter

        http.authorizeRequests() // configure authorization of requests
                .accessDecisionManager(accessDecisionManager()) // enable OAuth2 checks
                //TODO never do this in production !!!!!!!!! All health endpoints must be checked
                .antMatchers(HttpMethod.GET, "/actuator/**").permitAll()//permit any get request to health status endpoint to allow automated health checking
                .antMatchers("/").permitAll()
                .anyRequest().authenticated(); //Require authentication for any other request not covered above
    }

    @Bean
    protected UnanimousBased accessDecisionManager() {
        List<AccessDecisionVoter<? extends Object>> voterList = new ArrayList<>();
        WebExpressionVoter expressionVoter = new WebExpressionVoter();
        expressionVoter.setExpressionHandler(new OAuth2WebSecurityExpressionHandler());
        voterList.add(expressionVoter);
        voterList.add(new AuthenticatedVoter());
        return new UnanimousBased(voterList);
    }

}