package com.lsxy.app.portal.rest.file;

import com.lsxy.app.portal.base.AbstractRestController;
import com.lsxy.framework.api.tenant.model.Tenant;
import com.lsxy.framework.config.SystemConfig;
import com.lsxy.framework.core.utils.FileUtil;
import com.lsxy.framework.core.utils.Page;
import com.lsxy.framework.web.rest.RestResponse;
import com.lsxy.yunhuni.api.file.model.VoiceFileRecord;
import com.lsxy.yunhuni.api.file.service.VoiceFileRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 录音文件
 * Created by zhangxb on 2016/7/21.
 */
@RequestMapping("/rest/voice_file_record")
@RestController
public class VoiceFileRecordController extends AbstractRestController {
    private static final Logger logger = LoggerFactory.getLogger(VoiceFileRecordController.class);
    private  String path= SystemConfig.getProperty("portal.realauth.resource.upload.file.path");
    @Autowired
    VoiceFileRecordService voiceFileRecordService;
    /**
     * 根据放音文件id删除放音文件
     * @param id
     * @return
     */
    @RequestMapping("/delete")
    public RestResponse delete(String id) throws InvocationTargetException, IllegalAccessException {
        VoiceFileRecord voiceFileRecord = voiceFileRecordService.findById(id);
        voiceFileRecordService.delete(voiceFileRecord);
        //删除文件
        boolean flag = false;
        try{
            flag = FileUtil.delFile(path+voiceFileRecord.getUrl());
        }catch(Exception e){}
        logger.info(path+voiceFileRecord.getUrl()+"删除结果:"+flag);
        return RestResponse.success(voiceFileRecord);
    }

    /**
     * 批量删除
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping("/batch/delete")
    public RestResponse deleteAll(String appId,Date startTime,Date endTime){
        Tenant tenant = getCurrentAccount().getTenant();
        int result = voiceFileRecordService.batchUpdateStatus(appId,tenant.getId(),startTime,endTime);
        List<VoiceFileRecord> list = voiceFileRecordService.list(appId,tenant.getId(),startTime,endTime);
        //开始删除文件
        for(int i=0;i<list.size();i++){
            boolean flag = false;
            try{
                flag = FileUtil.delFile(path+list.get(i).getUrl());
            }catch(Exception e){}
            logger.info(path+list.get(0).getUrl()+"删除结果:"+flag);
        }
        return RestResponse.success(result);
    }
    /**
     * 批量删除
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping("/batch/download")
    public RestResponse batchDownload(String appId,Date startTime,Date endTime){
        Tenant tenant = getCurrentAccount().getTenant();
        List<VoiceFileRecord> list = voiceFileRecordService.list(appId,tenant.getId(),startTime,endTime);
        String result = "";
        for(int i=0;i<list.size();i++){
            //进行对文件进行压缩
            //
        }
        //返回zip文件的完整路径
        return RestResponse.success(result);
    }

    /**
     * 统计方法
     * @param appId
     * @return
     */
    @RequestMapping("/sum")
    public RestResponse sum(String appId,Date startTime,Date endTime){
        Tenant tenant = getCurrentAccount().getTenant();
        Map map = voiceFileRecordService.sumAndCount(appId,tenant.getId(),startTime,endTime);
        return RestResponse.success(map);
    }
    /**
     * 根据名字和应用id查询用户名下的录音文件
     * @param pageNo 第几页
     * @param pageSize 每页记录数
     * @param appId 应用id
     * @return
     */
    @RequestMapping("/plist")
    public RestResponse pageList(Integer pageNo,Integer pageSize ,String appId){
        Tenant tenant = getCurrentAccount().getTenant();
        Page<VoiceFileRecord> page = voiceFileRecordService.pageList(pageNo,pageSize,appId,tenant.getId());
        return RestResponse.success(page);
    }

}
