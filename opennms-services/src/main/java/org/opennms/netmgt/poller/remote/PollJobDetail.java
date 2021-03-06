/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: August 16, 2006
 *
 * Copyright (C) 2005-2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.poller.remote;

import org.quartz.JobDetail;

/**
 * <p>PollJobDetail class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class PollJobDetail extends JobDetail {
    
    /** Constant <code>GROUP="pollJobGroup"</code> */
    public static final String GROUP = "pollJobGroup";

	private static final long serialVersionUID = -6499411861193543030L;
	
	/**
	 * <p>Constructor for PollJobDetail.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param jobClass a {@link java.lang.Class} object.
	 */
	public PollJobDetail(String name, Class jobClass) {
		super(name, GROUP, jobClass);
	}
	
	/**
	 * <p>setPollService</p>
	 *
	 * @param pollService a {@link org.opennms.netmgt.poller.remote.PollService} object.
	 */
	public void setPollService(PollService pollService) {
		getJobDataMap().put("pollService", pollService);
	}
	
	/**
	 * <p>setPolledService</p>
	 *
	 * @param polledService a {@link org.opennms.netmgt.poller.remote.PolledService} object.
	 */
	public void setPolledService(PolledService polledService) {
		getJobDataMap().put("polledService", polledService);
	}
	
	/**
	 * <p>setPollerFrontEnd</p>
	 *
	 * @param pollerFrontEnd a {@link org.opennms.netmgt.poller.remote.PollerFrontEnd} object.
	 */
	public void setPollerFrontEnd(PollerFrontEnd pollerFrontEnd) {
		getJobDataMap().put("pollerFrontEnd", pollerFrontEnd);
	}
	
}
