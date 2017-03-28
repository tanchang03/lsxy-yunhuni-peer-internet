package com.lsxy.area.server.event.handler.call;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lsxy.area.api.BusinessState;
import com.lsxy.area.api.BusinessStateService;
import com.lsxy.area.api.ConfService;
import com.lsxy.area.server.event.EventHandler;
import com.lsxy.area.server.service.callcenter.CallCenterUtil;
import com.lsxy.area.server.service.callcenter.CallConversationService;
import com.lsxy.area.server.service.callcenter.ConversationService;
import com.lsxy.area.server.util.NotifyCallbackUtil;
import com.lsxy.area.server.util.RecordFileUtil;
import com.lsxy.call.center.api.model.AppExtension;
import com.lsxy.call.center.api.model.CallCenter;
import com.lsxy.call.center.api.model.CallCenterAgent;
import com.lsxy.call.center.api.model.EnQueue;
import com.lsxy.call.center.api.service.*;
import com.lsxy.call.center.api.states.lock.AgentLock;
import com.lsxy.call.center.api.states.state.AgentState;
import com.lsxy.call.center.api.states.state.ExtensionState;
import com.lsxy.call.center.api.utils.EnQueueDecoder;
import com.lsxy.framework.cache.manager.RedisCacheService;
import com.lsxy.framework.core.exceptions.api.*;
import com.lsxy.framework.core.utils.MapBuilder;
import com.lsxy.framework.rpc.api.RPCCaller;
import com.lsxy.framework.rpc.api.RPCRequest;
import com.lsxy.framework.rpc.api.RPCResponse;
import com.lsxy.framework.rpc.api.ServiceConstants;
import com.lsxy.framework.rpc.api.event.Constants;
import com.lsxy.framework.rpc.api.session.Session;
import com.lsxy.framework.rpc.api.session.SessionContext;
import com.lsxy.framework.rpc.exceptions.InvalidParamException;
import com.lsxy.yunhuni.api.app.model.App;
import com.lsxy.yunhuni.api.app.service.AppService;
import com.lsxy.yunhuni.api.session.model.Meeting;
import com.lsxy.yunhuni.api.session.model.MeetingMember;
import com.lsxy.yunhuni.api.session.service.CallSessionService;
import com.lsxy.yunhuni.api.session.service.MeetingMemberService;
import com.lsxy.yunhuni.api.session.service.MeetingService;
import com.lsxy.yunhuni.api.statistics.model.CallCenterStatistics;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Created by liuws on 2016/8/29.
 */
@Component
public class Handler_EVENT_SYS_CALL_CONF_ENTER_SUCC extends EventHandler{

    private static final Logger logger = LoggerFactory.getLogger(Handler_EVENT_SYS_CALL_CONF_ENTER_SUCC.class);

    @Autowired
    private AppService appService;

    @Autowired
    private BusinessStateService businessStateService;

    @Autowired
    private NotifyCallbackUtil notifyCallbackUtil;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingMemberService meetingMemberService;

    @Autowired
    private CallSessionService callSessionService;

    @Autowired
    private ConfService confService;

    @Autowired
    private CallConversationService callConversationService;

    @Reference(lazy = true,check = false,timeout = 3000)
    private CallCenterConversationService callCenterConversationService;

    @Reference(lazy = true,check = false,timeout = 3000)
    private CallCenterConversationMemberService callCenterConversationMemberService;

    @Reference(lazy = true,check = false,timeout = 3000)
    private EnQueueService enQueueService;

    @Reference(timeout=3000,check = false,lazy = true)
    private AppExtensionService appExtensionService;

    @Reference(timeout=3000,check = false,lazy = true)
    private CallCenterAgentService callCenterAgentService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private RPCCaller rpcCaller;

    @Autowired
    private SessionContext sessionContext;

    @Autowired
    private ExtensionState extensionState;

    @Autowired
    private AgentState agentState;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private CallCenterUtil callCenterUtil;

    @Override
    public String getEventName() {
        return Constants.EVENT_SYS_CALL_CONF_ENTER_SUCC;
    }

