//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.collectd;

import java.util.Map;

import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;

/**
 * The Collector class.
 *
 * @author <a href="mailto:mike@opennms.org">Mike</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 * @author <a href="mailto:mike@opennms.org">Mike</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 * @version $Id: $
 */
public interface ServiceCollector {
    /**
     * Status of the collector object.
     */
    public static final int COLLECTION_UNKNOWN = 0;

    /** Constant <code>COLLECTION_SUCCEEDED=1</code> */
    public static final int COLLECTION_SUCCEEDED = 1;

    /** Constant <code>COLLECTION_FAILED=2</code> */
    public static final int COLLECTION_FAILED = 2;

    /** Constant <code>statusType="{Unknown,COLLECTION_SUCCEEDED,COLLECTIO"{trunked}</code> */
    public static final String[] statusType = {
        "Unknown",
        "COLLECTION_SUCCEEDED",
        "COLLECTION_FAILED"
        };

    /**
     * <p>initialize</p>
     *
     * @param parameters a {@link java.util.Map} object.
     */
    public void initialize(Map parameters);

    /**
     * <p>release</p>
     */
    public void release();

    /**
     * <p>initialize</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param parameters a {@link java.util.Map} object.
     */
    public void initialize(CollectionAgent agent, Map parameters);

    /**
     * <p>release</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public void release(CollectionAgent agent);

    /**
     * Invokes a collection on the object.
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param eproxy a {@link org.opennms.netmgt.model.events.EventProxy} object.
     * @param parameters a {@link java.util.Map} object.
     * @return a {@link org.opennms.netmgt.collectd.CollectionSet} object.
     */
    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, String> parameters);

    /**
     * <p>getRrdRepository</p>
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.RrdRepository} object.
     */
    public RrdRepository getRrdRepository(String collectionName);
}
