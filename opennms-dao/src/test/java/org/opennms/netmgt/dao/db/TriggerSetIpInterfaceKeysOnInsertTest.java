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
package org.opennms.netmgt.dao.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.AssertionFailedError;

import org.opennms.test.ThrowableAnticipator;

public class TriggerSetIpInterfaceKeysOnInsertTest extends
        PopulatedTemporaryDatabaseTestCase {

    public void testSetIpInterfaceIdInIfService() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', 1, 1)");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, ipInterfaceID from ifServices");
            assertTrue("could not advance to read first row in ResultSet",
                       rs.next());
            assertEquals("expected ifServices id", 3, rs.getInt(1));
            assertEquals("expected ifServices ipInterfaceId", 2, rs.getInt(2));
            assertFalse("ResultSet contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }


    public void testSetIpInterfaceIdInIfServiceAllZeroesIpAddress() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '0.0.0.0', 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '0.0.0.0', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError("Could not execute statement: 'INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '0.0.0.0', 1, 1)': ERROR: IfServices Trigger Exception, Condition 0: ipAddr of 0.0.0.0 is not allowed in ifServices table"));
        try {
            executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '0.0.0.0', 1, 1)");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testSetIpInterfaceIdInIfServiceNullIfIndexBoth()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', null, 1)");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, ipInterfaceID from ifServices");
            assertTrue("could not advance to read first row in ResultSet",
                       rs.next());
            assertEquals("expected ifServices id", 2, rs.getInt(1));
            assertEquals("expected ifServices ipInterfaceId", 1, rs.getInt(2));
            assertFalse("ResultSet contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }

    public void testSetIpInterfaceIdInIfServiceNullIfIndexInIfServices()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', null, 1)");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, ipInterfaceID from ifServices");
            assertTrue("could not advance to read first row in ResultSet",
                       rs.next());
            assertFalse("ResultSet contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }

    public void testSetIpInterfaceIdInIfServiceNullIfIndexInIpInterface()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id from ipInterface");
            assertTrue("could not advance to read first results row from ipInterface",
                       rs.next());
            int ipInterfaceId = rs.getInt(1);

            executeSQL("INSERT INTO ifServices (ipInterfaceId, serviceID) VALUES ( "
                       + ipInterfaceId + ", 1)");

            st = connection.createStatement();
            rs = st.executeQuery("SELECT id, ipInterfaceID from ifServices");
            assertTrue("could not advance to read first row in ResultSet",
                       rs.next());
            rs.getInt(1);
            assertFalse("ifServices.id should be non-null", rs.wasNull());
            assertEquals("ifServices.ipInterfaceId", ipInterfaceId, rs.getInt(2));
            assertFalse("ResultSet contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }

}
