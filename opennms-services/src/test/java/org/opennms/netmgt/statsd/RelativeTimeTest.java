/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 05: Created this file. - dj@opennms.org
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
package org.opennms.netmgt.statsd;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.statsd.RelativeTime;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class RelativeTimeTest extends TestCase {
    public void testYesterdayBeginningDST() {
        final TimeZone usEastern = TimeZone.getTimeZone("US/Eastern");
            
        RelativeTime yesterday = RelativeTime.YESTERDAY;
        yesterday.setTimeKeeper(new TimeKeeper() {

            public Date getCurrentDate() {
                Calendar cal = new GregorianCalendar(usEastern);
                cal.set(2006, Calendar.APRIL, 3, 10, 0, 0);
                return cal.getTime();
            }

            public long getCurrentTime() {
                return getCurrentDate().getTime();
            }
            
        });
        
        Date start = yesterday.getStart();
        Date end = yesterday.getEnd();

        assertEquals("start date", "Sun Apr 02 00:00:00 EST 2006", start.toString());
        assertEquals("end date", "Mon Apr 03 00:00:00 EDT 2006", end.toString());
        assertEquals("end date - start date", 82800000, end.getTime() - start.getTime());
    }
    
    public void testYesterdayEndingDST() {
        final TimeZone usEastern = TimeZone.getTimeZone("US/Eastern");

        RelativeTime yesterday = RelativeTime.YESTERDAY;
        yesterday.setTimeKeeper(new TimeKeeper() {

            public Date getCurrentDate() {
                Calendar cal = new GregorianCalendar(usEastern);
                cal.set(2006, Calendar.OCTOBER, 30, 10, 0, 0);
                return cal.getTime();
            }

            public long getCurrentTime() {
                return getCurrentDate().getTime();
            }
            
        });
        
        Date start = yesterday.getStart();
        Date end = yesterday.getEnd();

        assertEquals("start date", "Sun Oct 29 00:00:00 EDT 2006", start.toString());
        assertEquals("end date", "Mon Oct 30 00:00:00 EST 2006", end.toString());
        assertEquals("end date - start date", 90000000, end.getTime() - start.getTime());
    }
}
