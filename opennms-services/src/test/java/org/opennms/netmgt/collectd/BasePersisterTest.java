/*
 * This file is part of the OpenNMS(R) Application.
 * 
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.mock.MockPlatformTransactionManager;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.test.FileAnticipator;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.transaction.PlatformTransactionManager;
/**
 * JUnit TestCase for the BasePersister.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class BasePersisterTest extends TestCase {
    private static boolean s_rrdInitialized = false;
    
    private FileAnticipator m_fileAnticipator;
    private File m_snmpDirectory;
    private BasePersister m_persister;
    private OnmsIpInterface m_intf;
    private OnmsNode m_node;
    private PlatformTransactionManager m_transMgr = new MockPlatformTransactionManager();
    private EasyMockUtils m_easyMockUtils = new EasyMockUtils();
    private IpInterfaceDao m_ifDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockUtil.println("------------ Begin Test " + getName() + " --------------------------");
        MockLogAppender.setupLogging();

        m_fileAnticipator = new FileAnticipator();
        
        m_intf = new OnmsIpInterface();
        m_node = new OnmsNode();
        m_node.setId(1);
        m_intf.setId(25);
        m_intf.setNode(m_node);
        m_intf.setIpAddress("1.1.1.1");
        
        m_ifDao = m_easyMockUtils.createMock(IpInterfaceDao.class);
        
        // Grumble grumble... side effects... grumble grumble
        if (!s_rrdInitialized) {
            RrdConfig.setProperties(new Properties());
            RrdUtils.setStrategy(new JRobinRrdStrategy());
            RrdUtils.initialize();
            s_rrdInitialized  = true;
        }
    }
    
    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
        m_fileAnticipator.deleteExpected();
    }
    
    @Override
    protected void tearDown() throws Exception {
        m_fileAnticipator.deleteExpected(true);
        m_fileAnticipator.tearDown();
        MockUtil.println("------------ End Test " + getName() + " --------------------------");
        super.tearDown();
    }
    
    public void testPersistStringAttributeWithExistingPropertiesFile() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.tempDir(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.tempFile(nodeDir, "strings.properties", "#just a test");
        
        CollectionAttribute attribute = buildStringAttribute();
        m_persister.persistStringAttribute(attribute);
    }
    
    public void testPersistStringAttributeWithParentDirectory() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.tempDir(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.expecting(nodeDir, "strings.properties");
        
        CollectionAttribute attribute = buildStringAttribute();
        m_persister.persistStringAttribute(attribute);
    }
    
    public void testPersistStringAttributeWithNoParentDirectory() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.expecting(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.expecting(nodeDir, "strings.properties");
        
        CollectionAttribute attribute = buildStringAttribute();
        m_persister.persistStringAttribute(attribute);
    }
    
    /**
     * Test for bug #1817 where a string attribute will get persisted to
     * both strings.properties and an RRD file if it is a numeric value.
     */
    public void testPersistStringAttributeUsingBuilder() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.expecting(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.expecting(nodeDir, "strings.properties");
        
        CollectionAttribute attribute = buildStringAttribute();

        m_persister.pushShouldPersist(attribute.getResource());
        
        m_persister.pushShouldPersist(attribute);

        m_persister.createBuilder(attribute.getResource(), attribute.getName(), attribute.getAttributeType());

        // This will end up calling m_persister.persistStringAttribute(attribute);
        m_persister.storeAttribute(attribute);

        m_persister.commitBuilder();
        
        m_persister.popShouldPersist();
    }

    private SnmpAttribute buildStringAttribute() {
        
        EasyMock.expect(m_ifDao.load(m_intf.getId())).andReturn(m_intf).anyTimes();
        
        m_easyMockUtils.replayAll();
        
        CollectionAgent agent = DefaultCollectionAgent.create(m_intf.getId(), m_ifDao, m_transMgr);
        
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();
        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, String>()), dataCollectionConfig);
        
        NodeResourceType resourceType = new NodeResourceType(agent, collection);
        
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        
        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.1.1.1");
        mibObject.setAlias("mibObjectAlias");
        mibObject.setType("string");
        mibObject.setInstance("0");
        mibObject.setMaxval(null);
        mibObject.setMinval(null);
        
        SnmpAttributeType attributeType = new StringAttributeType(resourceType, "some-collection", mibObject, new AttributeGroupType("mibGroup", "ignore"));
        
        return new SnmpAttribute(resource, attributeType, SnmpUtils.getValueFactory().getOctetString("foo".getBytes()));
    }

    private void initPersister() throws IOException {
        m_persister = new BasePersister();
        m_persister.setRepository(createRrdRepository());
    }

    private RrdRepository createRrdRepository() throws IOException {
        RrdRepository repository = new RrdRepository();
        repository.setRrdBaseDir(getSnmpRrdDirectory());
        repository.setHeartBeat(600);
        repository.setStep(300);
        repository.setRraList(Collections.singletonList("RRA:AVERAGE:0.5:1:100"));
        return repository;
    }

    private File getSnmpRrdDirectory() throws IOException {
        if (m_snmpDirectory == null) {
            m_snmpDirectory = m_fileAnticipator.tempDir("snmp"); 
        }
        return m_snmpDirectory;
    }
}
