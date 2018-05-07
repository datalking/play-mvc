<!-- This page is only for forwarding all root URL requests to controller page -->
<%@page language="java" %>
<%
    String redirectURL = "/controller";
    response.sendRedirect(redirectURL);
%>
