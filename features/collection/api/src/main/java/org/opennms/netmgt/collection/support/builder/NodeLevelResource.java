/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collection.support.builder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.collection.adapters.NodeLevelResourceAdapter;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.model.ResourcePath;

@XmlJavaTypeAdapter(NodeLevelResourceAdapter.class)
public class NodeLevelResource implements Resource {

    private final int m_nodeId;

    public NodeLevelResource(int nodeId) {
        m_nodeId = nodeId;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    @Override
    public Resource getParent() {
        return null;
    }

    @Override
    public String getInstance() {
        return String.valueOf(m_nodeId);
    }

    @Override
    public ResourcePath getPath(CollectionResource resource) {
        return ResourcePath.get();
    }

    @Override
    public String toString() {
        return String.format("NodeLevelResource[nodeId=%d]", m_nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_nodeId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof NodeLevelResource)) {
            return false;
        }
        NodeLevelResource other = (NodeLevelResource) obj;
        return Objects.equals(this.m_nodeId, other.m_nodeId);
    }
}
