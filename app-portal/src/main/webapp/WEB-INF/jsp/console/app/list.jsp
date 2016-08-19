<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@include file="/inc/import.jsp" %>
<!DOCTYPE html>
<html>

<!-- header -->
<head>
    <%@include file="/inc/meta.jsp" %>

</head>
<body>
<section class="vbox">
    <%@include file="/inc/headerNav.jsp"%>
    <section class='aside-section'>
        <section class="hbox stretch">
            <!-- .aside -->
            <aside class="bg-Green lter aside hidden-print" id="nav"><%@include file="/inc/leftMenu.jsp"%></aside>
            <!-- /.aside -->

        <section id="content">
            <section class="hbox stretch">
                <!-- 如果没有三级导航 这段代码注释-->
                <aside class="bg-green lter aside-sm hidden-print ybox" id="subNav">
                    <section class="vbox">
                        <div class="wrapper header"><span class="margin_lr"></span><span class="margin_lr border-left">&nbsp;应用管理</span>
                        </div>
                        <section class="scrollable">
                            <div class="slim-scroll">
                                <!-- nav -->
                                <nav class="hidden-xs">
                                    <ul class="nav">
                                        <li>
                                            <div class="aside-li-a active">
                                                <a href="${ctx}/console/app/list">应用列表</a>
                                            </div>
                                        </li>
                                        <li>
                                            <div class="aside-li-a">
                                                <a href="${ctx}/console/app/index">创建应用</a>
                                            </div>
                                        </li>
                                    </ul>
                                </nav>
                            </div>
                        </section>

                    </section>
                </aside>
                <aside>
                    <section class="vbox xbox">
                        <!-- 如果没有三级导航 这段代码注释-->
                        <div class="head-box"><a href="#subNav" data-toggle="class:hide"> <i
                                class="fa fa-angle-left text"></i> <i class="fa fa-angle-right text-active"></i> </a>
                        </div>
                        <div class="wrapper header">
                            <span class="border-left">&nbsp;应用列表</span>
                        </div>
                        <section class="scrollable wrapper w-f">
                            <section class="panel panel-default yunhuni-personal">
                                <div class="row m-l-none m-r-none bg-light lter">
                                    <div class="col-md-12 padder-v fix-padding">
                                        <a href="${ctx}/console/app/index" class="btn btn-primary query">创建应用</a>
                                    </div>
                                </div>
                            </section>
                            <section class="panel panel-default pos-rlt clearfix ">
                                <table class="table table-striped cost-table-history tablelist" id="tableModal">
                                    <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>应用名称</th>
                                        <th>APPID</th>
                                        <th>应用状态</th>
                                        <th>应用创建时间</th>
                                        <th>操作</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${pageObj.result}" var="result" varStatus="s">
                                        <tr id="app-${result.id}">
                                            <td scope="row">${s.index+1}</td>
                                            <td>${result.name}</td>
                                            <td>${result.id}</td>
                                            <c:if test="${result.status==1}"><td ><span class="success"  id="statusapp-${result.id}">已上线</span></td></c:if>
                                            <c:if test="${result.status==2}"><td ><span class="nosuccess" id="statusapp-${result.id}">未上线</span></td></c:if>
                                            <td><fmt:formatDate value="${result.createTime}" pattern="yyyy-MM-dd HH:mm:ss"></fmt:formatDate> </td>
                                            <td class="operation">
                                                <a href="${ctx}/console/app/detail?id=${result.id}">详情</a> <span ></span>
                                                <a onclick="delapp('${result.id}','${result.status}')" >删除</a> <span ></span>
                                                <c:if test="${result.status==2}"> <a onclick="tabtarget('${result.id}','${result.isIvrService==1?1:0}')" >申请上线</a></c:if>
                                                <c:if test="${result.status==1}"> <span class="apply" id="trb-${result.id}"><a onclick="offline('${result.id}','${result.isIvrService}')">下线</a></span></c:if>
                                            </td>

                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </section>
                            <c:set var="pageUrl" value="${ctx}/console/app/list"></c:set>
                            <%@include file="/inc/pagefooter.jsp" %>

                        </section>
                    </section>
                </aside>
            </section>
            <a href="#" class="hide nav-off-screen-block" data-toggle="class:nav-off-screen" data-target="#nav"></a>
        </section>
    </section>
</section>



<input type="hidden" value="" id="modal-appid" />

