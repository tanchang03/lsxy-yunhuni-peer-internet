<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@include file="/inc/import.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <%@include file="/inc/meta.jsp" %>
</head>
<body>
<section class="vbox">
    <%@include file="/inc/headerNav.jsp"%>
    <section class='aside-section'>
        <section class="hbox stretch">
            <!-- .aside -->
            <aside class="bg-Green  aside hidden-print "  id="nav"><%@include file="/inc/leftMenu.jsp"%></aside>
            <!-- /.aside -->

        <section id="content">
            <section class="hbox stretch">
                <aside>
                    <section class="vbox xbox">
                        <!-- 如果没有三级导航 这段代码注释-->
                        <!--<div class="head-box"><a href="#subNav" data-toggle="class:hide"> <i
                                class="fa fa-angle-left text"></i> <i class="fa fa-angle-right text-active"></i> </a>
                        </div>-->
                        <div class="wrapper header">
                            <span class="border-left">&nbsp;消息列表</span>
                        </div>
                        <section class="scrollable wrapper w-f">

                            <section class="panel panel-default pos-rlt clearfix ">
                                <table class="table table-striped cost-table-history">
                                    <thead>
                                    <tr>
                                        <th>消息时间</th>
                                        <th>消息内容</th>
                                        <th>操作</th>
                                    </tr>
                                    </thead>
                                    <tbody>

                                    <tr>
                                        <td>2016-01-01</td>
                                        <td> 注册用户：【15648569451】，您的企业认证审核已通过，已获得开发者权限，试着上线应用吧！</td>
                                        <td><a href="">删除</a></td>
                                    </tr>
                                    <tr>
                                        <td>2016-01-02</td>
                                        <td> 注册用户：【15648569451】，您的放音文件审核已通过，快去查看吧！</td>
                                        <td><a href="">删除</a></td>
                                    </tr>
                                    <tr>
                                        <td>2016-01-03</td>
                                        <td> 注册用户：【15648569451】，您的企业认证审核已通过，已获得开发者权限，试着上线应用吧！</td>
                                        <td><a href="">删除</a></td>
                                    </tr>
                                    <tr>
                                        <td>2016-01-01</td>
                                        <td>  注册用户：【15648569451】，您的开具发票申请已受理完成，发票已由专门的快递送出，请耐心等候！</td>
                                        <td><a href="">删除</a></td>
                                    </tr>
                                    <tr>
                                        <td>2016-01-01</td>
                                        <td> 注册用户：【15648569451】，您的账号余额已不足10元，可能将要面临收费功能不可用的情况，请及时充值吧！</td>
                                        <td><a href="">删除</a></td>
                                    </tr>
                                    <tr>
                                        <td>2016-01-01</td>
                                        <td> 注册用户：【15648569451】，您的反馈意见已飞速到达我们的产品研发部门，感谢您提供的宝贵意见！</td>
                                        <td><a href="">删除</a></td>
                                    </tr>
                                    </tbody>
                                </table>
                            </section>
                            <section class="panel panel-default yunhuni-personal">
                                <nav class='pageWrap'>
                                    <ul class="pagination">
                                        <li>
                                            <a href="#" aria-label="Previous">
                                                <span aria-hidden="true">&laquo;</span>
                                            </a>
                                        </li>
                                        <li class="active"><a href="#">1</a></li>
                                        <li><a href="#">2</a></li>
                                        <li><a href="#">3</a></li>
                                        <li><a href="#">4</a></li>
                                        <li><a href="#">5</a></li>
                                        <li>
                                            <a href="#" aria-label="Next">
                                                <span aria-hidden="true">&raquo;</span>
                                            </a>
                                        </li>
                                    </ul>
                                </nav>
                            </section>

                        </section>
                    </section>
                </aside>
            </section>
            <a href="#" class="hide nav-off-screen-block" data-toggle="class:nav-off-screen" data-target="#nav"></a>
        </section>
    </section>
</section>





<script src="${resPrefixUrl }/js/app.v2.js"></script> <!-- Bootstrap --> <!-- App -->
<script src="${resPrefixUrl }/js/charts/flot/jquery.flot.min.js" cache="false"></script>
<script src="${resPrefixUrl }/js/bootbox.min.js"></script>
<script src="${resPrefixUrl }/js/charts/flot/demo.js" cache="false"></script>



<script src="${resPrefixUrl }/bower_components/bootstrapvalidator/dist/js/bootstrapValidator.min.js"></script>
<script type="text/javascript" src='${resPrefixUrl }/js/bootstrap-datepicker/js/bootstrap-datepicker.js'> </script>
<script type="text/javascript" src='${resPrefixUrl }/js/bootstrap-datepicker/locales/bootstrap-datepicker.zh-CN.min.js'> </script>
<script type="text/javascript" src='${resPrefixUrl }/js/cost/history.js'> </script>
</body>
</html>

