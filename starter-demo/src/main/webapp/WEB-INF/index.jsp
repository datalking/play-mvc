<!-- This page is only for forwarding all root URL requests to controller page -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String redirectURL = "/controller";
    response.sendRedirect(redirectURL);
%>
