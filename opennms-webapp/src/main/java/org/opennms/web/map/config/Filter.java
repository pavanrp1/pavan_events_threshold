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
 * Created: February 10, 2007
 *
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
package org.opennms.web.map.config;

/**
 * <p>Filter class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class Filter{
	String table;
	String condition;
	/**
	 * <p>Constructor for Filter.</p>
	 *
	 * @param table a {@link java.lang.String} object.
	 * @param condition a {@link java.lang.String} object.
	 */
	public Filter(String table, String condition) {
		super();
		this.table = table;
		this.condition = condition;
	}
	/**
	 * <p>Getter for the field <code>condition</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondition() {
		return condition;
	}
	/**
	 * <p>Setter for the field <code>condition</code>.</p>
	 *
	 * @param condition a {@link java.lang.String} object.
	 */
	protected void setCondition(String condition) {
		this.condition = condition;
	}
	/**
	 * <p>Getter for the field <code>table</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	protected String getTable() {
		return table;
	}
	/**
	 * <p>Setter for the field <code>table</code>.</p>
	 *
	 * @param table a {@link java.lang.String} object.
	 */
	protected void setTable(String table) {
		this.table = table;
	}
	
}
