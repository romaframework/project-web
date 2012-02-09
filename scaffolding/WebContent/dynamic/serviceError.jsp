<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@page import="org.romaframework.core.flow.*"%>
<%@page import="org.romaframework.aspect.session.*"%>
<%@page import="org.romaframework.aspect.i18n.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<style type="text/css">
.default {
	font-family: trebuchet ms;
	font-size: 24;
}
</style>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Error on service execution</title>
</head>
<body>
<br />

<p class="default" align="center"><%=session.getAttribute("ErrorMessage")%><br />
<br />
<a href="../../app">HomePage</a></p>

</body>
</html>
