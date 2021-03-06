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
// Tab Size = 8
//
//
package org.opennms.protocols.snmp;

import java.io.Serializable;

import org.opennms.protocols.snmp.asn1.AsnDecodingException;
import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * This class defines the SNMP 32-bit signed integer used by the SNMP SMI. This
 * class also serves as a base class for any additional SNMP SMI types that
 * exits now or may be defined in the future.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @version $Id: $
 */
public class SnmpInt32 extends Object implements SnmpSyntax, Cloneable, Serializable {
    /**
     * The internal 32-bit signed quantity
     */
    private int m_value;

    /**
     * Added for serialization support
     */
    static final long serialVersionUID = -3472172482048507843L;

    /**
     * The ASN.1 type as defined by the SNMP SMI specification.
     */
    public static final byte ASNTYPE = SnmpSMI.SMI_INTEGER;

    /**
     * Default constructor. Builds a SnmpInt32 objeect with a value of zero.
     */
    public SnmpInt32() {
        m_value = 0;
    }

    /**
     * Constructs a SnmpInt32 object with the passed value.
     *
     * @param value
     *            The 32-bit signed integer value for the object.
     */
    public SnmpInt32(int value) {
        m_value = value;
    }

    /**
     * Constructs a SnmpInt32 object with the specified value.
     *
     * @param value
     *            An Integer object containing the 32-bte value.
     */
    public SnmpInt32(Integer value) {
        m_value = value.intValue();
    }

    /**
     * Class copy constructor. Constructs a new object with the same value as
     * the passed SnmpInt32 object.
     *
     * @param second
     *            The object to get the value from.
     */
    public SnmpInt32(SnmpInt32 second) {
        m_value = second.m_value;
    }

    /**
     * Simple class constructor that attempts to parse the passed string into a
     * valid integer value. If the String argument cannot be parse because it is
     * either invalid or malformed then an exception is generated.
     *
     * @param value
     *            The integer value represented as a String
     * @throws java.lang.NumberFormatException
     *             Thrown if the passed value cannot be turned into a valid
     *             integer.
     * @throws java.lang.NullPointerException
     *             Thrown if the passed string is a null reference.
     */
    public SnmpInt32(String value) {
        if (value == null)
            throw new NullPointerException("The constructor argument may not be null");

        // May throw a NumberFormatException
        //
        m_value = Integer.parseInt(value);
    }

    /**
     * Used to access the internal 32-bit signed quantity.
     *
     * @return The 32-bit value
     */
    public int getValue() {
        return m_value;
    }

    /**
     * Used to set the 32-bit signed quantity
     *
     * @param value
     *            The new value for the object.
     */
    public void setValue(int value) {
        m_value = value;
    }

    /**
     * Used to set the 32-bit signed quantity
     *
     * @param value
     *            The new value for the object
     */
    public void setValue(Integer value) {
        m_value = value.intValue();
    }

    /**
     * Used to retreive the ASN.1 type for this object.
     *
     * @return The ASN.1 value for the SnmpInt32
     */
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * {@inheritDoc}
     *
     * Used to encode the integer value into an ASN.1 buffer. The passed encoder
     * defines the method for encoding the data.
     */
    public int encodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnEncodingException {
        return encoder.buildInteger32(buf, offset, typeId(), m_value);
    }

    /**
     * {@inheritDoc}
     *
     * Used to decode the integer value from the ASN.1 buffer. The passed
     * encoder is used to decode the ASN.1 information and the integer value is
     * stored in the internal object.
     */
    public int decodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnDecodingException {
        Object[] rVals = encoder.parseInteger32(buf, offset);

        if (((Byte) rVals[1]).byteValue() != typeId())
            throw new AsnDecodingException("Invalid ASN.1 type");

        m_value = ((Integer) rVals[2]).intValue();

        return ((Integer) rVals[0]).intValue();
    }

    /**
     * Returns a duplicate of the current object.
     *
     * @return A newly allocated duplicate object.
     */
    public SnmpSyntax duplicate() {
        return new SnmpInt32(this);
    }

    /**
     * Returns a duplicate of the current object.
     *
     * @return A newly allocated duplicate object.
     */
    public Object clone() {
        return new SnmpInt32(this);
    }

    /**
     * Returns the string representation of the object.
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return Integer.toString(getValue());
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (obj instanceof SnmpInt32 ) {
            SnmpInt32 int32 = (SnmpInt32)obj;
          
            return (typeId() == int32.typeId() && getValue() == int32.getValue());
        }
        return false;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return 0;
    }

    /**
     * <p>toInteger</p>
     *
     * @param val a {@link org.opennms.protocols.snmp.SnmpInt32} object.
     * @return a {@link java.lang.Integer} object.
     */
    public static Integer toInteger(SnmpInt32 val) {
        return (val == null ? null : new Integer(val.getValue()));
    }
}
