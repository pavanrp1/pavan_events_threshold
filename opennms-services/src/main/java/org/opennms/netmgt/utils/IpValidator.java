//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

package org.opennms.netmgt.utils;

import java.lang.NumberFormatException;
import java.util.StringTokenizer;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 * A class containing a method to determine if a string represents
 * a vaild IP address
 *
 * @author ranger
 * @version $Id: $
 */
public class IpValidator extends Object {
    /**
     * <p>isIpValid</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isIpValid(String ipAddr) {
        Category log = ThreadCategory.getInstance(IpValidator.class);
        StringTokenizer token = new StringTokenizer(ipAddr, ".");
        if(token.countTokens() != 4) {
            if (log.isDebugEnabled())
                log.debug("Invalid format for IpAddress " + ipAddr);
            return false;
        }
        int temp;
        int i = 0;
        while (i < 4) {
            try{
                temp = Integer.parseInt(token.nextToken(), 10);
                if (temp < 0 || temp > 255) {
                    if (log.isDebugEnabled())
                        log.debug("Invalid value " + temp + " in IpAddress");
                    return false;
                }
                i++;
            } catch (NumberFormatException ex) {
                if (log.isDebugEnabled())
                    log.debug("Invalid format for IpAddress, " + ex);
                return false;
            }
        }
        return true;
    }     
}
