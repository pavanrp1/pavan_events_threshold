//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2004 Oct 07: Added code to support RTC rescan on asset update
// Aug 24, 2004: Created this file.
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

/*
 * TODO
 * Tremendous amount of code duplication that should be refactored.
 */
package org.opennms.netmgt.capsd;

import java.net.InetAddress;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.utils.XmlrpcUtil;
import org.opennms.netmgt.xml.event.Autoaction;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Forward;
import org.opennms.netmgt.xml.event.Operaction;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Script;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Value;

/**
 * Provides a collection of utility methods used by the DeleteEvent Processor
 * for dealing with Events
 *
 * @author brozow
 * @version $Id: $
 */
public class EventUtils {

    /**
     * Make the given listener object a listener for the list of events
     * referenced in the ueiList.
     *
     * @param listener
     *            the lister to add
     * @param ueiList
     *            the list of events the listener is interested
     */
    public static void addEventListener(EventListener listener, List<String> ueiList) {
        EventIpcManagerFactory.init();
        EventIpcManagerFactory.getIpcManager().addEventListener(listener, ueiList);
    }

    /**
     * Ensures that the event has a database eventId
     *
     * @param e
     *            the event
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if an event id is not evailable
     */
    static public void checkEventId(Event e) throws InsufficientInformationException {
        if (e == null)
            throw new NullPointerException("e is null");

        if (!e.hasDbid())
            throw new InsufficientInformationException("eventID is unavailable");
    }

    /**
     * Ensures the given event has an interface
     *
     * @param e
     *            the event
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if an interface is not available
     */
    static public void checkInterface(Event e) throws InsufficientInformationException {
        if (e == null)
            throw new NullPointerException("e is null");

        if (e.getInterface() == null || e.getInterface().length() == 0)
            throw new InsufficientInformationException("ipaddr for event is unavailable");
    }

    /**
     * Ensures the given event has a host
     *
     * @param e
     *            the event
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if an interface is not available
     */
    static public void checkHost(Event e) throws InsufficientInformationException {
        if (e == null)
            throw new NullPointerException("e is null");

        if (e.getHost() == null || e.getHost().length() == 0)
            throw new InsufficientInformationException("host for event is unavailable");
    }

    /**
     * Ensures that the given Event has a node id
     *
     * @param e
     *            the event
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if a node id is not available
     */
    static public void checkNodeId(Event e) throws InsufficientInformationException {
        if (e == null)
            throw new NullPointerException("e is null");

        if (!e.hasNodeid())
            throw new InsufficientInformationException("nodeid for event is unavailable");
    }

    /**
     * Ensures that the given event has a service parameter
     *
     * @param e
     *            the event to check
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if the event does not have a service
     */
    public static void checkService(Event e) throws InsufficientInformationException {
        if (e == null)
            throw new NullPointerException("e is null");

        if (e.getService() == null || e.getService().length() == 0)
            throw new InsufficientInformationException("service for event is unavailable");
    }

