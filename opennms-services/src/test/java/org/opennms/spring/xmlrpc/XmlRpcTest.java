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
 * Created: August 16, 2006
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
package org.opennms.spring.xmlrpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.xmlrpc.WebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.remoting.RemoteAccessException;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class XmlRpcTest {
    
    private WebServer m_webServer;

    @Before
    public void setUp() throws Exception {
        
        MockLogAppender.setupLogging();
        
        XmlRpcWebServerFactoryBean wsf = new XmlRpcWebServerFactoryBean();
        wsf.setPort(9192);
        wsf.setSecure(false);
        wsf.afterPropertiesSet();
        
        m_webServer = (WebServer)wsf.getObject();
    }
    
    @After
    public void tearDown() {
        m_webServer.shutdown();
        
    }

    @Test
	public void testXmlRpcProxyFactoryBeanAndServiceExporter() throws Throwable {
		TestBean target = new TestBean("myname", 99);
        
        
		final XmlRpcServiceExporter exporter = new XmlRpcServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
        exporter.setWebServer(m_webServer);
		exporter.afterPropertiesSet();

		XmlRpcProxyFactoryBean pfb = new XmlRpcProxyFactoryBean();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://localhost:9192/RPC2");
		pfb.afterPropertiesSet();

        ITestBean proxy = (ITestBean) pfb.getObject();
		assertEquals("myname", proxy.getName());
		assertEquals(99, proxy.getAge());
		proxy.setAge(50);
		assertEquals(50, proxy.getAge());

	}

    @Test
    @Ignore("We're not set up for HTTPS for these tests.")
    public void testXmlRpcProxyFactoryBeanAndServiceExporterWithHttps() throws Throwable {
        TestBean target = new TestBean("myname", 99);
        
        
        final XmlRpcServiceExporter exporter = new XmlRpcServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setWebServer(m_webServer);
        exporter.afterPropertiesSet();

        XmlRpcProxyFactoryBean pfb = new XmlRpcProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("https://localhost:9192/RPC2");
        pfb.afterPropertiesSet();

        ITestBean proxy = (ITestBean) pfb.getObject();
        assertEquals("myname", proxy.getName());
        assertEquals(99, proxy.getAge());
        proxy.setAge(50);
        assertEquals(50, proxy.getAge());

    }

    @Test
    @Ignore("If you're using OpenDNS, myurl gives an IP address")
	public void testXmlRpcProxyFactoryBeanAndServiceExporterWithIOException() throws Exception {
		TestBean target = new TestBean("myname", 99);

		final XmlRpcServiceExporter exporter = new XmlRpcServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
        exporter.setWebServer(m_webServer);
		exporter.afterPropertiesSet();

		XmlRpcProxyFactoryBean pfb = new XmlRpcProxyFactoryBean();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://myurl"); // this is wrong so we throw an exception
		pfb.afterPropertiesSet();

        ITestBean proxy = (ITestBean) pfb.getObject();
		try {
			proxy.setAge(50);
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
			assertTrue(ex.getCause() instanceof IOException);
		}
	}
    
    public static interface ITestBean {
        public String getName();
        public int getAge();
        public void setAge(int age);
    }
    
     static class TestBean implements ITestBean {
        private String name;
        private int age;

        TestBean(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        public String getName() {
            return this.name;
        }
        
        public int getAge() {
            return this.age;
        }
        
        public void setAge(int age) {
            this.age = age;
        }
    }



}
