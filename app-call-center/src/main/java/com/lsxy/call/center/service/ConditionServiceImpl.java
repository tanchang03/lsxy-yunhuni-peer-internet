package com.lsxy.call.center.service;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.lsxy.call.center.api.model.Condition;
import com.lsxy.call.center.api.service.ConditionService;
import com.lsxy.call.center.api.states.lock.ModifyConditionLock;
import com.lsxy.call.center.dao.ConditionDao;
import com.lsxy.call.center.utils.ExpressionUtils;
import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.framework.base.AbstractService;
import com.lsxy.framework.cache.manager.RedisCacheService;
import com.lsxy.framework.core.exceptions.api.*;
import com.lsxy.framework.core.utils.Page;
import com.lsxy.framework.mq.api.AbstractMQEvent;
import com.lsxy.framework.mq.api.MQService;
import com.lsxy.framework.mq.events.callcenter.CreateConditionEvent;
import com.lsxy.framework.mq.events.callcenter.DeleteConditionEvent;
import com.lsxy.framework.mq.events.callcenter.ModifyConditionEvent;
import com.lsxy.yunhuni.api.app.service.AppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhangxb on 2016/10/21.
 */
@Service
@com.alibaba.dubbo.config.annotation.Service
public class ConditionServiceImpl extends AbstractService<Condition> implements ConditionService {

    private static final Logger logger = LoggerFactory.getLogger(ConditionServiceImpl.class);

    @Autowired
    private ConditionDao conditionDao;

    @Autowired
    private MQService mqService;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private AppService appService;

    @Override
    public BaseDaoInterface<Condition, Serializable> getDao() {
        return conditionDao;
    }

    @Override
    public Condition save(String tenantId,String appId,Condition condition) throws YunhuniApiException{
        if(condition == null){
            throw new RequestIllegalArgumentException();
        }
        if(tenantId == null){
            throw new RequestIllegalArgumentException();
        }
        if(appId == null){
            throw new RequestIllegalArgumentException();
        }
        if(!ExpressionUtils.validSortExpression(condition.getSortExpression())){
            throw new ConditionExpressionException();
        }
        if(!ExpressionUtils.validWhereExpression(condition.getWhereExpression())){
            throw new ConditionExpressionException();
        }

        if(condition.getId() != null){
            Condition oldCondition = this.findById(condition.getId());
            if(oldCondition == null){
                throw new ConditionNotExistException();
            }
            if(!oldCondition.getTenantId().equals(tenantId)){
                throw new ConditionNotExistException();
            }
            if(!oldCondition.getAppId().equals(appId)){
                throw new ConditionNotExistException();
            }
            ModifyConditionLock lock = new ModifyConditionLock(redisCacheService,condition.getId());
            if(!lock.lock()){
                throw new SystemBusyException();
            }
            try{
                boolean modify_where  = !oldCondition.getWhereExpression().equals(condition.getWhereExpression());
                oldCondition.setWhereExpression(condition.getWhereExpression());

                boolean modify_sort = !(oldCondition.getSortExpression() == null?"":oldCondition.getSortExpression())
                        .equals(condition.getSortExpression() == null?"":condition.getSortExpression());
                oldCondition.setSortExpression(condition.getSortExpression());

                boolean modify_priority = Integer.compare(
                        oldCondition.getPriority() == null ? 0 : oldCondition.getPriority(),
                        condition.getPriority() == null ? 0 : condition.getPriority()) != 0;
                oldCondition.setPriority(condition.getPriority());

                oldCondition.setQueueTimeout(condition.getQueueTimeout());
                oldCondition.setFetchTimeout(condition.getFetchTimeout());
                oldCondition.setRemark(condition.getRemark());
                condition = oldCondition;
                condition = super.save(condition);
                //修改条件事件
                AbstractMQEvent event = new ModifyConditionEvent(condition.getId(),condition.getTenantId(),condition.getAppId(),
                        modify_where,modify_sort,modify_priority);
                mqService.publish(event);
            }catch (Throwable t){
                lock.unlock();
                throw t;
            }
        }else{
            condition.setTenantId(tenantId);
            condition.setAppId(appId);
            condition = super.save(condition);
            ModifyConditionLock lock = new ModifyConditionLock(redisCacheService,condition.getId());
            if(!lock.lock()){
                throw new SystemBusyException();
            }
            try{
                //创建条件事件
                AbstractMQEvent event = new CreateConditionEvent(condition.getId(),condition.getTenantId(),condition.getAppId());
                mqService.publish(event);
            }catch (Throwable t){
                lock.unlock();
                throw t;
            }
        }
        return condition;
    }