    /**
     * Constructs a deleteInterface event for the given nodeId, ipAddress pair.
     *
     * @param source
     *            the source for the event
     * @param nodeId
     *            the nodeId of the node that owns the interface
     * @param ipAddr
     *            the ipAddress of the interface being deleted
     * @param txNo
     *            the transaction number to use for processing this event
     * @return an Event representing a deleteInterface event for the given
     *         nodeId, ipaddr
     */
    public static Event createDeleteInterfaceEvent(String source, long nodeId, String ipAddr, long txNo) {
        Event newEvent = new Event();
        newEvent.setUei(EventConstants.DELETE_INTERFACE_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setInterface(ipAddr);
        newEvent.setNodeid(nodeId);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent(String.valueOf(txNo));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }

    /**
     * Construct a deleteNode event for the given nodeId.
     *
     * @param source
     *            the source for the event
     * @param nodeId
     *            the node to be deleted.
     * @param txNo
     *            the transaction number associated with deleting the node
     * @return an Event object representing a delete node event.
     */
    public static Event createDeleteNodeEvent(String source, long nodeId, long txNo) {
        Event newEvent = new Event();
        newEvent.setUei(EventConstants.DELETE_NODE_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setNodeid(nodeId);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent(String.valueOf(txNo));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }

    /**
     * Construct a deleteNode event for the given nodeId.
     *
     * @param source
     *            the source for the event
     * @param nodeId
     *            the node to be deleted.
     * @param txNo
     *            the transaction number associated with deleting the node
     * @return an Event object representing a delete node event.
     */
    public static Event createAssetInfoChangedEvent(String source, long nodeId, long txNo) {
        Event newEvent = new Event();
        newEvent.setUei(EventConstants.ASSET_INFO_CHANGED_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setNodeid(nodeId);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent(String.valueOf(txNo));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }

    /**
     * Construct an interfaceDeleted event for an interface.
     *
     * @param source
     *            the source of the event
     * @param nodeId
     *            the nodeId of the node the interface resides in
     * @param ipAddr
     *            the ipAdddr of the event
     * @param txNo
     *            a transaction number associated with the event
     * @return an Event represent an interfaceDeleted event for the given
     *         interface
     */
    public static Event createInterfaceDeletedEvent(String source, long nodeId, String ipAddr, long txNo) {
        Event newEvent = new Event();
        newEvent.setUei(EventConstants.INTERFACE_DELETED_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setNodeid(nodeId);
        newEvent.setInterface(ipAddr);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent(String.valueOf(txNo));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }

    /**
     * Construct a nodeDeleteed event for the given nodeId
     *
     * @param source
     *            the source for the event
     * @param nodeId
     *            the id of the node being deleted
     * @param txNo
     *            a transaction number associated with the event
     * @return an Event representing a nodeDeleted event for the given node
     */
    public static Event createNodeDeletedEvent(String source, long nodeId, long txNo) {
        Event newEvent = new Event();
        newEvent.setUei(EventConstants.NODE_DELETED_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setNodeid(nodeId);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent(String.valueOf(txNo));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }

    /**
     * Constructs a serviceDeleted Event for the nodeId, ipAddr, serviceName
     * triple
     *
     * @param source
     *            the source of the event
     * @param nodeId
     *            the nodeId that the service resides on
     * @param ipAddr
     *            the interface that the service resides on
     * @param service
     *            the name of the service that was deleted
     * @param txNo
     *            a transaction number associated with the event
     * @return an Event that represents the serviceDeleted event for the give
     *         triple
     */
    public static Event createServiceDeletedEvent(String source, long nodeId, String ipAddr, String service, long txNo) {
        Event newEvent = new Event();
        newEvent.setUei(EventConstants.SERVICE_DELETED_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setNodeid(nodeId);
        newEvent.setInterface(ipAddr);
        newEvent.setService(service);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent(String.valueOf(txNo));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }

    /**
     * Get the eventId for the given event
     *
     * @param e
     *            the event to get the eventId for
     * @return the eventId of the event or -1 of no eventId is assigned
     */
    public static long getEventID(Event e) {
        // get eventid
        long eventID = -1;
        if (e.hasDbid())
            eventID = e.getDbid();
        return eventID;
    }

    /**
     * Retrieve the value associated with an event parameter and parse it to a
     * long. If the value can not be found, return a default value.
     *
     * @param e
     *            the Event to retrieve the parameter from
     * @param parmName
     *            the name of the parameter to retrieve
     * @param defaultValue
     *            the value to return if the paramter can not be retrieved or
     *            parsed
     * @return the value of the parameter as a long
     */
    public static long getLongParm(Event e, String parmName, long defaultValue) {
        String longVal = getParm(e, parmName);

        if (longVal == null)
            return defaultValue;

        try {
            return Long.parseLong(longVal);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /**
     * Retrieve the value associated with an event parameter and parse it to an
     * int. If the value can not be found, return a default value.
     *
     * @param e
     *            the Event to retrieve the parameter from
     * @param parmName
     *            the name of the parameter to retrieve
     * @param defaultValue
     *            the value to return if the paramter can not be retrieved or
     *            parsed
     * @return the value of the parameter as a long
     */
    public static int getIntParm(Event e, String parmName, int defaultValue) {
        String intVal = getParm(e, parmName);

        if (intVal == null)
            return defaultValue;

        try {
            return Integer.parseInt(intVal);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
    
    /**
     * <p>getIntParm</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param parmName a {@link java.lang.String} object.
     * @return a int.
     */
    public static int getIntParm(Event e, String parmName) {
        return getIntParm(e, parmName, 0);
    }

    /**
     * Return the nodeId of the node associated with and event, or -1 of no node
     * is associated.
     *
     * @param e
     *            the event
     * @return the nodeId or -1 if no nodeId is set
     */
    public static long getNodeId(Event e) {
        // convert the node id
        long nodeID = -1;
        if (e.hasNodeid())
            nodeID = e.getNodeid();
        return nodeID;
    }

    /**
     * Return the value of an event parameter of null if it does not exist.
     *
     * @param e
     *            the Event to get the parameter for
     * @param parmName
     *            the name of the parameter to retrieve
     * @return the value of the parameter, or null of the parameter is not set
     */
    public static String getParm(Event e, String parmName) {
        return getParm(e, parmName, null);
    }

    /**
     * Retrieve a parameter from and event, returning defaultValue of the
     * parameter is not set.
     *
     * @param e
     *            The Event to retrieve the parameter from
     * @param parmName
     *            the name of the parameter to retrieve
     * @param defaultValue
     *            the default value to return if the parameter is not set
     * @return the value of the parameter, or defalutValue if the parameter is
     *         not set
     */
    public static String getParm(Event e, String parmName, String defaultValue) {
        Parms parms = e.getParms();
        if (parms == null)
            return defaultValue;

        Enumeration<Parm> parmEnum = parms.enumerateParm();
        while (parmEnum.hasMoreElements()) {
            Parm parm = parmEnum.nextElement();
            if (parmName.equals(parm.getParmName())) {
                if (parm.getValue() != null && parm.getValue().getContent() != null) {
                    return parm.getValue().getContent();
                } else {
                    return defaultValue;
                }
            }
        }

        return defaultValue;

    }

    /**
     * Throw an exception if an event does have the required parameter
     *
     * @param e
     *            the event the parameter must reside on
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if the paramter is not set on the event or if its value has
     *             no content
     * @param parmName a {@link java.lang.String} object.
     */
    public static void requireParm(Event e, String parmName) throws InsufficientInformationException {
        Parms parms = e.getParms();
        if (parms == null)
            throw new InsufficientInformationException("parameter " + parmName + " required but but no parms are available.");

        Enumeration<Parm> parmEnum = parms.enumerateParm();
        while (parmEnum.hasMoreElements()) {
            Parm parm = parmEnum.nextElement();
            if (parmName.equals(parm.getParmName())) {
                if (parm.getValue() != null && parm.getValue().getContent() != null) {
                    // we found a matching parm
                    return;
                } else {
                    throw new InsufficientInformationException("parameter " + parmName + " required but only null valued parms available");
                }
            }
        }

        throw new InsufficientInformationException("parameter " + parmName + " required but was not available");

    }

    /**
     * Send an event to the Event manaager to be broadcast to interested
     * listeners
     *
     * @param newEvent
     *            the event to send
     * @param isXmlRpcEnabled a boolean.
     * @param callerUei a {@link java.lang.String} object.
     * @param txNo a long.
     */
    public static void sendEvent(Event newEvent, String callerUei, long txNo, boolean isXmlRpcEnabled) {
        // Send event to Eventd
        Category log = ThreadCategory.getInstance(EventUtils.class);
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(newEvent);

            if (log.isDebugEnabled())
                log.debug("sendEvent: successfully sent event " + newEvent);
        } catch (Throwable t) {
            log.warn("run: unexpected throwable exception caught during send to middleware", t);
            if (isXmlRpcEnabled) {
                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, callerUei, "caught unexpected throwable exception.", status, "OpenNMS.Capsd");
            }

        }
    }

    /**
     * This method is responsible for generating a nodeAdded event and sending
     * it to eventd..
     *
     * @param nodeEntry
     *            The node Added.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeAddedEvent(DbNodeEntry nodeEntry) {
        return createNodeAddedEvent(nodeEntry.getNodeId(), nodeEntry.getLabel(), String.valueOf(nodeEntry.getLabelSource()));
    }

	/**
	 * <p>createNodeAddedEvent</p>
	 *
	 * @param nodeId a int.
	 * @param nodeLabel a {@link java.lang.String} object.
	 * @param labelSource a {@link java.lang.String} object.
	 * @return a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	public static Event createNodeAddedEvent(int nodeId, String nodeLabel, String labelSource) {
		Category log = ThreadCategory.getInstance(EventUtils.class);
		if (log.isDebugEnabled())
            log.debug("createAndSendNodeAddedEvent:  nodeId  " + nodeId);

        Event newEvent = new Event();
        newEvent.setUei(EventConstants.NODE_ADDED_EVENT_UEI);
        newEvent.setSource("OpenNMS.Capsd");
        newEvent.setNodeid(nodeId);
        newEvent.setHost(Capsd.getLocalHostAddress());
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_NODE_LABEL);
        parmValue = new Value();
		parmValue.setContent(nodeLabel);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add node label source
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_NODE_LABEL_SOURCE);
        parmValue = new Value();
		parmValue.setContent(labelSource);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
	}

    /**
     * This method is responsible for generating a nodeGainedInterface event and
     * sending it to eventd..
     *
     * @param nodeEntry
     *            The node that gained the interface.
     * @param ifaddr
     *            the interface gained on the node.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeGainedInterfaceEvent(DbNodeEntry nodeEntry, InetAddress ifaddr) {
        return createNodeGainedInterfaceEvent("OpenNMS.Capsd", nodeEntry.getNodeId(), ifaddr);
    }

	/**
	 * <p>createNodeGainedInterfaceEvent</p>
	 *
	 * @param source a {@link java.lang.String} object.
	 * @param nodeId a int.
	 * @param ifaddr a {@link java.net.InetAddress} object.
	 * @return a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	public static Event createNodeGainedInterfaceEvent(String source, int nodeId, InetAddress ifaddr) {
		Category log = ThreadCategory.getInstance(EventUtils.class);
		if (log.isDebugEnabled())
            log.debug("createAndSendNodeAddedEvent:  nodeId  " + nodeId);

        Event newEvent = new Event();
        newEvent.setUei(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI);
		newEvent.setSource(source);
        newEvent.setNodeid(nodeId);
        newEvent.setHost(Capsd.getLocalHostAddress());
        newEvent.setInterface(ifaddr.getHostAddress());
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        // Add IP host name
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_IP_HOSTNAME);
        parmValue = new Value();
        parmValue.setContent(ifaddr.getHostName());
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
	}

    /**
     * This method is responsible for generating a nodeDeleted event and sending
     * it to eventd..
     *
     * @param source
     *            A string representing the source of the event
     * @param nodeId
     *            Nodeid of the node got deleted.
     * @param hostName
     *            the Host server name.
     * @param nodeLabel
     *            the node label of the deleted node.
     * @param txNo a long.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeDeletedEvent(String source, int nodeId, String hostName, String nodeLabel, long txNo) {
        Category log = ThreadCategory.getInstance(EventUtils.class);
        if (log.isDebugEnabled())
            log.debug("createAndSendNodeDeletedEvent:  processing deleteNode event for nodeid:  " + nodeId);

        Event newEvent = new Event();
        newEvent.setUei(EventConstants.NODE_DELETED_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setNodeid(nodeId);
        newEvent.setHost(hostName);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_NODE_LABEL);
        parmValue = new Value();
        parmValue.setContent(nodeLabel);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent((new Long(txNo)).toString());
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        if ((nodeLabel != null) && (((new Long(txNo)).toString()) != null))
            newEvent.setParms(eventParms);

        return newEvent;
    }

    /**
     * This method is responsible for generating a deleteNode event and sending
     * it to eventd..
     *
     * @param source
     *            the source of the event
     * @param nodeLabel
     *            the nodelabel of the deleted node.
     * @param hostName
     *            the Host server name.
     * @param txNo
     *            the external transaction No of the event.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createAndSendDeleteNodeEvent(String source, String nodeLabel, String hostName, long txNo) {
        Category log = ThreadCategory.getInstance(EventUtils.class);
        if (log.isDebugEnabled())
            log.debug("createdAndSendDeleteNodeEvent: processing deleteInterface event... ");

        Event newEvent = new Event();
        newEvent.setUei(EventConstants.DELETE_NODE_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setHost(hostName);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_NODE_LABEL);
        parmValue = new Value();
        parmValue.setContent(nodeLabel);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent((new Long(txNo)).toString());
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }

    /**
     * This method is responsible for generating a forceRescan event and sending
     * it to eventd..
     *
     * @param hostName
     *            the Host server name.
     * @param nodeId
     *            the node ID of the node to rescan.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createForceRescanEvent(String hostName, long nodeId) {
        Category log = ThreadCategory.getInstance(EventUtils.class);
        if (log.isDebugEnabled())
            log.debug("createdAndSendForceRescanEvent: processing forceRescan event... ");

        Event newEvent = new Event();
        newEvent.setUei(EventConstants.FORCE_RESCAN_EVENT_UEI);
        newEvent.setSource("OpenNMS.Capsd");
        newEvent.setNodeid(nodeId);
        newEvent.setHost(hostName);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        return newEvent;
    }

    /**
     * This method is responsible for generating an interfaceDeleted event and
     * sending it to eventd...
     *
     * @param source
     *            the source of the event
     * @param nodeId
     *            Nodeid of the node that the deleted interface resides on.
     * @param ipaddr
     *            the ipaddress of the deleted Interface.
     * @param hostName
     *            the Host server name.
     * @param txNo
     *            the external transaction No. of the original event.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createAndSendInterfaceDeletedEvent(String source, int nodeId, String ipaddr, String hostName, long txNo) {
        Category log = ThreadCategory.getInstance(EventUtils.class);
        if (log.isDebugEnabled())
            log.debug("createAndSendInterfaceDeletedEvent:  processing deleteInterface event for interface: " + ipaddr + " at nodeid: " + nodeId);

        Event newEvent = new Event();
        newEvent.setUei(EventConstants.INTERFACE_DELETED_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setNodeid(nodeId);
        newEvent.setInterface(ipaddr);
        newEvent.setHost(hostName);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent((new Long(txNo)).toString());
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }

    /**
     * This method is responsible for generating a nodeGainedService event and
     * sending it to eventd..
     *
     * @param nodeEntry
     *            The node that gained the service.
     * @param ifaddr
     *            the interface gained the service.
     * @param service
     *            the service gained.
     * @param txNo
     *            the transaction no.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeGainedServiceEvent(DbNodeEntry nodeEntry, InetAddress ifaddr, String service, long txNo) {
        int nodeId = nodeEntry.getNodeId();
        String nodeLabel = nodeEntry.getLabel();
        String labelSource = String.valueOf(nodeEntry.getLabelSource());
        String sysName = nodeEntry.getSystemName();
        String sysDescr = nodeEntry.getSystemDescription();

        return createNodeGainedServiceEvent("OpenNMS.Capsd", nodeId, ifaddr, service, nodeLabel, labelSource, sysName, sysDescr);
    }

	/**
	 * <p>createNodeGainedServiceEvent</p>
	 *
	 * @param source a {@link java.lang.String} object.
	 * @param nodeId a int.
	 * @param ifaddr a {@link java.net.InetAddress} object.
	 * @param service a {@link java.lang.String} object.
	 * @param nodeLabel a {@link java.lang.String} object.
	 * @param labelSource a {@link java.lang.String} object.
	 * @param sysName a {@link java.lang.String} object.
	 * @param sysDescr a {@link java.lang.String} object.
	 * @return a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	public static Event createNodeGainedServiceEvent(String source, int nodeId, InetAddress ifaddr, String service, String nodeLabel, String labelSource, String sysName, String sysDescr) {
		Category log = ThreadCategory.getInstance(EventUtils.class);
		if (log.isDebugEnabled())
            log.debug("createAndSendNodeGainedServiceEvent:  nodeId/interface/service  " + nodeId + "/" + ifaddr.getHostAddress() + "/" + service);

        Event newEvent = new Event();
        newEvent.setUei(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI);
		newEvent.setSource(source);
        newEvent.setNodeid(nodeId);
        newEvent.setHost(Capsd.getLocalHostAddress());
        newEvent.setInterface(ifaddr.getHostAddress());
        newEvent.setService(service);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        // Add IP host name
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_IP_HOSTNAME);
        parmValue = new Value();
        parmValue.setContent(ifaddr.getHostName());
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Node Label
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_NODE_LABEL);
        parmValue = new Value();
		parmValue.setContent(nodeLabel);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Node Label source
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_NODE_LABEL_SOURCE);
        parmValue = new Value();
        parmValue.setContent(labelSource);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add sysName if available
		if (sysName != null) {
            eventParm = new Parm();
            eventParm.setParmName(EventConstants.PARM_NODE_SYSNAME);
            parmValue = new Value();
            parmValue.setContent(sysName);
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);
        }

        // Add sysDescr if available
		if (sysDescr != null) {
            eventParm = new Parm();
            eventParm.setParmName(EventConstants.PARM_NODE_SYSDESCRIPTION);
            parmValue = new Value();
            parmValue.setContent(sysDescr);
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);
        }

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
	}

    /**
     * This method is responsible for generating a deleteService event and
     * sending it to eventd..
     *
     * @param source
     *            the source of the event
     * @param nodeEntry
     *            The node that the service to get deleted on.
     * @param ifaddr
     *            the interface the service to get deleted on.
     * @param service
     *            the service to delete.
     * @param hostName
     *            set to the host field in the event
     * @param txNo
     *            the transaction no.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createAndSendDeleteServiceEvent(String source, DbNodeEntry nodeEntry, InetAddress ifaddr, String service, String hostName, long txNo) {
        Category log = ThreadCategory.getInstance(EventUtils.class);
        if (log.isDebugEnabled())
            log.debug("createAndSendDeleteServiceEvent:  nodeId/interface/service  " + nodeEntry.getNodeId() + "/" + ifaddr.getHostAddress() + "/" + service);

        Event newEvent = new Event();
        newEvent.setUei(EventConstants.DELETE_SERVICE_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setNodeid(nodeEntry.getNodeId());
        newEvent.setHost(hostName);
        newEvent.setInterface(ifaddr.getHostAddress());
        newEvent.setService(service);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        // Add IP host name
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_IP_HOSTNAME);
        parmValue = new Value();
        parmValue.setContent(ifaddr.getHostName());
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Node Label
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_NODE_LABEL);
        parmValue = new Value();
        parmValue.setContent(nodeEntry.getLabel());
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Node Label source
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_NODE_LABEL_SOURCE);
        parmValue = new Value();
        char labelSource[] = new char[] { nodeEntry.getLabelSource() };
        parmValue.setContent(new String(labelSource));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }

    /**
     * This method is responsible for generating an addInterface event and
     * sending it to eventd..
     *
     * @param source
     *            the source of the event
     * @param nodeLabel
     *            the node label of the node where the interface resides.
     * @param ipaddr
     *            IP address of the interface to be added.
     * @param hostName
     *            the Host server name.
     * @param txNo
     *            the exteranl transaction number
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createAddInterfaceEvent(String source, String nodeLabel, String ipaddr, String hostName, long txNo) {
        Category log = ThreadCategory.getInstance(EventUtils.class);
        if (log.isDebugEnabled())
            log.debug("createAndSendAddInterfaceEvent:  processing updateServer event for interface:  " + ipaddr + " on server: " + hostName);

        Event newEvent = new Event();
        newEvent.setUei(EventConstants.ADD_INTERFACE_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setInterface(ipaddr);
        newEvent.setHost(hostName);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_NODE_LABEL);
        parmValue = new Value();
        parmValue.setContent(nodeLabel);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent((new Long(txNo)).toString());
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }

    /**
     * This method is responsible for generating a deleteInterface event and
     * sending it to eventd..
     *
     * @param source
     *            the source of the event
     * @param nodeLabel
     *            the node label of the node where the interface resides.
     * @param ipaddr
     *            IP address of the interface to be deleted.
     * @param hostName
     *            the Host server name.
     * @param txNo
     *            the external transaction No.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createAndSendDeleteInterfaceEvent(String source, String nodeLabel, String ipaddr, String hostName, long txNo) {
        Category log = ThreadCategory.getInstance(EventUtils.class);
        if (log.isDebugEnabled())
            log.debug("createAndSendDeleteInterfaceEvent:  processing updateServer event for interface:  " + ipaddr + " on server: " + hostName);

        Event newEvent = new Event();
        newEvent.setUei(EventConstants.DELETE_INTERFACE_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setInterface(ipaddr);
        newEvent.setHost(hostName);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_NODE_LABEL);
        parmValue = new Value();
        parmValue.setContent(nodeLabel);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent((new Long(txNo)).toString());
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }

    /**
     * This method is responsible for generating a changeService event and
     * sending it to eventd..
     *
     * @param source
     *            the source of the event
     * @param ipaddr
     *            IP address of the interface where the service resides.
     * @param service
     *            the service to be changed(add or remove).
     * @param action
     *            what operation to perform for the service/interface pair.
     * @param hostName
     *            sets the host field of the event
     * @param txNo
     *            the external transaction No.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createChangeServiceEvent(String source, String ipaddr, String service, String action, String hostName, long txNo) {
        Category log = ThreadCategory.getInstance(EventUtils.class);
        if (log.isDebugEnabled())
            log.debug("createAndSendChangeServiceEvent:  processing updateService event for service:  " + service + " on interface: " + ipaddr);

        Event newEvent = new Event();
        newEvent.setUei(EventConstants.CHANGE_SERVICE_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setInterface(ipaddr);
        newEvent.setService(service);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_ACTION);
        parmValue = new Value();
        parmValue.setContent(action);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent((new Long(txNo)).toString());
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }

    /**
     * Constructs a deleteService event for the given nodeId, ipAddress,
     * serivcename triple.
     *
     * @param source
     *            the source for the event
     * @param nodeId
     *            the nodeId of the node that service resides on
     * @param ipAddr
     *            the ipAddress of the interface the service resides on
     * @param service
     *            the service that is being deleted
     * @param txNo
     *            the transaction number to use for processing this event
     * @return an Event representing a deleteInterface event for the given
     *         nodeId, ipaddr
     */
    public static Event createDeleteServiceEvent(String source, long nodeId, String ipAddr, String service, long txNo) {
        Event newEvent = new Event();
        newEvent.setUei(EventConstants.DELETE_SERVICE_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setInterface(ipAddr);
        newEvent.setNodeid(nodeId);
        newEvent.setService(service);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent(String.valueOf(txNo));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }
    
    /**
     * Constructs a reinitializePrimarySnmpInterface event for the given nodeId
     *
     * @param source
     *            the source for the event
     * @param nodeId
     *            the nodeId of the node that service resides on
     * @param ipAddr
     *            the ipAddress of the interface the service resides on
     * @param txNo
     *            the transaction number to use for processing this event
     * @return an Event representing a reinitializePrimarySnmpInterface event for
     *         the given nodeId, ipaddr
     */
    public static Event createReinitSnmpPrimaryEvent(String source, long nodeId, String ipAddr, long txNo) {
        Event newEvent = new Event();
        newEvent.setUei(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI);
        newEvent.setSource(source);
        newEvent.setInterface(ipAddr);
        newEvent.setNodeid(nodeId);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));
        
        return newEvent;
    }

    /**
     * <p>toString</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toString(Event event) {
            StringBuffer b = new StringBuffer("Event: ");
            if (event.getAutoacknowledge() != null) {
            	b.append(" Autoacknowledge: " + event.getAutoacknowledge() + "\n");
            }
            if (event.getAutoactionCount() > 0) {
            	b.append(" Autoactions:");
            	for (Iterator<Autoaction> i = event.getAutoactionCollection().iterator(); i.hasNext(); ) {
            		b.append(" " + i.next().toString());
            	}
            b.append("\n");
            }
            if (event.getCreationTime() != null) {
            	b.append(" CreationTime: " + event.getCreationTime() + "\n");
          }
    b.append(" Dbid: " + event.getDbid() + "\n");
    if (event.getDescr() != null) {
            b.append(" Descr: " + event.getDescr() + "\n");
    }
    if (event.getDistPoller() != null) {
            b.append(" DistPoller: " + event.getDistPoller() + "\n");
    }
    if (event.getForwardCount() > 0) {
            b.append(" Forwards:");
            for (Iterator<Forward> i = event.getForwardCollection().iterator(); i.hasNext(); ) {
            	b.append(" " + i.next().toString());
            }
            b.append("\n");
    }
    if (event.getHost() != null) {
            b.append(" Host: " + event.getHost() + "\n");
    }
    if (event.getInterface() != null) {
            b.append(" Interface: " + event.getInterface() + "\n");
    }
    if (event.getLoggroupCount() > 0) {
            b.append(" Loggroup:");
            for (Iterator<String> i = event.getLoggroupCollection().iterator(); i.hasNext(); ) {
            	b.append(" " + i.next().toString());
            }
            b.append("\n");
    }
    if (event.getLogmsg() != null) {
            b.append(" Logmsg: " + event.getLogmsg() + "\n");
    }
    if (event.getMask() != null) {
            b.append(" Mask: " + event.getMask() + "\n");
    }
    if (event.getMasterStation() != null) {
            b.append(" MasterStation: " + event.getMasterStation() + "\n");
    }
    if (event.getMouseovertext() != null) {
            b.append(" Mouseovertext: " + event.getMouseovertext() + "\n");
    }
    b.append(" Nodeid: " + event.getNodeid() + "\n");
    if (event.getOperactionCount() > 0) {
            b.append(" Operaction:");
            for (Iterator<Operaction> i = event.getOperactionCollection().iterator(); i.hasNext(); ) {
            	b.append(" " + i.next().toString());
            }
            b.append("\n");
    }
    if (event.getOperinstruct() != null) {
            b.append(" Operinstruct: " + event.getOperinstruct() + "\n");
    }
    if (event.getParms() != null) {
            b.append(" Parms: " + toString(event.getParms()) + "\n");
    }
    if (event.getScriptCount() > 0) {
            b.append(" Script:");
            for (Iterator<Script> i = event.getScriptCollection().iterator(); i.hasNext(); ) {
            	b.append(" " + i.next().toString());
            }
            b.append("\n");
    }
    if (event.getService() != null) {
            b.append(" Service: " + event.getService() + "\n");
    }
    if (event.getSeverity() != null) {
            b.append(" Severity: " + event.getSeverity() + "\n");
    }
    if (event.getSnmp() != null) {
            b.append(" Snmp: " + toString(event.getSnmp()) + "\n");
    }
    if (event.getSnmphost() != null) {
            b.append(" Snmphost: " + event.getSnmphost() + "\n");
    }
    if (event.getSource() != null) {
            b.append(" Source: " + event.getSource() + "\n");
    }
    if (event.getTime() != null) {
            b.append(" Time: " + event.getTime() + "\n");
    }
    if (event.getTticket() != null) {
            b.append(" Tticket: " + event.getTticket() + "\n");
    }
    if (event.getUei() != null) {
            b.append(" Uei: " + event.getUei() + "\n");
    }
    if (event.getUuid() != null) {
            b.append(" Uuid: " + event.getUuid() + "\n");
    }
          
    b.append("End Event\n");
          return b.toString();
        }

    /**
     * <p>toString</p>
     *
     * @param parms a {@link org.opennms.netmgt.xml.event.Parms} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toString(Parms parms) {
        if (parms.getParmCount() == 0) {
    		return "Parms: (none)\n";
    	}
    	
    	StringBuffer b = new StringBuffer();
    	b.append("Parms:\n");
    	for (Enumeration<Parm> e = parms.enumerateParm(); e.hasMoreElements(); ) {
    		Parm p = e.nextElement();
    		b.append(" ");
    		b.append(p.getParmName());
    		b.append(" = ");
    		b.append(toString(p.getValue()));
    		b.append("\n");
    	}
    	b.append("End Parms\n");
    	return b.toString();
    }
    
    /**
     * <p>toString</p>
     *
     * @param value a {@link org.opennms.netmgt.xml.event.Value} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toString(Value value) {
        return value.getType() + "(" + value.getEncoding() + "): " + value.getContent();
    }

    /**
     * <p>toString</p>
     *
     * @param snmp a {@link org.opennms.netmgt.xml.event.Snmp} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toString(Snmp snmp) {
        StringBuffer b = new StringBuffer("Snmp: ");
    
        if (snmp.getVersion() != null) {
    		b.append("Version: " + snmp.getVersion() + "\n");
    	}
    	
    	b.append("TimeStamp: " + new Date(snmp.getTimeStamp()) + "\n");
    	
    	if (snmp.getCommunity() != null) {
    		b.append("Community: " + snmp.getCommunity() + "\n");
    	}
    
    	b.append("Generic: " + snmp.getGeneric() + "\n");
    	b.append("Specific: " + snmp.getSpecific() + "\n");
    	
    	if (snmp.getId() != null) {
    		b.append("Id: " + snmp.getId() + "\n");
    	}
    	if (snmp.getIdtext() != null) {
    		b.append("Idtext: " + snmp.getIdtext() + "\n");
    	}
    	
    	b.append("End Snmp\n");
    	return b.toString();
    }

	/**
	 * <p>addParam</p>
	 *
	 * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
	 * @param parmName a {@link java.lang.String} object.
	 * @param pollResultId a {@link java.lang.Object} object.
	 */
	public static void addParam(Event event, String parmName, Object pollResultId) {
		
        // Add appropriate parms
		Parms eventParms = event.getParms();
		if (eventParms == null) {
			eventParms = new Parms();
			event.setParms(eventParms);
		}
        
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(parmName);
        parmValue = new Value();
        parmValue.setContent(String.valueOf(pollResultId));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);


	}

}
