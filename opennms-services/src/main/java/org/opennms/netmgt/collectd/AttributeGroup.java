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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 * <p>AttributeGroup class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AttributeGroup {
    
    private CollectionResource m_resource;
    private AttributeGroupType m_groupType;
    private Set<CollectionAttribute> m_attributes = new HashSet<CollectionAttribute>();
    
    /**
     * <p>Constructor for AttributeGroup.</p>
     *
     * @param resource a {@link org.opennms.netmgt.collectd.CollectionResource} object.
     * @param groupType a {@link org.opennms.netmgt.collectd.AttributeGroupType} object.
     */
    public AttributeGroup(CollectionResource resource, AttributeGroupType groupType) {
        m_resource = resource;
        m_groupType = groupType;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_groupType.getName();
    }
    
    /**
     * <p>getResource</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectionResource} object.
     */
    public CollectionResource getResource() {
        return m_resource;
    }
    
    /**
     * <p>getAttributes</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<CollectionAttribute> getAttributes() {
        return m_attributes;
    }
    
    /**
     * <p>addAttribute</p>
     *
     * @param attr a {@link org.opennms.netmgt.collectd.CollectionAttribute} object.
     */
    public void addAttribute(CollectionAttribute attr) {
        m_attributes.add(attr);
    }

    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.netmgt.collectd.CollectionSetVisitor} object.
     */
    public void visit(CollectionSetVisitor visitor) {
        if (log().isDebugEnabled()) {
            log().debug("Visiting Group "+this);
        }
        visitor.visitGroup(this);
        
        for(CollectionAttribute attr : getAttributes()) {
            attr.visit(visitor);
        }
        
        visitor.completeGroup(this);
    }
    
    /**
     * <p>shouldPersist</p>
     *
     * @param params a {@link org.opennms.netmgt.collectd.ServiceParameters} object.
     * @return a boolean.
     */
    public boolean shouldPersist(ServiceParameters params) {
        boolean shouldPersist = doShouldPersist();
        if (log().isDebugEnabled()) {
            log().debug(this+".shouldPersist = "+shouldPersist);
        }
        return shouldPersist;   
 
        
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    private boolean doShouldPersist() {
        if ("ignore".equals(getIfType())) return true;
        if ("all".equals(getIfType())) return true;
        
        String type = String.valueOf(m_resource.getType());
        
        if (type.equals(getIfType())) return true;
        
        StringTokenizer tokenizer = new StringTokenizer(getIfType(), ",");
        while(tokenizer.hasMoreTokens()) {
            if (type.equals(tokenizer.nextToken()))
                return true;
        }
        return false;
    }

    private String getIfType() {
        return m_groupType.getIfType();
    }

    /**
     * <p>getGroupType</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.AttributeGroupType} object.
     */
    public AttributeGroupType getGroupType() {
        return m_groupType;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return m_groupType + " for " + m_resource;
    }
    
}
