//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.poller.monitors;

import java.util.Map;

import org.opennms.netmgt.capsd.plugins.LoopPlugin;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.utils.ParameterMap;
/**
 * <p>LoopMonitor class.</p>
 *
 * @author david
 * @version $Id: $
 */

@Distributable
public class LoopMonitor implements ServiceMonitor {

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#initialize(org.opennms.netmgt.config.PollerConfig, java.util.Map)
     */
    /** {@inheritDoc} */
    public void initialize(Map parameters) {
        return;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#release()
     */
    /**
     * <p>release</p>
     */
    public void release() {
        return;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#initialize(org.opennms.netmgt.poller.MonitoredService)
     */
    /**
     * <p>initialize</p>
     *
     * @param svc a {@link org.opennms.netmgt.poller.MonitoredService} object.
     */
    public void initialize(MonitoredService svc) {
        return;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#release(org.opennms.netmgt.poller.MonitoredService)
     */
    /** {@inheritDoc} */
    public void release(MonitoredService svc) {
        return;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#poll(org.opennms.netmgt.poller.MonitoredService, java.util.Map, org.opennms.netmgt.config.poller.Package)
     */
    /** {@inheritDoc} */
    public PollStatus poll(MonitoredService svc, Map parameters) {
        LoopPlugin lp = new LoopPlugin();
        boolean isAvailable = lp.isProtocolSupported(svc.getAddress(), parameters);
        int status = (isAvailable ? PollStatus.SERVICE_AVAILABLE : PollStatus.SERVICE_UNAVAILABLE);
        StringBuffer sb = new StringBuffer();
        sb.append("LoopMonitor configured with is-supported =  ");
        sb.append(ParameterMap.getKeyedString(parameters, "is-supported", "false"));
        sb.append(" for ip-match: ");
        sb.append(ParameterMap.getKeyedString(parameters, "ip-match", "*.*.*.*"));
        
        return PollStatus.get(status, sb.toString());
    }

}
