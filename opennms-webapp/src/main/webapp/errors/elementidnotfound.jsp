<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
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
	isErrorPage="true"
	import="org.opennms.web.element.*"
%>

<%
     ElementIdNotFoundException einfe = null;
    
    if( exception instanceof ElementIdNotFoundException ) {
        einfe = (ElementIdNotFoundException)exception;
    }
    else if( exception instanceof ServletException ) {
        einfe = (ElementIdNotFoundException)((ServletException)exception).getRootCause();
    }
    else {
        throw new ServletException( "This error page does not handle this exception type.", exception );
    }
    
%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="headTitle" value="ID Not Found for <%=einfe.getElemType()%>" />
  <jsp:param name="headTitle" value="Error" />
  <jsp:param name="breadcrumb" value="Error" />
</jsp:include>

<h1><%=einfe.getElemType(true)%> ID Not Found</h1>

<p>
  The <%=einfe.getElemType()%> ID <%=einfe.getBadID()%> is invalid. <%=einfe.getMessage()%>
  <br/>
  <% if (einfe.getDetailUri() != null) { %>
  <p>
  To search again by <%=einfe.getElemType()%> ID, enter the ID here:
  </p>
  <form method="get" action="<%=einfe.getDetailUri()%>">
  <p>
    Get&nbsp;details&nbsp;for&nbsp;<%=einfe.getElemType()%>&nbsp;ID:
    <br/>
    <input type="text" name="<%=einfe.getDetailParam()%>"/>
    <input type="submit" value="Search"/>
  </p>
  </form>
  <% } %>
  
  <% if (einfe.getBrowseUri() != null) { %>
  <p>
  To find the <%=einfe.getElemType()%> you are looking for, you can
  browse the <a href="<%=einfe.getBrowseUri()%>"><%=einfe.getElemType()%> list</a>.
  </p>
  <% } %>
</p>

<jsp:include page="/includes/footer.jsp" flush="false" />