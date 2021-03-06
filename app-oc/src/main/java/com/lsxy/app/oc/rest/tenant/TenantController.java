package com.lsxy.app.oc.rest.tenant;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lsxy.app.oc.base.AbstractRestController;
import com.lsxy.app.oc.rest.dashboard.vo.ConsumeAndurationStatisticVO;
import com.lsxy.app.oc.rest.tenant.vo.ConsumesVO;
import com.lsxy.app.oc.rest.tenant.vo.RechargeInput;
import com.lsxy.app.oc.rest.tenant.vo.TenantIndicantVO;
import com.lsxy.app.oc.rest.tenant.vo.TenantInfoVO;
import com.lsxy.call.center.api.model.CallCenter;
import com.lsxy.call.center.api.service.CallCenterService;
import com.lsxy.framework.api.billing.model.Billing;
import com.lsxy.framework.api.billing.service.CalBillingService;
import com.lsxy.framework.api.tenant.model.*;
import com.lsxy.framework.api.tenant.service.*;
import com.lsxy.framework.config.SystemConfig;
import com.lsxy.framework.core.exceptions.MatchMutiEntitiesException;
import com.lsxy.framework.core.utils.*;
import com.lsxy.framework.mail.MailConfigNotEnabledException;
import com.lsxy.framework.mail.MailContentNullException;
import com.lsxy.framework.mq.api.MQService;
import com.lsxy.framework.mq.events.portal.ResetPwdVerifySuccessEvent;
import com.lsxy.framework.web.rest.RestRequest;
import com.lsxy.framework.web.rest.RestResponse;
import com.lsxy.yunhuni.api.apicertificate.model.ApiCertificate;
import com.lsxy.yunhuni.api.apicertificate.service.ApiCertificateService;
import com.lsxy.yunhuni.api.app.model.App;
import com.lsxy.yunhuni.api.app.service.AppService;
import com.lsxy.yunhuni.api.consume.enums.ConsumeCode;
import com.lsxy.yunhuni.api.consume.model.Consume;
import com.lsxy.yunhuni.api.consume.service.ConsumeService;
import com.lsxy.yunhuni.api.recharge.service.RechargeService;
import com.lsxy.yunhuni.api.statistics.model.*;
import com.lsxy.yunhuni.api.statistics.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Created by liuws on 2016/8/10.
 */
@Api(value = "租户中心", description = "租户中心相关的接口" )
@RestController
@RequestMapping("/tenant")
public class TenantController extends AbstractRestController {
    public static final Logger logger = LoggerFactory.getLogger(TenantController.class);
    @Autowired
    private TenantService tenantService;

    @Autowired
    private AccountService accountService;
    @Autowired
    private ApiCertificateService apiCertificateService;
    @Autowired
    private CalBillingService calBillingService;
    @Autowired
    private VoiceCdrMonthService voiceCdrMonthService;
    @Autowired
    private ConsumeMonthService consumeMonthService;
    @Autowired
    private RechargeMonthService rechargeMonthService;
    @Autowired
    private VoiceCdrDayService voiceCdrDayService;
    @Autowired
    private ConsumeDayService consumeDayService;
    @Autowired
    private ApiCallDayService apiCallDayService;
    @Autowired
    private RechargeService rechargeService;
    @Autowired
    private ConsumeService consumeService;
    @Autowired
    private RealnameCorpService realnameCorpService;
    @Autowired
    private RealnamePrivateService realnamePrivateService;
    @Autowired
    private MQService mqService;
    @Autowired
    private AppService appService;

    @Autowired
    private TenantServiceSwitchService tenantServiceSwitchService;
    @Autowired
    private CallCenterStatisticsService callCenterStatisticsService;

