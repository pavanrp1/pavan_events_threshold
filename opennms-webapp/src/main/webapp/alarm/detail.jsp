<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Dev 12: Allow you to retry failed ticket create or close - jonathan@opennms.org
// 2008 Oct 04: Severity -> OnmsSeverity name change. - dj@opennms.org
// 2008 Sep 27: Use new Severity enum. - dj@opennms.org
// 2007 Feb 20: Make the style match that of the event page. - dj@opennms.org
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 2002 Jul 18: Added operinstructions to display.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.WebSecurityUtils,
			org.opennms.web.alarm.*,
			org.opennms.netmgt.model.OnmsAlarm,
	        org.opennms.web.acegisecurity.Authentication"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags/form" prefix="form" %>

<%!

	public String alarmTicketLink(Alarm alarm) {
    	String template = System.getProperty("opennms.alarmTroubleTicketLinkTemplate");
    	if (template == null) {
    	    return alarm.getTroubleTicket();
    	} else {
    	    return template.replaceAll("\\$\\{id\\}", alarm.getTroubleTicket());
    	}
	}

%>

<%
    String alarmIdString = request.getParameter( "id" );

    if( alarmIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "id" );
    }

    int alarmId = -1;

    try {
        alarmId = WebSecurityUtils.safeParseInt( alarmIdString );
    }
    catch( NumberFormatException e ) {
        throw new org.opennms.web.alarm.AlarmIdNotFoundException( "The alarm id must be an integer.", alarmIdString );
    }

    Alarm alarm = AlarmFactory.getAlarms( alarmId );

    if( alarm == null ) {
        throw new org.opennms.web.alarm.AlarmIdNotFoundException( "An alarm with this id was not found.", String.valueOf(alarmId) );
    }
    
    pageContext.setAttribute("alarm", alarm);
    
    String action = null;
    String ackButtonName=null;
    boolean showEscalate=false;
    boolean showClear=false;
    
    if (alarm.getAcknowledgeTime()==null)
    {
        ackButtonName = "Acknowledge";
        action = AcknowledgeAlarmServlet.ACKNOWLEDGE_ACTION;
    }
    else
    {
        ackButtonName = "Unacknowledge";
        action = AcknowledgeAlarmServlet.UNACKNOWLEDGE_ACTION;
    }
    
    String escalateAction = AlarmSeverityChangeServlet.ESCALATE_ACTION;
    String clearAction = AlarmSeverityChangeServlet.CLEAR_ACTION;
    if (alarm.getSeverity() == OnmsAlarm.CLEARED_SEVERITY || (alarm.getSeverity() > OnmsAlarm.NORMAL_SEVERITY && alarm.getSeverity() < OnmsAlarm.CRITICAL_SEVERITY)) {
    	showEscalate=true;
    }
    if  (alarm.getSeverity() >= OnmsAlarm.NORMAL_SEVERITY && alarm.getSeverity() <= OnmsAlarm.CRITICAL_SEVERITY) {
    	showClear=true;
    }
%>

<%@page import="org.opennms.core.resource.Vault"%>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Alarm Detail" />
  <jsp:param name="headTitle" value="Detail" />
  <jsp:param name="headTitle" value="Alarms" />
  <jsp:param name="breadcrumb" value="<a href='alarm/index.jsp'>Alarms</a>" />
  <jsp:param name="breadcrumb" value="Detail" />
