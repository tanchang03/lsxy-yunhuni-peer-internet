package com.lsxy.msg.supplier;

import com.lsxy.msg.supplier.common.ResultMass;
import com.lsxy.msg.supplier.common.ResultOne;

import java.util.Date;
import java.util.List;

/**
 * Created by liups on 2017/3/8.
 */
public interface SupplierSendService {
    ResultOne sendOne(String tempId, List<String> tempArgs, String msg, String mobile,String sendType,String msgKey);

    ResultMass sendMass(String recordId,String tenantId,String appId,String subaccountId,String msgKey ,String taskName,String tempId,List<String> tempArgs,String msg, List<String> mobiles,Date sendTime,String sendType,String cost);

    Object getTask(String taskId,Object ...params);

    int getMaxSendNum();
}
