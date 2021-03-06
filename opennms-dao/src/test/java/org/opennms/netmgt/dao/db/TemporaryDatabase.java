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
 * 2008 May 10: Make unit tests using this class work again. - dj@opennms.org
 * 
 * Created: June 27, 2007
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
package org.opennms.netmgt.dao.db;

import org.opennms.test.ConfigurationTestUtils;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class TemporaryDatabase implements DataSource {
    private static final String TEST_DB_NAME_PREFIX = "opennms_test_";

    private static final String DRIVER_PROPERTY = "mock.db.driver";
    private static final String URL_PROPERTY = "mock.db.url";
    private static final String ADMIN_USER_PROPERTY = "mock.db.adminUser";
    private static final String ADMIN_PASSWORD_PROPERTY = "mock.db.adminPassword";

    private static final String DEFAULT_DRIVER = "org.postgresql.Driver";
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/";
    private static final String DEFAULT_ADMIN_USER = "postgres";
    private static final String DEFAULT_ADMIN_PASSWORD = "";

    private static final int MAX_DATABASE_DROP_ATTEMPTS = 10;

    private String m_testDatabase;

    private String m_driver;
    private String m_url;
    private String m_adminUser;
    private String m_adminPassword;

    private DataSource m_dataSource;
    private DataSource m_adminDataSource;

    private InstallerDb m_installerDb;

    private ByteArrayOutputStream m_outputStream;

    private boolean m_setupIpLike = true;

    private boolean m_populateSchema = false;

    private boolean m_destroyed = false;

    private SimpleJdbcTemplate m_jdbcTemplate;

    public TemporaryDatabase() throws Exception {
        this(TEST_DB_NAME_PREFIX + System.currentTimeMillis());
    }

    public TemporaryDatabase(String testDatabase) throws Exception {
        this(testDatabase, System.getProperty(DRIVER_PROPERTY, DEFAULT_DRIVER),
                System.getProperty(URL_PROPERTY, DEFAULT_URL),
                System.getProperty(ADMIN_USER_PROPERTY, DEFAULT_ADMIN_USER),
                System.getProperty(ADMIN_PASSWORD_PROPERTY, DEFAULT_ADMIN_PASSWORD));
    }

    public TemporaryDatabase(String testDatabase, String driver, String url,
                             String adminUser, String adminPassword) throws Exception {
        m_testDatabase = testDatabase;
        m_driver = driver;
        m_url = url;
        m_adminUser = adminUser;
        m_adminPassword = adminPassword;

    }

    public void setPopulateSchema(boolean populateSchema) {
        m_populateSchema = populateSchema;
    }

    public void create() throws Exception {
        setupDatabase();

        if (m_populateSchema) {
            initializeDatabase();
        }
    }

    private void initializeDatabase() throws Exception {
        m_installerDb = new InstallerDb();
        try {

            // Create a ByteArrayOutputSteam to effectively throw away output.
            resetOutputStream();
            m_installerDb.setDatabaseName(getTestDatabase());
            m_installerDb.setDataSource(getDataSource());
            
            m_installerDb.setAdminDataSource(getAdminDataSource());
            m_installerDb.setPostgresOpennmsUser(m_adminUser);

            m_installerDb.setCreateSqlLocation(getCreateSqlLocation());

            m_installerDb.setStoredProcedureDirectory(getStoredProcDirectory());

            // installerDb.setDebug(true);

            m_installerDb.readTables();

            m_installerDb.createSequences();
            m_installerDb.updatePlPgsql();
            m_installerDb.addStoredProcedures();

            /*
             * Here's an example of an iplike function that always returns true.
             * CREATE OR REPLACE FUNCTION iplike(text, text) RETURNS bool AS ' BEGIN
             * RETURN true; END; ' LANGUAGE 'plpgsql';
             *
             * Found this in BaseIntegrationTestCase.
             */

            if (isSetupIpLike()) {
                if (!m_installerDb.isIpLikeUsable()) {
                    m_installerDb.setupPlPgsqlIplike();
                }
            }

            m_installerDb.createTables();
            m_installerDb.insertData();
        } finally {
            m_installerDb.closeConnection();
        }

    }

    protected String getStoredProcDirectory() {
        return ConfigurationTestUtils.getFileForConfigFile("create.sql").getParentFile().getAbsolutePath();
    }

    protected String getCreateSqlLocation() {
        return ConfigurationTestUtils.getFileForConfigFile("create.sql").getAbsolutePath();
    }

    public boolean isSetupIpLike() {
        return m_setupIpLike;
    }

    public void setSetupIpLike(boolean setupIpLike) {
        m_setupIpLike = setupIpLike;
    }

    protected File findIpLikeLibrary() {
        File topDir = ConfigurationTestUtils.getTopProjectDirectory();

        File ipLikeDir = new File(topDir, "opennms-iplike");
        assertTrue("iplike directory exists at ../opennms-iplike: " + ipLikeDir.getAbsolutePath(), ipLikeDir.exists());

        File[] ipLikePlatformDirs = ipLikeDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.getName().matches("opennms-iplike-.*") && file.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        assertTrue("expecting at least one opennms iplike platform directory in " + ipLikeDir.getAbsolutePath() + "; got: " + StringUtils.arrayToDelimitedString(ipLikePlatformDirs, ", "), ipLikePlatformDirs.length > 0);

        File ipLikeFile = null;
        for (File ipLikePlatformDir : ipLikePlatformDirs) {
            assertTrue("iplike platform directory does not exist but was listed in directory listing: " + ipLikePlatformDir.getAbsolutePath(), ipLikePlatformDir.exists());

            File ipLikeTargetDir = new File(ipLikePlatformDir, "target");
            if (!ipLikeTargetDir.exists() || !ipLikeTargetDir.isDirectory()) {
                // Skip this one
                continue;
            }

            File[] ipLikeFiles = ipLikeTargetDir.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file.isFile() && file.getName().matches("opennms-iplike-.*\\.(so|dylib)")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            assertFalse("expecting zero or one iplike file in " + ipLikeTargetDir.getAbsolutePath() + "; got: " + StringUtils.arrayToDelimitedString(ipLikeFiles, ", "), ipLikeFiles.length > 1);

            if (ipLikeFiles.length == 1) {
                ipLikeFile = ipLikeFiles[0];
            }

        }

        assertNotNull("Could not find iplike shared object in a target directory in any of these directories: " + StringUtils.arrayToDelimitedString(ipLikePlatformDirs, ", "), ipLikeFile);

        return ipLikeFile;
    }

    private void assertNotNull(String string, Object o) {
        if (o == null) {
            throw new IllegalStateException(string);
        }
    }

    private void assertFalse(String string, boolean b) {
        if (b) {
            throw new IllegalStateException(string);
        }
    }

    private void assertTrue(String string, boolean b) {
        if (!b) {
            throw new IllegalStateException(string);
        }
    }

    private void resetOutputStream() {
        m_outputStream = new ByteArrayOutputStream();
        m_installerDb.setOutputStream(new PrintStream(m_outputStream));
    }

    public void setupDatabase() throws Exception {

        setDataSource(new SimpleDataSource(m_driver, m_url + getTestDatabase(),
                m_adminUser, m_adminPassword));
        setAdminDataSource(new SimpleDataSource(m_driver, m_url + "template1",
                m_adminUser, m_adminPassword));

        createTestDatabase();

        // Test connecting to test database.
        Connection connection = getConnection();
        connection.close();

        setJdbcTemplate(new SimpleJdbcTemplate(this));
    }

    private void createTestDatabase() throws Exception {
        Connection adminConnection = getAdminDataSource().getConnection();
        Statement st = null;
        try {
            st = adminConnection.createStatement();
            st.execute("CREATE DATABASE " + getTestDatabase()
                    + " WITH ENCODING='UNICODE'");
        } finally {
            if (st != null) {
                st.close();
            }
            adminConnection.close();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    destroyTestDatabase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

    }

    public void drop() throws Exception {
        destroyTestDatabase();
    }

    private void destroyTestDatabase() throws Exception {

        if (m_destroyed) {
            System.err.println("Database '" + getTestDatabase() + "' already destroyed");
            // database already destroyed
            return;
        }

        /*
        * Sleep before destroying the test database because PostgreSQL doesn't
        * seem to notice immediately clients have disconnected. Yeah, it's a
        * hack.
        */
        Thread.sleep(100);

        Connection adminConnection = getAdminDataSource().getConnection();

        try {
            for (int dropAttempt = 0; dropAttempt < MAX_DATABASE_DROP_ATTEMPTS; dropAttempt++) {
                Statement st = null;

                try {
                    st = adminConnection.createStatement();
                    st.execute("DROP DATABASE " + getTestDatabase());
                    break;
                } catch (SQLException e) {
                    if ((dropAttempt + 1) >= MAX_DATABASE_DROP_ATTEMPTS) {
                        final String message = "Failed to drop test database on last attempt " + (dropAttempt + 1) + ": " + e;
                        System.err.println(new Date().toString() + ": " + message);
                        dumpThreads();
                        
                        SQLException newException = new SQLException(message);
                        newException.initCause(e);
                        throw newException;
                    } else {
                        System.err.println(new Date().toString() + ": Failed to drop test database on attempt " + (dropAttempt + 1) + ": " + e);
                        Thread.sleep(1000);
                    }
                } finally {
                    if (st != null) {
                        st.close();
                        st = null;
                    }
                }
            }
        } finally {
            /*
             * Since we are already going to be throwing an exception at this
             * point, print any further errors to stdout so we don't mask the
             * first failure.
             */
            try {
                adminConnection.close();
            } catch (SQLException e) {
                System.err.println("Error closing administrative database "
                        + "connection after attempting to drop "
                        + "test database");
                e.printStackTrace();
            }

            /*
             * Sleep after disconnecting from template1, otherwise creating a
             * new test database in future tests may fail. Man, I hate this.
             */
            Thread.sleep(100);
        }

        m_destroyed = true;
    }
    
    public static void dumpThreads() {
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        int daemons = 0;
        for (Thread t : threads.keySet()) {
            if (t.isDaemon()) {
                daemons++;
            }
        }
        System.err.println("Thread dump of " + threads.size() + " threads (" + daemons + " daemons):");
        Map<Thread, StackTraceElement[]> sortedThreads = new TreeMap<Thread, StackTraceElement[]>(new Comparator<Thread>() {
            public int compare(Thread t1, Thread t2) {
                return new Long(t1.getId()).compareTo(new Long(t2.getId()));
            }
        });
        sortedThreads.putAll(threads);

        for (Entry<Thread, StackTraceElement[]> entry : sortedThreads.entrySet()) {
            Thread thread = entry.getKey();
            System.err.println("Thread " + thread.getId() + (thread.isDaemon() ? " (daemon)" : "") + ": " + thread + " (state: " + thread.getState() + ")");
            for (StackTraceElement e : entry.getValue()) {
                System.err.println("\t" + e);
            }
        }
        System.err.println("Thread dump completed.");
    }


    public Connection getConnection() throws SQLException {
        return m_dataSource.getConnection();
    }

    public void update(String stmt, Object... values) {
//        StringBuffer buf = new StringBuffer("[");
//        for(int i = 0; i < values.length; i++) {
//            if (i != 0)
//                buf.append(", ");
//            buf.append(values[i]);
//        }
//        buf.append("]");
//        MockUtil.println("Executing "+stmt+" with values "+buf);

        getJdbcTemplate().update(stmt, values);
    }

    public int countRows(String sql, Object... values) {
        RowCountCallbackHandler counter = new RowCountCallbackHandler();
        getJdbcTemplate().getJdbcOperations().query(sql, values, counter);
        return counter.getRowCount();
    }

    public String getNextSequenceValStatement(String seqName) {
        return "select nextval('" + seqName + "')";
    }

    protected Integer getNextId(String nxtIdStmt) {
        return getJdbcTemplate().queryForInt(nxtIdStmt);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return m_dataSource.getConnection(username, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return m_dataSource.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        m_dataSource.setLogWriter(out);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        m_dataSource.setLoginTimeout(seconds);
    }

    public int getLoginTimeout() throws SQLException {
        return m_dataSource.getLoginTimeout();
    }

    public SimpleJdbcTemplate getJdbcTemplate() {
        return m_jdbcTemplate;
    }

    public void setJdbcTemplate(SimpleJdbcTemplate jdbcTemplate) {
        m_jdbcTemplate = jdbcTemplate;
    }

    public DataSource getAdminDataSource() {
        return m_adminDataSource;
    }

    public void setAdminDataSource(DataSource adminDataSource) {
        m_adminDataSource = adminDataSource;
    }

    public DataSource getDataSource() {
        return m_dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    public String getTestDatabase() {
        return m_testDatabase;
    }

    /**
     * Returns an object that implements the given interface to allow access to
     * non-standard methods, or standard methods not exposed by the proxy.
     * <p/>
     * If the receiver implements the interface then the result is the receiver
     * or a proxy for the receiver. If the receiver is a wrapper
     * and the wrapped object implements the interface then the result is the
     * wrapped object or a proxy for the wrapped object. Otherwise return the
     * the result of calling <code>unwrap</code> recursively on the wrapped object
     * or a proxy for that result. If the receiver is not a
     * wrapper and does not implement the interface, then an <code>SQLException</code> is thrown.
     *
     * @param iface A Class defining an interface that the result must implement.
     * @return an object that implements the interface. May be a proxy for the actual implementing object.
     * @throws java.sql.SQLException If no object found that implements the interface
     * @since 1.6
     */
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;  //TODO
    }

    /**
     * Returns true if this either implements the interface argument or is directly or indirectly a wrapper
     * for an object that does. Returns false otherwise. If this implements the interface then return true,
     * else if this is a wrapper then return the result of recursively calling <code>isWrapperFor</code> on the wrapped
     * object. If this does not implement the interface and is not a wrapper, return false.
     * This method should be implemented as a low-cost operation compared to <code>unwrap</code> so that
     * callers can use this method to avoid expensive <code>unwrap</code> calls that may fail. If this method
     * returns true then calling <code>unwrap</code> with the same argument should succeed.
     *
     * @param iface a Class defining an interface.
     * @return true if this implements the interface or directly or indirectly wraps an object that does.
     * @throws java.sql.SQLException if an error occurs while determining whether this is a wrapper
     *                               for an object with the given interface.
     * @since 1.6
     */
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;  //TODO
    }

    public String getDriver() {
        return m_driver;
    }

    public String getUrl() {
        return m_url;
    }
}
