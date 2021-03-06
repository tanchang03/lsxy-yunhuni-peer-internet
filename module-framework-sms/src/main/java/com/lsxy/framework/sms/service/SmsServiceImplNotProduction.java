package com.lsxy.framework.sms.service;

import com.lsxy.framework.api.sms.model.SMSSendLog;
import com.lsxy.framework.cache.manager.RedisCacheService;
import com.lsxy.framework.sms.clients.SMSClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Tandy on 2016/6/29.
 * 非生产环境使用
 * 非生产环境直接入库出库
 *
 */

@Profile({"development","local"})
@Component
public class SmsServiceImplNotProduction extends AbstractSmsServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImplNotProduction.class);

    @Autowired
    RedisCacheService redisCacheService;

    @Autowired
    private SMSClientFactory smsClientFactory;

    @Autowired
    private AsyncSmsSaveTask asyncSmsSaveTask;

    public RedisCacheService getRedisCacheService(){
        return this.redisCacheService;
    }

    @Override
    public boolean sendsms(String to, String template, Map<String, Object> params) {
        String content = buildSmsContent(template,params);

        if(logger.isDebugEnabled()){
            logger.debug("非生产环境短信发送：{}",to);
            logger.debug("发送内容：{}",content);
        }

        //如果短信发送成功就异步存到数据库
        String clientName = smsClientFactory.getSMSClient().getClientName();
        SMSSendLog log = new SMSSendLog(to,content,clientName,"true");
        asyncSmsSaveTask.saveToDB(log);
        return true;
    }


}
