<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=utf-8"/>
    <title>Book Detail</title>
    <%--<link href="../css/bootstrap.min.css" type="text/css" rel="stylesheet"/>--%>
    <%--<script src="../js/jquery.min.js" type="text/javascript"></script>--%>
    <%--<script src="../js/bootstrap.min.js" type="text/jscript"></script>--%>
</head>
<body>

<br/>
<div class="box-123">
    <table class="table table-bordered">
        <tr>
            <th>图书详情</th>
            <th>..</th>
        </tr>
        <tbody id="cityCols">
        <tr>
            <td>图书id</td>
            <td>${book.bookId}</td>
        </tr>
        <tr>
            <td>书名</td>
            <td>${book.name}</td>
        </tr>
        <tr>
            <td>库存</td>
            <td>${book.number}</td>
        </tr>
        <tr>
            <td>简介</td>
            <td>这里是简介</td>
        </tr>
        <tr>
            <td>出版社</td>
            <td>这里是出版社</td>
        </tr>
        </tbody>
    </table>
</div>
</body>
</html>
