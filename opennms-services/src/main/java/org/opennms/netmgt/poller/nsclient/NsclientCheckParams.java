//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.poller.nsclient;

/**
 * This class contains the parameters used to perform and validate checks
 * against the NSClient daemon.
 *
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski</A>
 * @version $Id: $
 */
public class NsclientCheckParams {
    /**
     * Contains the warning threshold.
     */
    private int m_WarningPercent = 0;

    /**
     * Contains the critical threshold.
     */
    private int m_CriticalPercent = 0;

    /**
     * Contains a string holding parameters related to check items.
     */
    private String m_ParamString = "";

    /**
     * Constructor, sets the critical threshold.
     *
     * @param critPerc
     *            the value to be used for the critical threshold.
     */
    public NsclientCheckParams(int critPerc) {
        m_CriticalPercent = critPerc;
    }

    /**
     * Constructor, sets the critical and warning thresholds.
     *
     * @param critPerc
     *            the value to be used for the critical threshold.
     * @param warnPerc
     *            the value to be used for the warning threshold.
     */
    public NsclientCheckParams(int critPerc, int warnPerc) {
        m_CriticalPercent = critPerc;
        m_WarningPercent = warnPerc;
    }

    /**
     * Constructor, sets the critical and warning thresholds and the parameter
     * strings.
     *
     * @param critPerc
     *            the value to be used for the critical threshold.
     * @param warnPerc
     *            the value to be used for the warning threshold.
     * @param params
     *            the parameter string used for creating check requests.
     */
    public NsclientCheckParams(int critPerc, int warnPerc, String params) {
        m_CriticalPercent = critPerc;
        m_WarningPercent = warnPerc;
        m_ParamString = params;
    }

    /**
     * Constructor, sets the parameter string used when creating check
     * requests.
     *
     * @param params
     *            the parameter string used for creating check requests.
     */
    public NsclientCheckParams(String params) {
        m_ParamString = params;
    }

    /**
     * Returns the warning threshold value.
     *
     * @return the warning threshold value.
     */
    public int getWarningPercent() {
        return m_WarningPercent;
    }

    /**
     * Returns the critical threshold value.
     *
     * @return the critical threshold value.
     */
    public int getCriticalPercent() {
        return m_CriticalPercent;
    }

    /**
     * Returns the string containing the parameters for creating check
     * requests.
     *
     * @return the string containing the parameters for creating check
     *         requests.
     */
    public String getParamString() {
        return m_ParamString;
    }
}
