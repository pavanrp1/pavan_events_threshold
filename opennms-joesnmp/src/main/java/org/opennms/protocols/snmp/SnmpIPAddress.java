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

package org.opennms.protocols.snmp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.protocols.snmp.asn1.AsnDecodingException;
import org.opennms.protocols.snmp.asn1.AsnEncoder;

/**
 * <p>
 * This SnmpIPAddress is used to extend the Snmp Octet String SMI class. This is
 * normally used to transmit IP Addresses with a length of 4 bytes.
 * </p>
 *
 * <p>
 * Most of the management of the data is handled by the base class.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @version $Id: $
 */
public class SnmpIPAddress extends SnmpOctetString {
    /**
     * Required for evolving serialization format.
     */
    static final long serialVersionUID = -4375760318106654741L;

    /**
     * Defines the ASN.1 type for this object.
     */
    public static final byte ASNTYPE = SnmpSMI.SMI_APPSTRING;

    /**
     * Constructs a default object with a length of zero. See the super class
     * constructor for more details.
     */
    public SnmpIPAddress() {
        byte[] tmp = { 0, 0, 0, 0 };
        super.assumeString(tmp);
    }

    /**
     * Constructs an Application String with the passed data. The data is
     * managed by the base class.
     *
     * @param data
     *            The application string to manage (UTF-8)
     * @throws java.security.InvalidParameterException
     *             Thrown if the passed buffer is not exactly 4 octets in size.
     */
    public SnmpIPAddress(byte[] data) {
        super(data);
        if (data.length < 4)
            throw new java.security.InvalidParameterException("Buffer underflow error converting IP address");
        else if (data.length > 4)
            throw new java.security.InvalidParameterException("Buffer overflow error converting IP address");
    }

    /**
     * Copy constructor. Constructs a duplicate object based on the passed
     * application string object.
     *
     * @param second
     *            The object to copy.
     */
    public SnmpIPAddress(SnmpIPAddress second) {
        super(second);
    }

    /**
     * Copy constructor based on the base class.
     *
     * @param second
     *            The object to copy
     * @throws java.security.InvalidParameterException
     *             Thrown if the passed buffer is not exactly 4 octets in size.
     */
    public SnmpIPAddress(SnmpOctetString second) {
        super(second);
        if (getLength() < 4)
            throw new java.security.InvalidParameterException("Buffer underflow error converting IP address");
        else if (getLength() > 4)
            throw new java.security.InvalidParameterException("Buffer overflow error converting IP address");
    }

    /**
     * Constructs a new instance of the class with the IP Address recovered from
     * the passed address object.
     *
     * @param inetAddr
     *            The internet address instance that contains the IP Address.
     */
    public SnmpIPAddress(InetAddress inetAddr) {
        this(inetAddr.getAddress());
    }

    /**
     * Constructs a new instance of the class with the IP address of the
     * evaluated argument. The argument is evaluated by the
     * {@link java.net.InetAddress#getByName InetAddress}class and the returned
     * address is encoded in this instance.
     *
     * @param inetAddr
     *            The string encoded IP Address to encapsulate.
     * @exception SnmpBadConversionException
     *                Thrown if the string address cannot be converted to an IP
     *                Address.
     * @throws org.opennms.protocols.snmp.SnmpBadConversionException if any.
     */
    public SnmpIPAddress(String inetAddr) throws SnmpBadConversionException {
        try {
            byte[] data = InetAddress.getByName(inetAddr).getAddress();
            super.assumeString(data);
        } catch (UnknownHostException uhE) {
            throw new SnmpBadConversionException(uhE);
        }
    }

    /**
     * Returns the ASN.1 type for this object.
     *
     * @return The ASN.1 value for this object.
     */
    public byte typeId() {
        return SnmpSMI.SMI_APPSTRING;
    }

    /**
     * Create a new object that is a duplicate of the current object.
     *
     * @return A newly created duplicate object.
     */
    public SnmpSyntax duplicate() {
        return new SnmpIPAddress(this);
    }

    /**
     * Create a new object that is a duplicate of the current object.
     *
     * @return A newly created duplicate object.
     */
    public Object clone() {
        return new SnmpIPAddress(this);
    }

