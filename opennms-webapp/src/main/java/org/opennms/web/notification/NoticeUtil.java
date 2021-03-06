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
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.notification;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.opennms.web.WebSecurityUtils;

/**
 * <p>Abstract NoticeUtil class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public abstract class NoticeUtil extends Object {
    /** Constant <code>sortStylesString</code> */
    protected static final Map<String, NoticeFactory.SortStyle> sortStylesString;
    /** Constant <code>sortStyles</code> */
    protected static final Map<NoticeFactory.SortStyle, String> sortStyles;

    /** Constant <code>ackTypesString</code> */
    protected static final Map<String, NoticeFactory.AcknowledgeType> ackTypesString;
    /** Constant <code>ackTypes</code> */
    protected static final Map<NoticeFactory.AcknowledgeType, String> ackTypes;

    static {
        sortStylesString = new HashMap<String, NoticeFactory.SortStyle>();
        sortStylesString.put("user", NoticeFactory.SortStyle.USER);
        sortStylesString.put("responder", NoticeFactory.SortStyle.RESPONDER);
        sortStylesString.put("pagetime", NoticeFactory.SortStyle.PAGETIME);
        sortStylesString.put("respondtime", NoticeFactory.SortStyle.RESPONDTIME);
        sortStylesString.put("node", NoticeFactory.SortStyle.NODE);
        sortStylesString.put("interface", NoticeFactory.SortStyle.INTERFACE);
        sortStylesString.put("service", NoticeFactory.SortStyle.SERVICE);
        sortStylesString.put("id", NoticeFactory.SortStyle.ID);
        sortStylesString.put("rev_user", NoticeFactory.SortStyle.REVERSE_USER);
        sortStylesString.put("rev_responder", NoticeFactory.SortStyle.REVERSE_RESPONDER);
        sortStylesString.put("rev_pagetime", NoticeFactory.SortStyle.REVERSE_PAGETIME);
        sortStylesString.put("rev_respondtime", NoticeFactory.SortStyle.REVERSE_RESPONDTIME);
        sortStylesString.put("rev_node", NoticeFactory.SortStyle.REVERSE_NODE);
        sortStylesString.put("rev_interface", NoticeFactory.SortStyle.REVERSE_INTERFACE);
        sortStylesString.put("rev_service", NoticeFactory.SortStyle.REVERSE_SERVICE);
        sortStylesString.put("rev_id", NoticeFactory.SortStyle.REVERSE_ID);
        
        sortStyles = new HashMap<NoticeFactory.SortStyle, String>();
        sortStyles.put(NoticeFactory.SortStyle.USER, "user");
        sortStyles.put(NoticeFactory.SortStyle.RESPONDER, "responder");
        sortStyles.put(NoticeFactory.SortStyle.PAGETIME, "pagetime");
        sortStyles.put(NoticeFactory.SortStyle.RESPONDTIME, "respondtime");
        sortStyles.put(NoticeFactory.SortStyle.NODE, "node");
        sortStyles.put(NoticeFactory.SortStyle.INTERFACE, "interface");
        sortStyles.put(NoticeFactory.SortStyle.SERVICE, "service");
        sortStyles.put(NoticeFactory.SortStyle.ID, "id");
        sortStyles.put(NoticeFactory.SortStyle.REVERSE_USER, "rev_user");
        sortStyles.put(NoticeFactory.SortStyle.REVERSE_RESPONDER, "rev_responder");
        sortStyles.put(NoticeFactory.SortStyle.REVERSE_PAGETIME, "rev_pagetime");
        sortStyles.put(NoticeFactory.SortStyle.REVERSE_RESPONDTIME, "rev_respondtime");
        sortStyles.put(NoticeFactory.SortStyle.REVERSE_NODE, "rev_node");
        sortStyles.put(NoticeFactory.SortStyle.REVERSE_INTERFACE, "rev_interface");
        sortStyles.put(NoticeFactory.SortStyle.REVERSE_SERVICE, "rev_service");
        sortStyles.put(NoticeFactory.SortStyle.REVERSE_ID, "rev_id");

        ackTypesString = new HashMap<String, NoticeFactory.AcknowledgeType>();
        ackTypesString.put("ack", NoticeFactory.AcknowledgeType.ACKNOWLEDGED);
        ackTypesString.put("unack", NoticeFactory.AcknowledgeType.UNACKNOWLEDGED);

        ackTypes = new HashMap<NoticeFactory.AcknowledgeType, String>();
        ackTypes.put(NoticeFactory.AcknowledgeType.ACKNOWLEDGED, "ack");
        ackTypes.put(NoticeFactory.AcknowledgeType.UNACKNOWLEDGED, "unack");
    }

    /**
     * <p>getSortStyle</p>
     *
     * @param sortStyleString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.notification.NoticeFactory.SortStyle} object.
     */
    public static NoticeFactory.SortStyle getSortStyle(String sortStyleString) {
        if (sortStyleString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return sortStylesString.get(sortStyleString.toLowerCase());
    }

    /**
     * <p>getSortStyleString</p>
     *
     * @param sortStyle a {@link org.opennms.web.notification.NoticeFactory.SortStyle} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getSortStyleString(NoticeFactory.SortStyle sortStyle) {
        if (sortStyle == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return sortStyles.get(sortStyle);
    }

    /**
     * <p>getAcknowledgeType</p>
     *
     * @param ackTypeString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.notification.NoticeFactory.AcknowledgeType} object.
     */
    public static NoticeFactory.AcknowledgeType getAcknowledgeType(String ackTypeString) {
        if (ackTypeString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return ackTypesString.get(ackTypeString.toLowerCase());
    }

    /**
     * <p>getAcknowledgeTypeString</p>
     *
     * @param ackType a {@link org.opennms.web.notification.NoticeFactory.AcknowledgeType} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getAcknowledgeTypeString(NoticeFactory.AcknowledgeType ackType) {
        if (ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return ackTypes.get(ackType);
    }

    /**
     * <p>getFilter</p>
     *
     * @param filterString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.notification.NoticeFactory.Filter} object.
     */
    public static NoticeFactory.Filter getFilter(String filterString) {
        if (filterString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        NoticeFactory.Filter filter = null;

        StringTokenizer tokens = new StringTokenizer(filterString, "=");
        String type = tokens.nextToken();
        String value = tokens.nextToken();

        if (type.equals(NoticeFactory.UserFilter.TYPE)) {
            filter = new NoticeFactory.UserFilter(value);
        } else if (type.equals(NoticeFactory.ResponderFilter.TYPE)) {
            filter = new NoticeFactory.ResponderFilter(value);
        } else if (type.equals(NoticeFactory.NodeFilter.TYPE)) {
            filter = new NoticeFactory.NodeFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(NoticeFactory.InterfaceFilter.TYPE)) {
            filter = new NoticeFactory.InterfaceFilter(value);
        } else if (type.equals(NoticeFactory.ServiceFilter.TYPE)) {
            filter = new NoticeFactory.ServiceFilter(WebSecurityUtils.safeParseInt(value));
        }

        return filter;
    }

    /**
     * <p>getFilterString</p>
     *
     * @param filter a {@link org.opennms.web.notification.NoticeFactory.Filter} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getFilterString(NoticeFactory.Filter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return filter.getDescription();
    }

}
