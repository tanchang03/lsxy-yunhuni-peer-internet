package com.lsxy.app.portal.console.cost;

import com.alipay.config.AlipayConfig;
import com.alipay.util.AlipayNotify;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsxy.app.portal.base.AbstractPortalController;
import com.lsxy.app.portal.comm.PortalConstants;
import com.lsxy.app.portal.security.AvoidDuplicateSubmission;
import com.lsxy.framework.core.utils.BeanUtils;
import com.lsxy.framework.core.utils.JSONUtil;
import com.lsxy.framework.core.utils.Page;
import com.lsxy.framework.web.rest.RestRequest;
import com.lsxy.framework.web.rest.RestResponse;
import com.lsxy.framework.api.billing.model.Billing;
import com.lsxy.yunhuni.api.recharge.enums.RechargeStatus;
import com.lsxy.yunhuni.api.recharge.enums.RechargeType;
import com.lsxy.yunhuni.api.recharge.model.Recharge;
import com.lsxy.yunhuni.api.recharge.model.ThirdPayRecord;
import com.unionpay.acp.sdk.AcpService;
import com.unionpay.acp.sdk.SDKConstants;
import com.unionpay.acp.utils.UnionpayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 充值控制器
 * Created by liups on 2016/7/1.
 */
@Controller
@RequestMapping("/console/cost/recharge")
public class RechargeController extends AbstractPortalController {
    private static final Logger logger = LoggerFactory.getLogger(RechargeController.class);
    /**
     * 去往充值页面
     * @param request
     * @return
     */
    @RequestMapping(value = "",method = RequestMethod.GET)
    @AvoidDuplicateSubmission(needSaveToken = true) //需要生成防重token的方法用这个
    public ModelAndView index(HttpServletRequest request){
        Map<String,Object> model = new HashMap<>();
        String token = getSecurityToken(request);
        Billing billing = getBilling(token);
        if(billing != null){
            //金额格式化成整数和小数部分
            amountFormat(model, billing.getBalance());
        }

        return new ModelAndView("console/cost/recharge/index",model);
    }



    /**
     * 生成订单，并返回确认付款页面
     * @param request
     * @param type 支付类型
     * @param amount 支付金额
     * @return
     */
    @RequestMapping(value = "/create",method = RequestMethod.POST)
    @AvoidDuplicateSubmission(needRemoveToken = true) //需要检验token防止重复提交的方法用这个
    public ModelAndView sure(HttpServletRequest request,String type,BigDecimal amount) throws Exception {
        Map<String,Object> model = new HashMap<>();
        String token = getSecurityToken(request);
        Recharge recharge = createRecharge(token,type,amount);
        //构建VO
        RechargeVO rechargeVO = createRechargeVO(recharge);
        model.put("recharge",rechargeVO);
        //金额格式化成整数和小数部分
        amountFormat(model,recharge.getAmount());
        return new ModelAndView("console/cost/recharge/sure",model);
    }

    /**
     * 返回跳转到支付的页面(支宝付或银联)
     * @param request
     * @param orderId 充值记录的orderId
     * @return
     */
    @RequestMapping(value = "/to_pay",method = RequestMethod.POST)
    public ModelAndView toAliPay(HttpServletRequest request,String orderId) throws Exception {
        Map<String,Object> model = new HashMap<>();
        String returnHtml = null;
        String token = getSecurityToken(request);
        Recharge recharge = getRecharge(token, orderId);
        if(recharge.getType().equals(RechargeType.ALIPAY.name())){
            //支付宝支付处理调用
            returnHtml = PayBuilder.builderAliPay(recharge.getOrderId(), recharge.getAmount());
        }else if(recharge.getType().equals(RechargeType.UNIONPAY.name())){
            //银联支付调用
            returnHtml = PayBuilder.builderUnionpay(recharge.getOrderId(), recharge.getAmount());
        }
        model.put("returnHtml",returnHtml);
        return new ModelAndView("console/cost/recharge/topay",model);
    }


    /**
     * 支付宝支付完后的跳转页面(由支付宝跳转回我们的网站)
     * 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表
     * @param request HttpServletRequest
     * @param trade_status 交易状态
     * @return
     */
    @RequestMapping(value = "/alipay_return")
    public ModelAndView aliPayReturn(HttpServletRequest request,String trade_status){
        Map model = new HashMap();
        //处理支付宝返回的数据
        handleAliPayResult(request, trade_status);
        //重定向到列表
        return new ModelAndView("redirect:/console/cost/recharge/list",model);
    }

