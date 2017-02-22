package com.lsxy.yunhuni.statistics.service;

import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.framework.base.AbstractService;
import com.lsxy.framework.core.utils.DateUtils;
import com.lsxy.framework.core.utils.Page;
import com.lsxy.yunhuni.api.statistics.model.SubaccountDay;
import com.lsxy.yunhuni.api.statistics.model.SubaccountStatisticalVO;
import com.lsxy.yunhuni.api.statistics.service.SubaccountDayService;
import com.lsxy.yunhuni.statistics.dao.SubaccountDayDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by liups on 2017/2/21.
 */
@Service
public class SubaccountDayServiceImpl extends AbstractService<SubaccountDay> implements SubaccountDayService {
    @PersistenceContext
    private EntityManager em;
    @Autowired
    SubaccountDayDao subaccountDayDao;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Override
    public BaseDaoInterface<SubaccountDay, Serializable> getDao() {
        return this.subaccountDayDao;
    }

    @Override
    public Page<SubaccountStatisticalVO> getPageByConditions(Integer pageNo, Integer pageSize, Date startTime, Date endTime, String tenantId, String appId, String subaccountId) {
        String sql = " FROM db_lsxy_bi_yunhuni.tb_bi_cert_subaccount_day obj LEFT ON db_lsxy_bi_yunhuni.tb_bi_app a ON obj.appId=a.id WHERE deleted=0 ";
        if(StringUtils.isNotEmpty(appId)){
            sql += " AND app_id = :appId";
        }
        if(StringUtils.isNotEmpty(subaccountId)){
            sql += " AND obj.subaccount_id = :subaccountId ";
        }
        sql += " AND obj.tenant_id =:tenantId AND obj.dt BETWEEN :startTime AND :endTime ";
        String countSql = " SELECT COUNT(1) "+sql;
        String pageSql = " SELECT * "+sql;
        Query countQuery = em.createNativeQuery(countSql);
        pageSql +=" group by obj.dt desc";
        Query pageQuery = em.createNativeQuery(pageSql,SubaccountStatisticalVO.class);
        if(StringUtils.isNotEmpty(appId)){
            countQuery.setParameter("appId",appId);
            pageQuery.setParameter("appId",appId);
        }
        if(StringUtils.isNotEmpty(subaccountId)){
            countQuery.setParameter("subaccountId",subaccountId);
            pageQuery.setParameter("subaccountId",subaccountId);
        }
        countQuery.setParameter("tenantId",tenantId);
        pageQuery.setParameter("tenantId",tenantId);
        countQuery.setParameter("startTime",startTime);
        pageQuery.setParameter("startTime",startTime);
        countQuery.setParameter("endTime",endTime);
        pageQuery.setParameter("endTime",endTime);
        int total = ((BigInteger)countQuery.getSingleResult()).intValue();
        int start = (pageNo-1)*pageSize;
        if(total == 0){
            return new Page<>(start,total,pageSize,null);
        }
        pageQuery.setMaxResults(pageSize);
        pageQuery.setFirstResult(start);
        List list = pageQuery.getResultList();
        return new Page<>(start,total,pageSize,list);
    }

    @Override
    public Map sum(Date start, Date end, String tenantId, String appId, String subaccountId) {
        //amongAmount
        //amongDuration
        String sql = " select sum(among_amount) as amongAmount,sum(among_duration) as amongDuration from db_lsxy_bi_yunhuni.tb_bi_cert_subaccount_day where deleted=0 ";
        if(StringUtils.isNotEmpty(appId)){
            sql += " AND app_id = '"+appId+"'";
        }
        if(StringUtils.isNotEmpty(subaccountId)){
            sql += " AND subaccount_id = '"+subaccountId+"'";
        }
        sql += " AND tenant_id =? AND dt BETWEEN ? AND ? ";
        return jdbcTemplate.queryForMap(sql,tenantId,start,end);
    }

    @Override
    public void dayStatistics(Date date){
        String dd = DateUtils.formatDate(date, "dd");
        int day = Integer.parseInt(dd);
        String dateStr = DateUtils.formatDate(date, "yyyy-MM-dd");
        Date staticsDate = DateUtils.parseDate(dateStr, "yyyy-MM-dd");
        String statisticsDateStr = DateUtils.formatDate(staticsDate);
        Date preDate = DateUtils.getPreDate(staticsDate);
        String preDateStr = DateUtils.formatDate(preDate);
        Date nextDate = DateUtils.nextDate(staticsDate);
        String nextDateStr = DateUtils.formatDate(nextDate);

        String sql = "SELECT a.tenant_id,a.app_id,a.id AS subaccount_id , IFNULL(b.among_duration,0) AS among_duration, IFNULL(b.among_amount,0) AS among_amount, '"+statisticsDateStr+"' AS dt, "+ day +" AS day , 0 AS msg_used ," +
                "IFNULL((SELECT d.voice_used FROM tb_bi_cert_subaccount_day d WHERE d.subaccount_id = a.id AND d.dt = '" + preDateStr + "'),0) + IFNULL(b.among_duration,0) AS voice_used " +
                "FROM (SELECT p.tenant_id ,s.app_id ,p.id FROM db_lsxy_bi_yunhuni.tb_bi_api_cert p INNER JOIN db_lsxy_bi_yunhuni.tb_bi_api_cert_subaccount s ON p.id = s.id WHERE p.deleted = 0) a " +
                "LEFT JOIN " +
                "(SELECT tenant_id,app_id,subaccount_id,SUM(cost_time_long) AS among_duration,SUM(cost) AS among_amount  " +
                "FROM db_lsxy_bi_yunhuni.tb_bi_voice_cdr WHERE call_end_dt >= '" + statisticsDateStr + "'  AND call_end_dt < '" + nextDateStr + "' GROUP BY tenant_id,app_id,subaccount_id) b " +
                "ON a.id = b.subaccount_id";
        Query query = getEm().createNativeQuery(sql, SubaccountDay.class);
        List<SubaccountDay> result = query.getResultList();
        //TODO 批量保存
        if(result != null){
            for(SubaccountDay dayStatics : result){
                this.save(dayStatics);
            }
        }
    }

}