    @Override
    public void delete(String tenantId,String appId,String conditionId) throws YunhuniApiException{
        Condition condition = this.findOne(tenantId,appId,conditionId);
        ModifyConditionLock lock = new ModifyConditionLock(redisCacheService,condition.getId());
        if(!lock.lock()){
            throw new SystemBusyException(
                    new ExceptionContext()
                            .put("tenantId",tenantId)
                            .put("appId",appId)
                            .put("conditionId",conditionId)
                            .put("lock",false)
            );
        }
        try {
            this.delete(conditionId);
            AbstractMQEvent event = new DeleteConditionEvent(condition.getId(),
                    condition.getTenantId(),condition.getAppId(),condition.getSubaccountId());
            mqService.publish(event);
        } catch (Throwable t) {
            lock.unlock();
            throw new RuntimeException(t);
        }
    }

    @Override
    public Condition findOne(String tenantId,String appId,String conditionId) throws YunhuniApiException{
        if(conditionId == null){
            throw new RequestIllegalArgumentException(
                    new ExceptionContext()
                            .put("tenantId",tenantId)
                            .put("appId",appId)
                            .put("conditionId",conditionId)
            );
        }
        if(tenantId == null){
            throw new RequestIllegalArgumentException(
                    new ExceptionContext()
                            .put("tenantId",tenantId)
                            .put("appId",appId)
                            .put("conditionId",conditionId)
            );
        }
        if(appId == null){
            throw new RequestIllegalArgumentException(
                    new ExceptionContext()
                            .put("tenantId",tenantId)
                            .put("appId",appId)
                            .put("conditionId",conditionId)
            );
        }
        Condition condition = this.findById(conditionId);
        if(condition == null){
            throw new ConditionNotExistException(
                    new ExceptionContext()
                            .put("tenantId",tenantId)
                            .put("appId",appId)
                            .put("conditionId",conditionId)
            );
        }
        if(!tenantId.equals(condition.getTenantId())){
            throw new ConditionNotExistException(
                    new ExceptionContext()
                            .put("tenantId",tenantId)
                            .put("appId",appId)
                            .put("conditionId",conditionId)
                            .put("condition",condition)
            );
        }
        if(!appId.equals(condition.getAppId())){
            throw new ConditionNotExistException(
                    new ExceptionContext()
                            .put("tenantId",tenantId)
                            .put("appId",appId)
                            .put("conditionId",conditionId)
                            .put("condition",condition)
            );
        }
        return condition;
    }

    @Override
    public List<Condition> getAll(String tenantId, String appId) throws YunhuniApiException{
        if(tenantId == null){
            throw new RequestIllegalArgumentException(
                    new ExceptionContext()
                            .put("tenantId",tenantId)
                            .put("appId",appId)
            );
        }
        if(appId == null){
            throw new RequestIllegalArgumentException(
                    new ExceptionContext()
                            .put("tenantId",tenantId)
                            .put("appId",appId)
            );
        }
        return this.conditionDao.findByTenantIdAndAppId(tenantId,appId);
    }

    @Override
    public List<Condition> getAll(String tenantId, String appId,String subaccountId) throws YunhuniApiException{
        if(tenantId == null){
            throw new RequestIllegalArgumentException(
                    new ExceptionContext()
                            .put("tenantId",tenantId)
                            .put("appId",appId)
                            .put("subaccountId",subaccountId)
            );
        }
        if(appId == null){
            throw new RequestIllegalArgumentException(new ExceptionContext()
                    .put("tenantId",tenantId)
                    .put("appId",appId)
                    .put("subaccountId",subaccountId));
        }
        return this.conditionDao.findByTenantIdAndAppIdAndSubaccountId(tenantId,appId,subaccountId);
    }

    @Override
    public Page<Condition> getPageByCondition(Integer pageNo,Integer pageSize,String tenantId, String appId, String subaccountId, String id) throws YunhuniApiException {
        String hql = " from Condition obj where obj.tenantId=?1 and obj.appId =?2 ";
        if(StringUtils.isNotEmpty(subaccountId)){
            hql += " and obj.subaccountId in( "+subaccountId+")";
        }
        if(StringUtils.isNotEmpty(id)){
            hql += " and obj.id like '%"+id+"%' ";
        }
        return this.pageList(hql,pageNo,pageSize,tenantId,appId);
    }
}
