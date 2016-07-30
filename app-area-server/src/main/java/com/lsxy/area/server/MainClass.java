package com.lsxy.area.server;

import com.lsxy.framework.rpc.FrameworkRPCConfig;
import com.lsxy.framework.rpc.exceptions.RemoteServerStartException;
import com.lsxy.framework.web.web.AbstractSpringBootStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Created by tandy on 16/7/19.
 */
@SpringBootApplication
@ComponentScan
@Import(FrameworkRPCConfig.class)
public class MainClass extends AbstractSpringBootStarter{

    private static final Logger logger = LoggerFactory.getLogger(MainClass.class);
    @Override
    public String systemId() {
        return "area-server";
    }

    public static void main(String[] args) throws RemoteServerStartException {
        ApplicationContext applicationContext = SpringApplication.run(MainClass.class);

    }
}
