/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 22, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.dashboard.client;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>Alarm class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class Alarm implements IsSerializable {
    
    private String m_logMsg;
    private String m_descrption;
    private String m_severity;
    private int m_count;
    private String m_nodeLabel;
    private int m_nodeId;
    private String m_ipAddress;
    private String m_svcName;
    private Date m_firstEventTime;
    private Date m_lastEventTime;
    
    /**
     * <p>Constructor for Alarm.</p>
     */
    public Alarm() {
        
    }
    
    /**
     * <p>Constructor for Alarm.</p>
     *
     * @param severity a {@link java.lang.String} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param logMsg a {@link java.lang.String} object.
     * @param description a {@link java.lang.String} object.
     * @param count a int.
     * @param firstEventTime a {@link java.util.Date} object.
     * @param lastEventTime a {@link java.util.Date} object.
     */
    public Alarm(String severity, String nodeLabel, String logMsg, String description, int count, Date firstEventTime, Date lastEventTime) {
        m_severity = severity;
        m_nodeLabel = nodeLabel;
        m_logMsg = logMsg;
        m_descrption = description;
        m_count = count;
        m_firstEventTime = firstEventTime;
        m_lastEventTime = lastEventTime;
    }
    /**
     * <p>getCount</p>
     *
     * @return a int.
     */
    public int getCount() {
        return m_count;
    }
    /**
     * <p>setCount</p>
     *
     * @param count a int.
     */
    public void setCount(int count) {
        m_count = count;
    }
    /**
     * <p>getDescrption</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescrption() {
        return m_descrption;
    }
    /**
     * <p>setDescrption</p>
     *
     * @param descrption a {@link java.lang.String} object.
     */
    public void setDescrption(String descrption) {
        m_descrption = descrption;
    }
    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return m_ipAddress;
    }
    /**
     * <p>setIpAddress</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     */
    public void setIpAddress(String ipAddress) {
        m_ipAddress = ipAddress;
    }
    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return m_nodeId;
    }
    /**
     * <p>setNodeId</p>
     *
     * @param nodeId a int.
     */
    public void setNodeId(int nodeId) {
        m_nodeId = nodeId;
    }
    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return m_nodeLabel;
    }
    /**
     * <p>setNodeLabel</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     */
    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }
    /**
     * <p>getSeverity</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSeverity() {
        return m_severity;
    }
    /**
     * <p>setSeverity</p>
     *
     * @param severity a {@link java.lang.String} object.
     */
    public void setSeverity(String severity) {
        m_severity = severity;
    }
    /**
     * <p>getSvcName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSvcName() {
        return m_svcName;
    }
    /**
     * <p>setSvcName</p>
     *
     * @param svcName a {@link java.lang.String} object.
     */
    public void setSvcName(String svcName) {
        m_svcName = svcName;
    }

    /**
     * <p>getFirstEventTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getFirstEventTime() {
        return m_firstEventTime;
    }

    /**
     * <p>setFirstEventTime</p>
     *
     * @param firstEventTime a {@link java.util.Date} object.
     */
    public void setFirstEventTime(Date firstEventTime) {
        m_firstEventTime = firstEventTime;
    }

    /**
     * <p>getLastEventTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getLastEventTime() {
        return m_lastEventTime;
    }

    /**
     * <p>setLastEventTime</p>
     *
     * @param lastEventTime a {@link java.util.Date} object.
     */
    public void setLastEventTime(Date lastEventTime) {
        m_lastEventTime = lastEventTime;
    }

    /**
     * <p>getLogMsg</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLogMsg() {
        return m_logMsg;
    }

    /**
     * <p>setLogMsg</p>
     *
     * @param logMsg a {@link java.lang.String} object.
     */
    public void setLogMsg(String logMsg) {
        m_logMsg = logMsg;
    }
    
    

}