    /**
     * 返回列表
     * @param request
     * @param pageNo 当前页
     * @param pageSize 每页总数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    @RequestMapping(value = "/list")
    public ModelAndView list(HttpServletRequest request, @RequestParam(defaultValue = "1") Integer  pageNo,@RequestParam(defaultValue = "20")  Integer pageSize,
                             String startTime, String endTime) throws Exception {
        Map<String,Object> model = new HashMap<>();
        String token = getSecurityToken(request);
        Page page = getRechargePage(pageNo, pageSize, startTime, endTime, token);
        List result = page.getResult();
        for(int i = 0;i < result.size();i++){
            Recharge recharge = (Recharge) result.get(i);
            result.set(i,createRechargeVO(recharge));
        }
        model.put("pageObj",page);
        model.put("startTime",startTime);
        model.put("endTime",endTime);
        return new ModelAndView("console/cost/recharge/list",model);
    }

    /**
     * 返回订单，未支付则到支付页面，已支付则显示详情
     * @param request
     * @param orderId 充值记录的orderId
     * @return
     */
    @RequestMapping(value = "/get",method = RequestMethod.GET)
    public ModelAndView sure(HttpServletRequest request,String orderId) throws Exception {
        String viewName = "console/cost/recharge/list";
        Map<String,Object> model = new HashMap<>();
        String token = getSecurityToken(request);
        //根据orderId获取充值记录
        Recharge recharge = getRecharge(token,orderId);
        if(RechargeStatus.NOTPAID.name().equals(recharge.getStatus())){
            //未支付，则去往支付页面
            viewName = "console/cost/recharge/sure";
        }else if(RechargeStatus.PAID.name().equals(recharge.getStatus())){
            //已支付，则显示订单
            viewName = "console/cost/recharge/list";
        }
        //构建VO
        RechargeVO rechargeVO = createRechargeVO(recharge);
        model.put("recharge",rechargeVO);
        //金额格式化成整数和小数部分
        amountFormat(model,recharge.getAmount());
        return new ModelAndView(viewName,model);
    }





    /**
     * RestApi调用
     * 生成订单
     * @param token
     * @param type 充值类型
     * @param amount 金额
     * @return
     */
    private Recharge createRecharge(String token, String type, BigDecimal amount) {
        //此处调用生成订单restApi
        String orderUrl = PortalConstants.REST_PREFIX_URL + "/rest/recharge/create_recharge";
        Map<String,Object> map = new HashMap();
        map.put("type",type);
        map.put("amount",amount);
        RestResponse<Recharge> orderResponse = RestRequest.buildSecurityRequest(token).post(orderUrl,map, Recharge.class);
        return orderResponse.getData();
    }

    /**
     * RestApi调用
     * 获取订单
     * @param token
     * @param orderId 充值记录的orderId
     * @return
     */
    private Recharge getRecharge(String token, String orderId) {
        //此处调用获取订单restApi
        String getUrl = PortalConstants.REST_PREFIX_URL + "/rest/recharge/get?orderId={1}";
        RestResponse<Recharge> orderResponse = RestRequest.buildSecurityRequest(token).get(getUrl, Recharge.class,orderId);
        return orderResponse.getData();
    }

    /**
     * RestApi调用
     * 获取账务信息
     * @param token
     * @return
     */
    private Billing getBilling(String token) {
        //此处调用账务restApi
        String billingUrl = PortalConstants.REST_PREFIX_URL + "/rest/billing/get";
        RestResponse<Billing> billingResponse = RestRequest.buildSecurityRequest(token).get(billingUrl, Billing.class);
        return billingResponse.getData();
    }

    /**
     * 将金额分割成整数部分和小数部分
     * @param model 传进来一个装载的map
     * @param amount 金额
     */
    private void amountFormat(Map<String, Object> model, BigDecimal amount) {
        if(amount != null){
            if(amount.compareTo(BigDecimal.ZERO) == -1){
                model.put("arrearage","欠费");
            }
            int vTemp = amount.intValue();
            vTemp = Math.abs(vTemp);
            //余额整数部分
            model.put("balanceInt",vTemp);
            //余额小数部分
            DecimalFormat df   = new DecimalFormat("#0.000");
            String format = df.format(amount);
            model.put("balanceDec",format.substring(format.indexOf('.') + 1, format.length()));
        }
    }

    /**
     * 对支付宝返回的支付结果进行处理
     * @param request
     * @param tradeStatus 返回的支付状态
     */
    private void handleAliPayResult(HttpServletRequest request, String tradeStatus) {
        String token = getSecurityToken(request);
        Map<String,String> params = new HashMap<>();
        Map requestParams = request.getParameterMap();
        //将返回的数据进行处理
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        if(logger.isDebugEnabled()){
            logger.debug("开始校验阿里返回参数:{}", JSONUtil.mapToJson(params));
        }
        //计算得出通知验证结果
        boolean verify_result = AlipayNotify.verify(params);
        if(logger.isDebugEnabled()){
            logger.debug("阿里远程校验结果:{}", verify_result);
        }
        if(verify_result){
            //验证成功
            if(tradeStatus.equals("TRADE_FINISHED") || tradeStatus.equals("TRADE_SUCCESS")){
                //调用RestApi对该订单进行处理，并将付款记录存到数据库
                ThirdPayRecord payRecord = new ThirdPayRecord();
                payRecord.setPayType(RechargeType.ALIPAY.name());
                payRecord.setOrderId(params.get("out_trade_no"));
                payRecord.setTradeNo(params.get("trade_no"));
                payRecord.setTradeStatus(params.get("trade_status"));
                payRecord.setTotalFee(new BigDecimal(params.get("total_fee").trim()));
                payRecord.setSellerId(params.get("seller_id"));
                payRecord.setBuyerId(params.get("buyer_id"));
                payRecord.setSellerName(params.get("seller_email"));
                payRecord.setBuyerName(params.get("buyer_email"));
                if(AlipayConfig.seller_id.equals(payRecord.getSellerId())){
                    paySuccess(token, payRecord);
                }

            }
        }
    }

