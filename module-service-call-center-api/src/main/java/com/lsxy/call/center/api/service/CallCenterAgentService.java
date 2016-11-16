package com.lsxy.call.center.api.service;

import com.lsxy.call.center.api.model.CallCenterAgent;
import com.lsxy.framework.api.base.BaseService;
import com.lsxy.framework.core.exceptions.api.YunhuniApiException;
import com.lsxy.framework.core.utils.Page;

import java.util.List;

/**
 * Created by zhangxb on 2016/10/22.
 */
public interface CallCenterAgentService extends BaseService<CallCenterAgent> {

    public String login(CallCenterAgent agent) throws YunhuniApiException;

    public void logout(String tenantId, String appId, String channel, String agentName, boolean force) throws YunhuniApiException;

    void keepAlive(String appId, String channel, String agentName) throws YunhuniApiException;

    public boolean checkInSkill(String agent,String skillName,Boolean enable);

    public boolean appendSkill(String tenantId,String appId,String agent,String name,Integer level,Boolean active);

    public List<String> getAgentIdsByChannel(String tenantId, String appId, String channelId);

    CallCenterAgent get(String appId, String channel, String agentName) throws YunhuniApiException;

    Page getPage(String appId, String channel, Integer pageNo, Integer pageSize) throws YunhuniApiException;

    void extension(String appId, String channel, String agentName,String extensionId) throws YunhuniApiException;
}
