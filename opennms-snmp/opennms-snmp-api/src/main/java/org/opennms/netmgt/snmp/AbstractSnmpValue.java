//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.snmp;

import java.math.BigInteger;
import java.net.InetAddress;

/**
 * <p>Abstract AbstractSnmpValue class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractSnmpValue implements SnmpValue {
    
    private int m_type;
    
    /**
     * <p>Constructor for AbstractSnmpValue.</p>
     *
     * @param type a int.
     */
    public AbstractSnmpValue(int type) {
        m_type = type;
    }

    /**
     * <p>isEndOfMib</p>
     *
     * @return a boolean.
     */
    public boolean isEndOfMib() {
        return false;
    }

    /**
     * <p>toDisplayString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toDisplayString() {
        return toString();
    }

    /**
     * <p>isNumeric</p>
     *
     * @return a boolean.
     */
    public boolean isNumeric() {
        return false;
    }

    /**
     * <p>toInt</p>
     *
     * @return a int.
     */
    public int toInt() {
        return (int)toLong();
    }

    /**
     * <p>toInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress toInetAddress() {
        throw new IllegalArgumentException("Unable to convert " + this + " to an InetAddress");
    }

    /**
     * <p>toLong</p>
     *
     * @return a long.
     */
    public long toLong() {
        throw new IllegalArgumentException("Unable to convert " + this + " to a number");
    }

    /**
     * <p>toHexString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toHexString() {
        throw new IllegalArgumentException("Unable to convert " + this + " to a hex string");
    }

    /**
     * <p>getType</p>
     *
     * @return a int.
     */
    public int getType() {
        return m_type;
    }

    /**
     * <p>toBigInteger</p>
     *
     * @return a {@link java.math.BigInteger} object.
     */
    public BigInteger toBigInteger() {
        throw new IllegalArgumentException("Unable to convert " + this + " to a big integer");
    }

    /**
     * <p>toSnmpObjId</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public SnmpObjId toSnmpObjId() {
        throw new IllegalArgumentException("Unable to convert " + this + " to an SnmpObjId");
    }


}