    /**
     * 插入支付记录
     * @param token
     * @param payRecord
     */
    private void paySuccess(String token, ThirdPayRecord payRecord) {
        String successUrl = PortalConstants.REST_PREFIX_URL + "/rest/recharge/pay_success";
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.convertValue(payRecord, Map.class);
        RestRequest.buildSecurityRequest(token).post(successUrl, map,Recharge.class);
    }

    /**
     * RestApi调用
     * @param pageNo 当前页数
     * @param pageSize 每页总数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param token
     * @return
     */
    private Page getRechargePage(Integer pageNo, Integer pageSize, String startTime, String endTime, String token) {
        String orderUrl = PortalConstants.REST_PREFIX_URL + "/rest/recharge/list?pageNo={1}&pageSize={2}&startTime={3}&endTime={4}";
        RestResponse<Page<Recharge>> response = RestRequest.buildSecurityRequest(token).getPage(orderUrl, Recharge.class, pageNo, pageSize, startTime, endTime);
        return (Page) response.getData();
    }

    /**
     * 构建充值VO
     * @param recharge 要转换的充值实体类
     * @return
     * @throws Exception
     */
    private RechargeVO createRechargeVO(Recharge recharge) throws InvocationTargetException, IllegalAccessException {
        RechargeVO vo = new RechargeVO();
        BeanUtils.copyProperties2(vo,recharge,true);
        vo.setStatusName(RechargeStatus.valueOf(vo.getStatus()).getName());
        vo.setTypeName(RechargeType.valueOf(vo.getType()).getName());
        return vo;
    }

    /**
     * 银联支付完后的跳转页面(由银联跳转回我们的网站)
     * 获取银联的通知返回参数，可参考技术文档中页面跳转同步通知参数列表
     * @param request HttpServletRequest
     * @return
     */
    @RequestMapping(value = "/unionpay_return")
    public ModelAndView unionPayReturn(HttpServletRequest request) throws UnsupportedEncodingException {
        Map model = new HashMap();
        //处理银联返回的数据
        handleUnionPayResult(request);
        //重定向到列表
        return new ModelAndView("redirect:/console/cost/recharge/list",model);
    }

    /**
     * 处理银联返回的数据
     * @param request
     * @throws UnsupportedEncodingException
     */
    private void handleUnionPayResult(HttpServletRequest request) throws UnsupportedEncodingException {
        String token = getSecurityToken(request);
        logger.info("FrontRcvResponse前台接收报文返回开始");
        String encoding = request.getParameter(SDKConstants.param_encoding);
        logger.info("返回报文中encoding=[" + encoding + "]");

        Map<String, String> valideData = new HashMap<String, String>();
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);
                // 在报文上送时，如果字段的值为空，则不上送<下面的处理为在获取所有参数数据时，判断若值为空，则删除这个字段>
                if (value != null && !"".equals(value)) {
                    value = new String(value.getBytes(encoding), encoding);
                    valideData.put(en, value);
                }
            }
        }
        if (!AcpService.validate(valideData, encoding)) {
            logger.info("验证签名结果[失败].");
        } else {
            logger.info("验证签名结果[成功].");
//            System.out.println(valideData.get("orderId")); //其他字段也可用类似方式获取
            //调用RestApi对该订单进行处理，并将付款记录存到数据库
            ThirdPayRecord payRecord = new ThirdPayRecord();
            payRecord.setPayType(RechargeType.UNIONPAY.name());
            payRecord.setOrderId(valideData.get("orderId"));
            payRecord.setTradeNo(valideData.get("queryId"));
            payRecord.setTradeStatus(valideData.get("respCode"));
            //银联返回的金额是分为单位，所以要除以100
            BigDecimal amount = new BigDecimal(valideData.get("txnAmt").trim()).divide(new BigDecimal(100));
            payRecord.setTotalFee(amount);
            payRecord.setSellerId(valideData.get("merId"));
            if(UnionpayUtil.merId.equals(payRecord.getSellerId())){
                paySuccess(token, payRecord);
            }
        }
        logger.info("FrontRcvResponse前台接收报文返回结束");
        
    }




}