    /**
     * 接收到加入会议成功事件，需要向开发者发送通知
     * @param request
     * @param session
     * @return
     */
    @Override
    public RPCResponse handle(RPCRequest request, Session session) {
        RPCResponse res = null;
        Map<String,Object> params = request.getParamMap();
        if(MapUtils.isEmpty(params)){
            throw new InvalidParamException("request params is null");
        }
        String call_id = (String)params.get("user_data");
        if(StringUtils.isBlank(call_id)){
            throw new InvalidParamException("call_id=null");
        }
        BusinessState state = businessStateService.get(call_id);
        if(state == null){
            throw new InvalidParamException("businessstate is null,call_id="+call_id);
        }

        if(logger.isDebugEnabled()){
            logger.debug("call_id={},state={}",call_id,state);
        }
        if(BusinessState.TYPE_CC_INVITE_AGENT_CALL.equals(state.getType()) ||
                BusinessState.TYPE_CC_INVITE_OUT_CALL.equals(state.getType()) ||
                BusinessState.TYPE_CC_AGENT_CALL.equals(state.getType()) ||
                BusinessState.TYPE_CC_OUT_CALL.equals(state.getType()) ||
                conversationService.isCC(state)){
            conversation(state,call_id);
        }else{
            conf(state,call_id);
        }
        return res;
    }

