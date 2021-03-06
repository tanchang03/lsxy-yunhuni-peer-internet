package com.lsxy.app.portal.open;

import com.lsxy.framework.cache.manager.RedisCacheService;
import com.lsxy.framework.sms.exceptions.CheckCodeNotFoundException;
import com.lsxy.framework.sms.exceptions.CheckOutMaxTimesException;
import com.lsxy.framework.sms.exceptions.InvalidValidateCodeException;
import com.lsxy.framework.sms.exceptions.TooManyGenTimesException;
import com.lsxy.framework.sms.service.SmsService;
import com.lsxy.framework.web.rest.RestResponse;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 验证码
 * Created by liups on 2016/7/7.
 */
@RestController
@RequestMapping("/code")
public class CheckCodeController {
    private static final Logger logger = LoggerFactory.getLogger(CheckCodeController.class);
    @Autowired
    SmsService smsService;

    @Autowired
    private RedisCacheService cacheManager;

    /**
     * 发送短信验证码
     * @param mobile
     * @return
     */
    @RequestMapping("/send_mobile_code")
    public RestResponse sendMobileCode(String mobile){
        String code;
        try {
            code = smsService.genVC(mobile);
        } catch (TooManyGenTimesException e) {
            logger.error("认证码生成次数过于频繁",e);
            return RestResponse.failed("0000",e.getMessage());
        }
        String template = "01-portal-test-num-bind.vm";
        Map<String,Object> params = new HashedMap();
        params.put("vc",code);
        boolean result = smsService.sendsms(mobile,template,params);
        return RestResponse.success(result);
    }

    /**
     * 校验手机验证码
     * @param mobile
     * @param code
     * @return
     */
    @RequestMapping("/check_mobile_code")
    public RestResponse checkMobileCode(String mobile,String code){
        RestResponse response;
        try {
            boolean flag = smsService.checkVC(mobile, code);
            if(flag){
                response = RestResponse.success(flag);
            }else{
                response = RestResponse.failed("0000","校验出错");
            }
        } catch (InvalidValidateCodeException e) {
            logger.error("验证码错误",e);
            response = RestResponse.failed("0000","验证码错误");
        } catch (CheckOutMaxTimesException e) {
            logger.error("验证超过最大次数",e);
            response = RestResponse.failed("0000","验证超过最大次数");
        } catch (CheckCodeNotFoundException e) {
            logger.error("验证码不存在或已过期",e);
            response = RestResponse.failed("0000","验证码不存在或已过期");
        }
        return response;
    }

    /**
     * 删除Redis里的手机验证码
     * @param mobile
     * @return
     */
    @RequestMapping("remove_mobile_code")
    public RestResponse removeMobileCode(String mobile){
        if(StringUtils.isNotBlank(mobile)){
            cacheManager.del(SmsService.CODE_PREFIX + mobile);
            return RestResponse.success(null);
        }else{
            return RestResponse.failed("0000","空的手机号");
        }
    }

}
