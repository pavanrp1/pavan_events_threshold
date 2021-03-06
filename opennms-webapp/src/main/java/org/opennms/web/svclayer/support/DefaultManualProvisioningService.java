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
 * 2007 Aug 03: Change Castor methods clearX -> removeAllX. - dj@opennms.org
 * 
 * Created: November 3, 2006
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


package org.opennms.web.svclayer.support;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.modelimport.Category;
import org.opennms.netmgt.config.modelimport.Interface;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.config.modelimport.MonitoredService;
import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.web.BeanUtils;
import org.opennms.web.Util;
import org.opennms.web.svclayer.ManualProvisioningService;
import org.opennms.web.svclayer.dao.ManualProvisioningDao;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * <p>DefaultManualProvisioningService class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class DefaultManualProvisioningService implements
        ManualProvisioningService {

    private ManualProvisioningDao m_provisioningDao;
    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private ServiceTypeDao m_serviceTypeDao;
    
    /**
     * <p>setProvisioningDao</p>
     *
     * @param provisioningDao a {@link org.opennms.web.svclayer.dao.ManualProvisioningDao} object.
     */
    public void setProvisioningDao(ManualProvisioningDao provisioningDao) {
        m_provisioningDao = provisioningDao;
    }
    
    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    /**
     * <p>setCategoryDao</p>
     *
     * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
     */
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }
    
    /**
     * <p>setServiceTypeDao</p>
     *
     * @param serviceTypeDao a {@link org.opennms.netmgt.dao.ServiceTypeDao} object.
     */
    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }

    /** {@inheritDoc} */
    public ModelImport addCategoryToNode(String groupName, String pathToNode, String categoryName) {
        
        ModelImport group = m_provisioningDao.get(groupName);
        
        Node node = BeanUtils.getPathValue(group, pathToNode, Node.class);
        
        Category category = new Category();
        category.setName(categoryName);
        node.addCategory(0, category);
        
        m_provisioningDao.save(groupName, group);
        
        return m_provisioningDao.get(groupName);
    }

    /** {@inheritDoc} */
    public ModelImport addInterfaceToNode(String groupName, String pathToNode,
            String ipAddr) {
        ModelImport group = m_provisioningDao.get(groupName);
        
        Node node = BeanUtils.getPathValue(group, pathToNode, Node.class);
        
        String snmpPrimary = "P";
        if (node.getInterfaceCount() > 0)
            snmpPrimary = "S";

        Interface iface = createInterface(ipAddr, snmpPrimary);
        node.addInterface(0, iface);
        
        m_provisioningDao.save(groupName, group);
        
        return m_provisioningDao.get(groupName);
    }

    private Interface createInterface(String ipAddr, String snmpPrimary) {
        Interface iface = new Interface();
        iface.setIpAddr(ipAddr);
        iface.setStatus(1);
        iface.setSnmpPrimary(snmpPrimary);
        return iface;
    }

    /** {@inheritDoc} */
    public ModelImport addNewNodeToGroup(String groupName, String nodeLabel) {
        ModelImport group = m_provisioningDao.get(groupName);
        
        Node node = createNode(nodeLabel, String.valueOf(System.currentTimeMillis()));
        node.setBuilding(groupName);
        
        group.addNode(0, node);
        
        m_provisioningDao.save(groupName, group);
        return m_provisioningDao.get(groupName);
    }

    private Node createNode(String nodeLabel, String foreignId) {
        Node node = new Node();
        node.setNodeLabel(nodeLabel);
        node.setForeignId(foreignId);
        return node;
    }

    /** {@inheritDoc} */
    public ModelImport addServiceToInterface(String groupName, String pathToInterface, String serviceName) {
        ModelImport group = m_provisioningDao.get(groupName);
        
        Interface iface = BeanUtils.getPathValue(group, pathToInterface, Interface.class);
        
        MonitoredService monSvc = createService(serviceName);
        iface.addMonitoredService(0, monSvc);

        
        m_provisioningDao.save(groupName, group);
        
        return m_provisioningDao.get(groupName);
    }

    /** {@inheritDoc} */
    public ModelImport getProvisioningGroup(String name) {
        return m_provisioningDao.get(name);
    }
    
    /** {@inheritDoc} */
    public ModelImport saveProvisioningGroup(String groupName, ModelImport group) {
        m_provisioningDao.save(groupName, group);
        return m_provisioningDao.get(groupName);
    }

    /**
     * <p>getProvisioningGroupNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> getProvisioningGroupNames() {
        return m_provisioningDao.getProvisioningGroupNames();
    }
    
    /** {@inheritDoc} */
    public ModelImport createProvisioningGroup(String name) {
        ModelImport group = new ModelImport();
        group.setForeignSource(name);
        
        m_provisioningDao.save(name, group);
        return m_provisioningDao.get(name);
    }

    private MonitoredService createService(String serviceName) {
        MonitoredService svc = new MonitoredService();
        svc.setServiceName(serviceName);
        return svc;
    }


    /** {@inheritDoc} */
    public void importProvisioningGroup(String groupName) {

        // first we update the import timestamp
        ModelImport group = getProvisioningGroup(groupName);
        group.setLastImport(new Date());
        saveProvisioningGroup(groupName, group);
        
        
        // then we send an event to the importer
        EventProxy proxy = Util.createEventProxy();
        
        String url = m_provisioningDao.getUrlForGroup(groupName);
        Assert.notNull(url, "Could not find url for group "+groupName+".  Does it exists?");
        
        EventBuilder bldr = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, "Web");
        bldr.addParam(EventConstants.PARM_URL, url);
        
        try {
            proxy.send(bldr.getEvent());
        } catch (EventProxyException e) {
            throw new DataAccessResourceFailureException("Unable to send event to import group "+groupName, e);
        }
    }

    private static class PropertyPath {
        private PropertyPath parent = null;
        private String propertyName;
        private String key;
        
        PropertyPath(String nestedPath) {
            String canonicalPath = PropertyAccessorUtils.canonicalPropertyName(nestedPath);
            int lastIndex = PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(canonicalPath);
            if (lastIndex < 0) {
                propertyName = PropertyAccessorUtils.getPropertyName(canonicalPath);
                key = computeKey(canonicalPath);
            } 
            else {
                parent = new PropertyPath(canonicalPath.substring(0, lastIndex));
                String lastProperty = canonicalPath.substring(lastIndex+1);
                propertyName = PropertyAccessorUtils.getPropertyName(lastProperty);
                key = computeKey(lastProperty);
            }
        }

        private String computeKey(String property) {
            int keyPrefix = property.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);
            if (keyPrefix < 0)
                return "";
            
            int keySuffix = property.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR);
            return property.substring(keyPrefix+1, keySuffix);
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder(parent == null ? "" : parent.toString()+'.');
            buf.append(propertyName);
            if (key.length() > 0) {
                buf.append(PropertyAccessor.PROPERTY_KEY_PREFIX);
                buf.append(key);
                buf.append(PropertyAccessor.PROPERTY_KEY_SUFFIX);
            }
            return buf.toString();
        }

        public String getKey() {
            return key;
        }

        public PropertyPath getParent() {
            return parent;
        }

        public String getPropertyName() {
            return propertyName;
        }
        
        public Object getValue(Object root) {
            return getValue(new BeanWrapperImpl(root));
        }
        
        public Object getValue(BeanWrapper beanWrapper) {
            return beanWrapper.getPropertyValue(toString());
        }
        
    }
    
    /** {@inheritDoc} */
    public ModelImport deletePath(String groupName, String pathToDelete) {
        ModelImport group = m_provisioningDao.get(groupName);

        PropertyPath path = new PropertyPath(pathToDelete);
        
        Object objToDelete = path.getValue(group);
        Object parentObject = path.getParent() == null ? group : path.getParent().getValue(group);
        
        String propName = path.getPropertyName();
        String methodSuffix = Character.toUpperCase(propName.charAt(0))+propName.substring(1);
        String methodName = "remove"+methodSuffix;

        
        try {
            MethodUtils.invokeMethod(parentObject, methodName, new Object[] { objToDelete });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Unable to find method "+methodName+" on object of type "+parentObject.getClass(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("unable to access property "+pathToDelete, e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("an execption occurred deleting "+pathToDelete, e);
        }
        
        m_provisioningDao.save(groupName, group);
    
        return m_provisioningDao.get(groupName);
    }

    /**
     * <p>getAllGroups</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<ModelImport> getAllGroups() {
        Collection<ModelImport> groups = new LinkedList<ModelImport>();
        
        for(String groupName : getProvisioningGroupNames()) {
            groups.add(getProvisioningGroup(groupName));
        }
        
        return groups;
    }

    /** {@inheritDoc} */
    public void deleteProvisioningGroup(String groupName) {
        m_provisioningDao.delete(groupName);
    }

    /** {@inheritDoc} */
    public void deleteAllNodes(String groupName) {
        ModelImport group = m_provisioningDao.get(groupName);
        group.removeAllNode();
        m_provisioningDao.save(groupName, group);
    }

    /**
     * <p>getGroupDbNodeCounts</p>
     *
     * @return a java$util$Map object.
     */
    public Map<String, Integer> getGroupDbNodeCounts() {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        
        for(String groupName : getProvisioningGroupNames()) {
            counts.put(groupName, m_nodeDao.getNodeCountForForeignSource(groupName));
        }
        
        return counts;
        
    }

    /**
     * <p>getNodeCategoryNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> getNodeCategoryNames() {
        Collection<String> names = new LinkedList<String>();
        for (OnmsCategory category : m_categoryDao.findAll()) {
            names.add(category.getName());
        }
        return names;
    }

    /**
     * <p>getServiceTypeNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> getServiceTypeNames() {
        Collection<String> names = new LinkedList<String>();
        for(OnmsServiceType svcType : m_serviceTypeDao.findAll()) {
            names.add(svcType.getName());
        }
        return names;
    }



}
