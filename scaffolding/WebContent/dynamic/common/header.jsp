
<%@page import="org.romaframework.core.*"%>
<%@page import="org.romaframework.aspect.session.*"%>
<%@page import="org.romaframework.aspect.i18n.*"%>
<%@page import="org.romaframework.core.config.ApplicationConfiguration"%>
<%@page import="org.romaframework.aspect.authentication.AuthenticationAspect"%>

<%
	String appName = Utility.getCapitalizedString(Roma.component(ApplicationConfiguration.class).getApplicationName());
	SessionInfo sess = Roma.session().getActiveSessionInfo();
%>
<table width='100%'>
	<tr>
		<td>
		<h1><a href='<%=request.getContextPath()%>/app/direct/home'><%=appName%></a></h1>
		</td>
		<td align="right">
		<table>
			<tr>
				<td>User:</td>
				<td><a
					href='<%=request.getContextPath()%>/app/direct/changePassword'><%=sess.getAccount()%></a></td>
			</tr>
			<tr>
				<td>Logged On:</td>
				<td><%=Roma.i18n().getDateTimeFormat().format(sess.getCreated())%></td>
			</tr>
		</table>
		</td>
		<td align="right"><a
			href='<%=request.getContextPath()%>/dynamic/common/logout.jsp'><img
			src='<%=request.getContextPath()%>/static/base/image/logout.png'
			border='0' /></a></td>
	</tr>
</table>