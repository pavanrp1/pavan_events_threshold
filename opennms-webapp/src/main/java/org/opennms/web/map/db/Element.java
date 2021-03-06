/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 17, 2007
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
package org.opennms.web.map.db;

import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.web.map.MapsException;

/**
 * <p>Element class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 * @since 1.6.12
 */
public class Element implements Cloneable {
    private int mapId;

    private int id;

    protected String type;

    private String label;

    private String iconName;

    private int x;

    private int y;

    /** Constant <code>MAP_TYPE="M"</code> */
    public static final String MAP_TYPE = "M";

    /** Constant <code>NODE_TYPE="N"</code> */
    public static final String NODE_TYPE = "N";
    
    /** Constant <code>defaultNodeIcon="unspecified"</code> */
    public static final String defaultNodeIcon = "unspecified";
    /** Constant <code>defaultMapIcon="map"</code> */
    public static final String defaultMapIcon = "map";

    /**
     * <p>Constructor for Element.</p>
     */
    protected Element() {
        // blank
    }

    /**
     * <p>Constructor for Element.</p>
     *
     * @param e a {@link org.opennms.web.map.db.Element} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public Element(Element e) throws MapsException {
        this(e.mapId, e.id, e.type, e.label, e.iconName, e.x, e.y);
    }

    /**
     * <p>Constructor for Element.</p>
     *
     * @param mapId a int.
     * @param id a int.
     * @param type a {@link java.lang.String} object.
     * @param label a {@link java.lang.String} object.
     * @param iconName a {@link java.lang.String} object.
     * @param x a int.
     * @param y a int.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public Element(int mapId, int id, String type, String label,
            String iconName, int x, int y)throws MapsException {
        this.mapId = mapId;
        this.id = id;
        this.setType(type);
        this.label = label;
        setIcon(iconName);
        this.x = x;
        this.y = y;
    }

    /**
     * <p>getIcon</p>
     *
     * @return Returns the iconName.
     */
    public String getIcon() {
        return iconName;
    }

    /**
     * <p>setIcon</p>
     *
     * @param iconName
     *            The iconName to set.
     */
    public void setIcon(String iconName) {
    	if(iconName==null){
    		iconName=defaultNodeIcon;
    	}
        this.iconName = iconName;
    }

    /**
     * <p>Getter for the field <code>label</code>.</p>
     *
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * <p>Setter for the field <code>label</code>.</p>
     *
     * @param label
     *            The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * <p>Getter for the field <code>x</code>.</p>
     *
     * @return Returns the x.
     */
    public int getX() {
        return x;
    }

    /**
     * <p>Setter for the field <code>x</code>.</p>
     *
     * @param x
     *            The x to set.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * <p>Getter for the field <code>y</code>.</p>
     *
     * @return Returns the y.
     */
    public int getY() {
        return y;
    }

    /**
     * <p>Setter for the field <code>y</code>.</p>
     *
     * @param y
     *            The y to set.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * <p>Getter for the field <code>type</code>.</p>
     *
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * <p>Setter for the field <code>type</code>.</p>
     *
     * @param type
     *            The type to set.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public void setType(String type) throws MapsException {
        if (type.equals(MAP_TYPE) || type.equals(NODE_TYPE))  this.type = type;
        new MapsException("Cannot create an Element with type " + type);
    }

    /**
     * <p>Getter for the field <code>mapId</code>.</p>
     *
     * @return a int.
     */
    public int getMapId() {
        return mapId;
    }

    /**
     * <p>Setter for the field <code>mapId</code>.</p>
     *
     * @param mapId a int.
     */
    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }

    /**
     * <p>Setter for the field <code>id</code>.</p>
     *
     * @param id
     *            The id to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * <p>clone</p>
     *
     * @return a {@link org.opennms.web.map.db.Element} object.
     */
    public Element clone() {
        try {
            return (Element) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UndeclaredThrowableException(e, "CloneNotSupportedException thrown while calling super.clone(), which is odd since we implement the Cloneable interface");
        }
    }
    
    /**
     * <p>isMap</p>
     *
     * @return a boolean.
     */
    public boolean isMap() {
    	if (type.equals(MAP_TYPE)) return true;
    	return false;
    }

    /**
     * <p>isNode</p>
     *
     * @return a boolean.
     */
    public boolean isNode() {
    	if (type.equals(NODE_TYPE)) return true;
    	return false;
    }

}
