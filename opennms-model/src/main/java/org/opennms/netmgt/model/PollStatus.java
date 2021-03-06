//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005-2007 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 05: Add serialVersionUID, mark fields private, suppress warnings from unused method. - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;


/**
 * Represents the status of a node, interface or services
 *
 * @author brozow
 * @version $Id: $
 */
@Embeddable
public class PollStatus implements Serializable {
    private static final long serialVersionUID = 3L;

    private Date m_timestamp = new Date();

    /**
     * Status of the pollable object.
     */
    private int m_statusCode;
    
    private String m_reason;

    private Map<String, Number> m_properties = new LinkedHashMap<String, Number>();
    
    /**
     * <P>
     * The constant that defines a service that is up but is most likely
     * suffering due to excessive load or latency issues and because of that has
     * not responded within the configured timeout period.
     * </P>
     */
    public static final int SERVICE_UNRESPONSIVE = 3;

    /**
     * <P>
     * The constant that defines a service that is not working normally and
     * should be scheduled using the downtime models.
     * </P>
     */
    public static final int SERVICE_UNAVAILABLE = 2;

    /**
     * <P>
     * The constant that defines a service as being in a normal state. If this
     * is returned by the poll() method then the framework will re-schedule the
     * service for its next poll using the standard uptime interval
     * </P>
     */
    public static final int SERVICE_AVAILABLE = 1;

    /**
     * The constant the defines a status is unknown. Used mostly internally
     */
    public static final int SERVICE_UNKNOWN = 0;

    private static final String[] s_statusNames = {
        "Unknown",
        "Up",
        "Down",
        "Unresponsive"
    };

    private static int decodeStatusName(String statusName) {

        for (int statusCode = 0; statusCode < s_statusNames.length; statusCode++) {
            if (s_statusNames[statusCode].equalsIgnoreCase(statusName)) {
                return statusCode;
            }
        }
        return SERVICE_UNKNOWN;
    }

    /**
     * <p>decode</p>
     *
     * @param statusName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus decode(String statusName) {
        return decode(statusName, null, null);
    }

    /**
     * <p>decode</p>
     *
     * @param statusName a {@link java.lang.String} object.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus decode(String statusName, String reason) {
        return decode(statusName, reason, null);
    }

    /**
     * <p>decode</p>
     *
     * @param statusName a {@link java.lang.String} object.
     * @param responseTime a {@link java.lang.Double} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus decode(String statusName, Double responseTime) {
        return decode(statusName, null, responseTime);
    }

    /**
     * <p>decode</p>
     *
     * @param statusName a {@link java.lang.String} object.
     * @param reason a {@link java.lang.String} object.
     * @param responseTime a {@link java.lang.Double} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus decode(String statusName, String reason, Double responseTime) {
        return new PollStatus(decodeStatusName(statusName), reason, responseTime);
    }

    /**
     * <p>get</p>
     *
     * @param status a int.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus get(int status, String reason) {
        return get(status, reason, null);
    }

    /**
     * <p>get</p>
     *
     * @param status a int.
     * @param responseTime a {@link java.lang.Double} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus get(int status, Double responseTime) {
        return get(status, null, responseTime);
    }

    /**
     * <p>get</p>
     *
     * @param status a int.
     * @param reason a {@link java.lang.String} object.
     * @param responseTime a {@link java.lang.Double} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus get(int status, String reason, Double responseTime) {
        return new PollStatus(status, reason, responseTime);
    }

    private PollStatus() {
        this(SERVICE_UNKNOWN, null, null);
    }

    private PollStatus(int statusCode, String reason, Double responseTime) {
        m_statusCode = statusCode;
        m_reason = reason;
        setResponseTime(responseTime);
    }

    /**
     * <p>up</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus up() {
        return up(null);
    }

    /**
     * <p>up</p>
     *
     * @param responseTime a {@link java.lang.Double} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus up(Double responseTime) {
        return available(responseTime);
    }

    /**
     * <p>available</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus available() {
        return available(null);
    }

    /**
     * <p>available</p>
     *
     * @param responseTime a {@link java.lang.Double} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus available(Double responseTime) {
        return new PollStatus(SERVICE_AVAILABLE, null, responseTime);
    }

    /**
     * <p>unknown</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus unknown() {
        return unknown(null);
    }

    /**
     * <p>unknown</p>
     *
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus unknown(String reason) {
        return new PollStatus(SERVICE_UNKNOWN, reason, null);
    }

    /**
     * <p>unresponsive</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus unresponsive() {
        return unresponsive(null);
    }

    /**
     * <p>unresponsive</p>
     *
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus unresponsive(String reason) {
        return new PollStatus(SERVICE_UNRESPONSIVE, reason, null);
    }

    /**
     * <p>down</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus down() {
        return down(null);
    }

    /**
     * <p>unavailable</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus unavailable() {
        return unavailable(null);
    }

    /**
     * <p>down</p>
     *
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus down(String reason) {
        return unavailable(reason);
    }

    /**
     * <p>unavailable</p>
     *
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public static PollStatus unavailable(String reason) {
        return new PollStatus(SERVICE_UNAVAILABLE, reason, null);
    }

    /** {@inheritDoc} */
    public boolean equals(Object o) {
        if (o instanceof PollStatus) {
            return m_statusCode == ((PollStatus)o).m_statusCode;
        }
        return false;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return m_statusCode;
    }

