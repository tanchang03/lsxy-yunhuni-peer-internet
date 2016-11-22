package com.lsxy.area.server.event.handler.call;

import com.lsxy.area.api.BusinessState;
import com.lsxy.area.api.BusinessStateService;
import com.lsxy.area.server.event.EventHandler;
import com.lsxy.area.server.service.callcenter.ConversationService;
import com.lsxy.framework.core.exceptions.api.YunhuniApiException;
import com.lsxy.framework.rpc.api.RPCRequest;
import com.lsxy.framework.rpc.api.RPCResponse;
import com.lsxy.framework.rpc.api.event.Constants;
import com.lsxy.framework.rpc.api.session.Session;
import com.lsxy.framework.rpc.exceptions.InvalidParamException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by liuws on 2016/8/29.
 */
@Component
public class Handler_EVENT_SYS_CALL_ON_RINGING extends EventHandler{

    private static final Logger logger = LoggerFactory.getLogger(Handler_EVENT_SYS_CALL_ON_RINGING.class);

    @Autowired
    private BusinessStateService businessStateService;

    @Autowired
    private ConversationService conversationService;

    @Override
    public String getEventName() {
        return Constants.EVENT_SYS_CALL_ON_RINGING;
    }

    @Override
    public RPCResponse handle(RPCRequest request, Session session) {
        RPCResponse res = null;
        Map<String,Object> params = request.getParamMap();
        if(MapUtils.isEmpty(params)){
            throw new InvalidParamException("request params is null");
        }
        String call_id = (String)params.get("user_data");
        String res_id = (String)params.get("res_id");
        if(StringUtils.isBlank(call_id)){
            throw new InvalidParamException("call_id is null");
        }
        BusinessState state = businessStateService.get(call_id);
        if(state == null){
            throw new InvalidParamException("businessstate is null");
        }
        if(res_id!=null){
            state.setResId(res_id);
            businessStateService.save(state);
        }
        if(logger.isDebugEnabled()){
            logger.info("call_id={},state={}",call_id,state);
        }

        if(BusinessState.TYPE_CC_AGENT_CALL.equals(state.getType())){
            Map<String,Object> businessData = state.getBusinessData();
            //加入交谈
            String conversation = (String)businessData.get(ConversationService.CONVERSATION_FIELD);
            try {
                conversationService.join(state.getAppId(),conversation,call_id,null,null,null);
            } catch (YunhuniApiException e) {
                logger.error("将呼叫加入交谈失败",e);
            }
        }
        return res;
    }
}