    /**
     * <p>
     * Sets the internal string array so that it is identical to the passed
     * array. The array is actually copied so that changes to data after the
     * construction of the object are not reflected in the SnmpOctetString
     * Object.
     * </p>
     *
     * <p>
     * If the buffer is not valid according to the SNMP SMI then an exception is
     * thrown and the object is not modified.
     * </p>
     *
     * @param data
     *            The new octet string data.
     * @throws java.security.InvalidParameterException
     *             Thrown if the passed buffer is not valid against the SMI
     *             definition.
     */
    public void setString(byte[] data) {
        if (data == null || data.length < 4)
            throw new java.security.InvalidParameterException("Buffer underflow error converting IP address");
        else if (data.length > 4)
            throw new java.security.InvalidParameterException("Buffer overflow error converting IP address");

        // use setString instead of assumeString to ensure
        // that a duplicate copy of the buffer is made.
        //
        super.setString(data);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Sets the internal octet string equal to the converted stirng via the
     * method getBytes(). This may cause some data corruption since the
     * conversion is platform specific.
     * </p>
     *
     * <p>
     * If the buffer is not valid according to the SNMP SMI then an exception is
     * thrown and the object is not modified.
     * </p>
     * @see java.lang.String#getBytes()
     */
    public void setString(String data) {
        byte[] bdata = (data == null ? null : data.getBytes());
        if (bdata == null || bdata.length < 4)
            throw new java.security.InvalidParameterException("Buffer underflow error converting IP address");
        else if (bdata.length > 4)
            throw new java.security.InvalidParameterException("Buffer overflow error converting IP address");

        super.assumeString(bdata);
    }

    /**
     * {@inheritDoc}
     *
     * Decodes the ASN.1 octet string from the passed buffer. If an error occurs
     * during the decoding sequence then an AsnDecodingException is thrown by
     * the method. The value is decoded using the AsnEncoder passed to the
     * object.
     * @exception AsnDecodingException
     *                Thrown by the encoder if an error occurs trying to decode
     *                the data buffer.
     */
    public int decodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnDecodingException {
        Object[] rVals = encoder.parseString(buf, offset);

        if (((Byte) rVals[1]).byteValue() != typeId())
            throw new AsnDecodingException("Invalid ASN.1 type");

        byte[] data = (byte[]) rVals[2];
        if (data.length < 4)
            throw new AsnDecodingException("Buffer Underflow Exception, length = " + data.length);
        else if (data.length > 4)
            throw new AsnDecodingException("Buffer Overflow Exception, length = " + data.length);

        super.assumeString(data);

        return ((Integer) rVals[0]).intValue();
    }

    /**
     * Converts the current Application String to an IPv4Address object. If the
     * length is not four bytes in length or an error occurs during the
     * conversion then an exception is thrown.
     *
     * @return The IPv4Address converted from the appliation string
     * @exception SnmpBadConversionException
     *                Thrown if the length of the string is invalid. Must be
     *                equal to four
     */
    public InetAddress convertToIpAddress() {
        byte[] data = getString();

        byte addr[] = new byte[4];
        addr[0] = data[0];
        addr[1] = data[1];
        addr[2] = data[2];
        addr[3] = data[3];
                       
        try {
            return InetAddress.getByAddress(addr);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to convert "+this+" to an InetAddress", e);
        }
    }

    /**
     * Returns the application string as a IPv4 dotted decimal address
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        byte[] data = getString();

        StringBuffer buf = new StringBuffer();
        buf.append((int) (data[0] < 0 ? 256 + data[0] : data[0])).append('.');
        buf.append((int) (data[1] < 0 ? 256 + data[1] : data[1])).append('.');
        buf.append((int) (data[2] < 0 ? 256 + data[2] : data[2])).append('.');
        buf.append((int) (data[3] < 0 ? 256 + data[3] : data[3]));

        return buf.toString();
    }

    /**
     * <p>toInetAddress</p>
     *
     * @param val a {@link org.opennms.protocols.snmp.SnmpIPAddress} object.
     * @return a {@link java.net.InetAddress} object.
     */
    public static InetAddress toInetAddress(SnmpIPAddress val) {
        return (val == null ? null : val.convertToIpAddress());
    }

}