    public void conversation(BusinessState state,String call_id){
        String appId = state.getAppId();
        Map<String,String> businessData = state.getBusinessData();
        String conversation_id = null;
        if(businessData!=null){
            conversation_id = businessData.get(CallCenterUtil.CONVERSATION_FIELD);
        }
        if(StringUtils.isBlank(conversation_id)){
            throw new InvalidParamException("没有找到对应的交谈信息callid={},conversationid={}",call_id,conversation_id);
        }
        if(StringUtils.isBlank(appId)){
            throw new InvalidParamException("没有找到对应的app信息appId={}",appId);
        }

        BusinessState conversationState = businessStateService.get(conversation_id);
        if(conversationState == null || (conversationState.getClosed() !=null && conversationState.getClosed())){
            return;
        }
        if(businessStateService.closed(call_id)){
            return;
        }
        conversationService.join(conversation_id,call_id);
        if(conversationState.getBusinessData().get(CallCenterUtil.INVITETO_FIELD) != null){//邀请外线
            try{
                conversationService.inviteOut(conversationState.getSubaccountId(),appId,conversationState.getBusinessData().get(BusinessState.REF_RES_ID),
                        conversation_id,conversationState.getBusinessData().get(CallCenterUtil.INVITEFROM_FIELD),
                        conversationState.getBusinessData().get(CallCenterUtil.INVITETO_FIELD),null,null,null,null,conversationState.getUserdata());
                businessStateService.deleteInnerField(conversation_id,CallCenterUtil.INVITETO_FIELD,CallCenterUtil.INVITEFROM_FIELD);
            }catch (Throwable t){
                conversationService.exit(conversation_id,call_id);
            }
        }else if(conversationState.getBusinessData().get(CallCenterUtil.ENQUEUEXML_FIELD) != null){//排队
            try{
                EnQueue enqueue = EnQueueDecoder.decode(conversationState.getBusinessData().get(CallCenterUtil.ENQUEUEXML_FIELD));
                enQueueService.lookupAgent(state.getTenantId(),state.getAppId(),state.getSubaccountId(),
                        businessData.get(CallCenterUtil.AGENT_NAME_FIELD),call_id,enqueue,CallCenterUtil.QUEUE_TYPE_CALL_AGENT,conversation_id);
            }catch (Throwable t){
                logger.info("排队找坐席出错",t);
                conversationService.exit(conversation_id,call_id);
            }
        }else if(state.getBusinessData().get(CallCenterUtil.DIRECT_AGENT_FIELD) != null){//直拨坐席
            String agentId = state.getBusinessData().get(CallCenterUtil.DIRECT_AGENT_FIELD);
            String from_extension = state.getBusinessData().get(CallCenterUtil.DIRECT_FROM_FIELD);
            try{
                businessStateService.deleteInnerField(state.getId(),CallCenterUtil.DIRECT_AGENT_FIELD,CallCenterUtil.DIRECT_FROM_FIELD);
                CallCenterAgent agent = callCenterAgentService.findById(agentId);
                if(agent == null){
                    throw new AgentNotExistException(new ExceptionContext().put("agentId",agentId));
                }
                //获取坐席状态
                AgentState.Model aState = agentState.get(agentId);
                if(aState == null || aState.getState() == null){
                    throw new AgentNotExistException(
                            new ExceptionContext()
                                    .put("appId",appId)
                                    .put("agentId",agentId)
                                    .put("agentstate",aState)
                    );
                }
                if(aState.getExtension() == null){
                    throw new ExtensionNotExistException(
                            new ExceptionContext()
                                    .put("appId",appId)
                                    .put("agentName",agent.getName())
                                    .put("agentId",agentId)
                                    .put("agentstate",aState)
                    );
                }
                //座席没有报道
                if (aState.getLastRegTime() + AgentState.REG_EXPIRE < System.currentTimeMillis()) {
                    throw new AgentExpiredException(
                            new ExceptionContext()
                                    .put("appId",appId)
                                    .put("agentName",agent.getName())
                                    .put("agentId",agentId)
                                    .put("agentstate",aState)
                    );
                }
                ExtensionState.Model eState = extensionState.get(aState.getExtension());
                //分机不可用
                if(eState == null || !ExtensionState.Model.ENABLE_TRUE.equals(eState.getEnable())){
                    throw new ExtensionUnEnableException(
                            new ExceptionContext()
                                    .put("appId",appId)
                                    .put("agentName",agent.getName())
                                    .put("agentId",agentId)
                                    .put("extension_id",aState.getExtension())
                                    .put("extensionState",eState)
                    );
                }
                AppExtension extension = appExtensionService.findById(aState.getExtension());
                if(extension == null){
                    throw new ExtensionNotExistException(
                            new ExceptionContext()
                                    .put("appId",appId)
                                    .put("agentName",agent.getName())
                                    .put("agentId",agent.getId())
                                    .put("extension_id",aState.getExtension())
                    );
                }
                //坐席加锁，加锁失败直接拒绝
                AgentLock agentLock = new AgentLock(redisCacheService,agentId);
                if(!agentLock.lock()){
                    logger.info("坐席加锁失败,id={}",agentId);
                    throw new SystemBusyException();
                }
                try{
                    //判断坐席状态是否是空闲，非空闲直接拒绝
                    if(!CallCenterAgent.STATE_IDLE.equals(agentState.getState(agentId))){
                        throw new SystemBusyException(
                                new ExceptionContext()
                                        .put("appId",appId)
                                        .put("agentName",agent.getName())
                                        .put("agentId",agentId)
                                        .put("extension_id",aState.getExtension())
                        );
                    }
                    try{
                        if(businessStateService.closed(call_id)){
                            throw new CallNotExistsException(new ExceptionContext().put("分机直拨坐席","主叫分机已挂断").put("callid",call_id));
                        }
                        conversationService.inviteAgent(state.getSubaccountId(),state.getAppId(),conversationState.getBusinessData().get(BusinessState.REF_RES_ID),call_id,
                                        conversation_id,agent.getId(),
                                        agent.getName(),agent.getExtension(),null,from_extension,extension.getTelnum(),
                                        extension.getType(),extension.getUser(),ConversationService.MAX_DURATION,45,null,null);
                        agentState.setState(agentId,CallCenterAgent.STATE_FETCHING);
                        callCenterUtil.agentStateChangedEvent(state.getSubaccountId(),state.getCallBackUrl(),agent.getId(),agent.getName(),
                                CallCenterAgent.STATE_IDLE,CallCenterAgent.STATE_FETCHING,state.getUserdata());
                    }catch (Throwable t){
                        agentState.setState(agentId,CallCenterAgent.STATE_IDLE);
                        throw t;
                    }
                }finally {
                    agentLock.unlock();
                }
            }catch (Throwable t){
                logger.info("直拨坐席失败",t);
                conversationService.exit(conversation_id,call_id);
            }
        }else if(state.getBusinessData().get(CallCenterUtil.DIRECT_OUT_FIELD) != null){//直拨外线
            String out = state.getBusinessData().get(CallCenterUtil.DIRECT_OUT_FIELD);
            try {
                businessStateService.deleteInnerField(state.getId(),CallCenterUtil.DIRECT_OUT_FIELD);
                conversationService.inviteOut(state.getSubaccountId(),state.getAppId(),conversationState.getBusinessData().get(BusinessState.REF_RES_ID),conversation_id,null,out,
                        null,null,null,null,conversationState.getUserdata());
            } catch (YunhuniApiException e) {
                logger.info("",e);
                conversationService.exit(conversation_id,call_id);
            }
        }
    }