    @Autowired
    private ApiCallMonthService apiCallMonthService;
    @Reference(timeout = 10000,check = false,lazy = true)
    CallCenterService callCenterService;
    @Autowired
    private MsgDayService msgDayService;
    @Autowired
    private MsgMonthService msgMonthService;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ApiOperation(value = "获取全部数据")
    public RestResponse pList(){
        List list= (List)tenantService.list();
        return RestResponse.success(list);
    }
    @ApiOperation(value = "租户列表")
    @RequestMapping(value = "/tenants",method = RequestMethod.GET)
    public RestResponse tenants(
            @RequestParam(required = false) String name,
            @ApiParam(name = "begin",value = "格式:yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date begin,
            @ApiParam(name = "end",value = "格式:yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,
            @ApiParam(name = "authStatus",value = "认证状态，1已认证，0未认证")
            @RequestParam(required = false) Integer authStatus,
            @ApiParam(name = "accStatus",value = "账号状态，2正常/启用，1被锁定/禁用")
            @RequestParam(required = false) Integer accStatus,
            @ApiParam(name = "isCost",value = "消费状态，1已消费，0未消费")
            @RequestParam(required = false,defaultValue = "0") int isCost,
            @RequestParam(required = false,defaultValue = "1") Integer pageNo,
            @RequestParam(required = false,defaultValue = "10") Integer pageSize){
        if(begin!=null){
            begin = DateUtils.getFirstTimeOfDate(begin);
        }
        if(end!=null){
            end = DateUtils.getLastTimeOfDate(end);
        }
        Page<TenantVO> list = null;
        Page<TenantVO> list2 = null;
        if(isCost == 1){
            list = tenantService.pageListBySearchAndAccount(name, begin, end, authStatus, accStatus, pageNo, pageSize);
        }else{
            list = tenantService.pageListBySearch(name, begin, end, authStatus, accStatus, pageNo, pageSize);
        }
        try{
            //修改余额取值
            List<TenantVO> temp = list.getResult();
            List<TenantVO> list1 = new ArrayList();
            if(temp!=null) {
                for (int i = 0; i < temp.size(); i++) {
                    TenantVO tenantVO = new TenantVO();
                    BeanUtils.copyProperties(tenantVO, temp.get(i));
                    BigDecimal bigDecimal = calBillingService.getBalance(tenantVO.getId());
                    DayStatics currentStatics = calBillingService.getCurrentStatics(tenantVO.getId());
                    tenantVO.setRemainCoin(bigDecimal.doubleValue());
                    tenantVO.setCostCoin(currentStatics.getConsume());//消费额
                    tenantVO.setTotalCoin(currentStatics.getRecharge());
                    tenantVO.setSessionCount(currentStatics.getCallSum());
                    tenantVO.setSessionTime(currentStatics.getCallCostTime()/60);
                    list1.add(tenantVO);
                }
            }
            list2 = new Page<>(list.getStartIndex(),list.getTotalCount(),list.getPageSize(),list1);
        }catch (Exception e){
            logger.error("转换出错{}",e);
        }
        return RestResponse.success(list2);
    }

    @ApiOperation(value = "租户状态禁用/启用")
    @RequestMapping(value = "/tenants/{id}",method = RequestMethod.PATCH)
    public RestResponse tenants(
            @ApiParam(name = "id",value = "租户id")
            @PathVariable String id,
            @ApiParam(name = "status",value = "账号状态，2正常/启用，1被锁定/禁用")
            @RequestParam Integer status){
        return RestResponse.success(accountService.updateStatusByTenantId(id,status));
    }

    @ApiOperation(value = "租户鉴权信息")
    @RequestMapping(value = "/tenants/{id}/cert",method = RequestMethod.GET)
    public RestResponse cert(
            @ApiParam(name = "id",value = "租户id")
            @PathVariable String id){
        ApiCertificate cert = apiCertificateService.findApiCertificateByTenantId(id);
        Map<String,Object> result = new HashMap<>();
        result.put("cert",cert);
        result.put("apiUrl",SystemConfig.getProperty("api.gateway.url","http://api.yunhuni.com") + "/"+ SystemConfig.getProperty("api.gateway.version","v1")+"/account/" + cert.getCertId() + "/");
        return RestResponse.success(result);
    }

    @ApiOperation(value = "租户账务信息，余额/套餐/存储")
    @RequestMapping(value = "/tenants/{id}/billing",method = RequestMethod.GET)
    public RestResponse billing(
            @ApiParam(name = "id",value = "租户id")
            @PathVariable String id){
        Billing billing = calBillingService.getCalBilling(id);
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map map= objectMapper.convertValue(billing,Map.class);
        //余额正数部分
//        int vTemp = billing.getBalance().intValue();
        //余额小数部分
//        DecimalFormat df   = new DecimalFormat("######0.00");
//        String format = df.format(billing.getBalance());
//        String vTempDec = format.substring(format.indexOf('.') + 1, format.length());
//        map.put("balance",vTemp);
//        map.put("balanceDec",vTempDec);
        return RestResponse.success(billing);
    }

    @ApiOperation(value = "租户上个月数据指标")
    @RequestMapping(value = "/tenants/{id}/indicant",method = RequestMethod.GET)
    public RestResponse indicant(
            @ApiParam(name = "id",value = "租户id")
            @PathVariable String id){
        TenantIndicantVO dto = new TenantIndicantVO();
        //上个月
        Date preMonth = DateUtils.getPrevMonth(new Date());
        //上个月消费额
        double preConsume = consumeMonthService.getAmongAmountByDateAndTenant(preMonth,id,null).doubleValue();
        //上个月消费额
        double preRecharge = rechargeMonthService.getAmongAmountByDateAndTenant(preMonth,id).doubleValue();
        //上个月会话量
        long preAmongCall = voiceCdrMonthService.getAmongCallByDateAndTenant(preMonth,id,null);
        //上个月话务量 分钟
        long preAmongDuration = Math.round(voiceCdrMonthService.getAmongCostTimeByDateAndTenant(preMonth,id,null)/60);
        //上个月连通量
        long preAmongConnect = voiceCdrMonthService.getAmongConnectByDateAndTenant(preMonth,id);
        //上个月平均通话时长
        long preAvgTime = preAmongCall == 0 ? 0 :  new BigDecimal(preAmongDuration).divide(new BigDecimal(preAmongCall),0, BigDecimal.ROUND_HALF_UP).longValue() ;
        //上个月连通率
        double preConnectRate = preAmongCall == 0 ? 0 : new BigDecimal(preAmongConnect).divide(new BigDecimal(preAmongCall),4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).doubleValue();

        //上上个月
        Date prepreMonth = DateUtils.getPrevMonth(preMonth);
        //上上个月消费额
        double prepreConsume = consumeMonthService.getAmongAmountByDateAndTenant(prepreMonth,id,null).doubleValue();
        //上上个月消费额
        double prepreRecharge = rechargeMonthService.getAmongAmountByDateAndTenant(prepreMonth,id).doubleValue();
        //上上个月会话量
        long prepreAmongCall = voiceCdrMonthService.getAmongCallByDateAndTenant(prepreMonth,id,null);
        //上上个月话务量 分钟
        long prepreAmongDuration = Math.round(voiceCdrMonthService.getAmongCostTimeByDateAndTenant(prepreMonth,id,null)/60);
        //上上个月连通量
        long prepreAmongConnect = voiceCdrMonthService.getAmongConnectByDateAndTenant(prepreMonth,id);
        //上上个月平均通话时长
        long prepreAvgTime = prepreAmongCall == 0 ? 0 : prepreAmongDuration/prepreAmongCall;
        //上上个月连通率
        double prepreConnectRate = prepreAmongCall == 0 ? 0 : new BigDecimal((prepreAmongConnect/prepreAmongCall) * 100)
                .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        dto.setCostCoin(preConsume);
        dto.setRechargeCoin(preRecharge);
        dto.setSessionCount(preAmongCall);
        dto.setSessionTime(preAmongDuration);
        dto.setAvgSessionTime(preAvgTime);
        dto.setConnectedRate(preConnectRate);
        Map dto1 = new HashMap();
        dto1.put("costCoin",false);
        dto1.put("rechargeCoinRate",false);
        dto1.put("sessionCountRate",false);
        dto1.put("sessionTimeRate",false);
        dto1.put("avgSessionTimeRate",false);
        dto1.put("connectedRateRate",false);
        if(prepreConsume>0) {
            dto.setCostCoinRate(new BigDecimal(((preConsume - prepreConsume)*0.01 )/ (prepreConsume*0.01 )*100)
                    .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            dto1.put("costCoin",true);

        }
        if(prepreRecharge>0) {
            dto.setRechargeCoinRate(new BigDecimal(((preRecharge - prepreRecharge)*0.01) / (prepreRecharge *0.01)*100)
                    .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            dto1.put("rechargeCoinRate",true);

        }
        if(prepreAmongCall>0) {
            double b2 =  new BigDecimal(((preAmongCall - prepreAmongCall)*0.01) / (prepreAmongCall*0.01)*100)
                    .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            dto.setSessionCountRate(b2);
            dto1.put("sessionCountRate",true);

        }
        if(prepreAmongDuration>0) {
            double b1 = new BigDecimal(((preAmongDuration - prepreAmongDuration)*0.01) / (prepreAmongDuration*0.01 )*100)
                    .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            dto.setSessionTimeRate(b1);
            dto1.put("sessionTimeRate",true);

        }
        if(prepreAvgTime>0) {
            dto.setAvgSessionTimeRate(new BigDecimal(((preAvgTime - prepreAvgTime)*0.01) / (prepreAvgTime*0.01)*100)
                    .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            dto1.put("avgSessionTimeRate",true);

        }
        if(prepreConnectRate>0) {
            dto.setConnectedRateRate(new BigDecimal(((preConnectRate - prepreConnectRate)*0.01) / (prepreConnectRate*0.01)*100)
                    .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            dto1.put("connectedRateRate",true);
        }
        Map map = new HashMap();
        map.put("dto",dto);
        map.put("dto1",dto1);
        return RestResponse.success(map);
    }

    @ApiOperation(value = "租户(某月所有天/某年所有月)的（消费额和话务量）统计")
    @RequestMapping(value = "/tenants/{id}/consumeAnduration/statistic",method = RequestMethod.GET)
    public RestResponse consumeAndurationStatistic(
            @PathVariable String id,
            @RequestParam(value = "year") Integer year,
            @RequestParam(required = false,value = "month") Integer month,
            @RequestParam(required =false) String appId
    ){
        ConsumeAndurationStatisticVO dto = new ConsumeAndurationStatisticVO();
        if(month!=null){//某月所有天
            dto.setSession(perDayOfMonthDurationStatistic(year,month,id,appId));
            dto.setCost(perDayOfMonthConsumeStatistic(year,month,id,appId));
        }else{//某年所有月
            dto.setSession(perMonthOfYearDurationStatistic(year,id,appId));
            dto.setCost(perMonthOfYearConsumeStatistic(year,id,appId));
        }
        return RestResponse.success(dto);
    }

    /**
     * 统计租户某个月的每天的话务量
     * @return
     */
    private List<Long> perDayOfMonthDurationStatistic(int year, int month,String tenant,String appId){
        List<Long> results = new ArrayList<Long>();
        //先计算出某个月的所有天的开始和结束时间
        Date cdate = DateUtils.newDate(year,month,1);
        Date d1=DateUtils.getFirstTimeOfMonth(cdate);
        Date d2 =DateUtils.getLastTimeOfMonth(cdate);
        Date[] ds = DateUtils.getDatesBetween(d1,d2);
        if(ds!=null && ds.length>0){
            ExecutorService pool= Executors.newFixedThreadPool(ds.length);
            List<Future<Long>> fs = new ArrayList<Future<Long>>();
            for (final Date d: ds) {
                fs.add(pool.submit(new Callable<Long>() {
                    @Override
                    public Long call(){
                        //转换成分钟
                        return (long)Math.round(voiceCdrDayService.getAmongCostTimeByDateAndTenant(d,tenant,appId)/60);
                    }
                }));
            }
            pool.shutdown();
            for (Future<Long> future : fs) {
                try {
                    results.add(future.get());
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return results;
    }

    /**
     * 统计某个租户某年的每月的话务量
     * @return
     */
    private List<Long> perMonthOfYearDurationStatistic(int year,String tenant,String appId){
        List<Long> results = new ArrayList<Long>();
        int month_length = 12;
        ExecutorService pool= Executors.newFixedThreadPool(month_length);
        List<Future<Long>> fs = new ArrayList<Future<Long>>();
        //先计算出某年所有月的开始和结束时间
        for (int month =1;month<=month_length;month++){
            Date month_start = DateUtils.newDate(year,month,1);
            fs.add(pool.submit(new Callable<Long>() {
                @Override
                public Long call(){
                    return (long)Math.round(voiceCdrMonthService.getAmongCostTimeByDateAndTenant(month_start,tenant,appId)/60);
                }
            }));
        }
        pool.shutdown();
        for (Future<Long> future : fs) {
            try {
                results.add(future.get());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return results;
    }

    /**
     * 统计租户某个月的每天的消费额
     * @return
     */
    private List<Double> perDayOfMonthConsumeStatistic(int year,int month,String tenant,String appId){
        List<Double> results = new ArrayList<Double>();
        //先计算出某个月的所有天的开始和结束时间
        Date cdate = DateUtils.newDate(year,month,1);
        Date d1=DateUtils.getFirstTimeOfMonth(cdate);
        Date d2 =DateUtils.getLastTimeOfMonth(cdate);
        Date[] ds = DateUtils.getDatesBetween(d1,d2);
        if(ds!=null && ds.length>0){
            ExecutorService pool= Executors.newFixedThreadPool(ds.length);
            List<Future<Double>> fs = new ArrayList<Future<Double>>();
            for (final Date d: ds) {
                fs.add(pool.submit(new Callable<Double>() {
                    @Override
                    public Double call(){
                        return consumeDayService.getAmongAmountByDateAndTenant(d,tenant,appId).doubleValue();
                    }
                }));
            }
            pool.shutdown();
            for (Future<Double> future : fs) {
                try {
                    results.add(future.get());
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return results;
    }


    @ApiOperation(value = "租户（某月所有天/某年所有月）的（会话量/次）统计")
    @RequestMapping(value = "/tenants/{id}/session/statistic",method = RequestMethod.GET)
    public RestResponse sessionStatistic(
            @PathVariable String id,
            @RequestParam(value = "year") Integer year,
            @RequestParam(required = false,value = "month") Integer month,
            @RequestParam(required = false) String appId
    ){
        if(month!=null){//某月所有天
            return RestResponse.success(perDayOfMonthSessionCountStatistic(year,month,id,appId));
        }
        return RestResponse.success(perMonthOfYearSessionCountStatistic(year,id,appId));
    }

    private List<Long> perDayOfMonthSessionCountStatistic(int year,int month,String tenant,String appId){
        List<Long> results = new ArrayList<Long>();
        //先计算出某个月的所有天的开始和结束时间
        Date cdate = DateUtils.newDate(year,month,1);
        Date d1=DateUtils.getFirstTimeOfMonth(cdate);
        Date d2 =DateUtils.getLastTimeOfMonth(cdate);
        Date[] ds = DateUtils.getDatesBetween(d1,d2);
        if(ds!=null && ds.length>0){
            ExecutorService pool= Executors.newFixedThreadPool(ds.length);
            List<Future<Long>> fs = new ArrayList<Future<Long>>();
            for (final Date d: ds) {
                fs.add(pool.submit(new Callable<Long>() {
                    @Override
                    public Long call(){
                        return voiceCdrDayService.getAmongCallByDateAndTenant(d,tenant,appId);
                    }
                }));
            }
            pool.shutdown();
            for (Future<Long> future : fs) {
                try {
                    results.add(future.get());
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return results;
    }

    /**
     * 统计某个租户某年的每月的会话量
     * @return
     */
    private List<Long> perMonthOfYearSessionCountStatistic(int year,String tenant,String appId){
        List<Long> results = new ArrayList<Long>();
        int month_length = 12;
        ExecutorService pool= Executors.newFixedThreadPool(month_length);
        List<Future<Long>> fs = new ArrayList<Future<Long>>();
        //先计算出某年所有月的开始和结束时间
        for (int month =1;month<=month_length;month++){
            Date month_start = DateUtils.newDate(year,month,1);
            fs.add(pool.submit(new Callable<Long>() {
                @Override
                public Long call(){
                    return voiceCdrMonthService.getAmongCallByDateAndTenant(month_start,tenant,appId);
                }
            }));
        }
        pool.shutdown();
        for (Future<Long> future : fs) {
            try {
                results.add(future.get());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return results;
    }

    @ApiOperation(value = "租户（某年所有月/某月所有天）的api调用次数统计")
    @RequestMapping(value = "/tenants/{id}/interfaceInvoke/statistic",method = RequestMethod.GET)
    public RestResponse apiInvokeStatistic(
            @PathVariable String id,
            @RequestParam(value = "year") Integer year,
            @RequestParam(required = false,value = "month") Integer month,
            @RequestParam(required = false) String appId
    ){
        if(month!=null){//某月所有天
            return RestResponse.success(perDayOfMonthApiInvokeStatistic(year,month,id,appId));
        }
        return RestResponse.success(perMonthOfYearApiInvokeStatistic(year,id,appId));
    }

    /**
     * 统计某个租户某月所有天的api调用次数
     * @param year
     * @param month
     * @param tenant
     * @return
     */
    private List<Long> perDayOfMonthApiInvokeStatistic(int year,int month,String tenant,String appId){
        List<Long> results = new ArrayList<Long>();
        //先计算出某个月的所有天的开始和结束时间
        Date cdate = DateUtils.newDate(year,month,1);
        Date d1=DateUtils.getFirstTimeOfMonth(cdate);
        Date d2 =DateUtils.getLastTimeOfMonth(cdate);
        Date[] ds = DateUtils.getDatesBetween(d1,d2);
        if(ds!=null && ds.length>0){
            ExecutorService pool= Executors.newFixedThreadPool(ds.length);
            List<Future<Long>> fs = new ArrayList<Future<Long>>();
            for (final Date d: ds) {
                fs.add(pool.submit(new Callable<Long>() {
                    @Override
                    public Long call(){
                        return apiCallDayService.getInvokeCountByDateAndTenant(d,tenant,appId);
                    }
                }));
            }
            pool.shutdown();
            for (Future<Long> future : fs) {
                try {
                    results.add(future.get());
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return results;
    }

    /**
     * 统计某个租户某年所有月的api调用次数
     * @param year
     * @param tenant
     * @return
     */
    private List<Long> perMonthOfYearApiInvokeStatistic(int year,String tenant,String appId){
        List<Long> results = new ArrayList<Long>();
        int month_length = 12;
        ExecutorService pool= Executors.newFixedThreadPool(month_length);
        List<Future<Long>> fs = new ArrayList<Future<Long>>();
        //先计算出某年所有月的开始和结束时间
        for (int month =1;month<=month_length;month++){
            Date month_start = DateUtils.newDate(year,month,1);
            fs.add(pool.submit(new Callable<Long>() {
                @Override
                public Long call(){
                    return apiCallMonthService.getInvokeCountByDateAndTenant(month_start,tenant,appId);
                }
            }));
        }
        pool.shutdown();
        for (Future<Long> future : fs) {
            try {
                results.add(future.get());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return results;
    }

    @ApiOperation(value = "给租户充值")
    @RequestMapping(value = "/tenants/{id}/recharge",method = RequestMethod.PUT)
    public RestResponse recharge(
            @PathVariable String id,
            @RequestBody RechargeInput input){
        return RestResponse.success(rechargeService.doRecharge(id,input.getAmount(),input.getSource()));
    }

    @ApiOperation(value = "给租户平账")
    @RequestMapping(value = "/tenants/{id}/flat_balance",method = RequestMethod.PUT)
    public RestResponse flatAmount(
            @PathVariable String id,
            @RequestBody RechargeInput input){
        Tenant tenant = tenantService.findById(id);
        if(tenant == null){
            throw new IllegalArgumentException("租户不存在");
        }
        Consume consume = new Consume(new Date(), ConsumeCode.flat_balance.name(),input.getAmount(),ConsumeCode.flat_balance.getName(),"0",tenant.getId(),null);
        consumeService.consume(consume);
        return RestResponse.success(true);
    }

    @ApiOperation(value = "租户消费记录")
    @RequestMapping(value = "/tenants/{id}/consumes",method = RequestMethod.GET)
    public RestResponse consumes(
            @PathVariable String id,
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize){
        ConsumesVO dto = new ConsumesVO();
        Page<Consume> page = consumeService.pageListByTenantAndDate(id,year,month,pageNo,pageSize);
        changeTypeToChineseOfConsume(page.getResult());
        dto.setConsumes(page);

//        BigDecimal sum  = new BigDecimal("0.00");
//        for(int i=0;i<list.size();i++){
//            sum  = sum.add(list.get(i).getAmount());
//        }
//        dto.setSumAmount(sum);
        dto.setSumAmount(consumeDayService.getSumAmountByTenant(id,year+"-"+month));

        return RestResponse.success(dto);
    }

    @ApiOperation(value = "租户基本信息,联系信息,业务信息")
    @RequestMapping(value = "/tenants/{id}/info",method = RequestMethod.GET)
    public RestResponse info(@PathVariable String id) throws InvocationTargetException, IllegalAccessException {
        TenantInfoVO dto = new TenantInfoVO();
        if(id != null){
            Tenant tenant = tenantService.findById(id);
            if(tenant != null){
                Account account = accountService.findOneByTenant(id);
                dto.setTenantName(tenant.getTenantName());
                if(account!=null){
                    BeanUtils.copyProperties(dto,account);
                }
            }
        }
        return RestResponse.success(dto);
    }

    @ApiOperation(value = "认证信息,认证状态(未认证，未审核，已认证，认证失败)")
    @RequestMapping(value = "/tenants/{id}/auth/info",method = RequestMethod.GET)
    public RestResponse authInfo(@PathVariable String id) throws InvocationTargetException, IllegalAccessException {
//        AuthInfoVO info = new AuthInfoVO();
//        info.setStatus("未认证");
        Map map = new HashMap();
        RealnamePrivate realnamePrivate = null;
        RealnameCorp realnameCorp = null;
        Integer status = 100;
        if(id != null){
            Tenant tenant = tenantService.findById(id);
            if(tenant != null){
                status = tenant.getIsRealAuth();
                Integer[] privateAuth_status = new Integer[]{Tenant.AUTH_ONESELF_SUCCESS,
                        Tenant.AUTH_ONESELF_WAIT,Tenant.AUTH_ONESELF_FAIL,Tenant.AUTH_UPGRADE_FAIL,Tenant.AUTH_UPGRADE_WAIT,Tenant.AUTH_UPGRADE_SUCCESS
                };//个人认证
                Integer[] companyAuth_status = new Integer[]{Tenant.AUTH_COMPANY_SUCCESS, Tenant.AUTH_COMPANY_FAIL,Tenant.AUTH_WAIT,Tenant.AUTH_UPGRADE_WAIT,
                        Tenant.AUTH_UPGRADE_SUCCESS,Tenant.AUTH_UPGRADE_FAIL};//公司认证
                if(Arrays.asList(privateAuth_status).contains(status)) {
                    realnamePrivate = realnamePrivateService.findByTenantIdNewest(tenant.getId());
                }
                if(Arrays.asList(companyAuth_status).contains(status)) {
                    realnameCorp = realnameCorpService.findByTenantIdNewest(tenant.getId());
                }

            }
        }
        map.put("status",status);
        map.put("realnamePrivate",realnamePrivate);
        map.put("realnameCorp",realnameCorp);
        return RestResponse.success(map);
    }

    @ApiOperation(value = "重置租户的密码)")
    @RequestMapping(value="/tenants/{id}/resetPass",method = RequestMethod.PATCH)
    public RestResponse resetPass(@PathVariable String id) throws MailConfigNotEnabledException, MailContentNullException {
        Account account = accountService.findOneByTenant(id);
        if(account == null || account.getEmail() == null){
            return RestResponse.success(false);
        }
        mqService.publish(new ResetPwdVerifySuccessEvent(account.getEmail()));
        return RestResponse.success(true);
    }


    @ApiOperation(value = "获取功能开关")
    @RequestMapping(value = "/tenants/{tenant}/switchs",method = RequestMethod.GET)
    public RestResponse switchs(
            @PathVariable String tenant) throws MatchMutiEntitiesException {
        TenantServiceSwitch switchs=tenantServiceSwitchService.findOneByTenant(tenant);
        if(switchs == null){
            switchs = tenantServiceSwitchService.saveOrUpdate(tenant,null);
        }
        return RestResponse.success(switchs);
    }

    @ApiOperation(value = "保存功能开关")
    @RequestMapping(value = "/tenants/{tenant}/switch",method = RequestMethod.PUT)
    public RestResponse switchs(
            @PathVariable String tenant,@RequestBody TenantServiceSwitch switchs){
        return RestResponse.success(tenantServiceSwitchService.saveOrUpdate(tenant,switchs));
    }


    @ApiOperation(value = "租户的月结账单")
    @RequestMapping(value="/tenants/{tenant}/consume_month",method = RequestMethod.GET)
    public RestResponse get(@PathVariable String tenant,
                            @RequestParam(required = false) String appId,
                            @ApiParam(name = "month",value = "格式:yyyy-MM")
                            @RequestParam(required = false) String month){
        if(StringUtils.isBlank(month)){
            String curMonth = DateUtils.getDate("yyyy-MM");
            month = DateUtils.getPrevMonth(curMonth,"yyyy-MM");
        }
        List<ConsumeMonth> consumeMonths = consumeMonthService.getConsumeMonths(tenant,appId,month);
        changeTypeToChineseOfConsumeMonth(consumeMonths);
        return RestResponse.success(consumeMonths);
    }



    @ApiOperation(value = "租户(某月所有天/某年所有月)的消费额统计")
    @RequestMapping(value = "/tenants/{tenant}/consume/statistic",method = RequestMethod.GET)
    public RestResponse consumeStatistic(
            @PathVariable String tenant,
            @RequestParam(value = "year") Integer year,
            @ApiParam(name = "month",value="不传month就是某年所有月的统计")
            @RequestParam(value = "month",required = false) Integer month,
            @RequestParam(required = false) String appId
    ){
        if(month!=null){
            return RestResponse.success(perDayOfMonthConsumeStatistic(year,month,tenant,appId));
        }
        return RestResponse.success(perMonthOfYearConsumeStatistic(year,tenant,appId));
    }

    /**
     * 统计某个租户某年的每月的消费额
     * @return
     */
    private List<Double> perMonthOfYearConsumeStatistic(int year,String tenant,String appId){
        List<Double> results = new ArrayList<Double>();
        int month_length = 12;
        ExecutorService pool= Executors.newFixedThreadPool(month_length);
        List<Future<Double>> fs = new ArrayList<Future<Double>>();
        //先计算出某年所有月的开始和结束时间
        for (int month =1;month<=month_length;month++){
            Date month_start = DateUtils.newDate(year,month,1);
            fs.add(pool.submit(new Callable<Double>() {
                @Override
                public Double call(){
                    BigDecimal dec = consumeMonthService.getAmongAmountByDateAndTenant(month_start,tenant,appId);
                    if(dec!=null){
                        return dec.doubleValue();
                    }
                    return null;
                }
            }));
        }
        pool.shutdown();
        for (Future<Double> future : fs) {
            try {
                results.add(future.get());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return results;
    }

    @ApiOperation(value = "租户(某年所有月)的充值额统计")
    @RequestMapping(value = "/tenants/{tenant}/recharges/statistic",method = RequestMethod.GET)
    public RestResponse recharges(@PathVariable String tenant,@RequestParam Integer year){
        return RestResponse.success(perMonthOfYearRechargeStatistic(year,tenant));
    }

    /**
     * 统计某个租户某年的每月的充值额
     * @return
     */
    private List<BigDecimal> perMonthOfYearRechargeStatistic(int year,String tenant){
        List<BigDecimal> results = new ArrayList<BigDecimal>();
        int month_length = 12;
        ExecutorService pool= Executors.newFixedThreadPool(month_length);
        List<Future<BigDecimal>> fs = new ArrayList<Future<BigDecimal>>();
        //先计算出某年所有月的开始和结束时间
        for (int month =1;month<=month_length;month++){
            Date month_start = DateUtils.newDate(year,month,1);
            fs.add(pool.submit(new Callable<BigDecimal>() {
                @Override
                public BigDecimal call(){
                    return rechargeMonthService.getAmongAmountByDateAndTenant(month_start,tenant);
                }
            }));
        }
        pool.shutdown();
        for (Future<BigDecimal> future : fs) {
            try {
                results.add(future.get());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return results;
    }

    @ApiOperation(value = "租户的充值额详单")
    @RequestMapping(value = "/tenants/{tenant}/recharges",method = RequestMethod.GET)
    public RestResponse recharges(
            @PathVariable String tenant,
            @RequestParam String type,
            @RequestParam String source,
            @RequestParam(required = false,defaultValue = "1") Integer pageNo,
            @RequestParam(required = false,defaultValue = "10") Integer pageSize){
        return RestResponse.success(rechargeService.pageListByTenant(tenant,type,source,pageNo,pageSize));
    }


    /**
     * 将消费类型转换为中文，运用枚举
     * @param consumeMonths
     */
    private void changeTypeToChineseOfConsumeMonth(List<ConsumeMonth> consumeMonths){
        for(int i = 0;i < consumeMonths.size();i++){
            ConsumeMonth consumeMonth = new ConsumeMonth();
            try {
                BeanUtils.copyProperties(consumeMonth,consumeMonths.get(i));
            } catch (Exception e) {
                logger.error("复制属性异常",e);
            }
            String type = consumeMonth.getType();
            try{
                ConsumeCode consumeCode = ConsumeCode.valueOf(type);
                consumeMonth.setType(consumeCode.getName());
            }catch(Exception e){
                logger.error("发现未知消费项目",e);
                consumeMonth.setType("未知项目");
            }
            consumeMonths.set(i,consumeMonth);
        }
    }


    /**
     * 将消费类型转换为中文，运用枚举
     * @param consumes
     */
    private void changeTypeToChineseOfConsume(List<Consume> consumes){
        for(int i = 0;i < consumes.size();i++){
            Consume consume = new Consume();
            try {
                BeanUtils.copyProperties(consume,consumes.get(i));
            } catch (Exception e) {
                logger.error("复制属性异常",e);
            }
            String type = consume.getType();
            try{
                ConsumeCode consumeCode = ConsumeCode.valueOf(type);
                consume.setType(consumeCode.getName());
            }catch(Exception e){
                logger.error("发现未知消费项目",e);
                consume.setType("未知项目");
            }
            consumes.set(i,consume);
        }

    }
    @ApiOperation(value = "用户中心的应用的当月呼叫中心统计数据")
    @RequestMapping(value = "/tenants/{id}/call_center/current",method = RequestMethod.GET)
    public RestResponse getCallCenterByCurrent(
            @ApiParam(name = "id",value="租户id")@PathVariable String id,
            @ApiParam(name = "appId",value="应用id")@RequestParam(required = false) String appId
    ){
        CallCenterStatistics incStatics;
        if(StringUtils.isBlank(appId)){
            incStatics = callCenterStatisticsService.getIncStaticsOfCurrentMonthByTenantId(id);
        }else{
            incStatics = callCenterStatisticsService.getIncStaticsOfCurrentMonthByAppId(appId);
        }
        if(incStatics == null){
            incStatics = new CallCenterStatistics(null,null,null,0L,0L,0L,0L,0L,0L,0L,0L);
        }
        Map map = new HashMap<>();
        map.put("callIn",incStatics.getCallIn());//呼入量
        map.put("callOut",incStatics.getCallOut());//呼出量
        map.put("transferSuccess",incStatics.getToManualSuccess());//转接成功
        map.put("formTime",incStatics.getQueueNum()==0?0:Math.round((double)incStatics.getQueueDuration()/incStatics.getQueueNum()));//排队时间
        long callSuccess = incStatics.getCallInSuccess() + incStatics.getCallOutSuccess();
        map.put("callTime",callSuccess == 0?0:Math.round((double)incStatics.getCallTimeLong()/callSuccess));//平均通话时长
        map.put("callFail",incStatics.getCallIn()==0?0:Math.round((double)((incStatics.getQueueNum() - incStatics.getToManualSuccess())*100)/incStatics.getQueueNum()));//呼入流失率
        return RestResponse.success(map);
    }
    @ApiOperation(value = "用户中心的应用的呼叫中心统计数据")
    @RequestMapping(value = "/tenants/{id}/call_center",method = RequestMethod.GET)
    public RestResponse getCallCenterByType(
            @ApiParam(name = "id",value="租户id")@PathVariable String id,
            @ApiParam(name = "appId",value="应用id")@RequestParam(required = false) String appId,
            @ApiParam(name = "type",value="amongCall=拨打次数;amongCostTime=通话时间")@RequestParam String type,
            @ApiParam(name = "timeType",value="时间类型 年year 月month ")@RequestParam String timeType,
            @ApiParam(name = "time",value="时间")@RequestParam String time
    ){
        if(StringUtils.isNotEmpty(appId)) {
            App app = appService.findById(appId);
            if (app == null || StringUtils.isEmpty(id) || !app.getTenant().getId().equals(id)) {
                return RestResponse.failed("0000", "租户id或者应用id错误");
            }
        }
        if(StringUtils.isEmpty(time)){
            return RestResponse.failed("0000","时间不能为空");
        }
        if(StringUtils.isEmpty(type)||!("amongCall".equals(type)||"amongCostTime".equals(type))){
            return RestResponse.failed("0000","类型错误");
        }
        Date date1 = null;
        Date date2 = null;
        List tempVoiceCdrList = null;
        Object date = 12;
        if("year".equals(timeType)){
            try{
                date1 = DateUtils.parseDate(time,"yyyy");
                date2  = DateUtils.parseDate(DateUtils.getLastYearByDate(time)+" 23:59:59","yyyy-MM-dd HH:mm:ss");
                tempVoiceCdrList =  voiceCdrMonthService.list(id,  appId,  App.PRODUCT_CALL_CENTER,  date1,  date2 );
            }catch (Exception e){
                return RestResponse.failed("0000","日期格式错误");
            }
        }else if("month".equals(timeType)){
            try{
                date1 = DateUtils.parseDate(time,"yyyy-MM");
                date2 =  DateUtils.parseDate(DateUtils.getMonthLastTime(DateUtils.parseDate(time,"yyyy-MM")),"yyyy-MM-dd HH:mm:ss");
                tempVoiceCdrList =  voiceCdrDayService.list(id,  appId,  App.PRODUCT_CALL_CENTER,  date1,  date2 );
                date = date1;
            }catch (Exception e){
                return RestResponse.failed("0000","日期格式错误");
            }
        }else{
            return RestResponse.failed("0000","日期类型错误");
        }
        List list = new ArrayList();
        list.add(getArrays(tempVoiceCdrList,date,type));
        return RestResponse.success(list);
    }
    @ApiOperation(value = "用户中心的应用的呼叫中心记录明细")
    @RequestMapping(value = "/tenants/{id}/call_center/detail",method = RequestMethod.GET)
    public RestResponse getCallCenterByTenantAndApp(
            @ApiParam(name = "id",value="租户id")@PathVariable String id,
            @ApiParam(name = "appId",value="应用id")@RequestParam(required = false) String appId,
            @ApiParam(name = "startTime",value="开始时间")@RequestParam String startTime,
            @ApiParam(name = "endTime",value="开始时间")@RequestParam String endTime,
            @ApiParam(name = "pageNo",value="第几页")@RequestParam(defaultValue = "1") Integer pageNo,
            @ApiParam(name = "pageSize",value="每页记录数")@RequestParam(defaultValue = "20") Integer pageSize,
            @ApiParam(name = "type",value="1呼入2呼出")@RequestParam(required = false) String type,
            @ApiParam(name = "callnum",value="手机号码")@RequestParam(required = false) String callnum,
            @ApiParam(name = "agent",value="坐席")@RequestParam(required = false) String agent
    ){
        if(StringUtils.isNotEmpty(appId)) {
            App app = appService.findById(appId);
            if (app == null || StringUtils.isEmpty(id) || !app.getTenant().getId().equals(id)) {
                return RestResponse.failed("0000", "租户id或者应用id错误");
            }
        }
        try{
            DateUtils.parseDate(startTime,"yyyy-MM-dd");
            DateUtils.parseDate(endTime,"yyyy-MM-dd");
            startTime += " 00:00:00";
            endTime += " 23:59:59";
        }catch (Exception e){
            return RestResponse.failed("0000","返回日期类型错误");
        }
        Page<CallCenter> page =  callCenterService.pList( pageNo,pageSize, id, appId, startTime, endTime, type,callnum, agent);
        Map sum = callCenterService.sum(id,appId,startTime,endTime,type,callnum,agent);
        Map map = new HashMap<>();
        map.put("page",page);
        map.put("sum",sum);
        return RestResponse.success(map);
    }


    private int getLong(Object obj){
        int r = 0;
        if (obj instanceof Date) {
            r = Integer.valueOf(DateUtils.getLastDate((Date)obj).split("-")[2]);
        } else if (obj instanceof Integer) {
            r =Integer.valueOf((Integer)obj);
        }
        return r;
    }
    /**
     * 获取列表数据
     * @param list 待处理的list
     * @return
     */
    private Object[] getArrays(List list,Object date,String type) {
        int leng = getLong(date);
        Object[] list1 = new Object[leng];
        for(int j=0;j<leng;j++){
            list1[j]=0L;
        }
        for(int i=0;i<list.size();i++){
            Object obj = list.get(i);
            if(obj instanceof ConsumeMonth){
                int index = ((ConsumeMonth)obj).getMonth()-1;
                BigDecimal currentValue = (list1[index] instanceof BigDecimal)?((BigDecimal)list1[index]): BigDecimal.ZERO;
                list1[index] = currentValue.add(((ConsumeMonth)obj).getAmongAmount()).setScale(3,BigDecimal.ROUND_HALF_UP);
            }else if(obj instanceof VoiceCdrMonth){
                if("amongCostTime".equals(type)){
                    int index = ((VoiceCdrMonth)obj).getMonth()-1;
                    list1[index] = (Long)list1[index] + (((VoiceCdrMonth)obj).getAmongCostTime()/60);
                }else if("amongCall".equals(type)) {
                    int index = ((VoiceCdrMonth)obj).getMonth()-1;
                    list1[index] = (Long)list1[index]  + ((VoiceCdrMonth)obj).getAmongCall();
                }
            }else if(obj instanceof ConsumeDay){
                int index = ((ConsumeDay)obj).getDay()-1;
                BigDecimal currentValue = (list1[index] instanceof BigDecimal)?((BigDecimal)list1[index]): BigDecimal.ZERO;
                list1[index] = currentValue.add(((ConsumeDay)obj).getAmongAmount()).setScale(3,BigDecimal.ROUND_HALF_UP);
            }else if(obj instanceof VoiceCdrDay){
                if("amongCostTime".equals(type)){
                    int index = ((VoiceCdrDay)obj).getDay()-1;
                    list1[index]= (Long)list1[index] + ((VoiceCdrDay)obj).getAmongCostTime()/60;
                }else if("amongCall".equals(type)) {
                    int index = ((VoiceCdrDay)obj).getDay()-1;
                    list1[index] = (Long)list1[index] + ((VoiceCdrDay)obj).getAmongCall();
                }
            }else if(obj instanceof ApiCallDay){
                int index = ((ApiCallDay)obj).getDay()-1;
                list1[index]= (Long)list1[index] +((ApiCallDay)obj).getAmongApi();
            }else if(obj instanceof ApiCallMonth){
                int index = ((ApiCallMonth) obj).getMonth()-1;
                list1[index]= (Long)list1[index] + ((ApiCallMonth)obj).getAmongApi();
            }
        }
        return list1;
    }
    public static final String TYPE_MONTH = "month";//月统计类型 按年查找输出 返回按年
    public static final String TYPE_DAY = "day";//日统计类型 按月查找输出 返回按月
    @ApiOperation(value = "租户(某月所有天/某年所有月)的消息统计数据(succ成功fail失败)")
    @RequestMapping(value = "/tenants/{tenant}/msg/statistic",method = RequestMethod.GET)
    public RestResponse msgStatisticList(
            @PathVariable String tenant,
            @RequestParam(value = "year") Integer year,
            @ApiParam(name = "month",value="不传month就是某年所有月的统计")
            @RequestParam(value = "month",required = false) Integer month,
            @RequestParam(required = false) String appId
    ){
        String type = null;
        String startTime = null;
        if(month!=null){
            type = TYPE_MONTH;
            startTime = year+"-"+month;
        }else{
            type = TYPE_DAY;
            startTime = year+"";
        }
        Date date1 = getStartDate(startTime,type);
        Date date2 = getLastDate(startTime,type);
        List list = null;//getList( tenantId, appId,date1,date2,type);
        if(TYPE_MONTH.equals(type)){
            list = msgMonthService.getStatisticsList(tenant,appId,date1,date2);
        }else{
            list = msgDayService.getStatisticsList(tenant,appId,date1,date2);
        }
        return RestResponse.success(list);
    }
    @ApiOperation(value = "租户(某月所有天/某年所有月)的消息统计数据分页")
    @RequestMapping(value = "/tenants/{tenant}/msg/statistic/plist",method = RequestMethod.GET)
    public RestResponse msgStatistic(
            @PathVariable String tenant,
            @RequestParam(value = "year") Integer year,
            @ApiParam(name = "month",value="不传month就是某年所有月的统计")
            @RequestParam(value = "month",required = false) Integer month,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false,defaultValue = "1") Integer pageNo,
            @RequestParam(required = false,defaultValue = "70") Integer pageSize
    ){
        String type = null;
        String startTime = null;
        if(month!=null){
            type = TYPE_MONTH;
            startTime = year+"-"+month;
        }else{
            type = TYPE_DAY;
            startTime = year+"";
        }
        Date date1 = getStartDate(startTime,type);
        Date date2 = getLastDate(startTime,type);
        Page page = null;//getPage(tenantId, appId , type, date1, date2, pageNo, pageSize);
        if(TYPE_MONTH.equals(type)){
            page = msgMonthService.getStatisticsPage(tenant,appId,date1,date2,pageNo,pageSize);
        }else{
            page = msgDayService.getStatisticsPage(tenant,appId,date1,date2,pageNo,pageSize);
        }
        return RestResponse.success(page);
    }
    /**
     * 获取列表数据
     * @param list 待处理的list
     * @return
     */
    private Map getMsgArrays(List list,Object date) {
        int leng = getLong(date);
        Long[] succ = new Long[leng];
        Long[] fail = new Long[leng];
        for(int j=0;j<leng;j++){
            succ[j] = 0L;
            fail[j] = 0L;
        }
        for(int i=0;i<list.size();i++){
            Object obj = list.get(i);
            if(obj instanceof MsgStatisticsVo){
                MsgStatisticsVo temp = (MsgStatisticsVo)obj;
                int index = temp.getNum()-1;
                succ[index] = temp.getTotalSucc();
                fail[index] = temp.getTotalFail();
            }
        }
        return new HashMap<String,Long[]>(){{
            put("succ",succ);
            put("fail",fail);
        }};
    }
    private Page<MsgStatisticsVo> getPage(String tenantId,String appId ,String type,Date date1,Date date2,int pageNo,int pageSize){
        List<MsgStatisticsVo> list = getList(tenantId,appId,date1,date2,type);
        Page page = new Page( (pageNo-1)*pageSize ,  list.size(),  pageSize, list);
        return page;
    }
    private List<MsgStatisticsVo> getList(String tenantId, String appId, Date date1,Date date2,String type){
        int len = 0;
        if(TYPE_MONTH.equals(type)){
            len = getLong(12);
        }else{
            len = getLong(date1);
        }
        List<MsgStatisticsVo> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        for (int i = 0; i < len; i++) {
            list.add( MsgStatisticsVo.initMsgStatisticsVo(calendar.getTime(),(i+1)) );
            if(TYPE_MONTH.equals(type)){
                calendar.add(Calendar.MONTH,1);
            }else{
                calendar.add(Calendar.DAY_OF_MONTH,1);
            }
        }
        return list;
    }
    private Date getStartDate(String startTime,String type){
        Date date = null;
        if(TYPE_MONTH.equals(type)){
            date = DateUtils.parseDate(startTime,"yyyy");
        }else{
            date = DateUtils.parseDate(startTime,"yyyy-MM");
        }
        return date;
    }
    private Date getLastDate(String endTime,String type){
        Date date2 = null;
        if(TYPE_MONTH.equals(type)){
            date2  = DateUtils.parseDate(DateUtils.getLastYearByDate(endTime)+" 23:59:59","yyyy-MM-dd HH:mm:ss");
        }else{
            date2 =  DateUtils.parseDate(DateUtils.getMonthLastTime(DateUtils.parseDate(endTime,"yyyy-MM")),"yyyy-MM-dd HH:mm:ss");
        }
        return date2;
    }
    @Autowired
    private SubaccountMonthService subaccountMonthService;
    @Autowired
    private SubaccountDayService subaccountDayService;
    @ApiOperation(value = "租户(某月所有天/某年所有月)的子账户统计数据分页")
    @RequestMapping(value = "/tenants/{tenant}/sub/statistic/{type}/plist",method = RequestMethod.GET)
    public RestResponse msgStatistic(
            @PathVariable String tenant,
            @ApiParam(name = "type",value="day日统计格式（yyyy-MM-dd）month月统计(yyyy-MM)")
            @PathVariable String type,
            @ApiParam(name = "endTime",value="开始时间")
            @RequestParam(value = "startTime") String startTime,
            @ApiParam(name = "endTime",value="结束时间")
            @RequestParam(value = "endTime",required = false) String endTime,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String subId,
            @RequestParam(required = false,defaultValue = "1") Integer pageNo,
            @RequestParam(required = false,defaultValue = "70") Integer pageSize
    ){
        if("month".equals(type)){
            Date date1 = DateUtils.parseDate(startTime,"yyyy-MM");
            if(StringUtils.isEmpty(endTime)){
                endTime = startTime;
            }
            Date date2 =  DateUtils.parseDate(DateUtils.getMonthLastTime(DateUtils.parseDate(endTime,"yyyy-MM")),"yyyy-MM-dd HH:mm:ss");
            Page page = subaccountMonthService.getPageByConditions(pageNo,pageSize,date1,date2,tenant,appId,subId);
            Map map = subaccountMonthService.sum(date1,date2,tenant,appId,subId);
            map.put("page",page);
            return RestResponse.success(map);
        }else if("day".equals(type)){
            Date date1 = DateUtils.parseDate(startTime+" 00:00:00","yyyy-MM-dd HH:mm:ss");
            if(StringUtils.isEmpty(endTime)){
                endTime = startTime;
            }
            Date date2 =  DateUtils.parseDate(endTime+" 23:59:59","yyyy-MM-dd HH:mm:ss");
            Page page = subaccountDayService.getPageByConditions(pageNo,pageSize,date1,date2,tenant,appId,subId);
            Map map = subaccountDayService.sum(date1,date2,tenant,appId,subId);
            map.put("page",page);
            return RestResponse.success(map);
        }else{
            return RestResponse.failed("","类型错误");
        }
    }
    @ApiOperation(value = "租户(某月所有天/某年所有月)的子账户统计数据下载")
    @RequestMapping(value = "/tenants/{tenant}/sub/statistic/{type}/download",method = RequestMethod.GET)
    public void msgStatistic(
            HttpServletResponse response,
            @PathVariable String tenant,
            @ApiParam(name = "type",value="day日统计格式（yyyy-MM-dd）month月统计(yyyy-MM)")
            @PathVariable String type,
            @ApiParam(name = "endTime",value="开始时间")
            @RequestParam(value = "startTime") String startTime,
            @ApiParam(name = "endTime",value="结束时间")
            @RequestParam(value = "endTime",required = false) String endTime,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String subId,
            @RequestParam(required = false,defaultValue = "1") Integer pageNo,
            @RequestParam(required = false,defaultValue = "70") Integer pageSize
    ){
        String appId1 = appId;
        //初始化数据
        if(StringUtil.isEmpty(appId)){
            appId = "all";
        }
        if("all".equals(appId)){
            appId1 = "";
        }
        String title = "子账号综合统计";
        String one = "";
        String[] headers = null;
        String[] values = null;
        Map result2;
        List list ;
        if("month".equals(type)){
            Date date1 = DateUtils.parseDate(startTime,"yyyy-MM");
            if(StringUtils.isEmpty(endTime)){
                endTime = startTime;
            }
            Date date2 =  DateUtils.parseDate(DateUtils.getMonthLastTime(DateUtils.parseDate(endTime,"yyyy-MM")),"yyyy-MM-dd HH:mm:ss");
            one +=" 类型：月统计 时间："+startTime+"-"+endTime;
            result2 = subaccountMonthService.sum(date1,date2,tenant,appId1,subId);
            list = subaccountMonthService.getListByConditions(date1,date2,tenant,appId,subId);
        }else if("day".equals(type)){
            Date date1 = DateUtils.parseDate(startTime+" 00:00:00","yyyy-MM-dd HH:mm:ss");
            if(StringUtils.isEmpty(endTime)){
                endTime = startTime;
            }
            Date date2 =  DateUtils.parseDate(endTime+" 23:59:59","yyyy-MM-dd HH:mm:ss");
            one +=" 类型：日统计 时间："+startTime+"-"+endTime;
            result2 = subaccountDayService.sum(date1,date2,tenant,appId1,subId);
            list = subaccountDayService.getListByConditions(date1,date2,tenant,appId,subId);
        }else{
            return ;
        }
        if("all".equals(appId)){
            one += " 选中应用：全部应用 ";
            headers = new String[]{"鉴权账号", "密钥", "所属应用", "话务量（分钟）", "消费金额（元）","语音总用量 /配额（分钟）","坐席数/配额（个）"};
            values = new String[]{"certId", "secretKey", "appName", "amongDuration", "amongAmount","voiceNum","seatNum"};
        }else{
            App app = appService.findById(appId);
            one += " 选中应用："+app.getName();
            headers = new String[]{"鉴权账号", "密钥", "话务量（分钟）", "消费金额（元）","语音总用量 /配额（分钟）","坐席数/配额（个）"};
            values = new String[]{"certId", "secretKey", "amongDuration", "cost", "amongAmount","voiceNum","seatNum"};
        }

        String amongAmount = result2.get("amongAmount")==null? "": result2.get("amongAmount").toString();
        if(StringUtils.isNotEmpty(amongAmount)){
            one += " 总消费："+amongAmount+"元";
        }else{
            one += " 总消费：0元";
        }
        downloadExcel(title, one, headers, values, list, null, "amongAmount", response);
    }
    public <T>  void downloadExcel(String title,String one, String[] headers, String[] values, Collection<T> dataset, String pattern, String money,HttpServletResponse response) {
        try {
            Date d = new Date();
            String name = org.apache.http.client.utils.DateUtils.formatDate(d,"yyyyMMdd")+ d.getTime();
            HSSFWorkbook wb = ExportExcel.exportExcel(title,one,headers,values,dataset,pattern,money);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment;filename="+name+".xls");
            OutputStream ouputStream = response.getOutputStream();
            wb.write(ouputStream);
            ouputStream.flush();
            ouputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
