/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: November 21, 2006
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.threshd;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 * <p>ThresholdResourceType class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class ThresholdResourceType {

    private String m_dsType;

    private Map<String, Set<ThresholdEntity>> m_thresholdMap;
    
    /**
     * <p>Constructor for ThresholdResourceType.</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public ThresholdResourceType(String type) {
        m_dsType = type;
    }

    /**
     * <p>getDsType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDsType() {
        return m_dsType;
    }
    
    /**
     * <p>getThresholdMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Set<ThresholdEntity>> getThresholdMap() {
        return m_thresholdMap;
    }
    
    /**
     * <p>setThresholdMap</p>
     *
     * @param thresholdMap a {@link java.util.Map} object.
     */
    public void setThresholdMap(Map<String, Set<ThresholdEntity>> thresholdMap) {
    	m_thresholdMap = thresholdMap;
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.apache.log4j.Category} object.
     */
    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    

}
