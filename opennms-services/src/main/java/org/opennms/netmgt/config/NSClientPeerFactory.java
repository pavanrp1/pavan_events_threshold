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


package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.common.Range;
import org.opennms.netmgt.config.nsclient.Definition;
import org.opennms.netmgt.config.nsclient.NsclientConfig;
import org.opennms.netmgt.poller.nsclient.NSClientAgentConfig;
import org.opennms.protocols.ip.IPv4Address;

/**
 * This class is the main repository for NSCLient configuration information used by
 * the capabilities daemon. When this class is loaded it reads the nsclient
 * configuration into memory, and uses the configuration to find the
 * {@link org.opennms.netmgt.nsclient.NSClientAgentConfig NSClientAgentConfig} objects for specific
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
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="mailto:gturner@newedgenetworks.com">Gerald Turner </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="mailto:gturner@newedgenetworks.com">Gerald Turner </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="mailto:gturner@newedgenetworks.com">Gerald Turner </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class NSClientPeerFactory extends PeerFactory {
    /**
     * The singleton instance of this factory
     */
    private static NSClientPeerFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private static NsclientConfig m_config;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

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
    private NSClientPeerFactory(String configFile) throws IOException, MarshalException, ValidationException {
        InputStream cfgIn = new FileInputStream(configFile);

        m_config = (NsclientConfig) Unmarshaller.unmarshal(NsclientConfig.class, new InputStreamReader(cfgIn));
        cfgIn.close();

    }
    
    /**
     * <p>Constructor for NSClientPeerFactory.</p>
     *
     * @param rdr a {@link java.io.Reader} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public NSClientPeerFactory(Reader rdr) throws IOException, MarshalException, ValidationException {
        m_config = (NsclientConfig) Unmarshaller.unmarshal(NsclientConfig.class, rdr);
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
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.NSCLIENT_CONFIG_FILE_NAME);

        log().debug("init: config file path: " + cfgFile.getPath());

        m_singleton = new NSClientPeerFactory(cfgFile.getPath());

        m_loaded = true;
    }

    private static Category log() {
        return ThreadCategory.getInstance(NSClientPeerFactory.class);
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
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Saves the current settings to disk
     *
     * @throws java.lang.Exception if any.
     */
    public static synchronized void saveCurrent() throws Exception {
        optimize();

        // Marshall to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the XML from the marshall is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(m_config, stringWriter);
        if (stringWriter.toString() != null) {
            FileWriter fileWriter = new FileWriter(ConfigFileConstants.getFile(ConfigFileConstants.NSCLIENT_CONFIG_FILE_NAME));
            fileWriter.write(stringWriter.toString());
            fileWriter.flush();
            fileWriter.close();
        }

        reload();
    }

    /**
     * Combine specific and range elements so that NSClientPeerFactory has to spend
     * less time iterating all these elements.
     * TODO This really should be pulled up into PeerFactory somehow, but I'm not sure how (given that "Definition" is different for both
     * Snmp and NSClient.  Maybe some sort of visitor methodology would work.  The basic logic should be fine as it's all IP address manipulation
     */
    private static void optimize() throws UnknownHostException {
        Category log = log();

        // First pass: Remove empty definition elements
        for (Iterator<Definition> definitionsIterator =
                 m_config.getDefinitionCollection().iterator();
             definitionsIterator.hasNext();) {
            Definition definition = definitionsIterator.next();
            if (definition.getSpecificCount() == 0
                && definition.getRangeCount() == 0) {
                if (log.isDebugEnabled())
                    log.debug("optimize: Removing empty definition element");
                definitionsIterator.remove();
            }
        }

        // Second pass: Replace single IP range elements with specific elements
        for (Definition definition : m_config.getDefinitionCollection()) {
            for (Iterator<Range> rangesIterator =
                     definition.getRangeCollection().iterator();
                 rangesIterator.hasNext();) {
                Range range = rangesIterator.next();
                if (range.getBegin().equals(range.getEnd())) {
                    definition.addSpecific(range.getBegin());
                    rangesIterator.remove();
                }
            }
        }

        // Third pass: Sort specific and range elements for improved XML
        // readability and then combine them into fewer elements where possible
        for (Definition definition : m_config.getDefinitionCollection()) {
            // Sort specifics
            TreeMap<Integer, String> specificsMap = new TreeMap<Integer, String>();
            for (String specific : definition.getSpecificCollection()) {
                specificsMap.put(new Integer(new IPv4Address(specific).getAddress()),
                                 specific.trim());
            }

            // Sort ranges
            TreeMap<Integer, Range> rangesMap = new TreeMap<Integer, Range>();
            for (Range range : definition.getRangeCollection()) {
                rangesMap.put(new Integer(new IPv4Address(range.getBegin()).getAddress()),
                              range);
            }

            // Combine consecutive specifics into ranges
            Integer priorSpecific = null;
            Range addedRange = null;
            for (Integer specific : specificsMap.keySet()) {
                if (priorSpecific == null) {
                    priorSpecific = specific;
                    continue;
                }

                int specificInt = specific.intValue();
                int priorSpecificInt = priorSpecific.intValue();

                if (specificInt == priorSpecificInt + 1) {
                    if (addedRange == null) {
                        addedRange = new Range();
                        addedRange.setBegin
                             (IPv4Address.addressToString(priorSpecificInt));
                        rangesMap.put(priorSpecific, addedRange);
                        specificsMap.remove(priorSpecific);
                    }

                    addedRange.setEnd(IPv4Address.addressToString(specificInt));
                    specificsMap.remove(specific);
                }
                else {
                    addedRange = null;
                }

                priorSpecific = specific;
            }

            // Move specifics to ranges
            for (Integer specific : specificsMap.keySet()) {
                int specificInt = specific.intValue();
                for (Integer begin : rangesMap.keySet()) {
                    int beginInt = begin.intValue();

                    if (specificInt < beginInt - 1)
                        continue;

                    Range range = rangesMap.get(begin);

                    int endInt = new IPv4Address(range.getEnd()).getAddress();

                    if (specificInt > endInt + 1)
                        continue;

                    if (specificInt >= beginInt && specificInt <= endInt) {
                        specificsMap.remove(specific);
                        break;
                    }

                    if (specificInt == beginInt - 1) {
                        rangesMap.remove(begin);
                        rangesMap.put(specific, range);
                        range.setBegin(IPv4Address.addressToString(specificInt));
                        specificsMap.remove(specific);
                        break;
                    }

                    if (specificInt == endInt + 1) {
                        range.setEnd(IPv4Address.addressToString(specificInt));
                        specificsMap.remove(specific);
                        break;
                    }
                }
            }

            // Combine consecutive ranges
            Range priorRange = null;
            int priorBegin = 0;
            int priorEnd = 0;
            for (Iterator<Integer> rangesIterator = rangesMap.keySet().iterator();
                 rangesIterator.hasNext();) {
                Integer rangeKey = rangesIterator.next();

                Range range = rangesMap.get(rangeKey);

                int begin = rangeKey.intValue();
                int end = new IPv4Address(range.getEnd()).getAddress();

                if (priorRange != null) {
                    if (begin - priorEnd <= 1) {
                        priorRange.setBegin(IPv4Address.addressToString
                                             (Math.min(priorBegin, begin)));
                        priorRange.setEnd(IPv4Address.addressToString
                                           (Math.max(priorEnd, end)));

                        rangesIterator.remove();
                        continue;
                    }
                }

                priorRange = range;
                priorBegin = begin;
                priorEnd = end;
            }

            // Update changes made to sorted maps
            definition.setSpecific(new ArrayList<String>(specificsMap.values()));
            definition.setRange(new ArrayList<Range>(rangesMap.values()));
        }
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized NSClientPeerFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The NSClientPeerFactory has not been initialized");

        return m_singleton;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param singleton a {@link org.opennms.netmgt.config.NSClientPeerFactory} object.
     */
    public static synchronized void setInstance(NSClientPeerFactory singleton) {
        m_singleton = singleton;
        m_loaded = true;
    }

    /**
     * Puts a specific IP address with associated password into
     * the currently loaded nsclient-config.xml.
     *  Perhaps with a bit of jiggery pokery this could be pulled up into PeerFactory
     *
     * @param ip a {@link java.net.InetAddress} object.
     * @param password a {@link java.lang.String} object.
     * @throws java.net.UnknownHostException if any.
     */
    public void define(InetAddress ip, String password) throws UnknownHostException {
        Category log = log();

        // Convert IP to long so that it easily compared in range elements
        int address = new IPv4Address(ip).getAddress();

        // Copy the current definitions so that elements can be added and
        // removed
        ArrayList<Definition> definitions =
            new ArrayList<Definition>(m_config.getDefinitionCollection());

        // First step: Find the first definition matching the read-community or
        // create a new definition, then add the specific IP
        Definition definition = null;
        for (Iterator<Definition> definitionsIterator = definitions.iterator();
             definitionsIterator.hasNext();) {
            Definition currentDefinition =
                definitionsIterator.next();

            if ((currentDefinition.getPassword() != null
                 && currentDefinition.getPassword().equals(password))
                || (currentDefinition.getPassword() == null
                    && m_config.getPassword() != null
                    && m_config.getPassword().equals(password))) {
                if (log.isDebugEnabled())
                    log.debug("define: Found existing definition "
                              + "with read-community " + password);
                definition = currentDefinition;
                break;
            }
        }
        if (definition == null) {
            if (log.isDebugEnabled())
                log.debug("define: Creating new definition");

            definition = new Definition();
            definition.setPassword(password);
            definitions.add(definition);
        }
        definition.addSpecific(ip.getHostAddress());

        // Second step: Find and remove any existing specific and range
        // elements with matching IP among all definitions except for the
        // definition identified in the first step
        for (Iterator<Definition> definitionsIterator = definitions.iterator();
             definitionsIterator.hasNext();) {
            Definition currentDefinition =
                definitionsIterator.next();

            // Ignore this definition if it was the one identified by the first
            // step
            if (currentDefinition == definition)
                continue;

            // Remove any specific elements that match IP
            while (currentDefinition.removeSpecific(ip.getHostAddress())) {
                if (log.isDebugEnabled())
                    log.debug("define: Removed an existing specific "
                              + "element with IP " + ip);
            }

            // Split and replace any range elements that contain IP
            ArrayList<Range> ranges =
                new ArrayList<Range>(currentDefinition.getRangeCollection());
            Range[] rangesArray = currentDefinition.getRange();
            for (int rangesArrayIndex = 0;
                 rangesArrayIndex < rangesArray.length;
                 rangesArrayIndex++) {
                Range range = rangesArray[rangesArrayIndex];
                int begin = new IPv4Address(range.getBegin()).getAddress();
                int end = new IPv4Address(range.getEnd()).getAddress();
                if (address >= begin && address <= end) {
                    if (log.isDebugEnabled())
                        log.debug("define: Splitting range element "
                                  + "with begin " + range.getBegin() + " and "
                                  + "end " + range.getEnd());

                    if (begin == end) {
                        ranges.remove(range);
                        continue;
                    }

                    if (address == begin) {
                        range.setBegin(IPv4Address.addressToString(address + 1));
                        continue;
                    }

                    if (address == end) {
                        range.setEnd(IPv4Address.addressToString(address - 1));
                        continue;
                    }

                    Range head = new Range();
                    head.setBegin(range.getBegin());
                    head.setEnd(IPv4Address.addressToString(address - 1));

                    Range tail = new Range();
                    tail.setBegin(IPv4Address.addressToString(address + 1));
                    tail.setEnd(range.getEnd());

                    ranges.remove(range);
                    ranges.add(head);
                    ranges.add(tail);
                }
            }
            currentDefinition.setRange(ranges);
        }

        // Store the altered list of definitions
        m_config.setDefinition(definitions);
    }
    
    /**
     * <p>getAgentConfig</p>
     *
     * @param agentInetAddress a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.poller.nsclient.NSClientAgentConfig} object.
     */
    public synchronized NSClientAgentConfig getAgentConfig(InetAddress agentInetAddress) {

        if (m_config == null) {
            NSClientAgentConfig agentConfig = new NSClientAgentConfig(agentInetAddress);
            return agentConfig;
        }
        
        NSClientAgentConfig agentConfig = new NSClientAgentConfig(agentInetAddress);
        
        //Now set the defaults from the m_config
        setNSClientAgentConfig(agentConfig, new Definition());

        // Attempt to locate the node
        DEFLOOP: for (Definition def : m_config.getDefinitionCollection()) {
            // check the specifics first

            for (String saddr : def.getSpecificCollection()) {
                try {
                    InetAddress addr = InetAddress.getByName(saddr);
                    if (addr.equals(agentConfig.getAddress())) {
                        setNSClientAgentConfig(agentConfig, def);
                        break DEFLOOP;
                    }
                } catch (UnknownHostException e) {
                    Category log = ThreadCategory.getInstance(getClass());
                    log.warn("NSClientPeerFactory: could not convert host " + saddr + " to InetAddress", e);
                }
            }

            // check the ranges
            //
            long lhost = toLong(agentConfig.getAddress());
            for (Range rng : def.getRangeCollection()) {
                try {
                    InetAddress begin = InetAddress.getByName(rng.getBegin());
                    InetAddress end = InetAddress.getByName(rng.getEnd());

                    long start = toLong(begin);
                    long stop = toLong(end);

                    if (start <= lhost && lhost <= stop) {
                        setNSClientAgentConfig(agentConfig, def );
                        break DEFLOOP;
                    }
                } catch (UnknownHostException e) {
                    Category log = ThreadCategory.getInstance(getClass());
                    log.warn("NSClientPeerFactory: could not convert host(s) " + rng.getBegin() + " - " + rng.getEnd() + " to InetAddress", e);
                }
            }
            
            // check the matching IP expressions
            for (String ipMatch : def.getIpMatchCollection()) {
                if (verifyIpMatch(agentInetAddress.getHostAddress(), ipMatch)) {
                    setNSClientAgentConfig(agentConfig, def);
                    break DEFLOOP;
                }
            }
            
        } // end DEFLOOP

        if (agentConfig == null) {

            Definition def = new Definition();
            setNSClientAgentConfig(agentConfig, def);
        }

        return agentConfig;

    }
    
    private void setNSClientAgentConfig(NSClientAgentConfig agentConfig, Definition def) {
        setCommonAttributes(agentConfig, def);
        agentConfig.setPassword(determinePassword(def));       
    }
    
    /**
     * This is a helper method to set all the common attributes in the agentConfig.
     * 
     * @param agentConfig
     * @param def
     * @param version
     */
    private void setCommonAttributes(NSClientAgentConfig agentConfig, Definition def) {
        agentConfig.setPort(determinePort(def));
        agentConfig.setRetries(determineRetries(def));
        agentConfig.setTimeout((int)determineTimeout(def));
    }

     /**
     * Helper method to search the nsclient configuration for the appropriate password
     * @param def
     * @return
     */
    private String determinePassword(Definition def) {
        return (def.getPassword() == null ? (m_config.getPassword() == null ? NSClientAgentConfig.DEFAULT_PASSWORD :m_config.getPassword()) : def.getPassword());
    }

    /**
     * Helper method to search the nsclient configuration for a port
     * @param def
     * @return
     */
    private int determinePort(Definition def) {
        int port = 161;
        return (def.getPort() == 0 ? (m_config.getPort() == 0 ? port : m_config.getPort()) : def.getPort());
    }

    /**
     * Helper method to search the nsclient configuration 
     * @param def
     * @return
     */
    private long determineTimeout(Definition def) {
        long timeout = NSClientAgentConfig.DEFAULT_TIMEOUT;
        return (long)(def.getTimeout() == 0 ? (m_config.getTimeout() == 0 ? timeout : m_config.getTimeout()) : def.getTimeout());
    }

    private int determineRetries(Definition def) {        
        int retries = NSClientAgentConfig.DEFAULT_RETRIES;
        return (def.getRetry() == 0 ? (m_config.getRetry() == 0 ? retries : m_config.getRetry()) : def.getRetry());
    }

    /**
     * <p>getNSClientConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.nsclient.NsclientConfig} object.
     */
    public static NsclientConfig getNSClientConfig() {
        return m_config;
    }

    /**
     * <p>setNSClientConfig</p>
     *
     * @param m_config a {@link org.opennms.netmgt.config.nsclient.NsclientConfig} object.
     */
    public static synchronized void setNSClientConfig(NsclientConfig m_config) {
        NSClientPeerFactory.m_config = m_config;
    }

}
