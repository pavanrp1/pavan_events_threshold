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
// Modifications:
//
// 2005 Mar 08: Added saveCurrent, optimize, and define methods.
// 2003 Jan 31: Cleaned up some unused imports.
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
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.IPLike;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.common.Range;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * This class is the main respository for SNMP configuration information used by
 * the capabilities daemon. When this class is loaded it reads the snmp
 * configuration into memory, and uses the configuration to find the
 * {@link org.opennms.netmgt.snmp.SnmpAgentConfig SnmpAgentConfig} objects for specific
 * addresses. If an address cannot be located in the configuration then a
 * default peer instance is returned to the caller.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="mailto:gturner@newedgenetworks.com">Gerald Turner </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public class SnmpPeerFactory extends PeerFactory implements SnmpAgentConfigFactory {
    private static final int DEFAULT_SNMP_PORT = 161;
    private static final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private static final Lock m_readLock = m_globalLock.readLock();
    private static final Lock m_writeLock = m_globalLock.writeLock();
    
    /**
     * The singleton instance of this factory
     */
    private static SnmpPeerFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private static SnmpConfig m_config;
    
    private static File m_configFile;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    private static final int VERSION_UNSPECIFIED = -1;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    private SnmpPeerFactory(final File configFile) throws IOException, MarshalException, ValidationException {
        this(new FileSystemResource(configFile));
    }
    
    /**
     * <p>Constructor for SnmpPeerFactory.</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object.
     */
    public SnmpPeerFactory(final Resource resource) {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            m_config = CastorUtils.unmarshalWithTranslatedExceptions(SnmpConfig.class, resource);
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }
    
    /**
     * <p>Constructor for SnmpPeerFactory.</p>
     *
     * @param rdr a {@link java.io.Reader} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public SnmpPeerFactory(final Reader rdr) throws IOException, MarshalException, ValidationException {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            m_config = CastorUtils.unmarshalWithTranslatedExceptions(SnmpConfig.class, rdr);
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }
    
    /**
     * <p>Constructor for SnmpPeerFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     */
    public SnmpPeerFactory(final InputStream stream) {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            m_config = CastorUtils.unmarshalWithTranslatedExceptions(SnmpConfig.class, stream);
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    public static Lock getReadLock() {
        return m_readLock;
    }
    
    public static Lock getWriteLock() {
        return m_writeLock;
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static void init() throws IOException, MarshalException, ValidationException {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            if (m_loaded) {
                // init already called - return
                // to reload, reload() will need to be called
                return;
            }
    
            final File cfgFile = getFile();
            LogUtils.debugf(SnmpPeerFactory.class, "init: config file path: %s", cfgFile.getPath());
            m_singleton = new SnmpPeerFactory(cfgFile);
            m_loaded = true;
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static void reload() throws IOException, MarshalException, ValidationException {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            m_singleton = null;
            m_loaded = false;
    
            init();
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    /**
     * Saves the current settings to disk
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static void saveCurrent() throws IOException, MarshalException, ValidationException {
        // Marshall to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the XML from the marshall is hosed.
        final String marshalledConfig = marshallConfig();

        SnmpPeerFactory.getWriteLock().lock();
        try {
            if (marshalledConfig != null) {
                final Writer fileWriter = new OutputStreamWriter(new FileOutputStream(getFile()), "UTF-8");
                fileWriter.write(marshalledConfig);
                fileWriter.flush();
                fileWriter.close();
            }
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }

        reload();
    }


    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static SnmpPeerFactory getInstance() {
        SnmpPeerFactory.getReadLock().lock();
        try {
            if (!m_loaded) {
                throw new IllegalStateException("The factory has not been initialized");
            }
    
            return m_singleton;
        } finally {
            SnmpPeerFactory.getReadLock().unlock();
        }
    }
    
    /**
     * <p>setFile</p>
     *
     * @param configFile a {@link java.io.File} object.
     */
    public static void setFile(final File configFile) {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            final File oldFile = m_configFile;
            m_configFile = configFile;
            
            // if the file changed then we need to reload the config
            if (oldFile == null || m_configFile == null || !oldFile.equals(m_configFile)) {
                m_singleton = null;
                m_loaded = false;
            }
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }
    
    /**
     * <p>getFile</p>
     *
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    public static File getFile() throws IOException {
        SnmpPeerFactory.getReadLock().lock();
        try {
            if (m_configFile == null) {
                setFile(ConfigFileConstants.getFile(ConfigFileConstants.SNMP_CONF_FILE_NAME));
            }
            return m_configFile;
        } finally {
            SnmpPeerFactory.getReadLock().unlock();
        }
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param singleton a {@link org.opennms.netmgt.config.SnmpPeerFactory} object.
     */
    public static void setInstance(final SnmpPeerFactory singleton) {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            m_singleton = singleton;
            m_loaded = true;
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public SnmpAgentConfig getAgentConfig(InetAddress agentAddress) {
        return getAgentConfig(agentAddress, VERSION_UNSPECIFIED);
    }
    
    private SnmpAgentConfig getAgentConfig(InetAddress agentInetAddress, int requestedSnmpVersion) {
        SnmpPeerFactory.getReadLock().lock();
        try {
            if (m_config == null) {
                final SnmpAgentConfig agentConfig = new SnmpAgentConfig(agentInetAddress);
                if (requestedSnmpVersion == VERSION_UNSPECIFIED) {
                    agentConfig.setVersion(SnmpAgentConfig.DEFAULT_VERSION);
                } else {
                    agentConfig.setVersion(requestedSnmpVersion);
                }

                return agentConfig;
            }

            final SnmpAgentConfig agentConfig = new SnmpAgentConfig(agentInetAddress);

            // Now set the defaults from the m_config
            setSnmpAgentConfig(agentConfig, new Definition(), requestedSnmpVersion);

            // Attempt to locate the node
            DEFLOOP: for (final Definition def : m_config.getDefinitionCollection()) {
                // check the specifics first
                for (final String saddr : def.getSpecificCollection()) {
                    try {
                        final InetAddress addr = InetAddress.getByName(saddr);
                        if (addr.equals(agentConfig.getAddress())) {
                            setSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
                            break DEFLOOP;
                        }
                    } catch (final UnknownHostException e) {
                        LogUtils.warnf(this, e, "SnmpPeerFactory: could not convert host %s to InetAddress", saddr);
                    }
                }

                // check the ranges
                //
                for (final Range rng : def.getRangeCollection()) {
                    if (InetAddressUtils.isInetAddressInRange(agentConfig.getAddress().getHostAddress(), rng.getBegin(), rng.getEnd())) {
                        setSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
                        break DEFLOOP;
                    }
                }

                // check the matching ip expressions
                for (final String ipMatch : def.getIpMatchCollection()) {
                    if (IPLike.matches(agentInetAddress.getHostAddress(), ipMatch)) {
                        setSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
                        break DEFLOOP;
                    }
                }

            } // end DEFLOOP

            if (agentConfig == null) {
                final Definition def = new Definition();
                setSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
            }

            return agentConfig;
        } finally {
            SnmpPeerFactory.getReadLock().unlock();
        }
    }

    private void setSnmpAgentConfig(final SnmpAgentConfig agentConfig, final Definition def, final int requestedSnmpVersion) {
        int version = determineVersion(def, requestedSnmpVersion);
        
        setCommonAttributes(agentConfig, def, version);
        agentConfig.setSecurityLevel(determineSecurityLevel(def));
        agentConfig.setSecurityName(determineSecurityName(def));
        agentConfig.setAuthProtocol(determineAuthProtocol(def));
        agentConfig.setAuthPassPhrase(determineAuthPassPhrase(def));
        agentConfig.setPrivPassPhrase(determinePrivPassPhrase(def));
        agentConfig.setPrivProtocol(determinePrivProtocol(def));
        agentConfig.setReadCommunity(determineReadCommunity(def));
        agentConfig.setWriteCommunity(determineWriteCommunity(def));
    }
    
    /**
     * This is a helper method to set all the common attributes in the agentConfig.
     * 
     * @param agentConfig
     * @param def
     * @param version
     */
    private void setCommonAttributes(final SnmpAgentConfig agentConfig, final Definition def, final int version) {
        agentConfig.setVersion(version);
        agentConfig.setPort(determinePort(def));
        agentConfig.setRetries(determineRetries(def));
        agentConfig.setTimeout((int)determineTimeout(def));
        agentConfig.setMaxRequestSize(determineMaxRequestSize(def));
        agentConfig.setMaxVarsPerPdu(determineMaxVarsPerPdu(def));
        agentConfig.setMaxRepetitions(determineMaxRepetitions(def));
        InetAddress proxyHost = determineProxyHost(def);
        
        if (proxyHost != null) {
            agentConfig.setProxyFor(agentConfig.getAddress());
            agentConfig.setAddress(determineProxyHost(def));
        }
    }

    private int determineMaxRepetitions(final Definition def) {
        return (!def.hasMaxRepetitions() ? 
            (!m_config.hasMaxRepetitions() ?
              SnmpAgentConfig.DEFAULT_MAX_REPETITIONS : m_config.getMaxRepetitions()) : def.getMaxRepetitions());
    }

	private InetAddress determineProxyHost(final Definition def) {
        InetAddress inetAddr = null;
        final String address = def.getProxyHost() == null ? 
                (m_config.getProxyHost() == null ? null : m_config.getProxyHost()) : def.getProxyHost();
        if (address != null) {
            try {
                inetAddr =  InetAddress.getByName(address);
            } catch (final UnknownHostException e) {
                LogUtils.errorf(this, e, "determineProxyHost: Problem converting proxy host string to InetAddress");
            }
        }
        return inetAddr;
    }

    private int determineMaxVarsPerPdu(final Definition def) {
        return (!def.hasMaxVarsPerPdu() ? 
            (!m_config.hasMaxVarsPerPdu() ?
              SnmpAgentConfig.DEFAULT_MAX_VARS_PER_PDU : m_config.getMaxVarsPerPdu()) : def.getMaxVarsPerPdu());
    }
    /**
     * Helper method to search the snmp-config for the appropriate read
     * community string.
     * @param def
     * @return
     */
    private String determineReadCommunity(final Definition def) {
        return (def.getReadCommunity() == null ? (m_config.getReadCommunity() == null ? SnmpAgentConfig.DEFAULT_READ_COMMUNITY :m_config.getReadCommunity()) : def.getReadCommunity());
    }

    /**
     * Helper method to search the snmp-config for the appropriate write
     * community string.
     * @param def
     * @return
     */
    private String determineWriteCommunity(final Definition def) {
        return (def.getWriteCommunity() == null ? (m_config.getWriteCommunity() == null ? SnmpAgentConfig.DEFAULT_WRITE_COMMUNITY :m_config.getWriteCommunity()) : def.getWriteCommunity());
    }

    /**
     * Helper method to search the snmp-config for the appropriate maximum
     * request size.  The default is the minimum necessary for a request.
     * @param def
     * @return
     */
    private int determineMaxRequestSize(final Definition def) {
        return (!def.hasMaxRequestSize() ? (!m_config.hasMaxRequestSize() ? SnmpAgentConfig.DEFAULT_MAX_REQUEST_SIZE : m_config.getMaxRequestSize()) : def.getMaxRequestSize());
    }

    /**
     * Helper method to find a security name to use in the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determineSecurityName(final Definition def) {
        final String securityName = (def.getSecurityName() == null ? m_config.getSecurityName() : def.getSecurityName() );
        if (securityName == null) {
            return SnmpAgentConfig.DEFAULT_SECURITY_NAME;
        }
        return securityName;
    }

    /**
     * Helper method to find a security name to use in the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determineAuthProtocol(final Definition def) {
        final String authProtocol = (def.getAuthProtocol() == null ? m_config.getAuthProtocol() : def.getAuthProtocol());
        if (authProtocol == null) {
            return SnmpAgentConfig.DEFAULT_AUTH_PROTOCOL;
        }
        return authProtocol;
    }
    
    /**
     * Helper method to find a authentication passphrase to use from the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determineAuthPassPhrase(final Definition def) {
        final String authPassPhrase = (def.getAuthPassphrase() == null ? m_config.getAuthPassphrase() : def.getAuthPassphrase());
        if (authPassPhrase == null) {
            return SnmpAgentConfig.DEFAULT_AUTH_PASS_PHRASE;
        }
        return authPassPhrase;
    }

    /**
     * Helper method to find a privacy passphrase to use from the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determinePrivPassPhrase(final Definition def) {
        final String privPassPhrase = (def.getPrivacyPassphrase() == null ? m_config.getPrivacyPassphrase() : def.getPrivacyPassphrase());
        if (privPassPhrase == null) {
            return SnmpAgentConfig.DEFAULT_PRIV_PASS_PHRASE;
        }
        return privPassPhrase;
    }

    /**
     * Helper method to find a privacy protocol to use from the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determinePrivProtocol(final Definition def) {
        final String authPrivProtocol = (def.getPrivacyProtocol() == null ? m_config.getPrivacyProtocol() : def.getPrivacyProtocol());
        if (authPrivProtocol == null) {
            return SnmpAgentConfig.DEFAULT_PRIV_PROTOCOL;
        }
        return authPrivProtocol;
    }

    /**
     * Helper method to set the security level in v3 operations.  The default is
     * noAuthNoPriv if there is no authentication passphrase.  From there, if
     * there is a privacy passphrase supplied, then the security level is set to
     * authPriv else it falls out to authNoPriv.  There are only these 3 possible
     * security levels.
     * default 
     * @param def
     * @return
     */
    private int determineSecurityLevel(final Definition def) {
        
        // use the def security level first
        if (def.hasSecurityLevel()) {
            return def.getSecurityLevel();
        }
        
        // use a configured default security level next
        if (m_config.hasSecurityLevel()) {
            return m_config.getSecurityLevel();
        }

        // if no security level configuration exists use
        int securityLevel = SnmpAgentConfig.NOAUTH_NOPRIV;

        final String authPassPhrase = (def.getAuthPassphrase() == null ? m_config.getAuthPassphrase() : def.getAuthPassphrase());
        final String privPassPhrase = (def.getPrivacyPassphrase() == null ? m_config.getPrivacyPassphrase() : def.getPrivacyPassphrase());

        if (authPassPhrase == null) {
            securityLevel = SnmpAgentConfig.NOAUTH_NOPRIV;
        } else {
            if (privPassPhrase == null) {
                securityLevel = SnmpAgentConfig.AUTH_NOPRIV;
            } else {
                securityLevel = SnmpAgentConfig.AUTH_PRIV;
            }
        }
        
        return securityLevel;
    }

    /**
     * Helper method to search the snmp-config for a port
     * @param def
     * @return
     */
    private int determinePort(final Definition def) {
        return (def.getPort() == 0 ? (m_config.getPort() == 0 ? DEFAULT_SNMP_PORT : m_config.getPort()) : def.getPort());
    }

    /**
     * Helper method to search the snmp-config 
     * @param def
     * @return
     */
    private long determineTimeout(final Definition def) {
        final long timeout = SnmpAgentConfig.DEFAULT_TIMEOUT;
        return (def.getTimeout() == 0 ? (m_config.getTimeout() == 0 ? timeout : m_config.getTimeout()) : def.getTimeout());
    }

    private int determineRetries(final Definition def) {        
        final int retries = SnmpAgentConfig.DEFAULT_RETRIES;
        return (def.getRetry() == 0 ? (m_config.getRetry() == 0 ? retries : m_config.getRetry()) : def.getRetry());
    }

    /**
     * This method determines the configured SNMP version.
     * the order of operations is:
     * 1st: return a valid requested version
     * 2nd: return a valid version defined in a definition within the snmp-config
     * 3rd: return a valid version in the snmp-config
     * 4th: return the default version
     * 
     * @param def
     * @param requestedSnmpVersion
     * @return
     */
    private int determineVersion(final Definition def, final int requestedSnmpVersion) {
        
        int version = SnmpAgentConfig.VERSION1;
        
        String cfgVersion = "v1";
        if (requestedSnmpVersion == VERSION_UNSPECIFIED) {
            if (def.getVersion() == null) {
                if (m_config.getVersion() == null) {
                    return version;
                } else {
                    cfgVersion = m_config.getVersion();
                }
            } else {
                cfgVersion = def.getVersion();
            }
        } else {
            return requestedSnmpVersion;
        }
        
        if (cfgVersion.equals("v1")) {
            version = SnmpAgentConfig.VERSION1;
        } else if (cfgVersion.equals("v2c")) {
            version = SnmpAgentConfig.VERSION2C;
        } else if (cfgVersion.equals("v3")) {
            version = SnmpAgentConfig.VERSION3;
        }
        
        return version;
    }

    /**
     * <p>getSnmpConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.snmp.SnmpConfig} object.
     */
    public static SnmpConfig getSnmpConfig() {
        SnmpPeerFactory.getReadLock().lock();
        try {
            return m_config;
        } finally {
            SnmpPeerFactory.getReadLock().unlock();
        }
    }

    @SuppressWarnings("unused")
    private static void setSnmpConfig(final SnmpConfig m_config) {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            SnmpPeerFactory.m_config = m_config;
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    /**
     * Enhancement: Allows specific or ranges to be merged into snmp configuration
     * with many other attributes.  Uses new classes the wrap Castor generated code to
     * help with merging, comparing, and optimizing definitions.  Thanks for your
     * initial work on this Gerald.
     *
     * Puts a specific IP address with associated read-community string into
     * the currently loaded snmp-config.xml.
     *
     * @param info a {@link org.opennms.netmgt.config.SnmpEventInfo} object.
     */
    public void define(final SnmpEventInfo info) {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            SnmpConfigManager.mergeIntoConfig(getSnmpConfig(), info.createDef());
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }


    /**
     * Creates a string containing the XML of the current SnmpConfig
     *
     * @return Marshalled SnmpConfig
     */
    public static String marshallConfig() {
        SnmpPeerFactory.getReadLock().lock();

        try {
            String marshalledConfig = null;
            StringWriter writer = null;
            try {
                writer = new StringWriter();
                Marshaller.marshal(m_config, writer);
                marshalledConfig = writer.toString();
            } catch (final MarshalException e) {
                LogUtils.errorf(SnmpPeerFactory.class, e, "marshallConfig: Error marshalling configuration");
            } catch (final ValidationException e) {
                LogUtils.errorf(SnmpPeerFactory.class, e, "marshallConfig: Error validating configuration");
            } finally {
                IOUtils.closeQuietly(writer);
            }
            return marshalledConfig;
        } finally {
            SnmpPeerFactory.getReadLock().unlock();
        }
    }

}
