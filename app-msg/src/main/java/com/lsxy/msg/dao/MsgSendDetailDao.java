package com.lsxy.msg.dao;

import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.msg.api.model.MsgSendDetail;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by liups on 2017/3/1.
 */
public interface MsgSendDetailDao extends BaseDaoInterface<MsgSendDetail, Serializable> {
    @Modifying
    @Query("update MsgSendDetail d set d.state = :state where d.msgKey = :msgKey")
    void updateStateByMsgKey(@Param("msgKey") String msgKey, @Param("state") int state);

    MsgSendDetail findFirstByTaskIdAndMobile(String taskId, String mobile);

    @Modifying
    @Query("update MsgSendDetail d set d.state = :state,d.taskId = :taskId where d.recordId = :recordId AND d.mobile IN (:phones)")
    void updateDetailStateAndTaskIdByRecordId(@Param("recordId") String recordId, @Param("phones") List<String> phones, @Param("state") int state, @Param("taskId") String taskId);

    @Modifying
    @Query("update MsgSendDetail d set d.state = :state,d.endTime=:endTime where d.recordId = :recordId AND d.mobile IN (:phones)")
    void updateStateByRecordId(@Param("recordId") String recordId, @Param("phones") List<String> phones, @Param("state") int state,@Param("endTime") Date endTime);

    List<String> findIdByRecordIdAndMobileIn(String recordId, List<String> phones);

    @Modifying
    @Query("update MsgSendDetail d set d.state = :state,d.endTime=:endTime where d.recordId = :recordId AND d.state = :stateWait")
    void updateStateFromWaitedToSuccessAndSetEndTimeByRecordId(@Param("recordId") String recordId, @Param("stateWait") int stateWait, @Param("state") int state,@Param("endTime") Date endTime);

    List<MsgSendDetail> findByMsgKey(String msgKey);

    @Modifying
    @Query("update MsgSendDetail d set d.endTime = :endTime where d.msgKey = :msgKey AND d.endTime IS NOT NULL ")
    void setEndTimeByMsgKey(@Param("msgKey") String msgKey,@Param("endTime") Date endTime);
}
