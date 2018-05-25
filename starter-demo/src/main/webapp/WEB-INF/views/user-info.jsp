<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>user info</title>
</head>
<body>
<c:forEach items="${userList}" var="user">
    ${user.name} -  ${user.age}
    <br/>
</c:forEach>

<hr>

${userList}
</body>
</html>
