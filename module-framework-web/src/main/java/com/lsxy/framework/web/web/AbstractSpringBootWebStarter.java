package com.lsxy.framework.web.web;

import com.lsxy.framework.config.Constants;
import com.lsxy.framework.config.SystemConfig;
import com.lsxy.framework.core.AbstractSpringBootStarter;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Created by Tandy on 2016/7/4.
 */
public abstract class AbstractSpringBootWebStarter extends AbstractSpringBootStarter implements EmbeddedServletContainerCustomizer {
    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        container.setPort(Integer.parseInt(SystemConfig.getProperty(systemId() + ".http.port","8080")));
        //供系统其它位置使用
        System.setProperty("systemId",systemId());
    }

}
