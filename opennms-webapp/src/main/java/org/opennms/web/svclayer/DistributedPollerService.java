/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: November 30, 2006
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
package org.opennms.web.svclayer;

import org.opennms.web.command.LocationMonitorIdCommand;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;

/**
 * <p>DistributedPollerService interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
@Transactional(readOnly = true)
public interface DistributedPollerService {
    /**
     * <p>getLocationMonitorList</p>
     *
     * @return a {@link org.opennms.web.svclayer.LocationMonitorListModel} object.
     */
    public LocationMonitorListModel getLocationMonitorList();

    /**
     * <p>getLocationMonitorDetails</p>
     *
     * @param command a {@link org.opennms.web.command.LocationMonitorIdCommand} object.
     * @param errors a {@link org.springframework.validation.BindException} object.
     * @return a {@link org.opennms.web.svclayer.LocationMonitorListModel} object.
     */
    public LocationMonitorListModel getLocationMonitorDetails(LocationMonitorIdCommand command, BindException errors);

    /**
     * <p>pauseLocationMonitor</p>
     *
     * @param command a {@link org.opennms.web.command.LocationMonitorIdCommand} object.
     * @param errors a {@link org.springframework.validation.BindException} object.
     */
    @Transactional(readOnly = false)
    public void pauseLocationMonitor(LocationMonitorIdCommand command, BindException errors);
    
    /**
     * <p>resumeLocationMonitor</p>
     *
     * @param command a {@link org.opennms.web.command.LocationMonitorIdCommand} object.
     * @param errors a {@link org.springframework.validation.BindException} object.
     */
    @Transactional(readOnly = false)
    public void resumeLocationMonitor(LocationMonitorIdCommand command, BindException errors);
    
    /**
     * <p>deleteLocationMonitor</p>
     *
     * @param command a {@link org.opennms.web.command.LocationMonitorIdCommand} object.
     * @param errors a {@link org.springframework.validation.BindException} object.
     */
    @Transactional(readOnly = false)
    public void deleteLocationMonitor(LocationMonitorIdCommand command, BindException errors);
}
