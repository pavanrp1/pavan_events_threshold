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
// Modifications:
//
// 2008 Jan 06: Indent. - dj@opennms.org
// 2007 Apr 13: Add a few more test cases and deduplicate. - dj@opennms.org
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
/*
 * Created on Jan 24, 2005
 */
package org.opennms.netmgt.utils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Test the JavaMailer class.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class JavaMailerTest extends TestCase {
    private static final String TEST_ADDRESS = "test@opennms.org";

    protected void setUp() throws IOException {
        MockLogAppender.setupLogging();

        Resource resource = new ClassPathResource("/etc/javamail-configuration.properties");
        File homeDir = resource.getFile().getParentFile().getParentFile();
        System.setProperty("opennms.home", homeDir.getAbsolutePath());
    }

    @Override
    protected void runTest() throws Throwable {
        if (!Boolean.getBoolean("runMailTests")) {
            return;
        }
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
    }

    public void testNothing() throws Exception {

    }

    public final void testJavaMailerWithDefaults() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer using details");

        jm.mailSend();
    }

    public final void testJavaMailerWithNullTo() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setTo(null);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have a null to address."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public final void testJavaMailerWithEmptyTo() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setTo("");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have an empty to address."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public final void testJavaMailerWithNullFrom() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setFrom(null);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have a null from address."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public final void testJavaMailerWithEmptyFrom() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setFrom("");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have an empty from address."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public final void testJavaMailerWithNullSubject() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setSubject(null);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have a null subject."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public final void testJavaMailerWithEmptySbuject() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setSubject("");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have an empty subject."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public final void testJavaMailerWithNullMessageText() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setMessageText(null);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have a null messageText."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public final void testJavaMailerWithEmptyMessageText() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setMessageText("");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have an empty messageText."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    // FIXME: took this out a david's suggestion
    public final void FIXMEtestJavaMailerUsingMTAExplicitly() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer using MTA explicitly");

        if (jm.isSmtpSsl()) {
            return;
        }
        jm.setUseJMTA(true);

        jm.mailSend();
    }

    // FIXME: took this out a david's suggestion
    public final void FIXMEtestJavaMailerUsingMTAByTransport() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer using MTA by transport");

        if (jm.isSmtpSsl()) {
            return;
        }

        jm.setUseJMTA(false);
        jm.setTransport("mta");

        jm.mailSend();
    }

    public final void testJavaMailerWithFileAttachment() throws Exception {
        JavaMailer jm = createMailer("Test message with file attachment from testJavaMailer");

        jm.setFileName("/etc/shells");

        jm.mailSend();
    }

    private JavaMailer createMailer(String subject) throws JavaMailerException {
        JavaMailer jm = new JavaMailer();

        jm.setFrom(TEST_ADDRESS);
        jm.setMessageText(subject + ": " + getLocalHost());
        jm.setSubject("Testing JavaMailer");
        jm.setTo(TEST_ADDRESS);

        return jm;
    }

    private InetAddress getLocalHost()  {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            fail("Could not lookup local host address: " + e);
            return null; // never reached
        }
    }
}
