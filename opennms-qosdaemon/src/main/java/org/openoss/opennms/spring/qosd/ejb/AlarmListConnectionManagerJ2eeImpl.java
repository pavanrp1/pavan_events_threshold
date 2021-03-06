// This file is part of the OpenNMS(R) QoSD OSS/J interface.
//
// Copyright (C) 2006-2007 Craig Gallen, 
//                         University of Southampton,
//                         School of Electronics and Computer Science
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// See: http://www.fsf.org/copyleft/lesser.html
//

 package org.openoss.opennms.spring.qosd.ejb;

import java.util.Hashtable;
import java.util.Properties;

import org.openoss.opennms.spring.qosd.AlarmListConnectionManager;
import org.openoss.opennms.spring.qosd.PropertiesLoader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * This class provides an implimentation of a AlarmListConnectionManager which
 * connects to an external AlarmMonitor bean in a J2ee container ( i.e.
 * in Jboss ). It proxys the calls to the AlarmListJ2eeConnectionManagerThread which
 * coes the actiual connection. This allows Spring wiring to be used to
 * select this or nother class as the AlarmListConnectionManager in QosD
 *
 * @author ranger
 * @version $Id: $
 */
public class AlarmListConnectionManagerJ2eeImpl implements AlarmListConnectionManager {

	AlarmListJ2eeConnectionManagerThread cmt;
	
	/**
	 * <p>Constructor for AlarmListConnectionManagerJ2eeImpl.</p>
	 */
	public AlarmListConnectionManagerJ2eeImpl() {
		cmt = new AlarmListJ2eeConnectionManagerThread();
	}

	/**
	 * <p>getStatus</p>
	 *
	 * @return a int.
	 */
	public int getStatus() {
		return cmt.getStatus();
	}

	/** {@inheritDoc} */
	public void init(PropertiesLoader props, Properties env) {
		cmt.init(props, env);
	}

	/**
	 * <p>kill</p>
	 */
	public void kill() {
		cmt.kill();
	}

	/** {@inheritDoc} */
	public void reset_list(String _rebuilt_message) {
		cmt.reset_list(_rebuilt_message);
	}

	/**
	 * <p>run</p>
	 *
	 * @throws java.lang.IllegalStateException if any.
	 */
	public void run() throws IllegalStateException {
		cmt.run();
	}

	/** {@inheritDoc} */
	public void send(Hashtable alarmList) {
		cmt.send(alarmList);

	}
	
	/**
	 * Causes the thread supporting the connection Manager to start
	 */
	public void start(){
		cmt.start();
	}
	
	/**
	 * Makes a new empty alarm value object
	 * NOTE THIS IS A PATCH to proxy for JVTAlarmMonitorSession.makeAlarmValue()
	 *
	 * @return a {@link javax.oss.fm.monitor.AlarmValue} object.
	 */
	public  javax.oss.fm.monitor.AlarmValue makeAlarmValue(){
		return cmt.makeAlarmValue();
		
	}

	/**
	 * Makes a new alarm value object pre-populated with internal objects
	 * which have been made from a local invarient specification.
	 * NOTE THIS IS A PATCH to proxy for JVTAlarmMonitorSession
	 *
	 * @return a {@link javax.oss.fm.monitor.AlarmValue} object.
	 */
	public javax.oss.fm.monitor.AlarmValue makeAlarmValueFromSpec(){
		return cmt.makeAlarmValueFromSpec();
	}
	
	// SPRING DAO SETTERS - NOT USED IN THIS VERSION

	/**
	 * {@inheritDoc}
	 *
	 * Used by jmx mbean QoSD to pass in Spring Application context
	 */
	public  void setapplicationcontext(ClassPathXmlApplicationContext m_context){
		cmt.setapplicationcontext(m_context);
	}

}
