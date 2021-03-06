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
// 2007 Aug 15: combined node and domain reports into resource reports
// 2006 Mar 08: added standard and custom domain reports
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
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
//      http://www.opennms.com/
//

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="
        org.opennms.web.svclayer.ResourceService,
		org.springframework.web.context.WebApplicationContext,
      	org.springframework.web.context.support.WebApplicationContextUtils
	"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%!
    public ResourceService m_resourceService;

    public void init() throws ServletException { 
	    WebApplicationContext m_webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
	    m_resourceService = (ResourceService) m_webAppContext.getBean("resourceService", ResourceService.class);
    }
%>

<%
    pageContext.setAttribute("topLevelResources", m_resourceService.findTopLevelResources());
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Resource Graphs" />
  <jsp:param name="headTitle" value="Resource Graphs" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="location" value="performance" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="Resource Graphs" />
</jsp:include>

<script language="Javascript" type="text/javascript" >
  function validateResource()
  {
      var isChecked = false
      for( i = 0; i < document.choose_resource.parentResourceId.length; i++ )
      {
         //make sure something is checked before proceeding
         if (document.choose_resource.parentResourceId[i].selected)
         {
            isChecked=true;
         }
      }

      if (!isChecked)
      {
          alert("Please check the resource that you would like to report on.");
      }
      return isChecked;
  }

  function validateResourceAdhoc()
  {
      var isChecked = false
      for( i = 0; i < document.choose_resource_adhoc.parentResourceId.length; i++ )
      {
         //make sure something is checked before proceeding
         if (document.choose_resource_adhoc.parentResourceId[i].selected)
         {
            isChecked=true;
         }
      }

      if (!isChecked)
      {
          alert("Please check the resource that you would like to report on.");
      }
      return isChecked;
  }

  function submitResourceForm()
  {
      if (validateResource())
      {
          document.choose_resource.submit();
      }
  }

  function submitResourceFormAdhoc()
  {
      if (validateResourceAdhoc())
      {
          document.choose_resource_adhoc.submit();
      }
  }

</script>

<div class="TwoColLeft">
  <div class="boxWrapper">
    <h3>Standard Resource<br>Performance Reports</h3>

    <p>
      Choose a resource for a standard performance report.
    </p>

    <form method="get" name="choose_resource" action="graph/chooseresource.htm">
      <input type="hidden" name="reports" value="all" />

      <select style="width: 100%;" name="parentResourceId" size="10">
        <c:forEach var="resource" items="${topLevelResources}">
          <option value="${resource.id}">${resource.resourceType.label}: ${resource.label}</option>
        </c:forEach>
      </select>

      <br/>
      <br/>

      <input type="button" value="Start" onclick="submitResourceForm()"/>
    </form>
  </div>
  <div class="boxWrapper">

    <h3>Custom Resource<br>Performance Reports</h3>

    <p>
      Choose a resource for a custom performance report.
    </p>

    <form method="get" name="choose_resource_adhoc" action="graph/chooseresource.htm">
      <input type="hidden" name="endUrl" value="graph/adhoc2.jsp"/>
      <select style="width: 100%;" name="parentResourceId" size="10">
        <c:forEach var="resource" items="${topLevelResources}">
          <option value="${resource.id}">${resource.resourceType.label}: ${resource.label}</option>
        </c:forEach>
      </select>

      <br/>
      <br/>

      <input type="button" value="Start" onclick="submitResourceFormAdhoc()"/>
    </form>
  </div>
</div>

<div class="TwoColRight">
  <div class="boxWrapper">
    <h3 align=center>Network Performance Data</h3>

    <p>
      The <strong>Standard Performance Reports</strong> provide a stock way to
      easily visualize the critical SNMP data collected from managed nodes
      and interfaces throughout your network.
    </p>

    <p>
      <strong>Custom Performance Reports</strong> can be used to produce a
      single graph that contains the data of your choice from a single
      interface or node.  You can select the timeframe, line colors, line
       styles, and title of the graph and you can bookmark the results.
    </p>
  </div>
</div>

<jsp:include page="/includes/footer.jsp" flush="false"/>
