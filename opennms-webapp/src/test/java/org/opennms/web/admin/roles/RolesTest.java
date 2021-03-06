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
package org.opennms.web.admin.roles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.IntervalTestCase;
import org.opennms.netmgt.config.OwnedIntervalSequence;
import org.opennms.netmgt.config.Owner;
import org.opennms.netmgt.config.TimeInterval;
import org.opennms.netmgt.config.TimeIntervalSequence;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.notifd.mock.MockGroupManager;
import org.opennms.netmgt.notifd.mock.MockUserManager;
import org.opennms.test.mock.MockLogAppender;

public class RolesTest extends IntervalTestCase {
    
    public static final String GROUP_MANAGER = "<?xml version=\"1.0\"?>\n" + 
    "<groupinfo>\n" + 
    "    <header>\n" + 
    "        <rev>1.3</rev>\n" + 
    "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
    "        <mstation>dhcp-219.internal.opennms.org</mstation>\n" + 
    "    </header>\n" + 
    "    <groups>\n" + 
    "        <group>\n" + 
    "            <name>InitialGroup</name>\n" + 
    "            <comments>The group that gets notified first</comments>\n" + 
    "            <user>admin</user>" + 
    "            <user>brozow</user>" + 
    "        </group>\n" + 
    "        <group>\n" + 
    "            <name>EscalationGroup</name>\n" + 
    "            <comments>The group things escalate to</comments>\n" +
    "            <user>brozow</user>" + 
    "            <user>david</user>" + 
    "        </group>\n" + 
    "        <group>\n" + 
    "            <name>UpGroup</name>\n" + 
    "            <comments>The group things escalate to</comments>\n" +
    "            <user>upUser</user>" + 
    "        </group>\n" + 
    "    </groups>\n" +
    "  <roles>\n" + 
    "    <role supervisor=\"brozow\" name=\"oncall\" description=\"The On Call Schedule\" membership-group=\"InitialGroup\">\n" + 
    "      <schedule name=\"brozow\" type=\"weekly\">\n" + 
    "         <time day=\"sunday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
    "         <time day=\"monday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
    "         <time day=\"wednesday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
    "         <time day=\"friday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
    "      </schedule>\n" + 
    "      <schedule name=\"admin\" type=\"weekly\">\n" + 
    "         <time day=\"sunday\" begins=\"00:00:00\" ends=\"23:00:00\"/>\n" + 
    "         <time day=\"tuesday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
    "         <time day=\"thursday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
    "         <time day=\"saturday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
    "      </schedule>\n" + 
    "      <schedule name=\"david\" type=\"weekly\">\n" + 
    "         <time day=\"sunday\"    begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
    "         <time day=\"sunday\"    begins=\"17:00:00\" ends=\"23:00:00\"/>\n" + 
    "         <time day=\"monday\"    begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
    "         <time day=\"monday\"    begins=\"17:00:00\" ends=\"23:00:00\"/>\n" + 
    "         <time day=\"tuesday\"   begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
    "         <time day=\"tuesday\"   begins=\"17:00:00\" ends=\"23:00:00\"/>\n" + 
    "         <time day=\"wednesday\" begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
    "         <time day=\"wednesday\" begins=\"17:00:00\" ends=\"23:00:00\"/>\n" + 
    "         <time day=\"thursday\"  begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
    "         <time day=\"thursday\"  begins=\"17:00:00\" ends=\"23:00:00\"/>\n" + 
    "         <time day=\"friday\"    begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
    "         <time day=\"friday\"    begins=\"17:00:00\" ends=\"23:00:00\"/>\n" + 
    "         <time day=\"saturday\"  begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
    "         <time day=\"saturday\"  begins=\"17:00:00\" ends=\"23:00:00\"/>\n" + 
    "      </schedule>\n" + 
    "    </role>\n" +
    "    <role supervisor=\"admin\" name=\"unscheduled\" description=\"The Unscheduled Schedule\" membership-group=\"UpGroup\">\n" + 
    "           <schedule name=\"upUser\" type=\"weekly\">" +
    "               <time day=\"sunday\" begins=\"00:00:00\" ends=\"23:00:00\"/>\n" + 
    "               <time day=\"monday\" begins=\"00:00:00\" ends=\"23:00:00\"/>\n" + 
    "               <time day=\"tuesday\" begins=\"00:00:00\" ends=\"23:00:00\"/>\n" + 
    "               <time day=\"wednesday\" begins=\"00:00:00\" ends=\"23:00:00\"/>\n" + 
    "               <time day=\"thursday\" begins=\"00:00:00\" ends=\"23:00:00\"/>\n" + 
    "               <time day=\"friday\" begins=\"00:00:00\" ends=\"23:00:00\"/>\n" + 
    "               <time day=\"saturday\" begins=\"00:00:00\" ends=\"23:00:00\"/>\n" + 
    "           </schedule>" +
    "    </role>\n" +
    "  </roles>\n" + 
    "</groupinfo>\n" + 
    "";
public static final String USER_MANAGER = "<?xml version=\"1.0\"?>\n" + 
    "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\">\n" + 
    "   <header>\n" + 
    "       <rev>.9</rev>\n" + 
    "           <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
    "       <mstation>master.nmanage.com</mstation>\n" + 
    "   </header>\n" + 
    "   <users>\n" + 
    "       <user>\n" + 
    "           <user-id>brozow</user-id>\n" + 
    "           <full-name>Mathew Brozowski</full-name>\n" + 
    "           <user-comments>Test User</user-comments>\n" +
    "           <password>21232F297A57A5A743894A0E4A801FC3</password>\n" +
    "           <contact type=\"email\" info=\"brozow@opennms.org\"/>\n" + 
    "       </user>\n" + 
    "       <user>\n" + 
    "           <user-id>admin</user-id>\n" + 
    "           <full-name>Administrator</full-name>\n" + 
    "           <user-comments>Default administrator, do not delete</user-comments>\n" +
    "           <password>21232F297A57A5A743894A0E4A801FC3</password>\n" +
    "           <contact type=\"email\" info=\"admin@opennms.org\"/>\n" + 
    "       </user>\n" + 
    "       <user>\n" + 
    "           <user-id>upUser</user-id>\n" + 
    "           <full-name>User that receives up notifications</full-name>\n" + 
    "           <user-comments>Default administrator, do not delete</user-comments>\n" +
    "           <password>21232F297A57A5A743894A0E4A801FC3</password>\n" +
    "           <contact type=\"email\" info=\"up@opennms.org\"/>\n" + 
    "       </user>\n" + 
    "       <user>\n" + 
    "           <user-id>david</user-id>\n" + 
    "           <full-name>David Hustace</full-name>\n" + 
    "           <user-comments>A cool dude!</user-comments>\n" + 
    "           <password>18126E7BD3F84B3F3E4DF094DEF5B7DE</password>\n" + 
    "           <contact type=\"email\" info=\"david@opennms.org\"/>\n" + 
    "           <contact type=\"numericPage\" info=\"6789\" serviceProvider=\"ATT\"/>\n" + 
    "           <contact type=\"textPage\" info=\"9876\" serviceProvider=\"Sprint\"/>\n" + 
    "           <duty-schedule>MoTuWeThFrSaSu800-2300</duty-schedule>\n" + 
    "       </user>\n" + 
    "   </users>\n" + 
    "</userinfo>\n" + 
    "";

