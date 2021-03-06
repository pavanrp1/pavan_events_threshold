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
// 2004 Oct 07: Added code to support RTC rescan on asset update
//
// Orginal code baseCopyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.rtc;

import java.text.ParseException;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * <P>
 * The DataUpdater is created for each event by the event receiver. Depending on
 * the event UEI, relevant information is read from the event and the
 * DataManager informed so that data maintained by the RTC is kept up-to-date
 * </P>
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
final class DataUpdater implements Runnable {
    /**
     * The event from which data is to be read
     */
    private Event m_event;

    /**
     * If it is a nodeGainedService, create a new entry in the map
     */
    private void handleNodeGainedService(long nodeid, String ip, String svcName) {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        if (nodeid == -1 || ip == null || svcName == null) {
            log.warn(m_event.getUei() + " ignored - info incomplete - nodeid/ip/svc: " + nodeid + "/" + ip + "/" + svcName);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.nodeGainedService(nodeid, ip, svcName);
        if (log.isDebugEnabled())
            log.debug(m_event.getUei() + " added " + nodeid + ": " + ip + ": " + svcName + " to data store");

    }

    /**
     * If it is a nodeLostService, update downtime on the rtcnode
     */
    private void handleNodeLostService(long nodeid, String ip, String svcName, long eventTime) {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        if (nodeid == -1 || ip == null || svcName == null || eventTime == -1) {
            log.warn(m_event.getUei() + " ignored - info incomplete - nodeid/ip/svc/eventtime: " + nodeid + "/" + ip + "/" + svcName + "/" + eventTime);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.nodeLostService(nodeid, ip, svcName, eventTime);

        if (log.isDebugEnabled())
            log.debug("Added nodeLostService to nodeid: " + nodeid + " ip: " + ip + " svcName: " + svcName);
    }

    /**
     * If it is an interfaceDown, update downtime on the appropriate rtcnodes
     */
    private void handleInterfaceDown(long nodeid, String ip, long eventTime) {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        if (nodeid == -1 || ip == null || eventTime == -1) {
            log.warn(m_event.getUei() + " ignored - info incomplete - nodeid/ip/eventtime: " + nodeid + "/" + ip + "/" + eventTime);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.interfaceDown(nodeid, ip, eventTime);

        if (log.isDebugEnabled())
            log.debug("Recorded interfaceDown for nodeid: " + nodeid + " ip: " + ip);
    }

    /**
     * If it is an nodeDown, update downtime on the appropriate rtcnodes
     */
    private void handleNodeDown(long nodeid, long eventTime) {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        if (nodeid == -1 || eventTime == -1) {
            log.warn(m_event.getUei() + " ignored - info incomplete - nodeid/eventtime: " + nodeid + "/" + eventTime);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.nodeDown(nodeid, eventTime);

        if (log.isDebugEnabled())
            log.debug("Recorded nodeDown for nodeid: " + nodeid);
    }

    /**
     * If it is a nodeUp, update regained time on the appropriate rtcnodes
     */
    private void handleNodeUp(long nodeid, long eventTime) {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        if (nodeid == -1 || eventTime == -1) {
            log.warn(m_event.getUei() + " ignored - info incomplete - nodeid/eventtime: " + nodeid + "/" + eventTime);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.nodeUp(nodeid, eventTime);

        if (log.isDebugEnabled())
            log.debug("Recorded nodeUp for nodeid: " + nodeid);
    }

    /**
     * If it is an interfaceUp, update regained time on the appropriate rtcnodes
     */
    private void handleInterfaceUp(long nodeid, String ip, long eventTime) {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        if (nodeid == -1 || ip == null || eventTime == -1) {
            log.warn(m_event.getUei() + " ignored - info incomplete - nodeid/ip/eventtime: " + nodeid + "/" + ip + "/" + eventTime);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.interfaceUp(nodeid, ip, eventTime);

        if (log.isDebugEnabled())
            log.debug("Recorded interfaceUp for nodeid: " + nodeid + " ip: " + ip);
    }

    /**
     * If it is a nodeRegainedService, update downtime on the rtcnode
     */
    private void handleNodeRegainedService(long nodeid, String ip, String svcName, long eventTime) {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        if (nodeid == -1 || ip == null || svcName == null || eventTime == -1) {
            log.warn(m_event.getUei() + " ignored - info incomplete - nodeid/ip/svc/eventtime: " + nodeid + "/" + ip + "/" + svcName + "/" + eventTime);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.nodeRegainedService(nodeid, ip, svcName, eventTime);

        if (log.isDebugEnabled())
            log.debug("Added nodeRegainedService to nodeid: " + nodeid + " ip: " + ip + " svcName: " + svcName);
    }

    /**
     * If it is a serviceDeleted, remove corresponding rtcnodes from the map
     */
    private void handleServiceDeleted(long nodeid, String ip, String svcName) {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        if (nodeid == -1 || ip == null || svcName == null) {
            log.warn(m_event.getUei() + " ignored - info incomplete - nodeid/ip/svc: " + nodeid + "/" + ip + "/" + svcName);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.serviceDeleted(nodeid, ip, svcName);

        if (log.isDebugEnabled())
            log.debug(m_event.getUei() + " deleted " + nodeid + ": " + ip + ": " + svcName + " from data store");

    }

    /**
     * Record the interfaceReparented info in the datastore
     */
    private void handleInterfaceReparented(String ip, Parms eventParms) {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        if (ip == null || eventParms == null) {
            log.warn(m_event.getUei() + " ignored - info incomplete - ip/parms: " + ip + "/" + eventParms);
            return;
        }

        // old nodeid
        long oldNodeId = -1;

        // new nodeid
        long newNodeId = -1;

        String parmName = null;
        Value parmValue = null;
        String parmContent = null;

        Enumeration parmEnum = eventParms.enumerateParm();
        while (parmEnum.hasMoreElements()) {
            Parm parm = (Parm) parmEnum.nextElement();
            parmName = parm.getParmName();
            parmValue = parm.getValue();
            if (parmValue == null)
                continue;
            else
                parmContent = parmValue.getContent();

            // old nodeid
            if (parmName.equals(EventConstants.PARM_OLD_NODEID)) {
                String temp = parmContent;
                try {
                    oldNodeId = Long.valueOf(temp).longValue();
                } catch (NumberFormatException nfe) {
                    log.warn("Parameter " + EventConstants.PARM_OLD_NODEID + " cannot be non-numeric", nfe);
                    oldNodeId = -1;
                }
            }

            // new nodeid
            else if (parmName.equals(EventConstants.PARM_NEW_NODEID)) {
                String temp = parmContent;
                try {
                    newNodeId = Long.valueOf(temp).longValue();
                } catch (NumberFormatException nfe) {
                    log.warn("Parameter " + EventConstants.PARM_NEW_NODEID + " cannot be non-numeric", nfe);
                    newNodeId = -1;
                }
            }

        }

        if (oldNodeId == -1 || newNodeId == -1) {
            log.warn(m_event.getUei() + " did not have all required information for " + ip + " Values contained old nodeid: " + oldNodeId + " new nodeid: " + newNodeId);
        } else {
            DataManager dataMgr = RTCManager.getDataManager();
            dataMgr.interfaceReparented(ip, oldNodeId, newNodeId);
            if (log.isDebugEnabled())
                log.debug(m_event.getUei() + " reparented ip: " + ip + " from " + oldNodeId + " to " + newNodeId);

        }

    }

    /**
     * Inform the data sender of the new listener
     */
    private void handleRtcSubscribe(Parms eventParms) {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        if (eventParms == null) {
            log.warn(m_event.getUei() + " ignored - info incomplete - parms: " + eventParms);
            return;
        }

        String url = null;
        String clabel = null;
        String user = null;
        String passwd = null;

        String parmName = null;
        Value parmValue = null;
        String parmContent = null;

        Enumeration parmEnum = eventParms.enumerateParm();
        while (parmEnum.hasMoreElements()) {
            Parm parm = (Parm) parmEnum.nextElement();
            parmName = parm.getParmName();
            parmValue = parm.getValue();
            if (parmValue == null)
                continue;
            else
                parmContent = parmValue.getContent();

            if (parmName.equals(EventConstants.PARM_URL)) {
                url = parmContent;
            }

            else if (parmName.equals(EventConstants.PARM_CAT_LABEL)) {
                clabel = parmContent;
            }

            else if (parmName.equals(EventConstants.PARM_USER)) {
                user = parmContent;
            }

            else if (parmName.equals(EventConstants.PARM_PASSWD)) {
                passwd = parmContent;
            }

        }

        // check that we got all required parms
        if (url == null || clabel == null || user == null || passwd == null) {
            log.warn(m_event.getUei() + " did not have all required information. Values contained url:  " + url + " catlabel: " + clabel + " user: " + user + "passwd: " + passwd);
        } else {
            RTCManager.getInstance().getDataSender().subscribe(url, clabel, user, passwd);
            if (log.isDebugEnabled())
                log.debug(m_event.getUei() + " subscribed " + url + ": " + clabel + ": " + user);

        }
    }

    /**
     * Inform the data sender of the listener unsubscribing
     */
    private void handleRtcUnsubscribe(Parms eventParms) {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        if (eventParms == null) {
            log.warn(m_event.getUei() + " ignored - info incomplete - parms: " + eventParms);
            return;
        }

        String url = null;

        String parmName = null;
        Value parmValue = null;
        String parmContent = null;

        Enumeration parmEnum = eventParms.enumerateParm();
        while (parmEnum.hasMoreElements()) {
            Parm parm = (Parm) parmEnum.nextElement();
            parmName = parm.getParmName();
            parmValue = parm.getValue();
            if (parmValue == null)
                continue;
            else
                parmContent = parmValue.getContent();

            if (parmName.equals(EventConstants.PARM_URL)) {
                url = parmContent;
            }
        }

        // check that we got the required parm
        if (url == null) {
            log.warn(m_event.getUei() + " did not have required information.  Value of url:  " + url);
        } else {
            RTCManager.getInstance().getDataSender().unsubscribe(url);
            if (log.isDebugEnabled())
                log.debug(m_event.getUei() + " unsubscribed " + url);
        }
    }

    /**
     * If it is a assetInfoChanged method, update RTC
     */
    private void handleAssetInfoChangedEvent(long nodeid) {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        DataManager dataMgr = RTCManager.getDataManager();

        dataMgr.assetInfoChanged(nodeid);

        if (log.isDebugEnabled())
            log.debug(m_event.getUei() + " asset info changed for node " + nodeid);

    }
    
    /**
     * If a node's surveillance category membership changed,
     * update RTC since RTC categories may include surveillance
     * categories via "categoryName" or "catinc*" rules
     */
    private void handleNodeCategoryMembershipChanged(long nodeid) {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        DataManager dataMgr = RTCManager.getDataManager();

        dataMgr.nodeCategoryMembershipChanged(nodeid);

        if (log.isDebugEnabled())
            log.debug(m_event.getUei() + " surveillance category membership changed for node " + nodeid);
    }

    /**
     * Read the event UEI, nodeid, interface and service - depending on the UEI,
     * read event parms, if necessary, and call appropriate methods on the data
     * manager to update data
     */
    private void processEvent() {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        if (m_event == null) {
            if (log.isDebugEnabled())
                log.debug("Event is null, nothing to process");
            return;
        }

        // uei
        String eventUEI = m_event.getUei();
        if (eventUEI == null) {
            // huh? should only get registered events
            if (log.isDebugEnabled())
                log.debug("Event received with null UEI, ignoring event");
            return;
        }

        // node id
        long nodeid = -1;
        if (m_event.hasNodeid()) {
            nodeid = m_event.getNodeid();
        }

        // ip
        String ip = m_event.getInterface();

        // service name
        String svcName = m_event.getService();

        // event time
        long eventTime = -1;
        String eventTimeStr = m_event.getTime();
        try {
            java.util.Date date = EventConstants.parseToDate(eventTimeStr);
            eventTime = date.getTime();
        } catch (ParseException pe) {
            log.warn("Failed to convert time " + eventTime + " to java.util.Date, Setting current time instead", pe);

            eventTime = (new java.util.Date()).getTime();
        }

        if (log.isDebugEnabled())
            log.debug("Event UEI: " + eventUEI + "\tnodeid: " + nodeid + "\tip: " + ip + "\tsvcName: " + svcName + "\teventTime: " + eventTimeStr);

        //
        //
        // Check for any of the following UEIs:
        //
        // nodeGainedService
        // nodeLostService
        // interfaceDown
        // nodeDown
        // nodeUp
        // interfaceUp
        // nodeRegainedService
        // serviceDeleted
        // interfaceReparented
        // subscribe
        // unsubscribe
        //
        if (eventUEI.equals(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)) {
            handleNodeGainedService(nodeid, ip, svcName);
        } else if (eventUEI.equals(EventConstants.NODE_LOST_SERVICE_EVENT_UEI)) {
            handleNodeLostService(nodeid, ip, svcName, eventTime);
        } else if (eventUEI.equals(EventConstants.INTERFACE_DOWN_EVENT_UEI)) {
            handleInterfaceDown(nodeid, ip, eventTime);
        } else if (eventUEI.equals(EventConstants.NODE_DOWN_EVENT_UEI)) {
            handleNodeDown(nodeid, eventTime);
        } else if (eventUEI.equals(EventConstants.NODE_UP_EVENT_UEI)) {
            handleNodeUp(nodeid, eventTime);
        } else if (eventUEI.equals(EventConstants.INTERFACE_UP_EVENT_UEI)) {
            handleInterfaceUp(nodeid, ip, eventTime);
        } else if (eventUEI.equals(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI)) {
            handleNodeRegainedService(nodeid, ip, svcName, eventTime);
        } else if (eventUEI.equals(EventConstants.SERVICE_DELETED_EVENT_UEI)) {
            handleServiceDeleted(nodeid, ip, svcName);
        } else if (eventUEI.equals(EventConstants.SERVICE_UNMANAGED_EVENT_UEI)) {
            handleServiceDeleted(nodeid, ip, svcName);
        } else if (eventUEI.equals(EventConstants.INTERFACE_REPARENTED_EVENT_UEI)) {
            handleInterfaceReparented(ip, m_event.getParms());
        } else if (eventUEI.equals(EventConstants.RTC_SUBSCRIBE_EVENT_UEI)) {
            handleRtcSubscribe(m_event.getParms());
        } else if (eventUEI.equals(EventConstants.RTC_UNSUBSCRIBE_EVENT_UEI)) {
            handleRtcUnsubscribe(m_event.getParms());
        } else if (eventUEI.equals(EventConstants.ASSET_INFO_CHANGED_EVENT_UEI)) {
            handleAssetInfoChangedEvent(nodeid);
        } else if (eventUEI.equals(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI)) {
        	handleNodeCategoryMembershipChanged(nodeid);
        } else {
            if (log.isDebugEnabled())
                log.debug("Event subscribed for not handled?!: " + eventUEI);
        }

        // increment the counter on the rtcvcm
        RTCManager.getInstance().incrementCounter();
    }

    /**
     * Constructs the DataUpdater object
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public DataUpdater(Event event) {
        m_event = event;
    }

    /**
     * Process the event depending on the UEI and update date
     */
    public void run() {
        Category log = ThreadCategory.getInstance(DataUpdater.class);

        try {
            processEvent();
        } catch (Throwable t) {
            log.warn("Unexpected exception processing event", t);
        }
    }
}