</jsp:include>

      <h3>Alarm <%=alarm.getId()%></h3>
 
      <table>
        <tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
          <th width="10%">Severity</th>
          <td class="divider"><%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%></td>
          <th width="10%">Node</th>
          <td class="divider">
            <% if( alarm.getNodeId() > 0 ) { %>
              <a href="element/node.jsp?node=<%=alarm.getNodeId()%>"><%=alarm.getNodeLabel()%></a>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <th width="10%">Acknowledged&nbsp;By</th>
          <td class="divider"><%=alarm.getAcknowledgeUser()!=null ? alarm.getAcknowledgeUser() : "&nbsp"%></td>
        </tr>
        <tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
          <th>Last Event</th>
          <td><span title="Event <%= alarm.getLastEventID() %>"><a href="event/detail.jsp?id=<%= alarm.getLastEventID() %>"><%=org.opennms.netmgt.EventConstants.formatToUIString(alarm.getLastEventTime())%></a></span></td>
          <th>Interface</th>
          <td>
            <% if( alarm.getIpAddress() != null ) { %>
              <% if( alarm.getNodeId() > 0 ) { %>
                <a href="element/interface.jsp?node=<%=alarm.getNodeId()%>&intf=<%=alarm.getIpAddress()%>"><%=alarm.getIpAddress()%></a>
              <% } else { %>
                <%=alarm.getIpAddress()%>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <th>Time&nbsp;Acknowledged</th>
          <td><%=alarm.getAcknowledgeTime()!=null ? org.opennms.netmgt.EventConstants.formatToUIString(alarm.getAcknowledgeTime()) : "&nbsp"%></td>
        </tr>
        <tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
          <th>First Event</th>
          <td><%=org.opennms.netmgt.EventConstants.formatToUIString(alarm.getFirstEventTime())%></td>
          <th>Service</th>
          <td>
            <% if( alarm.getServiceName() != null ) { %>
              <% if( alarm.getIpAddress() != null && alarm.getNodeId() > 0 ) { %>
                <a href="element/service.jsp?node=<%=alarm.getNodeId()%>&intf=<%=alarm.getIpAddress()%>&service=<%=alarm.getServiceId()%>"><%=alarm.getServiceName()%></a>
              <% } else { %>
                <%=alarm.getServiceName()%>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <th>Ticket ID</th>
          <td><% if (alarm.getTroubleTicket() == null) { %>
                &nbsp;
              <% } else { %>
            	<%= alarmTicketLink(alarm) %> 
              <% } %>
          </td>
          </tr> 
          <tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
          	<th>Count</th>
	        <td><%=alarm.getCount()%></td>
          	<th>UEI</th>
          	<td>
          	<% if( alarm.getUei() != null ) { %>
          	      <%=alarm.getUei()%>
          	<% } else {%>
                      &nbsp;
          	<% } %>
		</td>
          <th>Ticket State</th>
          <td><% if (alarm.getTroubleTicketState() == null) { %>
                &nbsp;
              <% } else { %>
            	<%= alarm.getTroubleTicketState() %> 
              <% } %>
          </td>
        </tr>
        <tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
          	<th>Reduct. Key</th>
          	<td colspan="5">
          	<% if( alarm.getReductionKey() != null ) { %>
          	      <%=alarm.getReductionKey()%>
          	<% } else {%>
                      &nbsp;
          	<% } %>
		</td>
        </tr>
      </table>

      <table>
	<tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
          <th>Log Message</th>
        </tr>
	<tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
          <td><%=alarm.getLogMessage()%></td>
        </tr>
      </table>

      <table>
	<tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
          <th>Description</th>
        </tr>
	<tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
          <td><%=alarm.getDescription()%></td>
        </tr>
      </table>
      
      
      <table>
	<tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
          <th>Operator Instructions</th>
        </tr>
	
	<tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
          <td>
	    <%if (alarm.getOperatorInstruction()==null) { %>
              No instructions available
            <% } else { %>
              <%=alarm.getOperatorInstruction()%>
            <% } %>
	  </td>
        </tr>
      </table>

<% if( !(request.isUserInRole( Authentication.READONLY_ROLE ))) {     %>
      <table>
      <tbody>
      <tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
      <th colspan="2">Acknowledgement and Severity Actions</th>
      </tr>
      <tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
      <td>
      <form method="post" action="alarm/acknowledge">
        <input type="hidden" name="actionCode" value="<%=action%>" />
        <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
        <input type="hidden" name="redirect" value="<%=request.getContextPath() + request.getServletPath() + "?" + request.getQueryString()%>" />
        <input type="submit" value="<%=ackButtonName%>" />
      </form>
      </td>
      <td><%=ackButtonName%> this alarm</td>
      </tr>
      
      <%if (showEscalate || showClear) { %>
      <tr class="<%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%>">
      <td>
      <form method="post" action="alarm/changeSeverity">
	  <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
      <input type="hidden" name="redirect" value="<%=request.getContextPath() + request.getServletPath() + "?" + request.getQueryString()%>" />	  
      <select name="actionCode">
      	  <%if (showEscalate) { %>
          <option value="<%=escalateAction%>">Escalate</option>
          <% } %>
      	  <%if (showClear) { %>
          <option value="<%=clearAction%>">Clear</option>
          <% } %>
      </select>
	  <input type="submit" value="Go"/>
      </form>
      </td>
      <td>
      <%if (showEscalate) { %>
      Escalate
      <% } %>
      <%if (showEscalate && showClear) { %>
      or
      <% } %>
      <%if (showClear) { %>
      Clear
      <% } %>
      this alarm
      </td>
      </tr>
      <% } // showEscalate || showClear %>      
      </tbody>
      </table>
      
      <br/>
      
      <% if ("true".equalsIgnoreCase(Vault.getProperty("opennms.alarmTroubleTicketEnabled"))) { %>

      <form method="post" action="alarm/ticket/create.htm">
        <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
        <input type="hidden" name="redirect" value="<%=request.getServletPath() + "?" + request.getQueryString()%>" />
        <form:input type="submit" value="Create Ticket" disabled="${(!empty alarm.troubleTicketState) && (alarm.troubleTicketState != 'CREATE_FAILED')}" />
      </form>

      <form method="post" action="alarm/ticket/update.htm">
        <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
        <input type="hidden" name="redirect" value="<%=request.getServletPath() + "?" + request.getQueryString()%>" />
        <form:input type="submit" value="Update Ticket" disabled="${(empty alarm.troubleTicket)}"/>
      </form>

      <form method="post" action="alarm/ticket/close.htm">
        <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
        <input type="hidden" name="redirect" value="<%=request.getServletPath() + "?" + request.getQueryString()%>" />
        <form:input type="submit" value="Close Ticket" disabled="${(empty alarm.troubleTicketState) || ((alarm.troubleTicketState != 'OPEN') && (alarm.troubleTicketState != 'CLOSE_FAILED')) }" />
      </form>
      
      <% } // alarmTroubleTicketEnabled %>
      <% } // isUserInRole%>

<jsp:include page="/includes/footer.jsp" flush="false" />