    private GroupManager m_groupManager;
    private UserManager m_userManager;
    private WebRoleManager m_roleMgr;
    private WebGroupManager m_groupMgr;
    private WebUserManager m_userMgr;


    protected void setUp() throws Exception {
        super.setUp();
        
        MockLogAppender.setupLogging();
        m_groupManager = new MockGroupManager(GROUP_MANAGER);
        m_userManager = new MockUserManager(m_groupManager, USER_MANAGER);
        
        GroupFactory.setInstance(m_groupManager);
        UserFactory.setInstance(m_userManager);
        
        m_roleMgr = AppContext.getWebRoleManager();
        m_groupMgr = AppContext.getWebGroupManager();
        m_userMgr = AppContext.getWebUserManager();

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testRoles() throws Exception {
        assertNotNull(m_roleMgr);
        assertNotNull(m_roleMgr.getRoles());
        
        String[] roleNames = m_groupManager.getRoleNames();
        assertEquals(roleNames.length, m_roleMgr.getRoles().size());
        for (int i = 0; i < roleNames.length; i++) {
            String roleName = roleNames[i];
            Role role = m_groupManager.getRole(roleName);
            WebRole webRole = m_roleMgr.getRole(roleName);
            assertNotNull(webRole);
            assertRole(role, webRole);
        }
        
        for (String groupName : m_groupManager.getGroupNames()) {
            Group group = m_groupManager.getGroup(groupName);
            WebGroup webGroup = m_groupMgr.getGroup(groupName);
            assertGroup(group, webGroup);
        }
        
        for (String userName : m_userManager.getUserNames()) {
            User user = m_userManager.getUser(userName);
            WebUser webUser = m_userMgr.getUser(userName);
            assertUser(user, webUser);
        }
        
        WebRole oncall = m_roleMgr.getRole("oncall");
        assertEquals("oncall", oncall.getName());
        assertEquals(m_groupMgr.getGroup("InitialGroup"), oncall.getMembershipGroup());
        
    }
    
    public void testWeekCount() throws Exception {
        Date aug3 = getDate("2005-08-03");
        MonthlyCalendar calendar = new MonthlyCalendar(aug3, null, null);
        assertEquals(5, calendar.getWeeks().length);
        
        Date july17 = getDate("2005-07-17");
        calendar = new MonthlyCalendar(july17, null, null);
        assertEquals(6, calendar.getWeeks().length);
        
        Date may27 = getDate("2005-05-27");
        calendar = new MonthlyCalendar(may27, null, null);
        assertEquals(5, calendar.getWeeks().length);
        
        Date feb14_04 = getDate("2004-02-14");
        calendar = new MonthlyCalendar(feb14_04, null, null);
        assertEquals(5, calendar.getWeeks().length);
        
        Date feb7_09 = getDate("2009-02-09");
        calendar = new MonthlyCalendar(feb7_09, null, null);
        assertEquals(4, calendar.getWeeks().length);
        
    }
    
    public void testTimeIntervals() throws Exception {
        OwnedIntervalSequence intervals = m_groupManager.getRoleScheduleEntries("oncall", getDate("2005-08-18"), getDate("2005-08-19"));
        
        assertNotNull(intervals);
        
        Owner brozow = new Owner("oncall", "brozow", 1, 1);
        Owner admin = new Owner("oncall", "admin", 1, 1);
        Owner david = new Owner("oncall", "david", 1, 1);
        TimeIntervalSequence before = new TimeIntervalSequence();
        before.addInterval(owned(david, aug(18, 0, 9)));
        before.addInterval(owned(admin, aug(18, 9, 17)));
        before.addInterval(owned(david, aug(18, 17, 23)));
        before.addInterval(owned(brozow, aug(18, 23, 24)));

        TimeInterval[] expected = {
                owned(david, aug(18, 0, 9)),
                owned(admin, aug(18, 9, 17)),
                owned(david, aug(18, 17, 23)),
                owned(brozow, aug(18, 23, 24)), // brozow is the supervisor and this period is unscheduled
        };
        
        assertTimeIntervalSequence(expected, intervals);

        
    }
    
    private void assertUser(User user, WebUser webUser) {
        assertEquals(user.getUserId(), webUser.getName());
    }

    @SuppressWarnings("unchecked")
    private void assertGroup(Group group, WebGroup webGroup) throws Exception {
        assertEquals(group.getName(), webGroup.getName());
        Collection<String> userNames = group.getUserCollection();
        assertEquals(userNames.size(), webGroup.getUsers().size());
        for (Iterator<WebUser> it = webGroup.getUsers().iterator(); it.hasNext();) {
            WebUser user = it.next();
            assertTrue(userNames.contains(user.getName()));
            assertUser(m_userManager.getUser(user.getName()), user);
            
        }
    }

    @SuppressWarnings("unchecked")
    private void assertRole(Role role, WebRole webRole) throws Exception {
        assertEquals(role.getName(), webRole.getName());
        assertEquals(role.getDescription(), webRole.getDescription());
        assertNotNull(webRole.getMembershipGroup());
        assertEquals(role.getMembershipGroup(), webRole.getMembershipGroup().getName());
        assertNotNull(webRole.getDefaultUser());
        assertEquals(role.getSupervisor(), webRole.getDefaultUser().getName());
        Collection<WebUser> scheduledUsers = webRole.getCurrentUsers();
        for (Iterator<WebUser> it = scheduledUsers.iterator(); it.hasNext();) {
            WebUser currentUser = it.next();
            assertTrue(m_groupManager.isUserScheduledForRole(currentUser.getName(), webRole.getName(), new Date()));
        }
    }

    private Date getDate(String date) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date);
    }
    
    

}
