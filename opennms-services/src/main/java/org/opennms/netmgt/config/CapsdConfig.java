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
// Modifications:
//
// 2007 May 06: Moved plugin management and database synchronization
//              code out of CapsdConfigManager. - dj@opennms.org
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
package org.opennms.netmgt.config;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.capsd.CapsdConfiguration;
import org.opennms.netmgt.config.capsd.Property;
import org.opennms.netmgt.config.capsd.ProtocolConfiguration;
import org.opennms.netmgt.config.capsd.ProtocolPlugin;
import org.opennms.netmgt.config.capsd.SmbAuth;
import org.opennms.netmgt.config.common.Range;

/**
 * <p>CapsdConfig interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CapsdConfig {
    /**
     * This integer value is used to represent the primary snmp interface
     * ifindex in the ipinterface table for SNMP hosts that don't support
     * the MIB2 ipAddrTable
     */
    public static final int LAME_SNMP_HOST_IFINDEX = -100;

    /**
     * Saves the current in-memory configuration to disk and reloads
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public abstract void save() throws MarshalException, IOException, ValidationException;

    /**
     * Return the Capsd configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.capsd.CapsdConfiguration} object.
     */
    public abstract CapsdConfiguration getConfiguration();

    /**
     * Finds the SMB authentication object using the netbios name.
     *
     * The target of the search.
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.capsd.SmbAuth} object.
     */
    public abstract SmbAuth getSmbAuth(String target);

    /**
     * Checks the configuration to determine if the target is managed or
     * unmanaged.
     *
     * @param target
     *            The target to check against.
     * @return a boolean.
     */
    public abstract boolean isAddressUnmanaged(InetAddress target);

    /**
     * <p>getRescanFrequency</p>
     *
     * @return a long.
     */
    public abstract long getRescanFrequency();

    /**
     * <p>getInitialSleepTime</p>
     *
     * @return a long.
     */
    public abstract long getInitialSleepTime();

    /**
     * <p>getMaxSuspectThreadPoolSize</p>
     *
     * @return a int.
     */
    public abstract int getMaxSuspectThreadPoolSize();

    /**
     * <p>getMaxRescanThreadPoolSize</p>
     *
     * @return a int.
     */
    public abstract int getMaxRescanThreadPoolSize();

    /**
     * Defines Capsd's behavior when, during a protocol scan, it gets a
     * java.net.NoRouteToHostException exception. If abort rescan property is
     * set to "true" then Capsd will not perform any additional protocol scans.
     *
     * @return a boolean.
     */
    public abstract boolean getAbortProtocolScansFlag();

    /**
     * <p>getDeletePropagationEnabled</p>
     *
     * @return a boolean.
     */
    public abstract boolean getDeletePropagationEnabled();

    /**
     * Return the boolean xmlrpc as string to indicate if notification to
     * external xmlrpc server is needed.
     *
     * @return boolean flag as a string value
     */
    public abstract String getXmlrpc();
    
    /**
     * <p>isXmlRpcEnabled</p>
     *
     * @return a boolean.
     */
    public abstract boolean isXmlRpcEnabled();
    
    /**
     * <p>getProtocolPlugin</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.capsd.ProtocolPlugin} object.
     */
    public abstract ProtocolPlugin getProtocolPlugin(String svcName);

    /**
     * <p>addProtocolPlugin</p>
     *
     * @param plugin a {@link org.opennms.netmgt.config.capsd.ProtocolPlugin} object.
     */
    public abstract void addProtocolPlugin(ProtocolPlugin plugin);
    
    /**
     * <p>determinePrimarySnmpInterface</p>
     *
     * @param addressList a {@link java.util.List} object.
     * @param strict a boolean.
     * @return a {@link java.net.InetAddress} object.
     */
    public abstract InetAddress determinePrimarySnmpInterface(List<InetAddress> addressList, boolean strict);

    /**
     * <p>getConfiguredProtocols</p>
     *
     * @return a {@link java.util.List} object.
     */
    public abstract List<String> getConfiguredProtocols();

    /**
     * <p>getProtocolPlugins</p>
     *
     * @return a {@link java.util.List} object.
     */
    public abstract List<ProtocolPlugin> getProtocolPlugins();

    /**
     * <p>getProtocolConfigurations</p>
     *
     * @param plugin a {@link org.opennms.netmgt.config.capsd.ProtocolPlugin} object.
     * @return a {@link java.util.List} object.
     */
    public abstract List<ProtocolConfiguration> getProtocolConfigurations(ProtocolPlugin plugin);

    /**
     * <p>getSpecifics</p>
     *
     * @param pluginConf a {@link org.opennms.netmgt.config.capsd.ProtocolConfiguration} object.
     * @return a {@link java.util.List} object.
     */
    public abstract List<String> getSpecifics(ProtocolConfiguration pluginConf);

    /**
     * <p>getRanges</p>
     *
     * @param pluginConf a {@link org.opennms.netmgt.config.capsd.ProtocolConfiguration} object.
     * @return a {@link java.util.List} object.
     */
    public abstract List<Range> getRanges(ProtocolConfiguration pluginConf);

    /**
     * <p>getPluginProperties</p>
     *
     * @param plugin a {@link org.opennms.netmgt.config.capsd.ProtocolPlugin} object.
     * @return a {@link java.util.List} object.
     */
    public abstract List<Property> getPluginProperties(ProtocolPlugin plugin);

    /**
     * <p>getProtocolConfigurationProperties</p>
     *
     * @param pluginConf a {@link org.opennms.netmgt.config.capsd.ProtocolConfiguration} object.
     * @return a {@link java.util.List} object.
     */
    public abstract List<Property> getProtocolConfigurationProperties(ProtocolConfiguration pluginConf);

    /**
     * <p>toLong</p>
     *
     * @param start a {@link java.net.InetAddress} object.
     * @return a long.
     */
    public abstract long toLong(InetAddress start);



}
