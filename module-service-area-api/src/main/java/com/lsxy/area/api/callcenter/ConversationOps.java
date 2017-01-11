package com.lsxy.area.api.callcenter;

import com.lsxy.framework.core.exceptions.api.YunhuniApiException;

/**
 * Created by liuws on 2017/1/9.
 */
public interface ConversationOps {

    public boolean dismiss(String ip,String appId,String conversationId) throws YunhuniApiException;

    public boolean setVoiceMode(String ip,String appId,String conversationId, String agentId, Integer voiceMode) throws YunhuniApiException;

    public boolean inviteAgent(String ip,String appId,String conversationId, String enqueue, Integer voiceMode) throws YunhuniApiException;

    public String inviteOut(String ip, String appId, String conversationId, String from,
                      String to, Integer maxDial, Integer maxDuration, Integer voiceMode) throws YunhuniApiException;
}
