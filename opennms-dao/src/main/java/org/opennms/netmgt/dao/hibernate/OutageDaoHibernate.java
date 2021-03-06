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
// Modifications:
//
// 2008 Jul 05: Indent. - dj@opennms.org
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
package org.opennms.netmgt.dao.hibernate;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.ServiceSelector;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * <p>OutageDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OutageDaoHibernate extends AbstractDaoHibernate<OnmsOutage, Integer> implements OutageDao {

    /**
     * <p>Constructor for OutageDaoHibernate.</p>
     */
    public OutageDaoHibernate() {
        super(OnmsOutage.class);
    }

    /**
     * <p>currentOutageCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer currentOutageCount() {
        return queryInt("select count(*) from OnmsOutage as o where o.ifRegainedService is null");
    }

    /**
     * <p>currentOutages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OnmsOutage> currentOutages() {
        return find("from OnmsOutage as o where o.ifRegainedService is null");
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Collection<OnmsOutage> findAll(final Integer offset, final Integer limit) {
        return (Collection<OnmsOutage>)getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createCriteria(OnmsOutage.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }

        });
    }

    /** {@inheritDoc} */
    public Collection<OnmsOutage> matchingCurrentOutages(ServiceSelector selector) {
        Set<String> matchingIps = new HashSet<String>(FilterDaoFactory.getInstance().getIPList(selector.getFilterRule()));
        Set<String> matchingSvcs = new HashSet<String>(selector.getServiceNames());

        List<OnmsOutage> matchingOutages = new LinkedList<OnmsOutage>();
        Collection<OnmsOutage> outages = currentOutages();
        for (OnmsOutage outage : outages) {
            OnmsMonitoredService svc = outage.getMonitoredService();
            if ((matchingSvcs.contains(svc.getServiceName()) || matchingSvcs.isEmpty()) &&
                    matchingIps.contains(svc.getIpAddress())) {

                matchingOutages.add(outage);
            }

        }


        return matchingOutages;
    }

}
