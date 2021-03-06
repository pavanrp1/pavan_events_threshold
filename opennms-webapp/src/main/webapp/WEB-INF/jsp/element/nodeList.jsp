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

<%@page language="java" contentType="text/html" session="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="element" tagdir="/WEB-INF/tags/element" %>

<c:if test="${model.nodeCount == 1}">
  <jsp:forward page="/element/node.jsp?node=${model.nodes[0].node.id}"/>
</c:if>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Node List" />
  <jsp:param name="headTitle" value="Node List" />
  <jsp:param name="location" value="nodelist" />
  <jsp:param name="breadcrumb" value="<a href ='element/index.jsp'>Search</a>"/>
  <jsp:param name="breadcrumb" value="Node List"/>
</jsp:include>

<c:choose>
  <c:when test="${command.listInterfaces}">
    <h3>Nodes and their interfaces</h3>
  </c:when>
  
  <c:otherwise>
    <h3>Nodes</h3>
  </c:otherwise>
</c:choose>

<div class="boxWrapper">
  <c:choose>
    <c:when test="${model.nodeCount == 0}">
      <p>
        None found.
      </p>
    </c:when>

    <c:otherwise>
      <div class="TwoColLeft">
        <element:nodelist nodes="${model.nodesLeft}" isIfAliasSearch="${command.ifAlias != null}" isMaclikeSearch="${command.maclike != null}"/>
      </div>
        
      <div class="TwoColRight">
        <element:nodelist nodes="${model.nodesRight}" isIfAliasSearch="${command.ifAlias != null}" isMaclikeSearch="${command.maclike != null}"/>
      </div>

      <div class="spacer"><!-- --></div>
    </c:otherwise>
  </c:choose>
</div>

<p>
  <c:choose>
    <c:when test="${model.nodeCount == 1}">
      <c:set var="nodePluralized" value="Node"/>
    </c:when>
    
    <c:otherwise>
      <c:set var="nodePluralized" value="Nodes"/>
    </c:otherwise>
  </c:choose>
  
  <c:choose>
    <c:when test="${model.interfaceCount == 1}">
      <c:set var="interfacePluralized" value="Interface"/>
    </c:when>
    
    <c:otherwise>
      <c:set var="interfacePluralized" value="Interfaces"/>
    </c:otherwise>
  </c:choose>
  
  <c:choose>
    <c:when test="${command.listInterfaces}">
      ${model.nodeCount} ${nodePluralized}, ${model.interfaceCount} ${interfacePluralized}
    </c:when>
    
    <c:otherwise>
      ${model.nodeCount} ${nodePluralized}
    </c:otherwise>
  </c:choose>

  <c:url var="thisURL" value="${relativeRequestPath}">
    <c:if test="${command.nodename != null}">
      <c:param name="nodename" value="${command.nodename}"/>
    </c:if>
    <c:if test="${command.iplike != null}">
      <c:param name="iplike" value="${command.iplike}"/>
    </c:if>
    <c:if test="${command.service != null}">
      <c:param name="service" value="${command.service}"/>
    </c:if>
    <c:if test="${command.ifAlias != null}">
      <c:param name="ifAlias" value="${command.ifAlias}"/>
    </c:if>
    <c:if test="${command.maclike != null}">
      <c:param name="maclike" value="${command.maclike}"/>
    </c:if>
    <c:if test="${command.category1 != null}">
      <c:forEach var="category" items="${command.category1}">
        <c:param name="category1" value="${category}"/>
      </c:forEach>
    </c:if>
    <c:if test="${command.category2 != null}">
      <c:forEach var="category" items="${command.category2}">
        <c:param name="category2" value="${category}"/>
      </c:forEach>
    </c:if>
    <c:if test="${command.statusViewName != null}">
      <c:param name="statusViewName" value="${command.statusViewName}"/>
    </c:if>
    <c:if test="${command.statusSite != null}">
      <c:param name="statusSite" value="${command.statusSite}"/>
    </c:if>
    <c:if test="${command.statusRowLabel != null}">
      <c:param name="statusRowLabel" value="${command.statusRowLabel}"/>
    </c:if>
    <c:if test="${command.nodesWithOutages}">
      <c:param name="nodesWithOutages" value="${command.nodesWithOutages}"/>
    </c:if>
    <c:if test="${command.nodesWithDownAggregateStatus}">
      <c:param name="nodesWithDownAggregateStatus" value="${command.nodesWithDownAggregateStatus}"/>
    </c:if>
    <c:if test="${!command.listInterfaces}">
      <c:param name="listInterfaces" value="${!command.listInterfaces}"/>
    </c:if>
  </c:url>
  
  <c:choose>
    <c:when test="${!command.listInterfaces}">
    <a href="${thisURL}">Show interfaces</a>
    </c:when>
    <c:otherwise>
    <a href="${thisURL}">Hide interfaces</a>
    </c:otherwise>
  </c:choose>
</p>

<jsp:include page="/includes/footer.jsp" flush="false"/>
