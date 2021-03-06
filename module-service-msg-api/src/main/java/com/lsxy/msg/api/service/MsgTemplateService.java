package com.lsxy.msg.api.service;

import com.lsxy.framework.api.base.BaseService;
import com.lsxy.framework.core.exceptions.api.RequestIllegalArgumentException;
import com.lsxy.framework.core.utils.Page;
import com.lsxy.framework.core.exceptions.api.YunhuniApiException;
import com.lsxy.framework.core.utils.Page;
import com.lsxy.msg.api.model.MsgTemplate;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

/**
 * Created by liups on 2017/3/1.
 */
public interface MsgTemplateService extends BaseService<MsgTemplate> {
    Page<MsgTemplate> getPageByCondition(Integer pageNo, Integer pageSize, String appId, String subaccountId, String name);

    Page<MsgTemplate> getPageByCondition(Integer pageNo, Integer pageSize,int state, Date date1 , Date date2,  String[]  tenantIds);

    MsgTemplate createTemplate(MsgTemplate msgTemplate);

    Page<MsgTemplate> getPageForGW(String appId, String subaccountId, Integer pageNo, Integer pageSize);

    MsgTemplate findByTempId(String appId, String subaccountId, String tempId, boolean isGW) throws YunhuniApiException;

    void deleteMsgTemplate(String appId, String subaccountId, String tempId, boolean isGW) throws InvocationTargetException, IllegalAccessException;

    long findByWait();

    MsgTemplate updateMsgTemplate(MsgTemplate msgTemplate, boolean isGW) throws YunhuniApiException;

    MsgTemplate findByTempId(String tempId);

}
