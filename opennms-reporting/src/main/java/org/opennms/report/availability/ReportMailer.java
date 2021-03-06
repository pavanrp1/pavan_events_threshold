/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 21, 2007
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
package org.opennms.report.availability;

import java.io.IOException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.JavaMailer;
import org.opennms.netmgt.utils.JavaMailerException;

/**
 * <p>ReportMailer class.</p>
 *
 * @author <a href="mailto:sartin@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 */
public class ReportMailer {
	
	private static final String LOG4J_CATEGORY = "OpenNMS.Report";
	
	private Category log;
	
	private String m_filename;
	
	private String m_address;
	
	
	/**
	 * <p>Constructor for ReportMailer.</p>
	 */
	public ReportMailer() {
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(AvailabilityReport.class);
	}
	
	
	/**
	 * <p>Constructor for ReportMailer.</p>
	 *
	 * @param address a {@link java.lang.String} object.
	 * @param filename a {@link java.lang.String} object.
	 */
	public ReportMailer(String address, String filename) {
		this.m_address = address;
		this.m_filename = filename;
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(AvailabilityReport.class);
	}
	
	/**
	 * <p>send</p>
	 *
	 * @throws java.io.IOException if any.
	 */
	public void send() throws IOException {
        if (m_filename == null || m_address == null) {
            throw new IllegalArgumentException("Cannot take null paramters.");
        }

        try {
            JavaMailer jm = new JavaMailer();
            jm.setTo(m_address);
            jm.setSubject("OpenNMS Availability Report");
            jm.setFileName(m_filename);
            jm.setMessageText("Availability Report Mailed from JavaMailer class.");
            jm.mailSend();
        } catch (JavaMailerException e) {
            log.error("Caught JavaMailer exception sending file: " + m_filename, e);
            throw new IOException("Error sending file: " + m_filename);
        }
    }

	/**
	 * <p>getAddress</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getAddress() {
		return m_address;
	}

	/**
	 * <p>setAddress</p>
	 *
	 * @param address a {@link java.lang.String} object.
	 */
	public void setAddress(String address) {
		this.m_address = address;
	}

	/**
	 * <p>getFilename</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFilename() {
		return m_filename;
	}

	/**
	 * <p>setFilename</p>
	 *
	 * @param filename a {@link java.lang.String} object.
	 */
	public void setFilename(String filename) {
		this.m_filename = filename;
	}

}