    public void conf(BusinessState state,String call_id){
        String user_data = state.getUserdata();
        Map<String,String> businessData = state.getBusinessData();
        String conf_id = null;
        if(businessData!=null){
            conf_id = businessData.get("conf_id");
        }
        if(StringUtils.isBlank(conf_id)){
            throw new InvalidParamException("没有找到对应的会议信息callid={},confid={}",call_id,conf_id);
        }
        //会议成员增加
        confService.incrPart(conf_id,call_id);

        ifAutoRecording(conf_id);

        try {
            Meeting meeting = meetingService.findById(conf_id);
            if(meeting!=null){
                String callSessionId = businessData.get(BusinessState.SESSIONID);
                MeetingMember meetingMember = new MeetingMember();
                meetingMember.setId(call_id);
                meetingMember.setAppId(state.getAppId());
                meetingMember.setTenantId(state.getTenantId());
                meetingMember.setNumber(businessData.get("to"));
                meetingMember.setJoinTime(new Date());
                if(BusinessState.TYPE_IVR_INCOMING.equals(state.getType())){
                    meetingMember.setJoinType(MeetingMember.JOINTYPE_CALL);
                }else{
                    meetingMember.setJoinType(MeetingMember.JOINTYPE_INVITE);
                }
                meetingMember.setMeetingId(meeting.getId());
                if(callSessionId!=null){
                    meetingMember.setSessionId(callSessionId);
                }
                meetingMember.setResId(state.getResId());
                meetingMemberService.save(meetingMember);
            }
        } catch (Exception e) {
            logger.warn("保存会议成员记录失败",e);
        }

        //开始通知开发者
        if(logger.isDebugEnabled()){
            logger.debug("开始发送会议加入通知给开发者");
        }
        if(StringUtils.isNotBlank(state.getCallBackUrl())){
            Map<String,Object> notify_data = new MapBuilder<String,Object>()
                    .putIfNotEmpty("event","conf.joined")
                    .putIfNotEmpty("id",conf_id)
                    .putIfNotEmpty("subaccount_id",state.getSubaccountId())
                    .putIfNotEmpty("join_time",System.currentTimeMillis())
                    .putIfNotEmpty("call_id",call_id)
                    .putIfNotEmpty("part_uri",null)
                    .putIfNotEmpty("user_data",user_data)
                    .build();
            notifyCallbackUtil.postNotify(state.getCallBackUrl(),notify_data,3);
        }
        if(logger.isDebugEnabled()){
            logger.debug("会议加入通知发送成功");
        }
        if(logger.isDebugEnabled()){
            logger.debug("处理{}事件完成",getEventName());
        }
    }

    /**
     * 创建会议是否自动录音
     * @param conf_id
     */
    private void ifAutoRecording(String conf_id){
        BusinessState state = businessStateService.get(conf_id);
        if(state == null){
            return;
        }
        if(state.getClosed() != null && state.getClosed()){
            return;
        }
        if(state.getResId() == null){
            return;
        }
        if(!Boolean.parseBoolean(state.getBusinessData().get("recording"))){
            return;
        }
        Map<String,Object> params = new MapBuilder<String,Object>()
                .putIfNotEmpty("res_id",state.getResId())
                .putIfNotEmpty("max_seconds",state.getBusinessData().get("max_seconds"))
                .putIfNotEmpty("record_file", RecordFileUtil.getRecordFileUrl(state.getTenantId(),state.getAppId()))
                .putIfNotEmpty("user_data",conf_id)
                .put("areaId",state.getAreaId())
                .build();
        try {
            RPCRequest rpcrequest = RPCRequest.newRequest(ServiceConstants.MN_CH_SYS_CONF_RECORD, params);
            rpcCaller.invoke(sessionContext, rpcrequest,true);
            businessStateService.deleteInnerField(conf_id,"recording");
        } catch (Exception e) {
            logger.error(String.format("会议创建自动录音失败，appId=%s,call_id=%s",state.getAppId(),state.getId()),e);
        }
    }
}