<!---mobilebox-->
<div class="shadow-bg" id="show-bg" ></div>
<div id="mobilebox-1" class="appliation-modal-box" style="display: none;">
    <div class="addmobile1" >
        <div class="title">应用上线流程<a class="close_a modalCancel" data-type="1"></a></div>
        <div class="content" >
            <!--nav-->
            <div class="nav-modal-box">
                <ul class="nav-modal">
                    <li><a class="spot" data-action="1"></a><span class="lines"></span> </li>
                    <li><a class="spot" data-action="2"></a><span class="lines"></span></li>
                    <li><a class="spot" data-action="3"></a><span class="lines"></span></li>
                    <li><a class="spot" data-action="4"></a> </li>
                </ul>
                <ul class="nav-modal-text">
                    <li><span class="text">实名认证</span> </li>
                    <li><span class="text">IVR号码绑定</span> </li>
                    <li class="ml-3"><span class="text">支付</span> </li>
                    <li class="ml-30 mr-0"><span class="text">上线</span>  </li>
                </ul>
            </div>

            <div class="contentModal" data-action="1">
                <!--未认证显示-->
                <div class="not-real-auth" style="display: none">
                    <div class="input text-center " >
                        <img src="${resPrefixUrl }/images/index/l6.png" alt="" class="sre" />
                        <p>您还没有经过实名认证，请进行实名认证！</p>
                    </div>
                    <div class="input text-center" >
                        <a href="${ctx}/console/account/auth/index" type="button"  class="btn btn-primary btn-box">实名认证</a>
                    </div>
                </div>
                <!---end--->
                <!--认证显示-->
                <div class="real-auth">
                    <div class="input text-center" >
                        <img src="${resPrefixUrl }/images/index/l6.png" alt="" class="sre" />
                        <p>您已成功进行实名认证，点击进入下一步!</p>
                    </div>
                    <div class="input text-center" >
                        <a type="button"  class="btn btn-primary btn-box tabModalBtn"  data-id="2" data-fun="creatIVR()">下一步</a>
                    </div>
                </div>
                <!---end--->
            </div>

            <div class="contentModal" style="display: none" data-action="2">
                <div id="selectNewIvr" style="display: none">
                    <div class="input text-center">
                        <p>您选择开通IVR功能，我们给您分配了一个IVR号码供应用使用IVR功能</p>
                    </div>
                    <div class="input text-center">
                        <div class="defulatTips" id="creatIVR" ></div>
                        <a onclick="nolike()" class="font14" id="noLike">不喜欢 换一个?</a>
                    </div>
                    <div class="hideIVR">
                    </div>
                </div>
                <div id="selectOwnIvr" >
                    <div class="input text-center">
                        <p>您选择开通IVR功能，请从您拥有的IVR号在选择一个供使用</p>
                    </div>
                    <div class="input text-center">
                        <select id="ownIvr" >
                        </select>
                    </div>
                </div>
                <div class="input text-center" >
                    <a type="button"  class="btn btn-primary btn-box tabModalBtn" data-id="3" data-fun="getOrder()" >下一步</a>
                </div>
            </div>

            <div class="contentModal" style="display: none" data-action="3">
                <div class="input text-center mt-0">
                    <p>您需要支付：<span class="money" id="payAmount">1100.00</span> 元&nbsp;&nbsp;&nbsp; 账号余额：<span id="balance">1100.00</span> 元 &nbsp;&nbsp;&nbsp; <span class="nomoney" style="display: none">!!余额不足</span>
                        &nbsp;&nbsp;&nbsp;<a href="${ctx}/console/cost/recharge" target="_blank">充值</a>&nbsp;&nbsp;&nbsp;<a href="javascript:void(0);" id="refreshBalance">刷新余额</a> </p>
                </div>
                <div class="input text-center mb-0 mt-0">
                    <div class="defulatTips">IVR号码：<span id="selectIvr"></span></div>
                    <div id="payMoneyInfo" style="display: none">
                        <div class="defulatTips">IVR号码租用费：1000元</div>
                        <div class="defulatTips">IVR功能使用费：100元/月</div>
                    </div>
                </div>
                <div class="ivrserver"><input type="checkbox" name="readcheckbox" id="readbook" />已阅读<a target="_blank" href="ivragreement.html" >IVR服务协议</a></div>
                <div class="input text-center mt-0" >
                    <a type="button"  class="btn btn-primary btn-box tabModalBtn" data-id="4" data-fun="pay()" id="payButton">确认支付</a>
                    <a type="button"  class="btn btn-primary btn-box" onclick="resetIVR()">重新选择</a>
                </div>
            </div>

            <div class="contentModal" style="display:none " data-action="4">
                <div class="input text-center" >
                    <img src="${resPrefixUrl }/images/index/l1.png" alt="" class="sre" />
                    <p>上线成功</p>
                </div>
                <div class="input text-center" >
                    <a href="" type="button"  class="btn btn-primary btn-box tabModalBtn">上线成功</a>
                </div>
            </div>
            <div class="input">
                <div class="tips-error moadltips1 text-center" style="display: none">错误提示信息</div>
            </div>

        </div>
    </div>

