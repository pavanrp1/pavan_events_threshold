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
 * Created August 22, 2007
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
package org.opennms.netmgt.ping;

import java.net.InetAddress;

import org.opennms.protocols.icmp.ICMPEchoPacket;

/**
 * <p>PingResponseCallback interface.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @version $Id: $
 */
public interface PingResponseCallback {

	/**
	 * <p>handleResponse</p>
	 *
	 * @param address a {@link java.net.InetAddress} object.
	 * @param packet a {@link org.opennms.protocols.icmp.ICMPEchoPacket} object.
	 */
	public void handleResponse(InetAddress address, ICMPEchoPacket packet);
	/**
	 * <p>handleTimeout</p>
	 *
	 * @param address a {@link java.net.InetAddress} object.
	 * @param packet a {@link org.opennms.protocols.icmp.ICMPEchoPacket} object.
	 */
	public void handleTimeout(InetAddress address, ICMPEchoPacket packet);
    /**
     * <p>handleError</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param packet a {@link org.opennms.protocols.icmp.ICMPEchoPacket} object.
     * @param t a {@link java.lang.Throwable} object.
     */
    public void handleError(InetAddress address, ICMPEchoPacket packet, Throwable t);

}
