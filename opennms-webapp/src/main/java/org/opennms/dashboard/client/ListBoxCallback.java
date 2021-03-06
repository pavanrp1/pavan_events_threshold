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
 * Created: February 26, 2007
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

package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;

/**
 * <p>ListBoxCallback class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class ListBoxCallback implements AsyncCallback {
    private DashletLoader m_dashletLoader;
    private int m_direction = 1;
    private ListBox m_listBox;
    private String m_emptyListItemName = null;
    private String m_emptyListItemValue;
    private String m_nullListItemName;
    private String m_nullListItemValue;
    
    /**
     * <p>Constructor for ListBoxCallback.</p>
     *
     * @param dashlet a {@link org.opennms.dashboard.client.DashletLoader} object.
     * @param listBox a {@link com.google.gwt.user.client.ui.ListBox} object.
     */
    public ListBoxCallback(DashletLoader dashlet, ListBox listBox) {
        m_dashletLoader = dashlet;
        m_listBox = listBox;
    }
    
    /**
     * <p>getListBox</p>
     *
     * @return a {@link com.google.gwt.user.client.ui.ListBox} object.
     */
    public ListBox getListBox() {
        return m_listBox;
    }

    /**
     * <p>setDirection</p>
     *
     * @param direction a int.
     */
    public void setDirection(int direction) {
        m_direction = direction;
    }
    
    /**
     * <p>getDirection</p>
     *
     * @return a int.
     */
    public int getDirection() {
        return m_direction;
    }
    
    /**
     * <p>setNullListItem</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setNullListItem(String name, String value) {
        m_nullListItemName = name;
        m_nullListItemValue = value;
    }
    
    /**
     * <p>setEmptyListItem</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setEmptyListItem(String name, String value) {
        m_emptyListItemName = name;
        m_emptyListItemValue = value;
    }

    /**
     * <p>onDataLoaded</p>
     *
     * @param resources an array of {@link java.lang.String} objects.
     */
    public void onDataLoaded(String[][] resources) {
        m_listBox.clear();
        
        if (resources == null) {
            if (m_nullListItemName != null) {
                m_listBox.addItem(m_nullListItemName, m_nullListItemValue);
            }
        } else  if (resources.length == 0) {
            if (m_emptyListItemName != null) {
                m_listBox.addItem(m_emptyListItemName, m_emptyListItemValue);
            }
        } else {
            for (int i = 0; i < resources.length; i++) {
                m_listBox.addItem(resources[i][1], resources[i][0]);
            }
        }
        
        int itemCount = m_listBox.getItemCount();
        if (m_direction < 0 && itemCount > 0) {
            m_listBox.setSelectedIndex(itemCount - 1);
        } else if (m_direction > 0 && itemCount > 0) {
            m_listBox.setSelectedIndex(0);
        }

        m_dashletLoader.complete();
    }
    
    /** {@inheritDoc} */
    public void onFailure(Throwable caught) {
        m_dashletLoader.loadError(caught);
    }

    /** {@inheritDoc} */
    public void onSuccess(Object result) {
        onDataLoaded((String[][]) result);
    }

}