    /**
     * <p>isUp</p>
     *
     * @return a boolean.
     */
    @Transient
    public boolean isUp() {
        return !isDown();
    }

    /**
     * <p>isAvailable</p>
     *
     * @return a boolean.
     */
    @Transient
    public boolean isAvailable() {
        return this.m_statusCode == SERVICE_AVAILABLE;
    }

    /**
     * <p>isUnresponsive</p>
     *
     * @return a boolean.
     */
    @Transient
    public boolean isUnresponsive() {
        return this.m_statusCode == SERVICE_UNRESPONSIVE;
    }

    /**
     * <p>isUnavailable</p>
     *
     * @return a boolean.
     */
    @Transient
    public boolean isUnavailable() {
        return this.m_statusCode == SERVICE_UNAVAILABLE;
    }

    /**
     * <p>isDown</p>
     *
     * @return a boolean.
     */
    @Transient
    public boolean isDown() {
        return this.m_statusCode == SERVICE_UNAVAILABLE;
    }

    /**
     * <p>isUnknown</p>
     *
     * @return a boolean.
     */
    @Transient
    public boolean isUnknown() {
        return this.m_statusCode == SERVICE_UNKNOWN;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return getStatusName();
    }

    /**
     * <p>getTimestamp</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Column(name="statusTime", nullable=false)
    public Date getTimestamp() {
        return m_timestamp;
    }

    /**
     * <p>setTimestamp</p>
     *
     * @param timestamp a {@link java.util.Date} object.
     */
    public void setTimestamp(Date timestamp) {
        m_timestamp = timestamp;
    }

    /**
     * <p>getReason</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="statusReason", length=255, nullable=true)
    public String getReason() {
        return m_reason;
    }

    /**
     * <p>setReason</p>
     *
     * @param reason a {@link java.lang.String} object.
     */
    public void setReason(String reason) {
        m_reason = reason;
    }

    /**
     * <p>getResponseTime</p>
     *
     * @return a {@link java.lang.Double} object.
     */
    @Column(name="responseTime", nullable=true)
    public Double getResponseTime() {
        Number val = getProperty("response-time");
        return (val == null ? null : val.doubleValue());
    	
    }

    /* stores the individual item for compatibility with database schema, as well as the new property map */
    /**
     * <p>setResponseTime</p>
     *
     * @param responseTime a {@link java.lang.Double} object.
     */
    public void setResponseTime(Double responseTime) {
        if (responseTime == null) {
            m_properties.remove("response-time");
        } else {
            m_properties.put("response-time", responseTime);
        }
    }

    /**
     * <p>getProperties</p>
     *
     * @return a {@link java.util.Map} object.
     */
    @Transient
    public Map<String, Number> getProperties() {
    	if (m_properties == null) {
    		m_properties = new LinkedHashMap<String, Number>();
    	}
    	return m_properties;
    }
    
    /**
     * <p>setProperties</p>
     *
     * @param p a {@link java.util.Map} object.
     */
    public void setProperties(Map<String, Number> p) {
    	m_properties = p;
    }
    
    /**
     * <p>getProperty</p>
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.Number} object.
     */
    @Transient
    public Number getProperty(String key) {
    	if (m_properties != null) {
    		return m_properties.get(key);
    	} else {
    		return null;
    	}
    }

    /**
     * <p>setProperty</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.Number} object.
     */
    public void setProperty(String key, Number value) {
    	Map<String, Number> m = getProperties();
    	m.put(key, value);
    	setProperties(m);
    }

    /**
     * <p>getStatusCode</p>
     *
     * @return a int.
     */
    @Column(name="statusCode", nullable=false)
    public int getStatusCode() {
        return m_statusCode;
    }

    @SuppressWarnings("unused")
    private void setStatusCode(int statusCode) {
        m_statusCode = statusCode;
    }

    /**
     * <p>getStatusName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    public String getStatusName() {
        return s_statusNames[m_statusCode];
    }


}
