<!-- This page is only for forwarding all root URL requests to home page -->
<%@page language="java" %> 
<%
    String redirectURL = "/home";
    response.sendRedirect(redirectURL);
%>