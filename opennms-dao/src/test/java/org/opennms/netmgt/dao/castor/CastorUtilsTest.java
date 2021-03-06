/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created July 28, 2008
 *
 * Copyright (C) 2008 Daniel J. Gregor, Jr..  All rights reserved.
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
package org.opennms.netmgt.dao.castor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;

/**
 * Test class for CastorUtils.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class CastorUtilsTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockLogAppender.setupLogging();
    }
    
    @Override
    protected void runTest() throws Throwable {
        super.runTest();

        MockLogAppender.assertNoWarningsOrGreater();
    }
    
    @SuppressWarnings("deprecation")
    public void testUnmarshalReader() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        CastorUtils.unmarshal(Userinfo.class, ConfigurationTestUtils.getReaderForConfigFile("users.xml"));
    }

    public void testUnmarshalResource() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        CastorUtils.unmarshal(Userinfo.class, new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("users.xml")));
    }
    
    public void testExceptionContainsFileNameUnmarshalResourceWithBadResource() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        /*
         * We are going to attempt to unmarshal groups.xml with the wrong
         * class so we get a MarshalException and we can then test to see if the
         * file name is embedded in the exception.
         */
        boolean gotException = false;
        File file = ConfigurationTestUtils.getFileForConfigFile("groups.xml");
        try {
            CastorUtils.unmarshal(Userinfo.class, new FileSystemResource(file));
        } catch (MarshalException e) {
            String matchString = file.getAbsolutePath();
            if (e.toString().contains(matchString)) {
                gotException = true;
            } else {
                AssertionFailedError ae = new AssertionFailedError("Got an exception, but not one containing the message we were expecting ('" + matchString + "'): " + e);
                ae.initCause(e);
                throw ae;
            }
        }
        
        if (!gotException) {
            fail("Did not get a MarshalException, but we were expecting one.");
        }
    }
    
    public void testUnmarshalInputStreamQuietly() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        CastorUtils.unmarshal(Userinfo.class, ConfigurationTestUtils.getInputStreamForConfigFile("users.xml"));
        
        /*
         * Ensure that nothing was logged.
         * In particular, we want to make sure that we don't see this message:
         * 2008-07-28 16:04:53,260 DEBUG [main] org.exolab.castor.xml.Unmarshaller: *static* unmarshal method called, this will ignore any mapping files or changes made to an Unmarshaller instance.
         */
        MockLogAppender.assertNotGreaterOrEqual(Level.DEBUG);
    }
    
    @SuppressWarnings("deprecation")
    public void testUnmarshalReaderQuietly() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        CastorUtils.unmarshal(Userinfo.class, ConfigurationTestUtils.getReaderForConfigFile("users.xml"));
        
        /*
         * Ensure that nothing was logged.
         * In particular, we want to make sure that we don't see this message:
         * 2008-07-28 16:04:53,260 DEBUG [main] org.exolab.castor.xml.Unmarshaller: *static* unmarshal method called, this will ignore any mapping files or changes made to an Unmarshaller instance.
         */
        MockLogAppender.assertNotGreaterOrEqual(Level.DEBUG);
    }
    
    public void testUnmarshallInputStreamWithUtf8() throws MarshalException, ValidationException, IOException {
        Userinfo users = CastorUtils.unmarshal(Userinfo.class, ConfigurationTestUtils.getInputStreamForResource(this, "/users-utf8.xml"));
        
        assertEquals("user count", 1, users.getUsers().getUserCount());
        // \u00f1 is unicode for n~ 
        assertEquals("user name", "Admi\u00f1istrator", users.getUsers().getUser(0).getFullName());
    }
    
    public void testUnmarshallResourceWithUtf8() throws MarshalException, ValidationException, IOException {
        Userinfo users = CastorUtils.unmarshal(Userinfo.class, new InputStreamResource(ConfigurationTestUtils.getInputStreamForResource(this, "/users-utf8.xml")));
        
        assertEquals("user count", 1, users.getUsers().getUserCount());
        // \u00f1 is unicode for n~ 
        assertEquals("user name", "Admi\u00f1istrator", users.getUsers().getUser(0).getFullName());
    }
}
