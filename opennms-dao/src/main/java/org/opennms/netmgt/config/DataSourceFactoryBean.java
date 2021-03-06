//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.config;

import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>DataSourceFactoryBean class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DataSourceFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

    /**
     * <p>getObject</p>
     *
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    public Object getObject() throws Exception {
        return DataSourceFactory.getDataSource();
    }

    /**
     * <p>getObjectType</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public Class<? extends DataSource> getObjectType() {
        return (DataSourceFactory.getDataSource() == null ? DataSource.class : DataSourceFactory.getDataSource().getClass());
    }

    /**
     * <p>isSingleton</p>
     *
     * @return a boolean.
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        DataSourceFactory.init();
    }

    /**
     * <p>destroy</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void destroy() throws Exception {
        log().info("Closing DataSourceFactory!!!");
        DataSourceFactory.close();
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
