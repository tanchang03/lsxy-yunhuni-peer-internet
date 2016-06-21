package com.lsxy.app.portal.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

/**
 * Created by Tandy on 2016/6/7.
 */
@EnableWebSecurity
@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Log logger = LogFactory.getLog(SpringSecurityConfig.class);


    @Autowired
    AuthenticationUserDetailsService preUserDetailsService;

    @Autowired
    UserDetailsService daoUserDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        if(logger.isDebugEnabled()){
            logger.debug("初始化Spring Security安全框架");
        }

        http.authorizeRequests()
                .antMatchers("/rest/**").access("hasRole('ROLE_TENANT_USER')")
                .antMatchers("/*").permitAll()
                .and().addFilter(headerAuthenticationFilter())
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .httpBasic();
    }


    @Bean
    public PortalApiPreAuthenticationFilter headerAuthenticationFilter() throws Exception {
        return new PortalApiPreAuthenticationFilter(authenticationManager());
    }

    /**
     * 配置校验器
     * @param builder
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.authenticationProvider(getPreAuthenticationProvider());
        builder.authenticationProvider(getDaoAuthenticationProvider());
    }

    private AuthenticationProvider getPreAuthenticationProvider() {
        PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
        provider.setPreAuthenticatedUserDetailsService(preUserDetailsService);
        return provider;
    }

    private AuthenticationProvider getDaoAuthenticationProvider(){
        ReflectionSaltSource rss = new ReflectionSaltSource();
        rss.setUserPropertyToUse("username");
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setSaltSource(rss);
        provider.setUserDetailsService(daoUserDetailsService);
        provider.setHideUserNotFoundExceptions(false);
        provider.setPasswordEncoder(getPasswordEncode());
        return provider;
    }


    /**
     * MD5加密器
     * @return
     */
    private org.springframework.security.authentication.encoding.PasswordEncoder getPasswordEncode() {
        ShaPasswordEncoder encode =  new org.springframework.security.authentication.encoding.ShaPasswordEncoder(256);
        encode.setEncodeHashAsBase64(true);
        return encode;
    }

}


