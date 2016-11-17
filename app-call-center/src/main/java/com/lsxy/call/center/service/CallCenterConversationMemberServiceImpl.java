package com.lsxy.call.center.service;

import com.lsxy.call.center.api.model.CallCenterConversationMember;
import com.lsxy.call.center.api.service.CallCenterConversationMemberService;
import com.lsxy.call.center.dao.CallCenterConversationMemberDao;
import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.framework.base.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhangxb on 2016/11/11.
 */
@Service
@com.alibaba.dubbo.config.annotation.Service
public class CallCenterConversationMemberServiceImpl extends AbstractService<CallCenterConversationMember> implements CallCenterConversationMemberService {
    @Autowired
    CallCenterConversationMemberDao callCenterConversationMemberDao;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Override
    public BaseDaoInterface<CallCenterConversationMember, Serializable> getDao() {
        return this.callCenterConversationMemberDao;
    }

    @Override
    public List<String> getListBySessionId(String sessionId) {
        String sql = "SELECT DISTINCT relevance_id FROM db_lsxy_bi_yunhuni.tb_bi_call_center_conversation_member  WHERE deleted=0 AND session_id=? ";
        return jdbcTemplate.queryForList(sql,String.class,sessionId);
    }


}
