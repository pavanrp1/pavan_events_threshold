//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Add serialVersionUID and Java 5 generics. - dj@opennms.org
// 2005 Apr 18: This file created from EventQueryServlet.java
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

package org.opennms.web.alarm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.MissingParameterException;
import org.opennms.web.Util;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.alarm.filter.AfterLastEventTimeFilter;
import org.opennms.web.alarm.filter.AfterFirstEventTimeFilter;
import org.opennms.web.alarm.filter.BeforeLastEventTimeFilter;
import org.opennms.web.alarm.filter.BeforeFirstEventTimeFilter;
import org.opennms.web.alarm.filter.Filter;
import org.opennms.web.alarm.filter.IPLikeFilter;
import org.opennms.web.alarm.filter.LogMessageMatchesAnyFilter;
import org.opennms.web.alarm.filter.LogMessageSubstringFilter;
import org.opennms.web.alarm.filter.NodeNameLikeFilter;
import org.opennms.web.alarm.filter.ServiceFilter;
import org.opennms.web.alarm.filter.SeverityFilter;

/**
 * This servlet takes a large and specific request parameter set and maps it to
 * the more robust "filter" parameter set of the
 * {@link EventFilterServlet EventFilterServlet}via a redirect.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.6.12
 */
public class AlarmQueryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * The list of parameters that are extracted by this servlet and not passed
     * on to the {@link EventFilterServlet EventFilterServlet}.
     */
    protected static String[] IGNORE_LIST = new String[] { "msgsub", "msgmatchany", "nodenamelike", "service", "iplike", "severity", "relativetime", "usebeforetime", "beforehour", "beforeminute", "beforeampm", "beforedate", "beforemonth", "beforeyear", "useaftertime", "afterhour", "afterminute", "afterampm", "afterdate", "aftermonth", "afteryear" };

    /**
     * The URL for the {@link EventFilterServlet EventFilterServlet}. The
     * default is "list." This URL is a sibling URL, so it is relative to the
     * URL directory that was used to call this servlet (usually "event/").
     */
    protected String redirectUrl = "filter";

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();

        if (config.getInitParameter("redirect.url") != null) {
            this.redirectUrl = config.getInitParameter("redirect.url");
        }
    }

    /**
     * {@inheritDoc}
     *
     * Extracts the key parameters from the parameter set, translates them into
     * filter-based parameters, and then passes the modified parameter set to
     * the {@link EventFilterServlet EventFilterServlet}.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Filter> filterArray = new ArrayList<Filter>();

        // convenient syntax for LogMessageSubstringFilter
        String msgSubstring = request.getParameter("msgsub");
        if (msgSubstring != null && msgSubstring.length() > 0) {
            filterArray.add(new LogMessageSubstringFilter(msgSubstring));
        }

        // convenient syntax for LogMessageMatchesAnyFilter
        String msgMatchAny = request.getParameter("msgmatchany");
        if (msgMatchAny != null && msgMatchAny.length() > 0) {
            filterArray.add(new LogMessageMatchesAnyFilter(msgMatchAny));
        }

        // convenient syntax for NodeNameContainingFilter
        String nodeNameLike = request.getParameter("nodenamelike");
        if (nodeNameLike != null && nodeNameLike.length() > 0) {
            filterArray.add(new NodeNameLikeFilter(nodeNameLike));
        }

        // convenient syntax for ServiceFilter
        String service = request.getParameter("service");
        if (service != null && !service.equals(AlarmUtil.ANY_SERVICES_OPTION)) {
            filterArray.add(new ServiceFilter(WebSecurityUtils.safeParseInt(service)));
        }

        // convenient syntax for IPLikeFilter
        String ipLikePattern = request.getParameter("iplike");
        if (ipLikePattern != null && !ipLikePattern.equals("")) {
            filterArray.add(new IPLikeFilter(ipLikePattern));
        }

        // convenient syntax for SeverityFilter
        String severity = request.getParameter("severity");
        if (severity != null && !severity.equals(AlarmUtil.ANY_SEVERITIES_OPTION)) {
            filterArray.add(new SeverityFilter(WebSecurityUtils.safeParseInt(severity)));
        }

        // convenient syntax for AfterDateFilter as relative to current time
        String relativeTime = request.getParameter("relativetime");
        if (relativeTime != null && !relativeTime.equals(AlarmUtil.ANY_RELATIVE_TIMES_OPTION)) {
            try {
                filterArray.add(AlarmUtil.getRelativeTimeFilter(WebSecurityUtils.safeParseInt(relativeTime)));
            } catch (IllegalArgumentException e) {
                // ignore the relative time if it is an illegal value
                this.log("Illegal relativetime value", e);
            }
        }

        String useBeforeLastEventTime = request.getParameter("usebeforelasteventtime");
        if (useBeforeLastEventTime != null && useBeforeLastEventTime.equals("1")) {
            try {
                filterArray.add(this.getBeforeLastEventTimeFilter(request));
            } catch (IllegalArgumentException e) {
                // ignore the before time if it is an illegal value
                this.log("Illegal before last event time value", e);
            } catch (MissingParameterException e) {
                throw new ServletException(e);
            }
        }

        String useAfterLastEventTime = request.getParameter("useafterlasteventtime");
        if (useAfterLastEventTime != null && useAfterLastEventTime.equals("1")) {
            try {
                filterArray.add(this.getAfterLastEventTimeFilter(request));
            } catch (IllegalArgumentException e) {
                // ignore the after time if it is an illegal value
                this.log("Illegal after last event time value", e);
            } catch (MissingParameterException e) {
                throw new ServletException(e);
            }
        }

        String useBeforeFirstEventTime = request.getParameter("usebeforefirsteventtime");
        if (useBeforeFirstEventTime != null && useBeforeFirstEventTime.equals("1")) {
            try {
                filterArray.add(this.getBeforeFirstEventTimeFilter(request));
            } catch (IllegalArgumentException e) {
                // ignore the before time if it is an illegal value
                this.log("Illegal before first event time value", e);
            } catch (MissingParameterException e) {
                throw new ServletException(e);
            }
        }

        String useAfterFirstEventTime = request.getParameter("useafterfirsteventtime");
        if (useAfterFirstEventTime != null && useAfterFirstEventTime.equals("1")) {
            try {
                filterArray.add(this.getAfterFirstEventTimeFilter(request));
            } catch (IllegalArgumentException e) {
                // ignore the after time if it is an illegal value
                this.log("Illegal after first event time value", e);
            } catch (MissingParameterException e) {
                throw new ServletException(e);
            }
        }

        String queryString = "";

        if (filterArray.size() > 0) {
            String[] filterStrings = new String[filterArray.size()];

            for (int i = 0; i < filterStrings.length; i++) {
                Filter filter = filterArray.get(i);
                filterStrings[i] = AlarmUtil.getFilterString(filter);
            }

            Map<String, Object> paramAdditions = new HashMap<String, Object>();
            paramAdditions.put("filter", filterStrings);

            queryString = Util.makeQueryString(request, paramAdditions, IGNORE_LIST);
        } else {
            queryString = Util.makeQueryString(request, IGNORE_LIST);
        }

        response.sendRedirect(this.redirectUrl + "?" + queryString);
    }

    /**
     * <p>getBeforeFirstEventTimeFilter</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.opennms.web.alarm.filter.BeforeFirstEventTimeFilter} object.
     */
    protected BeforeFirstEventTimeFilter getBeforeFirstEventTimeFilter(HttpServletRequest request) {
        Date beforeFirstEventDate = this.getDateFromRequest(request, "beforefirsteventtime");
        return (new BeforeFirstEventTimeFilter(beforeFirstEventDate));
    }

    /**
     * <p>getAfterFirstEventTimeFilter</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.opennms.web.alarm.filter.AfterFirstEventTimeFilter} object.
     */
    protected AfterFirstEventTimeFilter getAfterFirstEventTimeFilter(HttpServletRequest request) {
        Date afterFirstEventDate = this.getDateFromRequest(request, "afterfirsteventtime");
        return (new AfterFirstEventTimeFilter(afterFirstEventDate));
    }

    /**
     * <p>getBeforeLastEventTimeFilter</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.opennms.web.alarm.filter.BeforeLastEventTimeFilter} object.
     */
    protected BeforeLastEventTimeFilter getBeforeLastEventTimeFilter(HttpServletRequest request) {
        Date beforeLastEventDate = this.getDateFromRequest(request, "beforelasteventtime");
        return (new BeforeLastEventTimeFilter(beforeLastEventDate));
    }

    /**
     * <p>getAfterLastEventTimeFilter</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.opennms.web.alarm.filter.AfterLastEventTimeFilter} object.
     */
    protected AfterLastEventTimeFilter getAfterLastEventTimeFilter(HttpServletRequest request) {
        Date afterLastEventDate = this.getDateFromRequest(request, "afterlasteventtime");
        return (new AfterLastEventTimeFilter(afterLastEventDate));
    }

    /**
     * <p>getDateFromRequest</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param prefix a {@link java.lang.String} object.
     * @return a {@link java.util.Date} object.
     * @throws org.opennms.web.MissingParameterException if any.
     */
    protected Date getDateFromRequest(HttpServletRequest request, String prefix) throws MissingParameterException {
        if (request == null || prefix == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Calendar cal = Calendar.getInstance();

        // be lenient to handle the inputs easier
        // read the java.util.Calendar javadoc for more info
        cal.setLenient(true);

        // hour, from 1-12
        String hourString = request.getParameter(prefix + "hour");
        if (hourString == null) {
            throw new MissingParameterException(prefix + "hour", this.getRequiredDateFields(prefix));
        }

        cal.set(Calendar.HOUR, WebSecurityUtils.safeParseInt(hourString));

        // minute, from 0-59
        String minuteString = request.getParameter(prefix + "minute");
        if (minuteString == null) {
            throw new MissingParameterException(prefix + "minute", this.getRequiredDateFields(prefix));
        }

        cal.set(Calendar.MINUTE, WebSecurityUtils.safeParseInt(minuteString));

        // AM/PM, either AM or PM
        String amPmString = request.getParameter(prefix + "ampm");
        if (amPmString == null) {
            throw new MissingParameterException(prefix + "ampm", this.getRequiredDateFields(prefix));
        }

        if (amPmString.equalsIgnoreCase("am")) {
            cal.set(Calendar.AM_PM, Calendar.AM);
        } else if (amPmString.equalsIgnoreCase("pm")) {
            cal.set(Calendar.AM_PM, Calendar.PM);
        } else {
            throw new IllegalArgumentException("Illegal AM/PM value: " + amPmString);
        }

        // month, 0-11 (Jan-Dec)
        String monthString = request.getParameter(prefix + "month");
        if (monthString == null) {
            throw new MissingParameterException(prefix + "month", this.getRequiredDateFields(prefix));
        }

        cal.set(Calendar.MONTH, WebSecurityUtils.safeParseInt(monthString));

        // date, 1-31
        String dateString = request.getParameter(prefix + "date");
        if (dateString == null) {
            throw new MissingParameterException(prefix + "date", this.getRequiredDateFields(prefix));
        }

        cal.set(Calendar.DATE, WebSecurityUtils.safeParseInt(dateString));

        // year
        String yearString = request.getParameter(prefix + "year");
        if (yearString == null) {
            throw new MissingParameterException(prefix + "year", this.getRequiredDateFields(prefix));
        }

        cal.set(Calendar.YEAR, WebSecurityUtils.safeParseInt(yearString));

        return cal.getTime();
    }

    /**
     * <p>getRequiredDateFields</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     */
    protected String[] getRequiredDateFields(String prefix) {
        return new String[] { prefix + "hour", prefix + "minute", prefix + "ampm", prefix + "date", prefix + "month", prefix + "year" };
    }

}
