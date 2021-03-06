package com.lsxy.app.oc.security;

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
    private UserDetailsService userDetailsService;
    @Autowired
    private PreUserDetailsService preUserDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        if(logger.isDebugEnabled()){
            logger.debug("初始化Spring Security安全框架");
        }

        http.authorizeRequests()
                .antMatchers("/auth/login","/v2/api-docs","/vc/code").permitAll()
                .antMatchers("/**").access("hasRole('ROLE_OC_USER')")
                .and().httpBasic()
                .and().csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        ;

        http.addFilter(headerAuthenticationFilter());
    }

    @Bean
    public TokenPreAuthenticationFilter headerAuthenticationFilter() throws Exception {
        return new TokenPreAuthenticationFilter(authenticationManager());
    }


    private AuthenticationProvider preAuthenticationProvider() {
        PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
        provider.setPreAuthenticatedUserDetailsService(preUserDetailsService);
        return provider;
    }

    /**
     * 配置校验器
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        ReflectionSaltSource rss = new ReflectionSaltSource();
        rss.setUserPropertyToUse("username");
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setSaltSource(rss);

        provider.setUserDetailsService(userDetailsService);

        provider.setPasswordEncoder(getPasswordEncode());

        provider.setHideUserNotFoundExceptions(false);

        auth.authenticationProvider(preAuthenticationProvider());
        auth.authenticationProvider(provider);



//        auth.inMemoryAuthentication().withUser("user001").password("123").roles("TENANT_USER");

//        super.configure(auth);

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


