
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/


package org.opennms.netmgt.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
/**
 * <p>OnmsLocationSpecificStatus class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Table(name="location_specific_status_changes")
public class OnmsLocationSpecificStatus {

    private Integer m_id;
    private OnmsLocationMonitor m_locationMonitor;
    private OnmsMonitoredService m_monitoredService;
    private PollStatus m_pollResult;

    /**
     * <p>Constructor for OnmsLocationSpecificStatus.</p>
     */
    public OnmsLocationSpecificStatus() {
        // this is used by hibernate to construct an object from the db
    }

    /**
     * <p>Constructor for OnmsLocationSpecificStatus.</p>
     *
     * @param locationMonitor a {@link org.opennms.netmgt.model.OnmsLocationMonitor} object.
     * @param monitoredService a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @param pollResult a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public OnmsLocationSpecificStatus(OnmsLocationMonitor locationMonitor, OnmsMonitoredService monitoredService, PollStatus pollResult) {
        m_locationMonitor = locationMonitor;
        m_monitoredService = monitoredService;
        m_pollResult = pollResult;
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
    }

    /**
     * <p>getLocationMonitor</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsLocationMonitor} object.
     */
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumn(name="locationMonitorId")
    public OnmsLocationMonitor getLocationMonitor() {
        return m_locationMonitor;
    }

    /**
     * <p>setLocationMonitor</p>
     *
     * @param locationMonitor a {@link org.opennms.netmgt.model.OnmsLocationMonitor} object.
     */
    public void setLocationMonitor(OnmsLocationMonitor locationMonitor) {
        m_locationMonitor = locationMonitor;
    }

    /**
     * <p>getMonitoredService</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumn(name="ifServiceId")
    public OnmsMonitoredService getMonitoredService() {
        return m_monitoredService;
    }

    /**
     * <p>setMonitoredService</p>
     *
     * @param monitoredService a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    public void setMonitoredService(OnmsMonitoredService monitoredService) {
        m_monitoredService = monitoredService;
    }

    /**
     * <p>getPollResult</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    @Embedded
    public PollStatus getPollResult() {
        return m_pollResult;
    }

    /**
     * <p>setPollResult</p>
     *
     * @param newStatus a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public void setPollResult(PollStatus newStatus) {
        m_pollResult = newStatus;
    }

    /**
     * <p>getStatusCode</p>
     *
     * @return a int.
     */
    @Transient
    public int getStatusCode() {
        return m_pollResult.getStatusCode();
    }
}
