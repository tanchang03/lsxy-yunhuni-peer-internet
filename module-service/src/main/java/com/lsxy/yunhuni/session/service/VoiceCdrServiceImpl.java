package com.lsxy.yunhuni.session.service;

import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.framework.base.AbstractService;
import com.lsxy.framework.core.utils.BeanUtils;
import com.lsxy.framework.core.utils.Page;
import com.lsxy.framework.core.utils.StringUtil;
import com.lsxy.yunhuni.api.session.model.CallSession;
import com.lsxy.yunhuni.api.session.model.VoiceCdr;
import com.lsxy.yunhuni.api.session.service.VoiceCdrService;
import com.lsxy.yunhuni.session.dao.VoiceCdrDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangxb on 2016/7/19.
 */
@Service
public class VoiceCdrServiceImpl extends AbstractService<VoiceCdr> implements  VoiceCdrService{
    @Autowired
    private VoiceCdrDao voiceCdrDao;
    @Override
    public BaseDaoInterface<VoiceCdr, Serializable> getDao() {
        return voiceCdrDao;
    }
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Page<VoiceCdr> pageList(Integer pageNo,Integer pageSize, Integer type,String tenantId, String time, String appId) {
        String sql = "from db_lsxy_bi_yunhuni.tb_bi_voice_cdr where deleted=0 and type=? and tenant_id=? and app_id=? ";
        String sqlCount = "select count(1) "+sql;
        Integer totalCount = jdbcTemplate.queryForObject(sqlCount,Integer.class,new Object[]{type,tenantId,appId});
        sql = "select "+StringUtil.sqlName(VoiceCdr.class)+sql+" limit ?,?";
        pageNo--;
        List rows = jdbcTemplate.queryForList(sql,new Object[]{type,tenantId,appId,pageNo*pageSize,pageSize});
        List list = new ArrayList();
        for(int i=0;i<rows.size();i++){
            VoiceCdr voiceCdr = new VoiceCdr();
            try {
                BeanUtils.copyProperties(voiceCdr,rows.get(i));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            list.add(voiceCdr);
        }
        Page<VoiceCdr> page = new Page((pageNo)*pageSize+1,totalCount,pageSize,list);
        return page;
    }

    @Override
    public BigDecimal sumCost( Integer type ,String tenantId, String time, String appId) {
        String costType = "cost";
        if(CallSession.TYPE_VOICE_RECORDING==type){
            costType = "record_size";
        }
        String sql = "select sum("+costType+") from db_lsxy_bi_yunhuni.tb_bi_voice_cdr  where deleted=0 and type=? and tenant_id=? and app_id=? ";
        BigDecimal result = this.jdbcTemplate.queryForObject(sql,BigDecimal.class,new Object[]{type,tenantId,appId});
        return result;
    }
}