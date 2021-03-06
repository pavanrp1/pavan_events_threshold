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
// 2008 Feb 09: Java 5 generics and loops. - dj@opennms.org
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
package org.opennms.netmgt.poller.mock;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.poller.pollables.PendingPollEvent;
import org.opennms.netmgt.poller.pollables.PollContext;
import org.opennms.netmgt.poller.pollables.PollEvent;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.MockUtil;


public class MockPollContext implements PollContext, EventListener {
    private String m_critSvcName;
    private boolean m_nodeProcessingEnabled;
    private boolean m_pollingAllIfCritServiceUndefined;
    private boolean m_serviceUnresponsiveEnabled;
    private EventIpcManager m_eventMgr;
    private MockDatabase m_db;
    private MockNetwork m_mockNetwork;
    private List<PendingPollEvent> m_pendingPollEvents = new LinkedList<PendingPollEvent>();
    

    public String getCriticalServiceName() {
        return m_critSvcName;
    }
    
    public void setCriticalServiceName(String svcName) {
        m_critSvcName = svcName;
    }
    
    public boolean isNodeProcessingEnabled() {
        return m_nodeProcessingEnabled;
    }
    public void setNodeProcessingEnabled(boolean nodeProcessingEnabled) {
        m_nodeProcessingEnabled = nodeProcessingEnabled;
    }
    public boolean isPollingAllIfCritServiceUndefined() {
        return m_pollingAllIfCritServiceUndefined;
    }
    public void setPollingAllIfCritServiceUndefined(boolean pollingAllIfCritServiceUndefined) {
        m_pollingAllIfCritServiceUndefined = pollingAllIfCritServiceUndefined;
    }
    
    public void setEventMgr(EventIpcManager eventMgr) {
        if (m_eventMgr != null) {
            m_eventMgr.removeEventListener(this);
        }
        m_eventMgr = eventMgr;
        if (m_eventMgr != null) {
            m_eventMgr.addEventListener(this);
        }
    }
    
    public void setDatabase(MockDatabase db) {
        m_db = db;
    }
    
    public void setMockNetwork(MockNetwork network) {
        m_mockNetwork = network;
    }
    public PollEvent sendEvent(Event event) {
        PendingPollEvent pollEvent = new PendingPollEvent(event);
        synchronized (this) {
            m_pendingPollEvents.add(pollEvent);
        }
        m_eventMgr.sendNow(event);
        return pollEvent;
    }
    
    

    public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, Date date, String reason) {
        String eventTime = EventConstants.formatToString(date);
        Event e = MockEventUtil.createEvent("Test", uei, nodeId, (address == null ? null : address.getHostAddress()), svcName, reason);
        e.setCreationTime(eventTime);
        e.setTime(eventTime);
        return e;
    }
    public void openOutage(final PollableService pSvc, final PollEvent svcLostEvent) {
        Runnable r = new Runnable() {
            public void run() {
                writeOutage(pSvc, svcLostEvent);
            }
        };
        if (svcLostEvent instanceof PendingPollEvent)
            ((PendingPollEvent)svcLostEvent).addPending(r);
        else  
            r.run();
    }
    
    private void writeOutage(PollableService pSvc, PollEvent svcLostEvent) {
        MockService mSvc = m_mockNetwork.getService(pSvc.getNodeId(), pSvc.getIpAddr(), pSvc.getSvcName());
        Timestamp eventTime = m_db.convertEventTimeToTimeStamp(EventConstants.formatToString(svcLostEvent.getDate()));
        MockUtil.println("Opening Outage for "+mSvc);
        m_db.createOutage(mSvc, svcLostEvent.getEventId(), eventTime);

    }
    public void resolveOutage(final PollableService pSvc, final PollEvent svcRegainEvent) {
        Runnable r = new Runnable() {
            public void run() {
                closeOutage(pSvc, svcRegainEvent);
            }
        };
        if (svcRegainEvent instanceof PendingPollEvent)
            ((PendingPollEvent)svcRegainEvent).addPending(r);
        else  
            r.run();
    }
    
    public void closeOutage(PollableService pSvc, PollEvent svcRegainEvent) {
        MockService mSvc = m_mockNetwork.getService(pSvc.getNodeId(), pSvc.getIpAddr(), pSvc.getSvcName());
        Timestamp eventTime = m_db.convertEventTimeToTimeStamp(EventConstants.formatToString(svcRegainEvent.getDate()));
        MockUtil.println("Resolving Outage for "+mSvc);
        m_db.resolveOutage(mSvc, svcRegainEvent.getEventId(), eventTime);
    }
    
    public void reparentOutages(String ipAddr, int oldNodeId, int newNodeId) {
        m_db.update("update outages set nodeId = ? where nodeId = ? and ipaddr = ?", newNodeId, oldNodeId, ipAddr);
    }
    
    public boolean isServiceUnresponsiveEnabled() {
        return m_serviceUnresponsiveEnabled;
    }
    public void setServiceUnresponsiveEnabled(boolean serviceUnresponsiveEnabled) {
        m_serviceUnresponsiveEnabled = serviceUnresponsiveEnabled;
    }

    public String getName() {
        return "MockPollContext";
    }

    public synchronized void onEvent(Event e) {
        synchronized (m_pendingPollEvents) {
            for (PendingPollEvent pollEvent : m_pendingPollEvents) {
                if (e.equals(pollEvent.getEvent())) {
                    pollEvent.complete(e);
                }
            }
            
            for (Iterator<PendingPollEvent> it = m_pendingPollEvents.iterator(); it.hasNext(); ) {
                PendingPollEvent pollEvent = it.next();
                if (pollEvent.isPending()) {
                    break;
                }
                
                pollEvent.processPending();
                it.remove();
            }
        }
    }
}