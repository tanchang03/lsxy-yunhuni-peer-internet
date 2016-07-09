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
            <aside class="bg-Green lter aside hidden-print"  id="nav"><%@include file="/inc/leftMenu.jsp"%></aside>
            <!-- /.aside -->


            <section id="content">
            <section class="hbox stretch">
                <!-- 如果没有三级导航 这段代码注释-->

                <!--<aside class="bg-green lter aside-sm hidden-print ybox" id="subNav">
                    <section class="vbox">
                        <div class="wrapper header"><span class="margin_lr"></span><span class="margin_lr border-left">&nbsp;消费管理</span>
                        </div>
                        <section class="scrollable">
                            <div class="slim-scroll">
                                <nav class="hidden-xs">
                                    <ul class="nav">
                                        <li>
                                            <div class="aside-li-a active">
                                                <a href="statistics_consume.html">消费统计</a>
                                            </div>
                                        </li>
                                    </ul>
                                </nav>
                            </div>
                        </section>
                    </section>
                </aside>-->
                <aside>
                    <section class="vbox xbox">
                        <!-- 如果没有三级导航 这段代码注释-->
                        <!--<div class="head-box"><a href="#subNav" data-toggle="class:hide"> <i
                                class="fa fa-angle-left text"></i> <i class="fa fa-angle-right text-active"></i> </a>
                        </div>-->
                        <div class="wrapper header">
                            <span class="border-left">&nbsp;消费统计</span>
                        </div>
                        <section class="scrollable wrapper w-f">
                            <section class="panel panel-default yunhuni-personal">
                                <div class="row m-l-none m-r-none bg-light lter">
                                    <div class="col-md-2 col-md-offset-9 padder-v fix-padding">
                                        <select   class="form-control myselectapp">
                                            <option value="0">全部应用</option>
                                            <c:forEach items="${appList}" var="app">
                                                <option value="${app.id}">${app.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                            </section>



                            <section class="panel panel-default pos-rlt clearfix ">
                                <div class="row">
                                    <div class="col-md-12">
                                        <input type="radio" name="stime" value="month" class="selectdata" checked/>日统计
                                        <input type="radio" name="stime" value="year" class="selectdata" />月统计
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-12 text-center">
                                        <p class="font18">消费额统计</p>
                                    </div>
                                </div>
                                <!--日统计-->
                                <div class="row monthform" >
                                    <div class="col-md-12">
                                        <input type="text" class="datepicker currentMonth form-control date_block monthstart" value="${consumeStatisticsVo.startTime}" />
                                        <span class="monthcontrast"><span>对比</span><input type="text" class="datepicker currentMonth form-control date_block monthend" /></span>
                                        <button class="btn btn-primary finddatebtn" data-id="month" >查询</button>
                                        <button class="btn btn-default compassbtn monthcbtn" data-id="month">对比</button>
                                        <span class="tips-error monthtips"></span>
                                    </div>
                                </div>

                                <!--月统计-->
                                <div class="row yearform" >
                                    <div class="col-md-12">
                                        <input type="text" class="datepicker  form-control currentYear date_block yearstart" value="2016"  />
                                        <span class="yearcontrast"><span>对比</span><input type="text" class="datepicker currentYear form-control date_block yearend" /></span>
                                        <button class="btn btn-default finddatebtn " data-id="year" >查询</button>
                                        <button class="btn btn-default compassbtn yearcbtn" data-id="year">对比</button>
                                        <span class="tips-error yeartips"></span>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-12 scanvas" >
                                        <!--统计列表-->
                                        <div class="ecpanel" id="ecpanel" style=" height: 500px;  "></div>
                                    </div>
                                </div>
                            </section>



                            <section class="panel panel-default pos-rlt clearfix ">
                                <div class="form-group">
                                    <span class="hr text-label"><strong>详情数据</strong></span>
                                </div>
                                <table class="table table-striped" >
                                    <thead>
                                    <tr>
                                        <th>日期</th>
                                        <th>消费（元）</th>
                                    </tr>
                                    </thead>
                                    <tbody id="tableModal">

                                    </tbody>
                                </table>
                            </section>

                            <section class="panel panel-default yunhuni-personal">
                                <div id="datatablepage"></div>
                            </section>

                        </section>
                    </section>
                </aside>
            </section>
            <a href="#" class="hide nav-off-screen-block" data-toggle="class:nav-off-screen" data-target="#nav"></a>
        </section>
    </section>
</section>



<%@include file="/inc/footer.jsp"%>
<script type="text/javascript" src='${resPrefixUrl }/js/bootstrap-datepicker/js/bootstrap-datepicker.js'> </script>
<script type="text/javascript" src='${resPrefixUrl }/js/bootstrap-datepicker/locales/bootstrap-datepicker.zh-CN.min.js'> </script>
<!--统计插件-->
<script src="${resPrefixUrl }/js/echarts.min.js" ></script>
<!--统计插件主题-->
<script src="${resPrefixUrl }/js/echartstheme/wonderland.js" ></script>
<!--syncpage-->
<script type="text/javascript" src='${resPrefixUrl }/js/page.js'></script>

<script>


    $('input[name="stime"]').click(function(){
        //参数
        var v = $(this).val();
        if(v=='year'){
            $('.yearform').show();
            $('.monthform').hide();
        }else{
            $('.monthform').show();
            $('.yearform').hide();s
        }
        initchart();
    });

    //查询
    $('.finddatebtn').click(function(){
        var id = $(this).attr('data-id');
        var tipsclass  = '.'+id+'tips';
        $(tipsclass).html('');
        var starttime =  $('.'+id+'start').val();
        var endtime = '';
        if(starttime==''){
            tips(tipsclass,'请先填写时间'); return;
        }
        if($('.'+id+'cbtn').html()=='取消对比'){
            endtime =  $('.'+id+'end').val();
            if(endtime==''){
                tips(tipsclass,'请填写对比时间'); return;
            }
        }
        initchart();
    });



    function tips(tipsclass,tips){
        $(tipsclass).html(tips);
    }

    //对比
    $('.compassbtn').click(function(){
        var id = $(this).attr('data-id');
        var val = $(this).html();
        $('.'+id+'tips').html('');
        if(val=='对比'){
            $('.'+id+'contrast').show();
            //$('.'+id+'end').val('');
            $(this).html('取消对比');
        }else{
            $('.'+id+'contrast').hide();
            $(this).html('对比');
        }
    });

    //应用
    $('.myselectapp').change(function(){
        initchart();
    });


    //初始时间
    function initialStartTime(id){
        var starttime =  $('.'+id+'start').val();
        var endtime = '';
        if(starttime==''){
            starttime = $('.'+id+'start').attr('data-time');
        }
        if($('.'+id+'cbtn').html()=='取消对比'){
            endtime =  $('.'+id+'end').val();
            if(endtime==''){

            }
        }
        return starttime;
    }


    //初始结束时间
    function initialEndTime(id){
        var endtime = '';
        if($('.'+id+'cbtn').html()=='取消对比'){
            endtime =  $('.'+id+'end').val();
        }
        return endtime;
    }



    //触发函数
    /**
     * starttime 时间
     * endtime 对比时间
     */
    function initchart(){
        var type = $('input[name="stime"]:checked').val();
        var type1 = 'day';
        if(type=='year'){type1='month'}
        var app = $('.myselectapp').val();
        var starttime = initialStartTime(type);
        var endtime = initialEndTime(type);
        $.ajax({
            url : "${ctx}/console/statistics/consume/list",
            type : 'post',
            async: true,//使用false同步的方式,true为异步方式
            data : {'type':type1,'appId':app,'startTime':starttime,'endTime':endtime, '${_csrf.parameterName}':'${_csrf.token}'},//这里使用json对象
            dataType: "json",
            success : function(data){
                resultData = data;
                var tdata = new Array();
                var count = 0;
                for(var i=0;i<resultData.length;i++){
                    tdata[i]=resultData[i].name;
                    count+=resultData[i].data.length;
                }
                var seriesjson=JSON.stringify(resultData);
                seriesjson = eval('('+seriesjson+')');
                charts(tdata,seriesjson,type);
                updatetable(count);
            },
            fail:function(){
            }
        });
    }

    var timeData  = ['一月','二月','三月','四月','五月','六月','七月','八月','九月','十月','十一月','十二月']
    var monthData = ['1日','2日','3日','4日','5日','6日','7日','8日','9日','10日','11日','12日','13日','14日','15日','16日','17日','18日','19日','20日','21日','22日','23日','24日','25日','26日','27日','28日','29日','30日','31日'];

    /**
     * 回调数据
     * @param tdata 标题项
     * @param tdata 标题项
     * @param tdata 标题项
     */
    function charts(tdata,series,type){
        var Xdata = monthData;
        if(type=='year'){
            Xdata = timeData;
        }
        var myChart = echarts.init(document.getElementById('ecpanel'),'wonderland');
        option = {
            title: {
                text: '',
                subtext:'金额（元）'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data:tdata
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            toolbox: {
                feature: {
                    saveAsImage: {}
                }
            },
            xAxis: {
                type: 'category',
                boundaryGap: true,
                data: Xdata
            },
            yAxis: {
                type: 'value'
            },
            series: series
        };

        myChart.setOption(option);
    }



    function updatetable(count){
        var html ='';
        html += '<table class="cost-table table table-striped sync-table-box" ><thead><th>日期</th><th>消费金额（元）</th></thead></table>';
        html +='';
        //获取数据总数
        //var count = 1;
        //每页显示数量
        var listRow = 70;
        //显示多少个分页按钮
        var showPageCount = 5;
        //指定id，创建分页标签
        var pageId = 'datatablepage';
        //searchTable 为方法名
        var page = new Page(count,listRow,showPageCount,pageId,searchTable);
        page.show();

    }


    /**
     * 分页回调方法
     * @param nowPage 当前页数
     * @param listRows 每页显示多少条数据
     * */
    var searchTable = function(nowPage,listRows)
    {
        var type = $('input[name="stime"]:checked').val();
        var type1 = 'day';
        if(type=='year'){type1='month'}
        var app = $('.myselectapp').val();
        var starttime = initialStartTime(type);
        var endtime = initialEndTime(type);
        $.ajax({
            url : "${ctx}/console/statistics/consume/page_list",
            type : 'post',
            async: true,//使用false同步的方式,true为异步方式
            data : {'type':type1,'appId':app,'startTime':starttime,'endTime':endtime,'pageNo':nowPage,'pageSize':listRows,'${_csrf.parameterName}':'${_csrf.token}'},//这里使用json对象
            dataType: "json",
            success : function(resultData){
                var data =[];
                for(var i=0;i<resultData.length;i++){
                    var tempData = new Date(resultData[i].dt);
                    var temp = [tempData.getFullYear()+"-"+(tempData.getMonth()+1)+"-"+tempData.getDate(),resultData[i].sumAmount];
                    data[i]=temp;
                }
                //var seriesjson=JSON.stringify(resultData);
                //seriesjson = eval('('+seriesjson+')');
                i++;
                var html ='';
                //数据列表
                for(var i = 0 ; i<data.length; i++){
                    html +='<tr><td>'+data[i][0]+'</td><td>'+data[i][1]+'</td></tr>';
                }
                $('#tableModal').find("tr").remove();
                $('#tableModal').append(html);
            },
            fail:function(){
            }
        });
    }




</script>


<!--must-->
<script type="text/javascript" src='${resPrefixUrl }/js/statistics/consume.js'> </script>
</body>
</html>
