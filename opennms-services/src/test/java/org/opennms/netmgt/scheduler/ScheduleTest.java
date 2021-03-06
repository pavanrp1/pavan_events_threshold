//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004-2005 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.scheduler;


import junit.framework.TestCase;

import org.opennms.netmgt.poller.mock.MockInterval;
import org.opennms.netmgt.poller.mock.MockScheduler;
import org.opennms.test.mock.MockLogAppender;

/**
 * Represents a ScheduleTest 
 *
 * @author brozow
 */
public class ScheduleTest extends TestCase {

    private MockSchedulable m_schedulable;
    private MockInterval m_interval;
    private MockScheduler m_scheduler;
    private Schedule m_sched;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ScheduleTest.class);
    }
    
    class MockSchedulable implements ReadyRunnable {
        private volatile int runCount = 0;
        private volatile boolean m_callingAdjustSchedule;
        
        public boolean isReady() {
            return true;
        }
    
        public void run() {
            runCount++;
            if (isCallingAdjustSchedule())
                m_sched.adjustSchedule();
        }
        
        public int getRunCount() {
            return runCount;
        }
        
        public void setCallingAdjustSchedule(boolean callingAdjustSchedule) {
            m_callingAdjustSchedule = callingAdjustSchedule;
        }
        
        public boolean isCallingAdjustSchedule() {
            return m_callingAdjustSchedule;
        }
        
    }
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        MockLogAppender.setupLogging();
        m_schedulable = new MockSchedulable();
        m_scheduler = new MockScheduler();
        m_interval = new MockInterval(m_scheduler, 1000L);
        m_sched = new Schedule(m_schedulable, m_interval, m_scheduler);        
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
        super.tearDown();
    }
    
    public void testSchedule() {
        m_sched.schedule();
        
        assertRunAndScheduled(0, 0, 0, 1);


        m_scheduler.next();

        assertRunAndScheduled(0, 1000, 1, 1);

        m_scheduler.next();
        
        assertRunAndScheduled(1000, 1000, 2, 1);
    }
    
    public void testAdjustSchedule() {
        
        m_sched.schedule();
        
        assertRunAndScheduled(0, 0, 0, 1);
        
        m_scheduler.next();
        
        m_interval.setInterval(900);
        m_sched.adjustSchedule();
        
        assertRunAndScheduled(0, 900, 1, 2);
        
        m_scheduler.next();
        
        assertRunAndScheduled(900, 900, 2, 2);

        // jump to the expired entry
        m_scheduler.next();
        
        // note that we don't increase the run count
        assertRunAndScheduled(1000, 800, 2, 1);
        
        m_scheduler.next();

        assertRunAndScheduled(1800, 900, 3, 1);
        
        m_scheduler.next();
        
        assertRunAndScheduled(2700, 900, 4, 1);
        
        m_interval.setInterval(1000);
        m_sched.adjustSchedule();
        
        // jump to the expired entry
        m_scheduler.next();
        
        assertRunAndScheduled(3600, 100, 4, 1);
        
        m_scheduler.next();

        assertRunAndScheduled(3700, 1000, 5, 1);
        
    }
    
    public void testUnschedule() {
        m_sched.schedule();
        
        assertRunAndScheduled(0, 0, 0, 1);

        m_scheduler.next();

        assertRunAndScheduled(0, 1000, 1, 1);

        m_scheduler.next();
        
        assertRunAndScheduled(1000, 1000, 2, 1);
        
        m_sched.unschedule();
        
        // jump to the expired entry
        m_scheduler.next();
        
        assertRunAndScheduled(2000, -1, 2, 0);
    }
    
    public void testTemporarilySuspend() {
        m_interval.addSuspension(1500, 2500);
        
        m_sched.schedule();
        
        assertRunAndScheduled(0, 0, 0, 1);

        m_scheduler.next();

        assertRunAndScheduled(0, 1000, 1, 1);

        m_scheduler.next();
        
        assertRunAndScheduled(1000, 1000, 2, 1);

        // this is the suspended entry
        m_scheduler.next();
        
        // assert that the entry has not run
        assertRunAndScheduled(2000, 1000, 2, 1);
        
        m_scheduler.next();
        
        assertRunAndScheduled(3000, 1000, 3, 1);
    }
    
    public void testAdjustScheduleWithinRun() {
        m_schedulable.setCallingAdjustSchedule(true);
        
        m_sched.schedule();
        
        assertRunAndScheduled(0, 0, 0, 1);

        m_scheduler.next();

        assertRunAndScheduled(0, 1000, 1, 1);

        m_scheduler.next();
        
        assertRunAndScheduled(1000, 1000, 2, 1);
    }
    
    private void assertRunAndScheduled(long currentTime, long interval, int count, int entryCount) {
        assertEquals(count, m_schedulable.getRunCount());
        assertEquals(currentTime, m_scheduler.getCurrentTime());
        assertEquals(entryCount, m_scheduler.getEntryCount());
        if (entryCount > 0)
            assertNotNull(m_scheduler.getEntries().get(new Long(currentTime+interval)));
        
    }


}