</div>


<div id="mobilebox-0" class="appliation-modal-box" style="display: none" >
    <div class="addmobile1" >
        <div class="title">应用上线流程<a class="close_a modalCancel" data-type="0"></a></div>
        <div class="content" >
            <!--nav-->
            <div class="nav-modal-box">
                <ul class="nav-modal navw-150">
                    <li><a class="spot" data-action="1"></a><span class="lines"></span> </li>
                    <li><a class="spot" data-action="2"></a></li>
                </ul>
                <ul class="nav-modal-text navw-150">
                    <li><span class="text">实名认证</span> </li>
                    <li class=" mr-0"><span class="text">上线</span>  </li>
                </ul>
            </div>

            <div class="contentModal" data-action="1">
                <!--未认证显示-->
                <div class="not-real-auth" style="display: none">
                    <div class="input text-center" >
                        <img src="${resPrefixUrl }/images/index/l6.png" alt="" class="sre" />
                        <p>您还没有经过实名认证，请进行实名认证！</p>
                    </div>
                    <div class="input text-center" >
                        <a href="${ctx}/console/account/auth/index" type="button"  class="btn btn-primary btn-box">实名认证</a>
                    </div>
                </div>
                <!---end--->
                <!--认证显示-->
                <div class="real-auth">
                    <div class="input text-center">
                        <img src="${resPrefixUrl }/images/index/l6.png" alt="" class="sre" />
                        <p>您已成功进行实名认证，点击进入下一步!</p>
                    </div>
                    <div class="input text-center" >
                        <a type="button"  class="btn btn-primary btn-box tabModalBtn" data-id="2" data-fun="directOnline()">下一步</a>
                    </div>
                </div>
                <!---end--->
            </div>

            <div class="contentModal" style="display: none" data-action="2">
                <div class="input text-center" >
                    <img src="${resPrefixUrl }/images/index/l1.png" alt="" class="sre" />
                    <p>上线成功</p>
                </div>
                <div class="input text-center" >
                    <a href="" class="btn btn-primary btn-box tabModalBtn"  data-fun="" >上线成功</a>
                </div>
            </div>
            <div class="input">
                <div class="tips-error moadltips1 text-center" style="display: none">错误提示信息</div>
            </div>

        </div>
    </div>

</div>

<%@include file="/inc/footer.jsp"%>
<script type="text/javascript" src='${resPrefixUrl }/js/bootstrap-datepicker/js/bootstrap-datepicker.js'> </script>
<script type="text/javascript" src='${resPrefixUrl }/js/bootstrap-datepicker/locales/bootstrap-datepicker.zh-CN.min.js'> </script>
<script type="text/javascript" src='${resPrefixUrl }/js/application/list.js'> </script>
<script type="text/javascript">
    var pageObj = {
        currentPageNo : '${pageObj.currentPageNo}',
        totalCount : '${pageObj.totalCount}',
        pageSize : '${pageObj.pageSize}',
        pageUrl : '${pageUrl}',
        totalPageCount : '${pageObj.totalPageCount}',
        initToTalPageCount :function(){
            this.totalPageCount = this.totalCount%this.pageSize==0?parseInt(this.totalCount/ this.pageSize) : parseInt(this.totalCount/ this.pageSize+1);
        }
    }
    console.info(JSON.stringify(pageObj));
