/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2004-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 17, 2004
 *
 * Copyright (C) 2004-2008 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.notifd;

import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.Argument;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.utils.JavaMailer;
import org.opennms.netmgt.utils.JavaMailerException;

/**
 * Implements NotificationStragey pattern used to send notifications via the
 * Java Mail API.
 *
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @version $Id: $
 */
public class JavaMailNotificationStrategy implements NotificationStrategy {

    /**
     * <p>Constructor for JavaMailNotificationStrategy.</p>
     */
    public JavaMailNotificationStrategy() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    /** {@inheritDoc} */
    public int send(List<Argument> arguments) {
        log().debug("In the JavaMailNotification class.");

        try {
            JavaMailer jm = buildMessage(arguments);
            jm.mailSend();
        } catch (JavaMailerException e) {
            log().error("send: Error sending notification.", e);
            return 1;
        }
        return 0;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * This method extracts the to, subject, and message text from the
     * parameters passed in the notification.
     * 
     * @param arguments
     * @throws JavaMailerException 
     */
    private JavaMailer buildMessage(List<Argument> arguments) throws JavaMailerException {

        JavaMailer jm = new JavaMailer();

        for (int i = 0; i < arguments.size(); i++) {

            Argument arg = arguments.get(i);
            log().debug("Current arg switch: " + i + " of " + arguments.size() + " is: " + arg.getSwitch());
            log().debug("Current arg  value: " + i + " of " + arguments.size() + " is: " + arg.getValue());

            /*
             * Note: The recipient gets set by whichever of the two switches:
             * (PARAM_EMAIL or PARAM_PAGER_EMAIL) are specified last in the
             * notificationCommands.xml file
             */
            if (NotificationManager.PARAM_EMAIL.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_EMAIL");
                jm.setTo(arg.getValue());
            } else if (NotificationManager.PARAM_PAGER_EMAIL.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_PAGER_EMAIL");
                jm.setTo(arg.getValue());
            } else if (NotificationManager.PARAM_SUBJECT.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_SUBJECT");
                jm.setSubject(arg.getValue());
            } else if (NotificationManager.PARAM_TEXT_MSG.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_TEXT_MSG");
                jm.setMessageText(arg.getValue());
            }
        }

        return jm;
    }

}
