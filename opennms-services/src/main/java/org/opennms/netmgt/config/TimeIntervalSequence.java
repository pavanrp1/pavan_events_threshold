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
// 2007 Jul 14: Use Java 5 generics and indent. - dj@opennms.org
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

// TODO make this implement Collection
/**
 * <p>TimeIntervalSequence class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class TimeIntervalSequence {
    
    private static class TimeIntervalSeqIter implements Iterator<TimeInterval> {

        private TimeIntervalSequence m_current;
        
        public TimeIntervalSeqIter(TimeIntervalSequence seq) {
            m_current = seq;
        }

        public boolean hasNext() {
            return m_current != null && m_current.m_interval != null;
        }

        public TimeInterval next() {
            TimeInterval interval = m_current.m_interval;
            m_current = m_current.m_tail;
            return interval;
        }

        public void remove() {
            throw new UnsupportedOperationException("not implemented yet");
        }

    }

    private TimeInterval m_interval;
    private TimeIntervalSequence m_tail;
    
    /**
     * <p>Constructor for TimeIntervalSequence.</p>
     */
    public TimeIntervalSequence() {
        this(null, null);
    }

    /**
     * <p>Constructor for TimeIntervalSequence.</p>
     *
     * @param interval a {@link org.opennms.netmgt.config.TimeInterval} object.
     */
    public TimeIntervalSequence(TimeInterval interval) {
        this(interval, null);
    }
    
    private TimeIntervalSequence(TimeInterval interval, TimeIntervalSequence tail) {
        m_interval = interval;
        m_tail = tail;
    }
    
    /**
     * <p>iterator</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    public Iterator<TimeInterval> iterator() {
        return new TimeIntervalSeqIter(this);
    }
    
    Date min(Date a, Date b) {
       return (a.before(b) ? a : b); 
    }
    
    Date max(Date a, Date b) {
        return (b.before(a) ? a: b);
    }

    /**
     * <p>addInterval</p>
     *
     * @param interval a {@link org.opennms.netmgt.config.TimeInterval} object.
     */
    public void addInterval(TimeInterval interval) {
        if (m_interval == null) {
            m_interval = interval;
        } else if (m_interval.preceeds(interval)) {
            addPreceedingInterval(interval);
        } else if (m_interval.follows(interval)) {
            addSucceedingInterval(interval);
        } else if (m_interval.overlaps(interval)) {
            addOverlappingInterval(interval);
        }
        
    }

    private void addOverlappingInterval(TimeInterval newInterval) {
        // overlapping intervals
        Collection newIntervals = combineIntervals(m_interval, newInterval);
        
        // remove the current interval since we are replacing it with the new ones
        removeCurrent();
        
        // now add the new intervals
        addAll(newIntervals);
        
        
    }

    /**
     * <p>combineIntervals</p>
     *
     * @param currentInterval a {@link org.opennms.netmgt.config.TimeInterval} object.
     * @param newInterval a {@link org.opennms.netmgt.config.TimeInterval} object.
     * @return a {@link java.util.Collection} object.
     */
    protected Collection combineIntervals(TimeInterval currentInterval, TimeInterval newInterval) {
        List<TimeInterval> newIntervals = new ArrayList<TimeInterval>(3);
        
        // overlapping intervals get divided into three non-overlapping segments
        // that are bounded by the below
        Date first = min(currentInterval.getStart(), newInterval.getStart());
        Date second = max(currentInterval.getStart(), newInterval.getStart());
        Date third = min(currentInterval.getEnd(), newInterval.getEnd());
        Date fourth = max(currentInterval.getEnd(), newInterval.getEnd());
        
        // contruct up to three non-overlapping intervals that can be added to the list
        if (first.equals(second)) { 
            // if the first segment is empty then the second segment because head of the list
            // the second segment is not empty becuase intervals can't be empty
            newIntervals.add(createInterval(first, third));
        } else {
            // first segment is not empty make it head of the list and add the 
            // second the the tail
            newIntervals.add(createInterval(first, second));
            newIntervals.add(createInterval(second, third));
        }
        
        
        if (!third.equals(fourth)) {
            // if the third segment no empty add it to the tail as well.
            // Note: this segment may overlap with the original lists next interval
            newIntervals.add(createInterval(third, fourth));
        }
        return newIntervals;
    }

    private void addSucceedingInterval(TimeInterval interval) {
        // new interval is earlier than current interval
        // replace current with new and add current to the tail
        TimeIntervalSequence oldTail = m_tail;
        m_tail = createTail(m_interval);
        m_tail.m_tail = oldTail;
        m_interval = interval;
    }

    private void addPreceedingInterval(TimeInterval interval) {
        // new interval is later than current interval so add it to the tail
        addToTail(interval);
    }

    private void addToTail(TimeInterval interval) {
        if (m_tail == null) {
            m_tail = createTail(interval);
        } else {
            m_tail.addInterval(interval);
        }
    }
    
    /**
     * <p>createInterval</p>
     *
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @return a {@link org.opennms.netmgt.config.TimeInterval} object.
     */
    protected TimeInterval createInterval(Date start, Date end) {
        return new TimeInterval(start, end);
    }
    
    /**
     * <p>createTail</p>
     *
     * @param interval a {@link org.opennms.netmgt.config.TimeInterval} object.
     * @return a {@link org.opennms.netmgt.config.TimeIntervalSequence} object.
     */
    protected TimeIntervalSequence createTail(TimeInterval interval) {
        return new TimeIntervalSequence(interval);
    }
    
    private void removeCurrent() {
        if (m_tail == null) {
            m_interval = null;
        } else {
            m_interval = m_tail.m_interval;
            m_tail = m_tail.m_tail;
        }
    }

    /**
     * <p>removeInterval</p>
     *
     * @param removedInterval a {@link org.opennms.netmgt.config.TimeInterval} object.
     */
    public void removeInterval(TimeInterval removedInterval) {
        if (m_interval == null) {
            return;
        }
        
        if (m_interval.preceeds(removedInterval)) {
            removeFromTail(removedInterval);
        } else if (m_interval.follows(removedInterval)) {
            // no need to do anything because the entire remove interval is before this
            return; 
        } else if (m_interval.overlaps(removedInterval)) {
            
            TimeInterval origInterval = m_interval;

            // remove the region from the tail of sequence
            removeFromTail(removedInterval);
            
            // remove the current element
            removeCurrent();
            
            
            // add back any part of the original interval that followd the remove interval
            Collection newIntervals = separateIntervals(origInterval, removedInterval);
            
            addAll(newIntervals);
        }
    }

    /**
     * <p>separateIntervals</p>
     *
     * @param origInterval a {@link org.opennms.netmgt.config.TimeInterval} object.
     * @param removedInterval a {@link org.opennms.netmgt.config.TimeInterval} object.
     * @return a {@link java.util.Collection} object.
     */
    protected Collection separateIntervals(TimeInterval origInterval, TimeInterval removedInterval) {
        List<TimeInterval> newIntervals = new ArrayList<TimeInterval>(2);
        if (removedInterval.getEnd().before(origInterval.getEnd())) {
            newIntervals.add(createInterval(removedInterval.getEnd(), origInterval.getEnd()));
        }
        
        // add back any part of the original interval the preceeded the remove interval
        if (origInterval.getStart().before(removedInterval.getStart())) {
            newIntervals.add(createInterval(origInterval.getStart(), removedInterval.getStart()));
        }
        return newIntervals;
    }

    private void removeFromTail(TimeInterval interval) {
        if (m_tail == null) {
            return;
        }
        
        m_tail.removeInterval(interval);
        if (m_tail.m_interval == null) {
            m_tail = null;
        }
    }
    
    /**
     * <p>bound</p>
     *
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     */
    public void bound(Date start, Date end) {
        removeInterval(createInterval(new Date(0), start));
        removeInterval(createInterval(end, new Date(Long.MAX_VALUE)));
    }

    /**
     * <p>bound</p>
     *
     * @param interval a {@link org.opennms.netmgt.config.TimeInterval} object.
     */
    public void bound(TimeInterval interval) {
        bound(interval.getStart(), interval.getEnd());
    }
    
    /**
     * <p>getStart</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getStart() {
        if (m_interval == null) return null;
        return m_interval.getStart();
    }
    
    /**
     * <p>getEnd</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getEnd() {
        if (m_interval == null) return null;
        if (m_tail == null) return m_interval.getEnd();
        return m_tail.getEnd();
    }
    
    /**
     * <p>getBounds</p>
     *
     * @return a {@link org.opennms.netmgt.config.TimeInterval} object.
     */
    public TimeInterval getBounds() {
        Date start = getStart();
        Date end = getEnd();
        return (start == null || end == null ? null : new TimeInterval(start, end));
    }

    /**
     * <p>addAll</p>
     *
     * @param intervals a {@link org.opennms.netmgt.config.TimeIntervalSequence} object.
     */
    public void addAll(TimeIntervalSequence intervals) {
        for (Iterator it = intervals.iterator(); it.hasNext();) {
            TimeInterval interval = (TimeInterval) it.next();
            addInterval(interval);
        }
    }
    
    /**
     * <p>addAll</p>
     *
     * @param intervals a {@link java.util.Collection} object.
     */
    public void addAll(Collection intervals) {
        for (Iterator it = intervals.iterator(); it.hasNext();) {
            TimeInterval interval = (TimeInterval) it.next();
            addInterval(interval);
        }
    }
    
    /**
     * <p>removeAll</p>
     *
     * @param intervals a {@link org.opennms.netmgt.config.TimeIntervalSequence} object.
     */
    public void removeAll(TimeIntervalSequence intervals) {
        for (Iterator it = intervals.iterator(); it.hasNext();) {
            TimeInterval interval = (TimeInterval) it.next();
            removeInterval(interval);
        }
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("[");
        boolean first = true;
        for (Iterator it = this.iterator(); it.hasNext();) {
            TimeInterval interval = (TimeInterval) it.next();
            if (first) {
                first = false;
            } else {
                buf.append(",");
            }
            
            buf.append(interval);
        }
        buf.append(']');
        return buf.toString();
    }

}