</script>
<script>

    //判断是否实名认证
    function isRealAuth(){
        var realAuth = null;
        //获取用户实名认证状态
        ajaxsync(ctx + "/console/account/auth/is_real_auth",null,function(result){
            realAuth = (result.data == 1 || result.data == 2);
        },"get");
        return realAuth;
    }

    /**
     * @param id 应用id
     * @param type 类型
     * type 1 ivr  2no ivr
     */
    function tabtarget(id,type){
        //赋值appid
        $('#modal-appid').val(id);
        //步骤
        var index = 1;
        var flag = true;//是否能显示上线框
        //获取应用所处的步骤

        ajaxsync(ctx + "/console/app_action/"+ id,null,function(response){
            if(response.success){
                switch (response.data){
                    case 11: index = 2;break;   //选号
                    case 12: index = 3;break;   //支付
                    case 13: index = 2;break;   //支付返回选号
                    case 14: showtoast('应用已上线');flag = false;break;   //上线完成
                    case 21: index = 1;break;   //下线
                    default: index = 1;break;
                }
            }else{
                flag = false;
                showtoast(response.errorMsg?response.errorMsg:'数据异常，请稍后重试！');
            }
        },"get");

        if(flag){
            //进入实名认证
            if(index == 1){
                var realAuth = isRealAuth();
                if(realAuth != null){
                    if(realAuth){
                        $("div.real-auth").show();
                        $("div.not-real-auth").hide();
                    }else{
                        $("div.real-auth").hide();
                        $("div.not-real-auth").show();
                    }
                    //初始化
                    cleanModal(type);
                }else{
                    flag = false;
                }
            //进入选号
            }else if(index == 2){
                if(!creatIVR()){
                    flag = false;
                }
            }else if(index == 3){
                if(!getOrder()){
                    flag = false;
                }
            }
            //是否最后显示上线框
            if(flag){
                modalAction(index);
                showBox(type);
            }
        }

    }

    var ivrnumber = 1;
    //生成IVR
    function creatIVR(){
        var result = false;
        var appId = $('#modal-appid').val();
        $('.hideIVR').html('');
        $('#ownIvr').html('');
        var ivr = [];
        var ownIvr = [];
        //远端生成

        ajaxsync(ctx + "/console/app_action/select_ivr/" + appId,null,function(response){
            if(response.success && response.data != null ){
                ivr = response.data.selectIvr;
                ownIvr = response.data.ownIvr;
                result = true;
            }else{
                result = false;
                showtoast(response.errorMsg?response.errorMsg:'数据异常，请稍后重试！');
            }
        },"get");

        if(ownIvr.length > 0){
            $("#selectOwnIvr").show();
            $("#selectNewIvr").hide();
            for(var i = 0;i<ownIvr.length;i++){
                $('#ownIvr').append('<option value="'+ ownIvr[i]+'">'+ownIvr[i]+'</option>')
            }
            $('#creatIVR').html(ownIvr[0]);
        }else if(ivr.length > 0){
            $("#selectOwnIvr").hide();
            $("#selectNewIvr").show();
            for (var i = 0; i < ivr.length; i++) {
                $('.hideIVR').append('<sapn class="hideIVR-p-'+(i+1)+'">'+ivr[i]+'</sapn>');
                //赋值第一个
            }
            $('#creatIVR').html(ivr[0]);
        }else{
            $("#noLike").replaceWith('<div class="tips-error text-center" >IVR号码池异常，请联系客服</div>');
        }
        ivrnumber = 1;
        return result;
    }

    function nolike(){
        ivrnumber++;
        if(ivrnumber==6){
            ivrnumber = 1;
        }
        var ivr = $('.hideIVR-p-'+ivrnumber).html();
        $('#creatIVR').html(ivr);

    }


    /**
     * 应用id
     * status 该应用状态
     */
    function delapp(id,status){
        bootbox.setLocale("zh_CN");
        if(status==1){
            bootbox.alert("当前应用正在运营中，请将其下线后进行删除", function(result) {}); return;
        }
        if(status==2){
            bootbox.confirm("删除应用：将会使该操作即时生效，除非您非常清楚该操作带来的后续影响", function(result) {
                if(result){
                    ajaxsync(ctx + "/console/app/delete",{'id':id,'${_csrf.parameterName}':'${_csrf.token}'},function(response){
                        if(response.success){
                            $('#app-'+id).remove();
                            pageObj.totalCount--;
                            pageObj.initToTalPageCount();
                            if(pageObj.currentPageNo>pageObj.totalPageCount){
                                pageObj.currentPageNo = pageObj.totalPageCount;
                            }
                            showtoast("删除成功！",pageObj.pageUrl+"?pageNo="+pageObj.currentPageNo+"&pageSize="+pageObj.pageSize,1000);
                        }else{
                            showtoast("删除失败！" + response.errorMsg);
                        }
                    },"post");

                }else{
                    //showtoast('取消');
                }
            });
        }
    }


    //应用下线
    function offline(id,type){
        bootbox.setLocale("zh_CN");
        var h1="下线应用：将会使该操作即时生效，除非您非常清楚该操作带来的后续影响";
        if(type==1){
            h1 = "应用下线后，选择的功能服务将终止，IVR号码关联将解除，应用上线后需要重新选择绑定（应用下线不影响IVR号码的月租费的收取）";
        }
        bootbox.confirm(h1, function(result){
            if(result){

                ajaxsync(ctx + "/console/app_action/offline",{'appId':id,'${_csrf.parameterName}':'${_csrf.token}'},function(response){
                    if(response.success){
                        var isIvrService = response.data.isIvrService==1?1:0;
                        $('#trb-'+id).html('');
                        $('#statusapp-'+id).html('未上线').removeClass('success').addClass('nosuccess');
                        $('#trb-'+id).html('<a onclick="tabtarget(\''+id+'\',\''+ isIvrService +'\')">申请上线</a>');
                        showtoast('下线成功');
                    }else{
                        result = false;
                        showtoast(response.errorMsg?response.errorMsg:'数据异常，请稍后重试！');
                    }
                },"post");

            }else{
                showtoast('取消');
            }

        });
    }

    //获取订单（进入应用上线支付页面）
    function getOrder(){
        var result = false;
        var appId = $('#modal-appid').val();
        var ivr = $('#creatIVR').html();//当为创建支付订单时（Action），ivr取值有效，当为取出原有的订单时，ivr取值用数据库中的值（在后台中处理）

        ajaxsync(ctx + "/console/app_action/get_pay",{appId:appId,ivr:ivr},function(response){
            if(response.success && response.data.action != null && response.data.balance != null){
                $("#selectIvr").html(response.data.action.telNumber);
                $("#payAmount").html(response.data.action.amount.toFixed(2));
                $("#balance").html(response.data.balance.toFixed(2));
                if(response.data.action.amount > response.data.balance){
                    $(".nomoney").show();
                }else{
                    $(".nomoney").hide();
                }
                if(response.data.action.amount == 0){
                    $("#payMoneyInfo").hide();
                    $("#payButton").text("确定上线");
                }else{
                    $("#payMoneyInfo").show();
                    $("#payButton").text("确定支付");
                }
                result = true;
            }else{
                result = false;
                showtoast(response.errorMsg?response.errorMsg:'数据异常，请稍后重试！');
            }
        },"get");

        return result;
    }

    /**
     * 支付
     */
    function pay(){
        if(!$('#readbook').is(':checked')) {
            showtoast('请先阅读IVR协议');
            return false;
        }
        var result = false;
        var appId = $('#modal-appid').val();

        ajaxsync(ctx + "/console/app_action/pay",{appId:appId},function(response){
            if(!response.success){
                result = false;
                showtoast(response.errorMsg?response.errorMsg:'数据异常，请稍后重试！');
            }else{
                result = true;
            }
        },"get");

        return result;
    }

    function directOnline(){
        var result = false;
        var appId = $('#modal-appid').val();

        ajaxsync(ctx + "/console/app_action/direct_online",{appId:appId},function(response){
            if(!response.success){
                result = false;
                showtoast(response.errorMsg?response.errorMsg:'数据异常，请稍后重试！');
            }else{
                result = true;
            }
        },"get");

        return result;
    }

    //重选择事件
    function resetIVR(){
        var appId = $('#modal-appid').val();
        var flag = false;
        ajaxsync(ctx + "/console/app_action/reset_ivr",{appId:appId},function(response){
            if(!response.success){
                flag = false;
                showtoast(response.errorMsg?response.errorMsg:'数据异常，请稍后重试！');
            }else{
                flag = true;
            }
        },"get");

        if(flag){
            tabModalBtn(2,'creatIVR()');
        }
    }



    //监听支付状态 返回true success
    function syncpay(){
        return true;
    }

    $("#ownIvr").change(function(){
        $('#creatIVR').html($(this).val());
    })

    $("#refreshBalance").click(function(){
        ajaxsync(ctx + "/console/app_action/balance",null,function(response){
            if(response.success){
                var payAmount = $("#payAmount").html();
                var balance = response.data;
                if(Number(payAmount) > balance){
                    $(".nomoney").show();
                }else{
                    $(".nomoney").hide();
                }
                $("#balance").html(balance.toFixed(2));
            }else{
                showtoast(response.errorMsg?response.errorMsg:'数据异常，请稍后重试！');
            }
        },"get");
    })

</script>

</body>
</html>

