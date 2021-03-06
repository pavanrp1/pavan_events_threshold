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
// 2008 Jul 29: Check for database maximum version, too. - dj@opennms.org
// 2008 May 31: Catch and rethrow SQLExceptions with more data when trying to
//              connect to the database. - dj@opennms.org
// 2008 Mar 25: Remove admin database username and password--they are not
//              used anymore.  Use Spring's Assert class where possible and
//              clean up granting code.  Also assert that the OpenNMS databae
//              user is set before we use it. - dj@opennms.org
// 2008 Mar 05: Avoid catching a database exception in checkIndexUniqueness, do pre-query checks instead to make sure all columns exist. - dj@opennms.org
// 2007 Jul 03: Remove non-functional (i.e.: empty) setupPgPlSqlIplike
//              method and make setupPlPgsqlIplike public. - dj@opennms.org
// 2007 Jun 10: Rearrange the iplike code a bit and add better error
//              reporting. - dj@opennms.org
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
package org.opennms.netmgt.dao.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <p>InstallerDb class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class InstallerDb {

    private static final String IPLIKE_SQL_RESOURCE = "iplike.sql";

    /** Constant <code>POSTGRES_MIN_VERSION=7.3f</code> */
    public static final float POSTGRES_MIN_VERSION = 7.3f;

    /** Constant <code>POSTGRES_MAX_VERSION_PLUS_ONE=8.5f</code> */
    public static final float POSTGRES_MAX_VERSION_PLUS_ONE = 8.5f;

    /**
     * This enumeration contains all of the possible values for the
     * lanname field in the pg_language table of PostgreSQL.
     */
    public static enum PostgresPgLanguageLanname {
        ANY,
        internal,
        c,
        sql,
        plpgsql
    }

    private static final int s_fetch_size = 1024;

    private static Comparator<Constraint> constraintComparator = new Comparator<Constraint>() {

		public int compare(Constraint o1, Constraint o2) {
			return o1.getName().compareTo(o2.getName());
		}
    	
    }; 
    
    private IndexDao m_indexDao = new IndexDao();

    private TriggerDao m_triggerDao = new TriggerDao();

    private DataSource m_dataSource = null;
    
    private DataSource m_adminDataSource = null;
    
    private PrintStream m_out = System.out;

    private boolean m_debug = false;

    private String m_createSqlLocation = null;
    
    private String m_storedProcedureDirectory = null;
    
    private String m_databaseName = null;
    
    private String m_pass = null;
    
    private String m_pg_iplike = null;

    private String m_pg_plpgsql = null;
    
    private Map<String, ColumnChangeReplacement> m_columnReplacements =
        new HashMap<String, ColumnChangeReplacement>();

    private String m_sql;

    private LinkedList<String> m_tables = null;

    private LinkedList<String> m_sequences = null;

    // private LinkedList m_cfunctions = new LinkedList(); // Unused, not in
    // create.sql
    // private LinkedList m_functions = new LinkedList(); // Unused, not in create.sql
    // private LinkedList m_languages = new LinkedList(); // Unused, not in create.sql

    private HashMap<String, List<Insert>> m_inserts = new HashMap<String, List<Insert>>();

    private HashSet<String> m_drops = new HashSet<String>();

    private HashSet<String> m_changed = new HashSet<String>();
    
    private Map<String, Integer> m_dbtypes = null;
    
    private HashMap<String, String[]> m_seqmapping = null;
    private Connection m_connection;
    private Connection m_adminConnection;
    private String m_user;
    
    private float m_pg_version;

    private boolean m_force = false;
    
    private boolean m_ignore_notnull = false;
    
    private boolean m_no_revert = false;

    /**
     * <p>Constructor for InstallerDb.</p>
     */
    public InstallerDb() {
        
    }
    
    /**
     * <p>readTables</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void readTables() throws Exception {
        readTables(new FileReader(m_createSqlLocation));
    }

    /**
     * <p>readTables</p>
     *
     * @param reader a {@link java.io.Reader} object.
     * @throws java.lang.Exception if any.
     */
    public void readTables(Reader reader) throws Exception {
        BufferedReader r = new BufferedReader(reader);
        String line;

        m_tables = new LinkedList<String>();
        m_seqmapping = new HashMap<String, String[]>();
        m_sequences = new LinkedList<String>();
        m_indexDao.reset();

        LinkedList<String> sql_l = new LinkedList<String>();

        Pattern seqmappingPattern = Pattern.compile("\\s*--#\\s+install:\\s*"
                + "(\\S+)\\s+(\\S+)\\s+(\\S+)\\s*.*");
        Pattern createPattern = Pattern.compile("(?i)\\s*create\\b.*");
        Pattern criteriaPattern = Pattern.compile("\\s*--#\\s+criteria:\\s*(.*)");
        Pattern insertPattern = Pattern.compile("(?i)INSERT INTO "
                + "[\"']?([\\w_]+)[\"']?.*");
        Pattern dropPattern = Pattern.compile("(?i)DROP TABLE [\"']?"
                + "([\\w_]+)[\"']?.*");
        
        
        String criteria = null;
        while ((line = r.readLine()) != null) {
            Matcher m;

            if (line.matches("\\s*") || line.matches("\\s*\\\\.*")) {
                continue;
            }

            m = seqmappingPattern.matcher(line);
            if (m.matches()) {
                String[] a = { m.group(2), m.group(3) };
                m_seqmapping.put(m.group(1), a);
                continue;
            }
            
            m = criteriaPattern.matcher(line);
            if (m.matches()) {
                criteria = m.group(1);
                continue;
            }

            if (line.matches("--.*")) {
                continue;
            }

            if (createPattern.matcher(line).matches()) {
                m = Pattern.compile(
                                    "(?i)\\s*create\\s+((?:unique )?\\w+)"
                                            + "\\s+[\"']?(\\w+)[\"']?.*").matcher(
                                                                                  line);
                if (m.matches()) {
                    String type = m.group(1);
                    String name = m.group(2).replaceAll("^[\"']", "").replaceAll(
                                                                                 "[\"']$",
                                                                                 "");

                    if (type.toLowerCase().indexOf("table") != -1) {
                        m_tables.add(name);
                    } else if (type.toLowerCase().indexOf("sequence") != -1) {
                        m_sequences.add(name);
                    } else if (type.toLowerCase().indexOf("function") != -1) {
                        if (type.toLowerCase().indexOf("language 'c'") != -1) {
                            //m_cfunctions.add(name);
                        } else {
                            //m_functions.add(name);
                        }
                    } else if (type.toLowerCase().indexOf("trusted") != -1) {
                        m = Pattern.compile("(?i)\\s*create\\s+trusted "
                                            + "procedural language\\s+[\"']?"
                                            + "(\\w+)[\"']?.*").matcher(line);
                        if (!m.matches()) {
                            throw new Exception("Could not match name and "
                                                + "type of the trusted "
                                                + "procedural language in "
                                                + "this line: " + line);
                        }
                        //m_languages.add(m.group(1));
                    } else if (type.toLowerCase().matches(".*\\bindex\\b.*")) {
                        Index i = Index.findIndexInString(line);
                        if (i == null) {
                            throw new Exception("Could not match name and "
                                    + "type of the index " + "in this line: "
                                    + line);
                        }
                        m_indexDao.add(i);
                    } else {
                        throw new Exception("Unknown CREATE encountered: "
                                + "CREATE " + type + " " + name);
                    }
                } else {
                    throw new Exception("Unknown CREATE encountered: " + line);
                }

                sql_l.add(line);
                continue;
            }

            m = insertPattern.matcher(line);
            if (m.matches()) {
                String table = m.group(1);
                Insert insert = new Insert(table, line, criteria);
                criteria = null;
                if (!m_inserts.containsKey(table)) {
                    m_inserts.put(table, new LinkedList<Insert>());
                }
                m_inserts.get(table).add(insert);

                continue;
            }

            if (line.toLowerCase().startsWith("select setval ")) {
                String table = "select_setval";
                Insert insert = new Insert("select_setval", line, null);
                if (!m_inserts.containsKey(table)) {
                    m_inserts.put(table, new LinkedList<Insert>());
                }
                m_inserts.get(table).add(insert);

                sql_l.add(line);
                continue;
            }

            m = dropPattern.matcher(line);
            if (m.matches()) {
                m_drops.add(m.group(1));

                sql_l.add(line);
                continue;
            }

            // XXX should do something here so we can catch what we can't
            // parse
            // m_out.println("unmatched line: " + line);

            sql_l.add(line);
        }
        r.close();

        m_sql = cleanText(sql_l);
    }
    

    /**
     * <p>cleanText</p>
     *
     * @param list a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    public static String cleanText(List<String> list) {
        StringBuffer s = new StringBuffer();

        for (String l : list) {
            s.append(l.replaceAll("\\s+", " "));
            if (l.indexOf(';') != -1) {
                s.append('\n');
            }
        }

        return s.toString();
    }
    

    /**
     * <p>createSequences</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void createSequences() throws Exception {
        assertUserSet();
        
        Statement st = getConnection().createStatement();
        ResultSet rs;

        m_out.println("- creating sequences... ");

        for (String sequence : getSequenceNames()) {
            if (getSequenceMapping(sequence) == null) {
                throw new Exception("Cannot find sequence mapping for "
                        + sequence);
            }
        }

        for (String sequence : getSequenceNames()) {
            int minvalue = 1;
            boolean alreadyExists;

            m_out.print("  - checking \"" + sequence + "\" sequence... ");

            rs = st.executeQuery("SELECT relname FROM pg_class "
                    + "WHERE relname = '" + sequence.toLowerCase() + "'");
            alreadyExists = rs.next();
            if (alreadyExists) {
                m_out.println("ALREADY EXISTS");
            } else {
                m_out.println("DOES NOT EXIST");
                m_out.print("    - creating sequence \"" + sequence + "\"... ");
                st.execute("CREATE SEQUENCE " + sequence + " minvalue "
                           + minvalue);
                m_out.println("OK");
                grantAccessToObject(sequence, 4);
            }
        }

        m_out.println("- creating sequences... DONE");
    }
    
    /**
     * <p>updatePlPgsql</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void updatePlPgsql() throws Exception {
        Statement st = getConnection().createStatement();
        ResultSet rs;

        m_out.print("- adding PL/pgSQL call handler... ");
        rs = st.executeQuery("SELECT oid FROM pg_proc WHERE "
                + "proname='plpgsql_call_handler' AND " + "proargtypes = ''");
        if (rs.next()) {
            m_out.println("EXISTS");
        } else if (isPgPlPgsqlLibPresent()) {
            st.execute("CREATE FUNCTION plpgsql_call_handler () " + "RETURNS OPAQUE AS '"
                       + m_pg_plpgsql + "' LANGUAGE 'c'");
            m_out.println("OK");
        } else {
            m_out.println("SKIPPED (location of PL/pgSQL library not set, will try to continue)");
        }

        m_out.print("- adding PL/pgSQL language module... ");
        rs = st.executeQuery("SELECT pg_language.oid FROM "
                + "pg_language, pg_proc WHERE "
                + "pg_proc.proname='plpgsql_call_handler' AND "
                + "pg_proc.proargtypes = '' AND "
                + "pg_proc.oid = pg_language.lanplcallfoid AND "
                + "pg_language.lanname = 'plpgsql'");
        if (rs.next()) {
            m_out.println("EXISTS");
        } else {
            st.execute("CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql' "
                    + "HANDLER plpgsql_call_handler LANCOMPILER 'PL/pgSQL'");
            m_out.println("OK");
        }
    }
    
    /**
     * <p>isIpLikeUsable</p>
     *
     * @return a boolean.
     */
    public boolean isIpLikeUsable() {
        Statement st = null;
        try {            
            m_out.print("- checking if iplike is usable... ");
            st = getConnection().createStatement();
            st.execute("SELECT IPLIKE('127.0.0.1', '*.*.*.*')");
            m_out.println("YES");
            return true;
        } catch (SQLException selectException) {
            m_out.println("NO");
            return false;
        } finally {
            closeQuietly(st);
        }
    }
    
    /**
     * <p>updateIplike</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void updateIplike() throws Exception {

        boolean insert_iplike = !isIpLikeUsable();
        
        if (insert_iplike) {
        	dropExistingIpLike();

        	boolean success = installCIpLike();
            
            if (!success) {
                setupPlPgsqlIplike();
        	}
        }

        // XXX This error is generated from Postgres if eventtime(text)
        // does not exist:
        // ERROR: function eventtime(text) does not exist
        m_out.print("- checking for stale eventtime.so references... ");
        Statement st = null;
        try {
            st = getConnection().createStatement();
            st.execute("DROP FUNCTION eventtime(text)");
            m_out.println("REMOVED");
        } catch (SQLException e) {
            /*
             * SQL Status code: 42883: ERROR: function %s does not exist
             */
            if (e.toString().indexOf("does not exist") != -1
                    || "42883".equals(e.getSQLState())) {
                m_out.println("OK");
            } else {
            	m_out.println("FAILED");
                throw e;
            }
        } finally {
            closeQuietly(st);
        }
    }

    private boolean installCIpLike() {
        m_out.print("- inserting C iplike function... ");
        boolean success;
        if (m_pg_iplike == null) {
            success = false;
            
            m_out.println("SKIPPED (location of iplike function not set)");
        } else {
            Statement st = null;
        	try {
                st = getConnection().createStatement();

                st.execute("CREATE FUNCTION iplike(text,text) RETURNS bool " + "AS '"
                        + m_pg_iplike + "' LANGUAGE 'c' WITH(isstrict)");
                
                success = true;
                m_out.println("OK");
        	} catch (SQLException e) {
                success = false;
        		m_out.println("FAILED (" + e + ")");
            } finally {
                closeQuietly(st);
            }
        }
        return success;
    }

    private void dropExistingIpLike() throws SQLException {
        Statement st = null;
        m_out.print("- removing existing iplike definition (if any)... ");
        try {
            st = getConnection().createStatement();
        	st.execute("DROP FUNCTION iplike(text,text)");
        	m_out.println("OK");
        } catch (SQLException dropException) {
        	if (dropException.toString().contains("does not exist")
        			|| "42883".equals(dropException.getSQLState())) {
        		m_out.println("OK");
        	} else {
        		m_out.println("FAILED");
        		throw dropException;
        	}
        }
        finally {
            closeQuietly(st);
        }
    }

    /**
     * <p>setupPlPgsqlIplike</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void setupPlPgsqlIplike() throws Exception {
        InputStream sqlfile = null;
        Statement st = null;
        try {
            st = getConnection().createStatement();
            m_out.print("- inserting PL/pgSQL iplike function... ");
            
        	sqlfile = getClass().getResourceAsStream(IPLIKE_SQL_RESOURCE);
        	if (sqlfile == null) {
                String message = "unable to locate " + IPLIKE_SQL_RESOURCE;
                m_out.println("FAILED (" + message + ")");
        		throw new Exception(message);
        	}
        	
        	BufferedReader in = new BufferedReader(new InputStreamReader(sqlfile));
        	StringBuffer createFunction = new StringBuffer();
        	String line;
        	while ((line = in.readLine()) != null) {
        		createFunction.append(line).append("\n");
        	}
        	st.execute(createFunction.toString());
        	m_out.println("OK");
        } catch (Exception e) {
        	m_out.println("FAILED");
        	throw e;
        } finally {
            // don't forget to close the statement
            closeQuietly(st);
            // don't forget to close the input stream
            closeQuietly(sqlfile);
        }
    }


    private void closeQuietly(InputStream sqlfile) {
        try {
            if (sqlfile != null) {
                sqlfile.close();
            }
        } catch(IOException e) {
            
        }
    }

    private void closeQuietly(Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch(Exception e) {
            
        }
    }

    /**
     * <p>addStoredProcedures</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void addStoredProcedures() throws Exception {
        m_triggerDao.reset();

        Statement st = getConnection().createStatement();

        m_out.print("- adding stored procedures... ");

        FileFilter sqlFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return (pathname.getName().startsWith("get") && pathname.getName().endsWith(".sql"))
                     || pathname.getName().endsWith("Trigger.sql");
            }
        };

        File[] list = new File(m_storedProcedureDirectory).listFiles(sqlFilter);

        for (int i = 0; i < list.length; i++) {
            LinkedList<String> drop = new LinkedList<String>();
            StringBuffer create = new StringBuffer();
            String line;

            m_out.print("\n  - " + list[i].getName() + "... ");

            BufferedReader r = new BufferedReader(new FileReader(list[i]));
            while ((line = r.readLine()) != null) {
                line = line.trim();

                if (line.matches("--.*")) {
                    continue;
                }

                if (line.toLowerCase().startsWith("drop function")
                    || line.toLowerCase().startsWith("drop trigger")) {
                    drop.add(line);
                } else {
                    create.append(line);
                    create.append("\n");
                }
            }
            r.close();
            
            /*
             * Find the trigger first, because if there is a trigger that
             * uses this stored procedure on this table, we'll need to drop
             * it first.
             */
            Trigger t = Trigger.findTriggerInString(create.toString());
            if (t != null) {
                m_triggerDao.add(t);
            }

            Matcher m = Pattern.compile("(?is)\\b(CREATE(?: OR REPLACE)? FUNCTION\\s+"
                                                + "(\\w+)\\s*\\((.*?)\\)\\s+"
                                                + "RETURNS\\s+(\\S+)\\s+AS\\s+"
                                                + "(.+? language ['\"]?\\w+['\"]?);)").matcher(
                                                                                              create.toString());

            if (!m.find()) {
                throw new Exception("For stored procedure in file '"
                                    + list[i].getName()
                                    + "' couldn't match \""
                                    + m.pattern().pattern()
                                    + "\" in string \"" + create + "\"");
            }
            String createSql = m.group(1);
            String function = m.group(2);
            String columns = m.group(3);
            String returns = m.group(4);
            // String rest = m.group(5);

            
            if (functionExists(function, columns, returns)) {
                if (t != null && t.isOnDatabase(getConnection())) {
                    t.removeFromDatabase(getConnection());
                    
                }
                st.execute("DROP FUNCTION " + function + " (" + columns + ")");
                st.execute(createSql);
                m_out.print("OK (dropped and re-added)");
            } else {
                st.execute(createSql);
                m_out.print("OK");
            }
        }
        m_out.println("");
    }


    /**
     * <p>functionExists</p>
     *
     * @param function a {@link java.lang.String} object.
     * @param columns a {@link java.lang.String} object.
     * @param returnType a {@link java.lang.String} object.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     * @throws java.lang.Exception if any.
     */
    public boolean functionExists(String function, String columns,
        String returnType) throws SQLException, Exception {
        return this.functionExists(function, PostgresPgLanguageLanname.ANY, columns,
            returnType);
    }

    /**
     * <p>functionExists</p>
     *
     * @param function a {@link java.lang.String} object.
     * @param language a {@link org.opennms.netmgt.dao.db.InstallerDb.PostgresPgLanguageLanname} object.
     * @param columns a {@link java.lang.String} object.
     * @param returnType a {@link java.lang.String} object.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     * @throws java.lang.Exception if any.
     */
    public boolean functionExists(String function, PostgresPgLanguageLanname language, String columns,
            String returnType) throws SQLException, Exception {
        Map<String, Integer> types = getTypesFromDB();

        int[] columnTypes = new int[0];
        columns = columns.trim();
        if (columns.length() > 0) {
            String[] splitColumns = columns.split("\\s*,\\s*");
            columnTypes = new int[splitColumns.length];
            Column c;
            for (int j = 0; j < splitColumns.length; j++) {
                c = new Column();
                c.parseColumnType(splitColumns[j]);
                columnTypes[j] = (types.get(c.getType())).intValue();
            }
        }

        Column c = new Column();
        try {
            c.parseColumnType(returnType);
        } catch (Exception e) {
            throw new Exception("Could not parse column type '" + returnType + "' for function '" + function + "'.  Nested exception: " + e.getMessage(), e);
        }
        int retType = (types.get(c.getType())).intValue();

        return functionExists(function, language, columnTypes, retType);
    }

    /**
     * <p>functionExists</p>
     *
     * @param function a {@link java.lang.String} object.
     * @param columnTypes an array of int.
     * @param retType a int.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    public boolean functionExists(String function, int[] columnTypes,
            int retType) throws SQLException {
        return this.functionExists(function, PostgresPgLanguageLanname.ANY, columnTypes, retType);
    }

    /**
     * <p>functionExists</p>
     *
     * @param function a {@link java.lang.String} object.
     * @param language a {@link org.opennms.netmgt.dao.db.InstallerDb.PostgresPgLanguageLanname} object.
     * @param columnTypes an array of int.
     * @param retType a int.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    public boolean functionExists(String function, PostgresPgLanguageLanname language, 
        int[] columnTypes, int retType) throws SQLException {
        Statement st = getConnection().createStatement();
        ResultSet rs;

        StringBuffer ct = new StringBuffer();
        for (int j = 0; j < columnTypes.length; j++) {
            ct.append(" " + columnTypes[j]);
        }

        String query = null;
        if (PostgresPgLanguageLanname.ANY.equals(language)) {
            query = "SELECT oid FROM pg_proc WHERE " +
                "proname='" + function.toLowerCase() + "' AND " + 
                "prorettype=" + retType + " AND " + 
                "proargtypes='" + ct.toString().trim() + "'";
        } else {
            query = "SELECT pg_proc.oid FROM pg_proc,pg_language WHERE " +
                "pg_proc.prolang=pg_language.oid AND " +
                "pg_language.lanname='" + language.toString().toLowerCase() + "' AND " +
                "pg_proc.proname='" + function.toLowerCase() + "' AND " +
                "pg_proc.prorettype=" + retType + " AND " + 
                "pg_proc.proargtypes='" + ct.toString().trim() + "'";
        }

        rs = st.executeQuery(query);
        return rs.next();
    }

    /**
     * <p>getTypesFromDB</p>
     *
     * @return a {@link java.util.Map} object.
     * @throws java.sql.SQLException if any.
     */
    public Map<String, Integer> getTypesFromDB() throws SQLException {
        if (m_dbtypes != null) {
            return m_dbtypes;
        }

        Statement st = getConnection().createStatement();
        ResultSet rs;
        HashMap<String, Integer> m = new HashMap<String, Integer>();

        rs = st.executeQuery("SELECT oid,typname,typlen FROM pg_type");

        while (rs.next()) {
            try {
                m.put(Column.normalizeColumnType(rs.getString(2),
                                                 (rs.getInt(3) < 0)),
                      new Integer(rs.getInt(1)));
            } catch (Exception e) {
                // ignore
            }
        }

        m_dbtypes = m;
        return m_dbtypes;
    }
    
    /**
     * <p>addTriggersForTable</p>
     *
     * @param table a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public void addTriggersForTable(String table) throws SQLException {
        List<Trigger> triggers =
            m_triggerDao.getTriggersForTable(table.toLowerCase());
        for (Trigger trigger : triggers) {
            m_out.print("    - checking trigger '" + trigger.getName()
                        + "' on this table... ");
            if (!trigger.isOnDatabase(getConnection())) {
                trigger.addToDatabase(getConnection());
            }
            m_out.println("DONE");
        }
    }

    /**
     * <p>createTables</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void createTables() throws Exception {
        assertUserSet();
        
        Statement st = getConnection().createStatement();
        ResultSet rs;

        m_out.println("- creating tables...");

        for (String tableName : getTableNames()) {
            if (m_force) {
                tableName = tableName.toLowerCase();

                String create = getTableCreateFromSQL(tableName);

                boolean remove;

                rs = st.executeQuery("SELECT relname FROM pg_class "
                        + "WHERE relname = '" + tableName + "'");

                remove = rs.next();

                m_out.print("  - removing old table... ");
                if (remove) {
                    st.execute("DROP TABLE " + tableName + " CASCADE");
                    m_out.println("REMOVED");
                } else {
                    m_out.println("CLEAN");
                }

                m_out.print("  - creating table \"" + tableName + "\"... ");
                st.execute("CREATE TABLE " + tableName + " (" + create + ")");
                m_out.println("CREATED");

                addIndexesForTable(tableName);
                addTriggersForTable(tableName);
                grantAccessToObject(tableName, 2);
            } else {
                m_out.println("  - checking table \"" + tableName + "\"... ");

                tableName = tableName.toLowerCase();

                Table newTable = getTableFromSQL(tableName);
                Table oldTable = getTableFromDB(tableName);

                if (newTable.equals(oldTable)) {
                    addIndexesForTable(tableName);
                    addTriggersForTable(tableName);
                    m_out.println("  - checking table \"" + tableName  + "\"... UPTODATE");
                } else {
                    if (oldTable == null) {
                        String create = getTableCreateFromSQL(tableName);
                        String createSql = "CREATE TABLE " + tableName + " (" + create + ")"; 
                        m_out.print("  - checking table \"" + tableName + "\"... ");
                        st.execute(createSql);
                        m_out.println("CREATED");

                        addIndexesForTable(tableName);
                        addTriggersForTable(tableName);
                        grantAccessToObject(tableName, 2);
                    } else {
                        try {
                            changeTable(tableName, oldTable, newTable);
                        } catch (Exception e) {
                            throw new Exception("Error changing table '"
                                                + tableName
                                                + "'.  Nested exception: "
                                                + e.getMessage(), e);
                        }
                    }
                }
            }
        }

        m_out.println("- creating tables... DONE");
    }
    

    /**
     * <p>getTableFromSQL</p>
     *
     * @param tableName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.dao.db.Table} object.
     * @throws java.lang.Exception if any.
     */
    public Table getTableFromSQL(String tableName) throws Exception {
        Table table = new Table();

        LinkedList<Column> columns = new LinkedList<Column>();
        LinkedList<Constraint> constraints = new LinkedList<Constraint>();

        boolean parens = false;
        StringBuffer accumulator = new StringBuffer();

        String create = getTableCreateFromSQL(tableName);
        for (int i = 0; i <= create.length(); i++) {
            char c = ' ';

            if (i < create.length()) {
                c = create.charAt(i);

                if (c == '(' || c == ')') {
                    parens = (c == '(');
                    accumulator.append(c);
                    continue;
                }
            }

            if (((c == ',') && !parens) || i == create.length()) {
                String a = accumulator.toString().trim();

                if (a.toLowerCase().startsWith("constraint ")) {
                    Constraint constraint;
                    try {
                        constraint = new Constraint(tableName, a);
                    } catch (Exception e) {
                        throw new Exception("Could not parse constraint for table '" + tableName + "'.  Nested exception: " + e.getMessage(), e);
                    }
                    List<String> constraintColumns = constraint.getColumns();
                    if (constraint.getType() != Constraint.CHECK) {
                        Assert.state(constraintColumns.size() > 0, "constraint '" + constraint.getName() + "' has no constrained columns");

                    	for (String constrainedName : constraintColumns) {
                    		Column constrained = findColumn(columns,
                                                        	constrainedName);
                    		if (constrained == null) {
                    			throw new Exception(
                                                	"constraint "
                                                        + constraint.getName()
                                                        + " references column \""
                                                        + constrainedName
                                                        + "\", which is not a column in the table "
                                                        + tableName);
                    		}
                    	}
                    }
                    constraints.add(constraint);
                } else {
                    Column column = new Column();
                    try {
                        column.parse(accumulator.toString());
                        columns.add(column);
                    } catch (Exception e) {
                        throw new Exception("Could not parse table " + tableName
                                + ".  Chained: " + e.getMessage(), e);
                    }
                }

                accumulator = new StringBuffer();
            } else {
                accumulator.append(c);
            }
        }

        table.setName(tableName);
        table.setColumns(columns);
        Collections.sort(constraints, InstallerDb.constraintComparator);
        table.setConstraints(constraints);
        table.setNotNullOnPrimaryKeyColumns();

        return table;
    }


    /**
     * <p>getXFromSQL</p>
     *
     * @param item a {@link java.lang.String} object.
     * @param regex a {@link java.lang.String} object.
     * @param itemGroup a int.
     * @param returnGroup a int.
     * @param description a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public String getXFromSQL(String item, String regex, int itemGroup,
            int returnGroup, String description) throws Exception {

        item = item.toLowerCase();
        Matcher m = Pattern.compile(regex).matcher(getSql());

        while (m.find()) {
            if (m.group(itemGroup).toLowerCase().equals(item)) {
                return m.group(returnGroup);
            }
        }

        throw new Exception("could not find " + description + " \"" + item
                + "\"");
    }
    

    /**
     * <p>findColumn</p>
     *
     * @param columns a {@link java.util.List} object.
     * @param column a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.dao.db.Column} object.
     */
    public Column findColumn(List<Column> columns, String column) {
        for (Column c : columns) {
            if (c.getName().equals(column.toLowerCase())) {
                return c;
            }
        }

        return null;
    }
    
    /**
     * <p>tableColumnExists</p>
     *
     * @param table a {@link java.lang.String} object.
     * @param column a {@link java.lang.String} object.
     * @return a boolean.
     * @throws java.lang.Exception if any.
     */
    public boolean tableColumnExists(String table, String column)
            throws Exception {
        return (findColumn(getTableColumnsFromDB(table), column) != null);
    }
    
    /**
     * <p>getTableColumnsFromDB</p>
     *
     * @param tableName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     * @throws java.lang.Exception if any.
     */
    public List<Column> getTableColumnsFromDB(String tableName)
            throws Exception {
        Table table = getTableFromDB(tableName);
        if (table == null) {
            return null;
        }
        return table.getColumns();
    }
    

    /**
     * <p>getTableFromDB</p>
     *
     * @param tableName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.dao.db.Table} object.
     * @throws java.lang.Exception if any.
     */
    public Table getTableFromDB(String tableName) throws Exception {
        if (!tableExists(tableName)) {
            return null;
        }

        Table table = new Table();
        table.setName(tableName.toLowerCase());

        List<Column> columns = getColumnsFromDB(tableName);
        List<Constraint> constraints = getConstraintsFromDB(tableName);
        Collections.sort(constraints, InstallerDb.constraintComparator);
        
        table.setColumns(columns);
        table.setConstraints(constraints);
        return table;
    }
    

    /**
     * <p>tableExists</p>
     *
     * @param table a {@link java.lang.String} object.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    public boolean tableExists(String table) throws SQLException {
        Statement st = getConnection().createStatement();
        ResultSet rs;

        rs = st.executeQuery("SELECT DISTINCT tablename FROM pg_tables "
                + "WHERE lower(tablename) = '" + table.toLowerCase() + "'");
        return rs.next();
    }

    /**
     * <p>getColumnsFromDB</p>
     *
     * @param tableName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     * @throws java.lang.Exception if any.
     */
    public List<Column> getColumnsFromDB(String tableName) throws Exception {
        LinkedList<Column> columns = new LinkedList<Column>();

        Statement st = getConnection().createStatement();

        String query = "SELECT "
                + "        attname, "
                + "        format_type(atttypid, atttypmod), "
                + "        attnotnull "
                + "FROM "
                + "        pg_attribute "
                + "WHERE "
                + "        attrelid = (SELECT oid FROM pg_class WHERE relname = '" + tableName.toLowerCase() + "') "
                + "    AND "
                + "        attnum > 0";

        if (m_pg_version >= 7.3) {
            query = query + " AND attisdropped = false";
        }

        query = query + " ORDER BY attnum";

        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            Column c = new Column();
            c.setName(rs.getString(1));
            String columnType = rs.getString(2);
            try {
                c.parseColumnType(columnType);
            } catch (Exception e) {
                throw new Exception("Error parsing column type '"
                        + columnType + "' for column '" + rs.getString(1)
                        + "' in table '" + tableName + "'.  Nested: "
                        + e.getMessage(), e);
            }
            c.setNotNull(rs.getBoolean(3));

            columns.add(c);
        }

        rs.close();
        st.close();

        st = getConnection().createStatement();

        query = "SELECT "
                + "        attr.attname, "
                + "        pg_get_expr(def.adbin, def.adrelid) "
                + "FROM "
                + "        pg_attribute attr, "
                + "        pg_attrdef def "
                + "WHERE "
                + "        attr.attrelid = (SELECT oid FROM pg_class WHERE relname = '" + tableName.toLowerCase() + "') "
                + "    AND "
                + "        attr.attnum > 0"
                + "    AND "
                + "        attr.atthasdef = 't' "
                + "    AND "
                + "        attr.attrelid = def.adrelid";


        if (m_pg_version >= 7.3) {
            query = query + " AND attr.attisdropped = false";
        }

        rs = st.executeQuery(query);

        while (rs.next()) {
            Column column = null;
            for (Column c : columns) {
                if (c.getName().equals(rs.getString(1))) {
                    column = c;
                    break;
                }
            }
            
            if (column == null) {
                throw new Exception("Could not find column '" + rs.getString(1) + "' in original column list when adding default values");
            }
            
            column.setDefaultValue(rs.getString(2).replaceAll("'(.*)'::([a-zA-Z ]+)", "'$1'"));
        }

        rs.close();
        st.close();
        
        return columns;

    }


    /**
     * <p>getConstraintsFromDB</p>
     *
     * @param tableName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     * @throws java.lang.Exception if any.
     */
    public List<Constraint> getConstraintsFromDB(String tableName)
            throws SQLException, Exception {
        Statement st = getConnection().createStatement();
        ResultSet rs;

        LinkedList<Constraint> constraints = new LinkedList<Constraint>();

        String query = "SELECT c.oid, c.conname, c.contype, c.conrelid, "
            + "c.confrelid, a.relname, c.confupdtype, c.confdeltype, c.consrc from pg_class a "
            + "right join pg_constraint c on c.confrelid = a.oid "
            + "where c.conrelid = (select oid from pg_class where relname = '"
                + tableName.toLowerCase() + "') order by c.oid";

        rs = st.executeQuery(query);

        while (rs.next()) {
            int oid = rs.getInt(1);
            String name = rs.getString(2);
            String type = rs.getString(3);
            int conrelid = rs.getInt(4);
            int confrelid = rs.getInt(5);
            String ftable = rs.getString(6);
            String foreignUpdType = rs.getString(7);
            String foreignDelType = rs.getString(8);
            String checkExpression = rs.getString(9);
  
            Constraint constraint;
            if ("p".equals(type)) {
                List<String> columns = getConstrainedColumnsFromDBForConstraint(
                                                                                oid,
                                                                                conrelid);
                constraint = new Constraint(tableName.toLowerCase(), name,
                                            columns);
            } else if ("f".equals(type)) {
                List<String> columns = getConstrainedColumnsFromDBForConstraint(
                                                                                oid,
                                                                                conrelid);
                List<String> fcolumns = getForeignColumnsFromDBForConstraint(
                                                                             oid,
                                                                             confrelid);
                constraint = new Constraint(tableName.toLowerCase(), name,
                                            columns, ftable, fcolumns,
                                            foreignUpdType, foreignDelType);
            } else if ("c".equals(type)) {
            	constraint = new Constraint(tableName.toLowerCase(), name, checkExpression);
            } else {
                throw new Exception("Do not support constraint type \""
                        + type + "\" in constraint \"" + name + "\"");
            }

            constraints.add(constraint);
        }

        return constraints;
    }



    private List<String> getConstrainedColumnsFromDBForConstraint(int oid,
            int conrelid) throws Exception {
        Statement st = getConnection().createStatement();
        ResultSet rs;

        LinkedList<String> columns = new LinkedList<String>();

        String query = "select a.attname from pg_attribute a, pg_constraint c where a.attrelid = c.conrelid and a.attnum = ANY (c.conkey) and c.oid = "
                + oid + " and a.attrelid = " + conrelid;
        rs = st.executeQuery(query);

        while (rs.next()) {
            columns.add(rs.getString(1));
        }

        rs.close();
        st.close();

        return columns;
    }

    private List<String> getForeignColumnsFromDBForConstraint(int oid,
            int confrelid) throws Exception {
        Statement st = getConnection().createStatement();
        ResultSet rs;

        LinkedList<String> columns = new LinkedList<String>();

        String query = "select a.attname from pg_attribute a, pg_constraint c where a.attrelid = c.confrelid and a.attnum = ANY (c.confkey) and c.oid = "
                + oid + " and a.attrelid = " + confrelid;
        rs = st.executeQuery(query);

        while (rs.next()) {
            columns.add(rs.getString(1));
        }

        rs.close();
        st.close();

        return columns;
    }

    /**
     * <p>changeTable</p>
     *
     * @param table a {@link java.lang.String} object.
     * @param oldTable a {@link org.opennms.netmgt.dao.db.Table} object.
     * @param newTable a {@link org.opennms.netmgt.dao.db.Table} object.
     * @throws java.lang.Exception if any.
     */
    public void changeTable(String table, Table oldTable, Table newTable)
            throws Exception {
        assertUserSet();
        
        List<Column> oldColumns = oldTable.getColumns();
        List<Column> newColumns = newTable.getColumns();

        Statement st = getConnection().createStatement();
        TreeMap<String, ColumnChange> columnChanges = new TreeMap<String, ColumnChange>();
        String[] oldColumnNames = new String[oldColumns.size()];

        int i;

        if (hasTableChanged(table)) {
            return;
        }
        
        tableChanged(table);

        m_out.println("  - checking table \"" + table
                      + "\"... SCHEMA DOES NOT MATCH");

        m_out.println("    - differences:");
        for (Constraint newConstraint : newTable.getConstraints()) {
            m_out.println("new constraint: " + newConstraint.getTable()
                    + ": " + newConstraint);
        }
        for (Constraint oldConstraint : oldTable.getConstraints()) {
            m_out.println("old constraint: " + oldConstraint.getTable()
                    + ": " + oldConstraint);
        }

        for (Column newColumn : newColumns) {
            Column oldColumn = findColumn(oldColumns, newColumn.getName());

            if (oldColumn == null || !newColumn.equals(oldColumn)) {
                m_out.println("      - column \"" + newColumn.getName()
                        + "\" is different");
                if (m_debug) {
                    m_out.println("        - old column: "
                            + ((oldColumn == null) ? "null"
                                                  : oldColumn.toString()));
                    m_out.println("        - new column: " + newColumn);
                }
            }

            if (!columnChanges.containsKey(newColumn.getName())) {
                columnChanges.put(newColumn.getName(), new ColumnChange());
            }

            ColumnChange columnChange = columnChanges.get(newColumn.getName());
            columnChange.setColumn(newColumn);

            /*
             * If the new column has a NOT NULL constraint, set a null replace
             * value for the column. Throw an exception if it is possible for
             * null data to be inserted into the new column. This would happen
             * if there is not a null replacement and the column either didn't
             * exist before or it did NOT have the NOT NULL constraint before.
             */
            if (m_columnReplacements.containsKey(table + "." + newColumn.getName())) {
                columnChange.setColumnReplacement(m_columnReplacements.get(table + "." + newColumn.getName()));
            }
            if (newColumn.isNotNull() && columnChange.getColumnReplacement() == null) {
                if (oldColumn == null) {
                    String message = "Column " + newColumn.getName()
                            + " in new table has NOT NULL "
                            + "constraint, however this column "
                            + "did not exist before and there is "
                            + "no change replacement for this column";
                    if (m_ignore_notnull) {
                        m_out.println(message + ".  Ignoring due to '-N'");
                    } else {
                        throw new Exception(message);
                    }
                } else if (!oldColumn.isNotNull()) {
                    String message = "Column " + newColumn.getName()
                            + " in new table has NOT NULL "
                            + "constraint, however this column "
                            + "did not have the NOT NULL "
                            + "constraint before and there is "
                            + "no change replacement for this column";
                    if (m_ignore_notnull) {
                        m_out.println(message + ".  Ignoring due to '-N'");
                    } else {
                        throw new Exception(message);
                    }
                }
            }
        }

        i = 0;
        for (Column oldColumn : oldColumns) {
            oldColumnNames[i] = oldColumn.getName();

            if (columnChanges.containsKey(oldColumn.getName())) {
                ColumnChange columnChange = (ColumnChange) columnChanges.get(oldColumn.getName());
                Column newColumn = (Column) columnChange.getColumn();
                if (newColumn.getType().indexOf("timestamp") != -1) {
                    columnChange.setUpgradeTimestamp(true);
                }
            } else {
                m_out.println("      * WARNING: column \""
                        + oldColumn.getName() + "\" exists in the "
                        + "database but is not in the new schema.  "
                        + "REMOVING COLUMN");
            }
            
            i++;
        }

        String tmpTable = table + "_old_" + System.currentTimeMillis();

        try {
            if (tableExists(tmpTable)) {
                st.execute("DROP TABLE " + tmpTable + " CASCADE");
            }

            m_out.print("    - creating temporary table... ");
            st.execute("CREATE TABLE " + tmpTable + " AS SELECT "
                       + StringUtils.arrayToDelimitedString(oldColumnNames, ", ")
                       + " FROM " + table);
            m_out.println("done");

            st.execute("DROP TABLE " + table + " CASCADE");

            m_out.print("    - creating new '" + table + "' table... ");
            st.execute("CREATE TABLE " + table + " ("
                    + getTableCreateFromSQL(table) + ")");
            m_out.println("done");
            
            addIndexesForTable(table);
            addTriggersForTable(table);

            transformData(table, tmpTable, columnChanges, oldColumnNames);

            grantAccessToObject(table, 4);

            m_out.print("    - optimizing table " + table + "... ");
            st.execute("VACUUM ANALYZE " + table);
            m_out.println("DONE");
        } catch (Exception e) {
            if (m_no_revert) {
                m_out.println("FAILED!  Not reverting due to '-R' being "
                        + "passed.  Old data in " + tmpTable);
                throw e;
            }

            try {
                getConnection().rollback();
                getConnection().setAutoCommit(true);

                if (tableExists(table)) {
                    st.execute("DROP TABLE " + table + " CASCADE");
                }
                st.execute("CREATE TABLE " + table + " AS SELECT "
                           + StringUtils.arrayToDelimitedString(oldColumnNames, ", ")
                           + " FROM " + tmpTable);
                st.execute("DROP TABLE " + tmpTable);
            } catch (SQLException se) {
                throw new Exception("Got SQLException while trying to "
                        + "revert table changes due to original " + "error: "
                        + e + "\n" + "SQLException while reverting table: "
                        + se, e);
            }
            m_out.println("FAILED!  Old data restored, however indexes and "
                    + "constraints on this table were not re-added");
            throw e;
        }

        // We don't care if dropping the tmp table fails since we've
        // completed copying it, so it's outside of the try/catch block above.
        st.execute("DROP TABLE " + tmpTable);


        m_out.println("  - checking table \"" + table
                      + "\"... COMPLETED UPDATING TABLE");
    }

    /*
     * Note: every column has a ColumnChange record for it, which lists
     * the column name, a null replacement, if any, and the indexes for
     * selected rows (for using in ResultSet.getXXX()) and prepared rows
     * (PreparedStatement.setObject()).
     * Monkey.  Make monkey dance.
     */
    /**
     * <p>transformData</p>
     *
     * @param table a {@link java.lang.String} object.
     * @param oldTable a {@link java.lang.String} object.
     * @param columnChanges a {@link java.util.TreeMap} object.
     * @param oldColumnNames an array of {@link java.lang.String} objects.
     * @throws java.sql.SQLException if any.
     * @throws java.text.ParseException if any.
     * @throws java.lang.Exception if any.
     */
    public void transformData(String table, String oldTable,
            TreeMap<String, ColumnChange> columnChanges,
            String[] oldColumnNames) throws SQLException, ParseException,
            Exception {
        Statement st = getConnection().createStatement();
        int i;

        st.setFetchSize(s_fetch_size);

        for (i = 0; i < oldColumnNames.length; i++) {
            ColumnChange c = columnChanges.get(oldColumnNames[i]);
            if (c != null) {
                c.setSelectIndex(i + 1);
            }
        }
        
        LinkedList<String> insertColumns = new LinkedList<String>();
        LinkedList<String> questionMarks = new LinkedList<String>();
        for (ColumnChange c : columnChanges.values()) {
            c.setColumnType(c.getColumn().getColumnSqlType());
            
            ColumnChangeReplacement r = c.getColumnReplacement();
            if (r == null || c.getSelectIndex() > 0
                    || r.addColumnIfColumnIsNew()) {
                insertColumns.add(c.getColumn().getName());
                questionMarks.add("?");
                c.setPrepareIndex(questionMarks.size());
            }
        }

        /*
         * Pull everything in from the old table and filter it to update the
         * data to any new formats.
         */

        m_out.print("    - transforming data into the new table...\r");

        ResultSet rs = st.executeQuery("SELECT count(*) FROM " + oldTable);
        rs.next();
        long num_rows = rs.getLong(1);

        String order;
        if (table.equals("outages")) {
            order = " ORDER BY iflostservice";
        } else {
            order = "";
        }

        String dbcmd = "SELECT "
            + StringUtils.arrayToDelimitedString(oldColumnNames, ", ")
            + " FROM "
            + oldTable + order;
        if (m_debug) {
            m_out.println("    - performing select: " + dbcmd);
        }

        PreparedStatement select = getConnection().prepareStatement(dbcmd);
        select.setFetchSize(s_fetch_size);

        dbcmd = "INSERT INTO " + table + " ("
            + StringUtils.collectionToDelimitedString(insertColumns, ", ")
            + ") values ("
            + StringUtils.collectionToDelimitedString(questionMarks, ", ")
            + ")";
        if (m_debug) {
            m_out.println("    - performing insert: " + dbcmd);
        }

        PreparedStatement insert = getConnection().prepareStatement(dbcmd);

        rs = select.executeQuery();
        getConnection().setAutoCommit(false);

        Object obj;
        SimpleDateFormat dateParser = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        char spin[] = { '/', '-', '\\', '|' };

        int current_row = 0;

        while (rs.next()) {
            for (ColumnChange change : columnChanges.values()) {
                String name = change.getColumn().getName();
                
                if (change.getSelectIndex() == 0 &&
                        change.hasColumnReplacement()
                        && !change.getColumnReplacement().addColumnIfColumnIsNew()) {
                    continue;
                }

                if (change.getSelectIndex() > 0) {
                    obj = rs.getObject(change.getSelectIndex());
                    if (rs.wasNull()) {
                        obj = null;
                    }
                } else {
                    if (m_debug) {
                        m_out.println("      - don't know what to do "
                                + "for \"" + name + "\", prepared column "
                                + change.getPrepareIndex()
                                + ": setting to null");
                    }
                    obj = null;
                }

                if (obj == null && change.hasColumnReplacement()) {
                    ColumnChangeReplacement replacement =
                        change.getColumnReplacement();
                    obj = replacement.getColumnReplacement(rs, columnChanges);
                    if (m_debug) {
                        m_out.println("      - " + name
                                + " was NULL but is a "
                                + "requires NULL replacement -- "
                                + "replacing with '" + obj + "'");
                    }
                }

                if (obj != null) {
                    if (change.isUpgradeTimestamp()
                            && !obj.getClass().equals(java.sql.Timestamp.class)) {
                        if (m_debug) {
                            m_out.println("      - " + name
                                    + " is an old-style timestamp");
                        }
                        String newObj = dateFormatter.format(dateParser.parse((String) obj));
                        if (m_debug) {
                            m_out.println("      - " + obj + " -> " + newObj);
                        }

                        obj = newObj;
                    }
                    if (m_debug) {
                        m_out.println("      - " + name + " = " + obj);
                    }
                } else {
                    if (m_debug) {
                        m_out.println("      - " + name + " = undefined");
                    }
                }

                if (obj == null) {
                    insert.setNull(change.getPrepareIndex(),
                                   change.getColumnType());
                } else {
                    insert.setObject(change.getPrepareIndex(), obj);
                }
            }

            try {
                insert.execute();
            } catch (SQLException e) {
                SQLException ex = new SQLException(
                                                   "Statement.execute() threw an "
                                                           + "SQLException while inserting a row: "
                                                           + "\""
                                                           + insert.toString()
                                                           + "\".  "
                                                           + "Original exception: "
                                                           + e.toString(),
                                                   e.getSQLState(),
                                                   e.getErrorCode());
                ex.setNextException(e);
                throw ex;
            }

            current_row++;

            if ((current_row % 20) == 0) {
                System.err.print("    - transforming data into the new "
                        + "table... "
                        + (int) Math.floor((current_row * 100) / num_rows)
                        + "%  [" + spin[(current_row / 20) % spin.length]
                        + "]\r");
            }
        }
        
        rs.close();
        select.close();
        insert.close();

        getConnection().commit();
        getConnection().setAutoCommit(true);

        if (table.equals("events") && num_rows == 0) {
            st.execute("INSERT INTO events (eventid, eventuei, eventtime, "
                    + "eventsource, eventdpname, eventcreatetime, "
                    + "eventseverity, eventlog, eventdisplay) values "
                    + "(0, 'http://uei.opennms.org/dummyevent', now(), "
                    + "'OpenNMS.Eventd', 'localhost', now(), 1, 'Y', 'Y')");
        }
        
        st.close();

        m_out.println("    - transforming data into the new table... "
                + "DONE           ");
    }


    /**
     * <p>databaseCheckVersion</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void databaseCheckVersion() throws Exception {
        m_out.print("- checking database version... ");

        Statement st = getAdminConnection().createStatement();
        ResultSet rs = st.executeQuery("SELECT version()");
        if (!rs.next()) {
            throw new Exception("Database didn't return any rows for 'SELECT version()'");
        }

        String versionString = rs.getString(1);

        rs.close();
        st.close();
        closeAdminConnection();

        Matcher m = Pattern.compile("^PostgreSQL (\\d+\\.\\d+\\.\\d+)").matcher(versionString);
        if (m.find()) {
            String pgFullVersion = m.group(1);
            if (pgFullVersion.equals("8.4.0")) {
                throw new Exception(String.format("Unsupported database version 8.4.0.  PostgreSQL 8.4.0 has a bug" +
                		" in left join subselects which causes issues with OpenNMS."));
            }
        }

        m = Pattern.compile("^PostgreSQL (\\d+\\.\\d+)").matcher(versionString);

        if (!m.find()) {
            throw new Exception("Could not parse version number out of version string: " + versionString);
        }
        m_pg_version = Float.parseFloat(m.group(1));

        String message = "Unsupported database version \""
                            + m_pg_version + "\" -- you need at least "
                            + POSTGRES_MIN_VERSION + " and less than "
                            + POSTGRES_MAX_VERSION_PLUS_ONE
                            + ".  Use the \"-Q\" option to disable this check "
                            + "if you feel brave and are willing to find and "
                            + "fix bugs found yourself.";
        if (m_pg_version < POSTGRES_MIN_VERSION) {
            throw new Exception(message);
        } else if (m_pg_version >= POSTGRES_MAX_VERSION_PLUS_ONE) {
            throw new Exception(message);
        }

        m_out.println(Float.toString(m_pg_version));
        m_out.println("  - Full version string: " + versionString);
    }

    /**
     * <p>databaseCheckLanguage</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void databaseCheckLanguage() throws Exception {
        /*
         * Don't bother checking if the database version is 7.4 or greater and
         * just return without throwing an exception. We can (and do) use SQL
         * state checks instead of matching on the exception text, so the
         * language of server error messages does not matter.
         */
        if (m_pg_version >= 7.4) {
            return;
        }

        /*
         * Use column names that should never exist and also encode the
         * current time, in hopes that this should never actually succeed.
         */
        String timestamp = Long.toString(System.currentTimeMillis());
        String bogus_query = "SELECT bogus_column_" + timestamp + " "
                + "FROM bogus_table_" + timestamp + " "
                + "WHERE another_bogus_column_" + timestamp + " IS NULL";

        // Expected error: "ERROR: relation "bogus_table" does not exist"
        Statement st = getAdminConnection().createStatement();
        try {
            st.executeQuery(bogus_query);
        } catch (SQLException e) {
            if (e.toString().indexOf("does not exist") != -1) {
                /*
                 * Everything is fine, since we matched the error. We should
                 * be safe to assume that all of the other error messages we
                 * need to check for are in English.
                 */
                return;
            }
            throw new Exception("The database server's error messages "
                    + "are not in English, however the installer "
                    + "requires them to be in English when using "
                    + "PostgreSQL earlier than 7.4.  You either "
                    + "need to set \"lc_messages = 'C'\" in your "
                    + "postgresql.conf file and restart "
                    + "PostgreSQL or upgrade to PostgreSQL 7.4 or "
                    + "later.  The installer executed the query " + "\""
                    + bogus_query + "\" and expected "
                    + "\"does not exist\" in the error message, "
                    + "but this exception was received instead: " + e, e);
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
            try {
                closeAdminConnection();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
        }

        /*
         * We should not get here, as the above command should always throw an
         * exception, so complain and throw an exception about not getting the
         * exception we were expecting. Are you lost yet? Good!
         */
        throw new Exception("Expected an SQLException when executing a "
                + "bogus query to test for the server's error "
                + "message language, however the query succeeded "
                + "unexpectedly.  SQL query: \"" + bogus_query + "\".");

    }

    /**
     * <p>checkOldTables</p>
     *
     * @throws java.sql.SQLException if any.
     * @throws org.opennms.netmgt.dao.db.BackupTablesFoundException if any.
     */
    public void checkOldTables() throws SQLException,
            BackupTablesFoundException {
        Statement st = getConnection().createStatement();
        ResultSet rs = st.executeQuery("SELECT relname FROM pg_class "
                + "WHERE relkind = 'r' AND " + "relname LIKE '%_old_%'");
        LinkedList<String> oldTables = new LinkedList<String>();

        m_out.print("- checking database for old backup tables... ");

        while (rs.next()) {
            oldTables.add(rs.getString(1));
        }

        rs.close();
        st.close();

        if (oldTables.size() == 0) {
            // No problems, so just print "NONE" and return.
            m_out.println("NONE");
            return;
        }

        throw new BackupTablesFoundException(oldTables);
    }

    /**
     * <p>getForeignKeyConstraints</p>
     *
     * @return a {@link java.util.List} object.
     * @throws java.lang.Exception if any.
     */
    public List<Constraint> getForeignKeyConstraints() throws Exception {
        LinkedList<Constraint> constraints = new LinkedList<Constraint>();

        for (String table : getTableNames()) {
            String tableLower = table.toLowerCase();
            for (Constraint constraint : getTableFromSQL(tableLower).getConstraints()) {
                if (constraint.getType() == Constraint.FOREIGN_KEY) {
                    constraints.add(constraint);
                }
            }
        }

        return constraints;
    }

    /**
     * <p>checkConstraints</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void checkConstraints() throws Exception {
        List<Constraint> constraints = getForeignKeyConstraints();

        m_out.println("- checking for rows that violate constraints...");

        for (Constraint constraint : constraints) {
            m_out.print("  - checking for rows that violate constraint '"
                        + constraint.getName() + "'... ");
            checkConstraint(constraint);
            m_out.println("DONE");
        }
        
        m_out.println("- checking for rows that violate constraints... DONE");
    }
    
    /**
     * <p>checkConstraint</p>
     *
     * @param constraint a {@link org.opennms.netmgt.dao.db.Constraint} object.
     * @throws java.lang.Exception if any.
     */
    public void checkConstraint(Constraint constraint) throws Exception {
        String name = constraint.getName();
        String table = constraint.getTable();
        List<String> columns = constraint.getColumns();
        String ftable = constraint.getForeignTable();
        List<String> fcolumns = constraint.getForeignColumns();

        if (!tableExists(table)) {
            // The constrained table does not exist
            return;
        }
        for (String column : columns) {
            if (!tableColumnExists(table, column)) {
                // This constrained column does not exist
                return;
            }
        }

        // XXX Not sure if it's okay to leave this out
        /*
         * if (table.equals("usersNotified") && column.equals("id")) { //
         * m_out.print("Skipping usersNotified.id"); continue; }
         */

//        String partialQuery = "FROM " + table + " WHERE "
//                + getForeignConstraintWhere(table, columns, ftable, fcolumns);
        
        String partialQuery = getJoinForRowsThatFailConstraint(table, columns, ftable, fcolumns);
        

        Statement st = getConnection().createStatement();
        ResultSet rs;
        String query = "SELECT count(*) " + partialQuery; 
        try {
            rs = st.executeQuery(query);
        } catch (SQLException e) {
            throw new Exception("Failed to execute query '" + query + "'.  "
                                + "Nested exception: " + e.getMessage(), e);
        }

        rs.next();
        int count = rs.getInt(1);
        rs.close();

        if (count != 0) {
            rs = st.executeQuery("SELECT count(*) FROM " + table);
            rs.next();
            int total = rs.getInt(1);
            rs.close();
            st.close();

            throw new Exception("Table " + table + " contains " + count
                    + " rows " + "(out of " + total
                    + ") that violate new constraint " + name + ".  "
                    + "See the install guide for details "
                    + "on how to correct this problem.  You can execute this "
                    + "SQL query to see a list of the rows that violate the "
                    + "constraint:\n"
                    + "SELECT * " + partialQuery);
        }

        st.close();

    }

    private String getJoinForRowsThatFailConstraint(String table, List<String> columns, String ftable, List<String> fcolumns) throws Exception {
        String notNulls = notNullWhereClause(table, columns);
        String noForeign = "FROM " + table + " WHERE " + notNulls;
        
        if (!tableExists(ftable)) {
            return noForeign;
        }
        
        for (String fcolumn : fcolumns) {
            if (!tableColumnExists(ftable, fcolumn)) {
                return noForeign;
            }
        }

        
        String partialQuery = "FROM " + table + " LEFT JOIN " + ftable + " ON (";
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            String fcolumn = fcolumns.get(i);
            if (i != 0) {
                partialQuery += " AND ";
            }
                
            partialQuery += table+'.'+column+" = "+ftable+'.'+fcolumn;
        }
        
        partialQuery += ") WHERE "+ftable+'.'+fcolumns.get(0)+" is NULL AND "+ notNulls;
        
        return partialQuery;
    }

    /**
     * <p>getForeignConstraintWhere</p>
     *
     * @param table a {@link java.lang.String} object.
     * @param columns a {@link java.util.List} object.
     * @param ftable a {@link java.lang.String} object.
     * @param fcolumns a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public String getForeignConstraintWhere(String table, List<String> columns,
            String ftable, List<String> fcolumns) throws Exception {
        String notNulls = notNullWhereClause(table, columns);
 
        if (!tableExists(ftable)) {
            return notNulls;
        }
        for (String fcolumn : fcolumns) {
            if (!tableColumnExists(ftable, fcolumn)) {
                return notNulls;
            }
        }

        return notNulls + " AND ( "
            + StringUtils.collectionToDelimitedString(tableColumnList(table, columns), ", ")
            + " ) NOT IN (SELECT "
            + StringUtils.collectionToDelimitedString(tableColumnList(ftable, fcolumns), ", ")
            + " FROM " + ftable + ")";
    }

    /**
     * <p>notNullWhereClause</p>
     *
     * @param table a {@link java.lang.String} object.
     * @param columns a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    public String notNullWhereClause(String table, List<String> columns) {
        List<String> isNotNulls = new ArrayList<String>(columns.size());
        
        for (String column : columns) {
            isNotNulls.add(table + "." + column + " IS NOT NULL");
        }
        
        return StringUtils.collectionToDelimitedString(isNotNulls, " AND ");
    }
    
    /**
     * <p>tableColumnList</p>
     *
     * @param table a {@link java.lang.String} object.
     * @param columns a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public List<String> tableColumnList(String table, List<String> columns) {
        List<String> tableColumns = new ArrayList<String>(columns.size());
        
        for (String column : columns) {
            tableColumns.add(table + "." + column);
        }
        
        return tableColumns;
    }


    /**
     * <p>fixConstraint</p>
     *
     * @param constraintName a {@link java.lang.String} object.
     * @param removeRows a boolean.
     * @throws java.lang.Exception if any.
     */
    public void fixConstraint(String constraintName, boolean removeRows)
            throws Exception {
        List<Constraint> constraints = getForeignKeyConstraints();

        m_out.print("- fixing rows that violate constraint "
                + constraintName + "... ");

        for (Constraint c : constraints) {
            if (constraintName.equals(c.getName())) {
                m_out.println(fixConstraint(c, removeRows));
                return;
            }
        }

        throw new Exception("Did not find constraint "
                            + constraintName + " in the database.");
    }
    
    /**
     * <p>fixConstraint</p>
     *
     * @param constraint a {@link org.opennms.netmgt.dao.db.Constraint} object.
     * @param removeRows a boolean.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public String fixConstraint(Constraint constraint, boolean removeRows)
            throws Exception {
        String table = constraint.getTable();
        List<String> columns = constraint.getColumns();
        String ftable = constraint.getForeignTable();
        List<String> fcolumns = constraint.getForeignColumns();

        if (!tableExists(table)) {
            throw new Exception("Constraint " + constraint.getName()
                    + " is on table " + table + ", but table does "
                    + "not exist (so fixing this constraint does "
                    + "nothing).");
        }

        for (String column : columns) {
            if (!tableColumnExists(table, column)) {
                throw new Exception("Constraint " + constraint.getName()
                                    + " constrains column " + column
                                    + " of table " + table
                                    + ", but column does "
                                    + "not exist (so fixing this constraint "
                                    + "does nothing).");
            }
        }

//        String where = getForeignConstraintWhere(table, columns, ftable,
//                                                 fcolumns);
        
        String tuple = "";
        for(int i = 0; i < columns.size(); i++) {
                if (i != 0) {
                        tuple += ", ";
                }
                tuple += table+'.'+columns.get(i);
        }
        
        String where = "( "+ tuple + ") IN ( SELECT " + tuple + " " +
                getJoinForRowsThatFailConstraint(table, columns, ftable, fcolumns) +")";

        String query;
        String change_text;

        if (removeRows) {
            query = "DELETE FROM " + table + " WHERE " + where;
            change_text = "DELETED";
        } else {
            List<String> sets = new ArrayList<String>(columns.size());
            for (String column : columns) {
                sets.add(column + " = NULL");
            }
            
            query = "UPDATE " + table + " SET "
                + StringUtils.collectionToDelimitedString(sets, ", ") + " "
                + "WHERE " + where;
            change_text = "UPDATED";
        }

        Statement st = getConnection().createStatement();
        int num = st.executeUpdate(query);

        return change_text + " " + num + (num == 1 ? " ROW" : " ROWS");
    }

    /**
     * <p>databaseUserExists</p>
     *
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    public boolean databaseUserExists() throws SQLException {
        assertUserSet();

        boolean exists = false;

        Statement st = getAdminConnection().createStatement();
        ResultSet rs = null;
        try {
            rs = st.executeQuery("SELECT usename FROM pg_user WHERE "
                    + "usename = '" + m_user + "'");
            exists = rs.next();
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
            try {
                st.close();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
            try {
                closeAdminConnection();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
        }

        return exists;
    }

    /**
     * <p>databaseAddUser</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public void databaseAddUser() throws SQLException {
        assertUserSet();

        Statement st = getAdminConnection().createStatement();
        try {
            st.execute("CREATE USER " + m_user + " WITH PASSWORD '" + m_pass
                + "' CREATEDB CREATEUSER");
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
            try {
                closeAdminConnection();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
        }
    }

    /**
     * <p>databaseRemoveUser</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public void databaseRemoveUser() throws SQLException {
        assertUserSet();

        m_out.print("- removing user '" + m_user + "'... ");
        Statement st = getAdminConnection().createStatement();
        try {
            st.execute("DROP USER IF EXISTS " + m_user);
            m_out.print("DONE");
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
            try {
                closeAdminConnection();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
        }
    }
    
    /**
     * <p>databaseDBExists</p>
     *
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    public boolean databaseDBExists() throws SQLException {
        boolean exists;

        Statement st = getAdminConnection().createStatement();
        ResultSet rs = null;
        try {
            rs = st.executeQuery("SELECT datname from pg_database "
                    + "WHERE datname = '" + m_databaseName + "'");
            exists = rs.next();
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
            try {
                st.close();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
            try {
                closeAdminConnection();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
        }

        return exists;
    }

    /**
     * <p>databaseAddDB</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void databaseAddDB() throws Exception {
        assertUserSet();

        m_out.print("- creating database '" + m_databaseName + "'... ");
        Statement st = getAdminConnection().createStatement();
        try {
            st.execute("CREATE DATABASE \"" + m_databaseName + "\" WITH ENCODING='UNICODE'");
            st.execute("GRANT ALL ON DATABASE \"" + m_databaseName + "\" TO \"" + m_user + "\"");
            m_out.print("DONE");
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
            try {
                closeAdminConnection();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
        }
    }
    
    /**
     * <p>databaseRemoveDB</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public void databaseRemoveDB() throws SQLException {
        assertUserSet();

        m_out.print("- removing database '" + m_databaseName + "'... ");
        Statement st = getAdminConnection().createStatement();
        try {
            st.execute("DROP DATABASE \"" + m_databaseName + "\"");
            m_out.print("DONE");
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
            try {
                closeAdminConnection();
            } catch (SQLException e) {
                m_out.print("ERROR: " + e.getMessage());
            }
        }
    }

    /**
     * <p>addIndexesForTable</p>
     *
     * @param table a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public void addIndexesForTable(String table) throws SQLException {
        List<Index> indexes =
            getIndexDao().getIndexesForTable(table.toLowerCase());
        for (Index index : indexes) {
            m_out.print("    - checking index '" + index.getName()
                        + "' on this table... ");
            if (!index.isOnDatabase(getConnection())) {
                index.addToDatabase(getConnection());
            }
            m_out.println("DONE");
        }

    }

    /**
     * <p>grantAccessToObject</p>
     *
     * @param object a {@link java.lang.String} object.
     * @param indent a int.
     * @throws java.sql.SQLException if any.
     */
    public void grantAccessToObject(String object, int indent) throws SQLException {
        assertUserSet();
        
        for (int i = 0; i < indent; i++) {
            m_out.print(" ");
        }
        
        m_out.print("- granting access to '" + object + "' for user '" + m_user + "'... ");
        
        Statement st = getConnection().createStatement();
        
        try {
            st.execute("GRANT ALL ON " + object + " TO " + m_user);
        } finally {
            st.close();
        }
        
        m_out.println("DONE");
    }

    /**
     * <p>fixData</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void fixData() throws Exception {
        Statement st = getConnection().createStatement();

        st.execute("UPDATE ipinterface SET issnmpprimary='N' "
                + "WHERE issnmpprimary IS NULL");
        st.execute("UPDATE service SET servicename='SSH' "
                + "WHERE servicename='OpenSSH'");
        st.execute("UPDATE snmpinterface SET snmpipadentnetmask=NULL");
    }

    // XXX This causes the following Postgres error:
    // ERROR: duplicate key violates unique constraint "pk_dpname"
    /**
     * <p>insertData</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void insertData() throws Exception {

        for (String table : getInserts().keySet()) {
            Status status = Status.OK;

            m_out.print("- inserting initial table data for \"" + table + "\"... ");

            // XXX: criteria are checked for all inserts before
            // any of them are done so inserts don't interfere with
            // other inserts criteria
            List<Insert> toBeInserted = new LinkedList<Insert>();
            for (Insert insert : getInserts().get(table)) {
                if (insert.isCriteriaMet()) {
                    toBeInserted.add(insert);
                }
            }
            
            for(Insert insert : toBeInserted) {
                status = status.combine(insert.doInsert());
            }

            m_out.println(status);
        }
    }

    /**
     * <p>checkUnicode</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void checkUnicode() throws Exception {
        assertUserSet();

        m_out.print("- checking if database \"" + m_databaseName + "\" is unicode... ");

        Statement st = null;
        ResultSet rs = null;

        try {
            try {
                st = getAdminConnection().createStatement();
                
                try {
                    rs = st.executeQuery("SELECT encoding FROM pg_database WHERE LOWER(datname)='" + m_databaseName.toLowerCase() + "'");
                    if (rs.next()) {
                        if (rs.getInt(1) == 5 || rs.getInt(1) == 6) {
                            m_out.println("ALREADY UNICODE");
                            return;
                        }
                    }
                    m_out.println("NOT UNICODE");
                    
                    throw new Exception("OpenNMS requires a Unicode database.  Please delete and recreate your\ndatabase and try again.");
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
            } finally {
                if (st != null) {
                    st.close();
                }
            }
        } finally {
            this.disconnect();
        }
    }


    /**
     * <p>checkIndexUniqueness</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void checkIndexUniqueness() throws Exception {
        Collection<Index> indexes = getIndexDao().getAllIndexes();

        Statement st = getConnection().createStatement();

        for (Index index : indexes) {
            if (!index.isUnique()) {
                continue;
            }
            if (!tableExists(index.getTable())) {
                continue;
            }
            boolean missingColumn = false;
            for (String column : index.getColumns()) {
                if (!tableColumnExists(index.getTable(), column)) {
                    missingColumn = true;
                }
            }
            if (missingColumn) {
                continue;
            }
            
            String query = index.getIndexUniquenessQuery();
            if (query == null) {
                continue;
            }
            
            String countQuery = query.replaceFirst("(?i)\\s(\\S+)\\s+FROM",
                " count(\\1) FROM").replaceFirst("(?i)\\s*ORDER\\s+BY\\s+[^()]+$", "");
            
            ResultSet rs = st.executeQuery(countQuery);

            rs.next();
            int count = rs.getInt(1);
            rs.close();

            if (count > 0) {
                st.close();
                throw new Exception("Unique index '" +  index.getName() + "' "
                                    + "cannot be added to table '" +
                                    index.getTable() + "' because " + count
                                    + " rows are not unique.  See the "
                                    + "install guide for details on how to "
                                    + "correct this problem.  You can use the "
                                    + "following SQL to see which rows are not "
                                    + "unique:\n"
                                    + query);
            }
        }
        
        st.close();
    }

    
    /**
     * <p>getTableColumnsFromSQL</p>
     *
     * @param tableName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     * @throws java.lang.Exception if any.
     */
    public List<Column> getTableColumnsFromSQL(String tableName)
            throws Exception {
        return getTableFromSQL(tableName).getColumns();
    }


    /**
     * <p>getTableCreateFromSQL</p>
     *
     * @param table a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public String getTableCreateFromSQL(String table) throws Exception {
        return getXFromSQL(table, "(?i)\\bcreate table\\s+['\"]?(\\S+)['\"]?"
                + "\\s+\\((.+?)\\);", 1, 2, "table");
    }

    /**
     * <p>getIndexFromSQL</p>
     *
     * @param index a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public String getIndexFromSQL(String index) throws Exception {
        return getXFromSQL(index, "(?i)\\b(create (?:unique )?index\\s+"
                + "['\"]?(\\S+)['\"]?\\s+.+?);", 2, 1, "index");
    }

    /**
     * <p>getFunctionFromSQL</p>
     *
     * @param function a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public String getFunctionFromSQL(String function) throws Exception {
        return getXFromSQL(function, "(?is)\\bcreate function\\s+"
                + "['\"]?(\\S+)['\"]?\\s+"
                + "(.+? language ['\"]?\\w+['\"]?);", 1, 2, "function");
    }

    /**
     * <p>getLanguageFromSQL</p>
     *
     * @param language a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public String getLanguageFromSQL(String language) throws Exception {
        return getXFromSQL(language, "(?is)\\bcreate trusted procedural "
                + "language\\s+['\"]?(\\S+)['\"]?\\s+(.+?);", 1, 2,
                           "language");
    }


    private void assertUserSet() {
        Assert.state(m_user != null, "postgresOpennmsUser property has not been set");
    }

    private Connection getConnection() throws SQLException {
        if (m_connection == null) {
            initializeConnection();
        }
        return m_connection;
    }

    private void initializeConnection() throws SQLException {
        Assert.state(m_dataSource != null, "dataSource property has not been set");

        try {
            m_connection = getDataSource().getConnection();
        } catch (SQLException e) {
            rethrowDatabaseConnectionException(getDataSource(), e, "Could not get a connection to the OpenNMS database.");
        }
    }
    
    /**
     * <p>closeConnection</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public void closeConnection() throws SQLException {
        if (m_connection == null) {
            return;
        }
        m_connection.close();
        m_connection = null;
    }

    private Connection getAdminConnection() throws SQLException {
        if (m_adminConnection == null) {
            initializeAdminConnection();
        }
        return m_adminConnection;
    }

    private void initializeAdminConnection() throws SQLException {
        Assert.state(m_adminDataSource != null, "adminDataSource property has not been set");

        try {
            m_adminConnection = getAdminDataSource().getConnection();
        } catch (SQLException e) {
            rethrowDatabaseConnectionException(getAdminDataSource(), e, "Could not get an administrative connection to the database.");
        }
    }
    
    /**
     * <p>closeAdminConnection</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public void closeAdminConnection() throws SQLException {
        if (m_adminConnection == null) {
            return;
        }
        m_adminConnection.close();
        m_adminConnection = null;
    }

    /**
     * Close all connections to the database.
     *
     * @throws java.sql.SQLException if any.
     */
    public void disconnect() throws SQLException {
        this.closeColumnReplacements();
        this.closeConnection();
        this.closeAdminConnection();
    }

    private void rethrowDatabaseConnectionException(DataSource ds, SQLException e, String msg) throws SQLException {
        SQLException newE = new SQLException(msg + "  Is the database running, listening for TCP connections, and allowing us to connect and authenticate from localhost?  Tried connecting to database specified by data source " + ds.toString() + ".  Original error: " + e);
        newE.initCause(e);
        throw newE;
    }

    /**
     * <p>setCreateSqlLocation</p>
     *
     * @param createSqlLocation a {@link java.lang.String} object.
     */
    public void setCreateSqlLocation(String createSqlLocation) {
        m_createSqlLocation = createSqlLocation;
    }

    /**
     * <p>getCreateSqlLocation</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCreateSqlLocation() {
        return m_createSqlLocation;
    }

    /**
     * <p>getTableNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getTableNames() {
        return m_tables;
    }

    /**
     * <p>getSequenceNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getSequenceNames() {
        return m_sequences;
    }

    /**
     * <p>getSequenceMapping</p>
     *
     * @param sequence a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getSequenceMapping(String sequence) {
        return m_seqmapping.get(sequence);
    }

    /**
     * <p>getIndexDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.db.IndexDao} object.
     */
    public IndexDao getIndexDao() {
        return m_indexDao;
    }

    /**
     * <p>getInserts</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, List<Insert>> getInserts() {
        return m_inserts;
    }

    /**
     * <p>getSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSql() {
        return m_sql;
    }

    /**
     * <p>hasTableChanged</p>
     *
     * @param table a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean hasTableChanged(String table) {
        return m_changed.contains(table);
    }

    /**
     * <p>tableChanged</p>
     *
     * @param table a {@link java.lang.String} object.
     */
    public void tableChanged(String table) {
        m_changed.add(table);
    }

    /**
     * <p>setOutputStream</p>
     *
     * @param out a {@link java.io.PrintStream} object.
     */
    public void setOutputStream(PrintStream out) {
        m_out = out;
    }

    /**
     * <p>getTriggerDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.db.TriggerDao} object.
     */
    public TriggerDao getTriggerDao() {
        return m_triggerDao;
    }

    /**
     * <p>setStoredProcedureDirectory</p>
     *
     * @param directory a {@link java.lang.String} object.
     */
    public void setStoredProcedureDirectory(String directory) {
        m_storedProcedureDirectory = directory;
    }

    /**
     * <p>getStoredProcedureDirectory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStoredProcedureDirectory() {
        return m_storedProcedureDirectory;
    }

    /**
     * <p>setDataSource</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
     */
    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }
    
    /**
     * <p>getDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    public DataSource getDataSource() {
        return m_dataSource;
    }

    /**
     * <p>setAdminDataSource</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
     */
    public void setAdminDataSource(DataSource dataSource) {
        m_adminDataSource = dataSource;
    }
    
    /**
     * <p>getAdminDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    public DataSource getAdminDataSource() {
        return m_adminDataSource;
    }

    /**
     * <p>setForce</p>
     *
     * @param force a boolean.
     */
    public void setForce(boolean force) {
        m_force = force;
    }

    /**
     * <p>getForce</p>
     *
     * @return a boolean.
     */
    public boolean getForce() {
        return m_force;
    }

    /**
     * <p>setDebug</p>
     *
     * @param debug a boolean.
     */
    public void setDebug(boolean debug) {
        m_debug = debug;
    }
    
    /**
     * <p>getDebug</p>
     *
     * @return a boolean.
     */
    public boolean getDebug() {
        return m_debug;
    }

    /**
     * <p>addColumnReplacement</p>
     *
     * @param tableColumn a {@link java.lang.String} object.
     * @param replacement a {@link org.opennms.netmgt.dao.db.ColumnChangeReplacement} object.
     */
    public void addColumnReplacement(String tableColumn, ColumnChangeReplacement replacement) {
        m_columnReplacements.put(tableColumn, replacement);
    }

    /**
     * <p>setIgnoreNotNull</p>
     *
     * @param ignoreNotNull a boolean.
     */
    public void setIgnoreNotNull(boolean ignoreNotNull) {
        m_ignore_notnull = ignoreNotNull;
    }

    /**
     * <p>getDatabaseName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDatabaseName() {
        return m_databaseName;
    }

    /**
     * <p>setDatabaseName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setDatabaseName(String name) {
        m_databaseName = name;
    }

    /**
     * <p>setNoRevert</p>
     *
     * @param noRevert a boolean.
     */
    public void setNoRevert(boolean noRevert) {
        m_no_revert = noRevert;
    }

    /**
     * <p>setPostgresOpennmsUser</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public void setPostgresOpennmsUser(String user) {
        m_user = user;
    }

    /**
     * <p>getPostgresOpennmsUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPostgresOpennmsUser() {
        return m_user;
    }

    /**
     * <p>setPostgresOpennmsPassword</p>
     *
     * @param password a {@link java.lang.String} object.
     */
    public void setPostgresOpennmsPassword(String password) {
        m_pass = password;
    }

    /**
     * <p>getPostgresOpennmsPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPostgresOpennmsPassword() {
        return m_pass;
    }
    
    /**
     * <p>setPostgresIpLikeLocation</p>
     *
     * @param location a {@link java.lang.String} object.
     */
    public void setPostgresIpLikeLocation(String location) {
        if (location != null) {
            File iplike = new File(location);
            if (!iplike.exists()) {
                m_out.println("WARNING: missing " + location + ": OpenNMS will use a slower stored procedure if the native library is not available");
            }
        }

        m_pg_iplike = location;
    }

    /**
     * <p>getPgIpLikeLocation</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPgIpLikeLocation() {
        return m_pg_iplike;
    }

    /**
     * <p>setPostgresPlPgsqlLocation</p>
     *
     * @param location a {@link java.lang.String} object.
     */
    public void setPostgresPlPgsqlLocation(String location) {
        if (location != null) {
            File plpgsql = new File(location);
            if (!plpgsql.exists()) {
                m_out.println("FATAL: missing " + location + ": Unable to set up even the slower IPLIKE stored procedure without PL/PGSQL language support");
            }
        }
        
        m_pg_plpgsql = location;
    }

    /**
     * <p>getPgPlPgsqlLocation</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPgPlPgsqlLocation() {
        return m_pg_plpgsql;
    }
    
    /**
     * <p>isPgPlPgsqlLibPresent</p>
     *
     * @return a boolean.
     */
    public boolean isPgPlPgsqlLibPresent() {
        if (m_pg_plpgsql == null)
            return false;
        
        File plpgsqlLib = new File(m_pg_plpgsql);
        if (plpgsqlLib.exists() && plpgsqlLib.canRead())
            return true;
        
        return false;
    }
    
    /**
     * <p>addColumnReplacements</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public void addColumnReplacements() throws SQLException {
        /*
         * The DEFAULT value for these columns will take care of these
         * primary keys
         */
        addColumnReplacement("snmpinterface.id",
                                           new DoNotAddColumn());
        addColumnReplacement("ipinterface.id",
                                           new DoNotAddColumn());
        addColumnReplacement("ifservices.id",
                                           new DoNotAddColumn());
        addColumnReplacement("assets.id", new DoNotAddColumn());

        addColumnReplacement("atinterface.id", new DoNotAddColumn());

        // Triggers will take care of these surrogate foreign keys
        addColumnReplacement("ipinterface.snmpinterfaceid",
                                           new DoNotAddColumn());
        addColumnReplacement("ifservices.ipinterfaceid",
                                           new DoNotAddColumn());
        addColumnReplacement("outages.ifserviceid",
                                           new DoNotAddColumn());
        
        addColumnReplacement("events.eventsource",
                                           new EventSourceReplacement());
        
        addColumnReplacement("outages.outageid",
                                           new AutoInteger(1));
        
        addColumnReplacement("snmpinterface.nodeid",
                                           new RowHasBogusData("snmpInterface",
                                                               "nodeId"));
        
        addColumnReplacement("snmpinterface.snmpifindex",
                                           new RowHasBogusData("snmpInterface",
                                                               "snmpIfIndex"));

        addColumnReplacement("ipinterface.nodeid",
                                 new RowHasBogusData("ipInterface", "nodeId"));

        addColumnReplacement("ipinterface.ipaddr",
                                 new RowHasBogusData("ipInterface", "ipAddr"));

        addColumnReplacement("ifservices.nodeid",
                                 new RowHasBogusData("ifservices", "nodeId"));

        addColumnReplacement("ifservices.ipaddr",
                                 new RowHasBogusData("ifservices", "ipaddr"));

        addColumnReplacement("ifservices.serviceid",
                                 new RowHasBogusData("ifservices",
                                                     "serviceId"));

        addColumnReplacement("outages.nodeid",
                                 new RowHasBogusData("outages", "nodeId"));
        
        addColumnReplacement("outages.serviceid",
                                 new RowHasBogusData("outages", "serviceId"));
        
        /*
         * This is totally bogus.  outages.svcregainedeventid is a foreign
         * key that points at events.eventid, and a fixed replacement of zero
         * will break, because there should never be an event with an ID of
         * zero.  I don't think it ever got executed before due to the
         * null replacement only being performed if a column was marked as
         * NOT NULL.
         */
        /*
        m_columnReplacements.put("outages.svcregainedeventid",
                                 new FixedIntegerReplacement(0));
                                 */
        
        // Disabled for the same reason as above
        /*
        m_columnReplacements.put("notifications.eventid",
                                 new FixedIntegerReplacement(0));
                                 */
        
        addColumnReplacement("usersnotified.id",
                                 new NextValReplacement("userNotifNxtId",
                                                        getDataSource()));
        
        addColumnReplacement("alarms.x733probablecause", new FixedIntegerReplacement(0));
        
    }
    
    /**
     * <p>closeColumnReplacements</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public void closeColumnReplacements() throws SQLException {
        for (ColumnChangeReplacement r : m_columnReplacements.values()) {
            r.close();
        }
    }
    
    
    public class AutoInteger implements ColumnChangeReplacement {
        private int m_value;
        
        public AutoInteger(int initialValue) {
            m_value = initialValue;
        }
        
        public int getInt() {
            return m_value++;
        }
        
        public Integer getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) {
            return getInt();
        }

        public boolean addColumnIfColumnIsNew() {
            return true;
        }
        
        public void close() {
        }
    }
    
    public class AutoIntegerIdMapStore implements ColumnChangeReplacement {
        private int m_value;
        private String[] m_indexColumns;
        private Map<MultiColumnKey, Integer> m_idMap =
            new HashMap<MultiColumnKey, Integer>();
        
        public AutoIntegerIdMapStore(int initialValue, String[] indexColumns) {
            m_value = initialValue;
            m_indexColumns = indexColumns;
        }
        
        public Integer getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            MultiColumnKey key = getKeyForColumns(rs, columnChanges, m_indexColumns);
            Integer newInteger = m_value++;
            m_idMap.put(key, newInteger);
            return newInteger;
        }
        
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
        
        public Integer getIntegerForColumns(ResultSet rs, Map<String, ColumnChange> columnChanges, String[] columns, boolean noMatchOkay) throws SQLException {
            MultiColumnKey key = getKeyForColumns(rs, columnChanges, columns);

            Integer oldInteger = m_idMap.get(key);
            Assert.isTrue(oldInteger != null || noMatchOkay, "No entry in the map for " + key);
            
            return oldInteger;
        }
        
        private MultiColumnKey getKeyForColumns(ResultSet rs, Map<String, ColumnChange> columnChanges, String[] columns) throws SQLException {
            Object[] objects = new Object[columns.length];
            for (int i = 0; i < columns.length; i++) { 
                String indexColumn = columns[i];
                
                ColumnChange columnChange = columnChanges.get(indexColumn);
                Assert.notNull(columnChange, "No ColumnChange entry for '" + indexColumn + "'");
                
                int index = columnChange.getSelectIndex();
                Assert.isTrue(index > 0, "ColumnChange entry for '" + indexColumn + "' has no select index");
                
                objects[i] = rs.getObject(index);
            }

            return new MultiColumnKey(objects);
        }
        
        public class MultiColumnKey {
            private Object[] m_keys;
            
            public MultiColumnKey(Object[] keys) {
                m_keys = keys;
            }
            
            @Override
            public boolean equals(Object otherObject) {
                if (!(otherObject instanceof MultiColumnKey)) {
                    return false;
                }
                MultiColumnKey other = (MultiColumnKey) otherObject;
                
                if (m_keys.length != other.m_keys.length) {
                    return false;
                }
                
                for (int i = 0; i < m_keys.length; i++) {
                    if (m_keys[i] == null && other.m_keys[i] == null) {
                        continue;
                    }
                    if (m_keys[i] == null || other.m_keys[i] == null) {
                        return false;
                    }
                    if (!m_keys[i].equals(other.m_keys[i])) {
                        return false;
                    }
                }
                
                return true;
            }
            
            @Override
            public String toString() {
                return StringUtils.arrayToDelimitedString(m_keys, ", ");
            }
            
            @Override
            public int hashCode() {
                int value = 1;
                for (Object o : m_keys) {
                    if (o != null) {
                        // not the other way around, since 1 ^ anything == 1
                        value = o.hashCode() ^ value;
                    }
                }
                return value;
            }
        }
        
        public void close() {
        }
    }
    
    public class MapStoreIdGetter implements ColumnChangeReplacement {
        private AutoIntegerIdMapStore m_storeFoo;
        private String[] m_indexColumns;
        private boolean m_noMatchOkay;
        
        public MapStoreIdGetter(AutoIntegerIdMapStore storeFoo,
                String[] columns, boolean noMatchOkay) {
            m_storeFoo = storeFoo;
            m_indexColumns = columns;
            m_noMatchOkay = noMatchOkay;
        }

        public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            return m_storeFoo.getIntegerForColumns(rs, columnChanges, m_indexColumns, m_noMatchOkay);
        }
        
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
        
        public void close() {
        }
    }
    
    public class EventSourceReplacement implements ColumnChangeReplacement {
        private static final String m_replacement = "OpenNMS.Eventd";
        
        public EventSourceReplacement() {
            // we do nothing!
        }

        public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            return m_replacement;
        }
        
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
        
        public void close() {
        }
    }
    
    public class FixedIntegerReplacement implements ColumnChangeReplacement {
        private Integer m_replacement;
        
        public FixedIntegerReplacement(int value) {
            m_replacement = value;
        }

        public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            return m_replacement;
        }
        
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
        
        public void close() {
        }
    }
    
    public class RowHasBogusData implements ColumnChangeReplacement {
        private String m_table;
        private String m_column;
        
        public RowHasBogusData(String table, String column) {
            m_table = table;
            m_column = column;
        }
        
        public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            throw new IllegalArgumentException("The '" + m_column
                                               + "' column in the '"
                                               + m_table
                                               + "' table should never be "
                                               + "null, but the entry for this "
                                               + "row does have a null '"
                                               + m_column + "' column.  "
                                               + "It needs to be "
                                               + "removed or udpated to "
                                               + "reflect a valid '"
                                               + m_column + "' value.");
        }

        public boolean addColumnIfColumnIsNew() {
            return true;
        }
        
        public void close() {
        }
    }
    
    public class NullReplacement implements ColumnChangeReplacement {
        public NullReplacement() {
            // do nothing
        }
        
        public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            return null;
        }
        
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
        
        public void close() {
        }
    }
    
    public class DoNotAddColumn implements ColumnChangeReplacement {
        public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            return null;
        }

        public boolean addColumnIfColumnIsNew() {
            return false;
        }

        
        public void close() {
        }
    }
    
    public class NextValReplacement implements ColumnChangeReplacement {
        private String m_sequence;
        
        private Connection m_connection;
        private PreparedStatement m_statement;
        
        public NextValReplacement(String sequence, DataSource dataSource) throws SQLException {
            m_sequence = sequence;
//            m_dataSource = dataSource;
            m_connection = dataSource.getConnection();
            m_statement = m_connection.prepareStatement("SELECT nextval('"
                                                        + m_sequence
                                                        + "')");
        }
        
        private PreparedStatement getStatement() {
            /*
            if (m_statement == null) {
                createStatement();
            }
            */
            return m_statement;
        }

        /*
        private void createStatement() throws SQLException {
            m_statement = getConnection().prepareStatement("SELECT nextval('" + m_sequence + "')");
        }
        
        private Connection getConnection() throws SQLException {
            if (m_connection == null) {
                createConnection();
            }
            
            return m_connection;
        }
        
        private void createConnection() throws SQLException {
            m_connection = m_dataSource.getConnection();
        }
        */

        public Integer getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            ResultSet r = getStatement().executeQuery();
            
            if (!r.next()) {
                r.close();
                throw new SQLException("Query for next value of sequence did not return any rows.");
            }
            
            int i = r.getInt(1);
            r.close();
            return i;
        }
        
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
        
        public void close() throws SQLException {
            finalize();
        }
        
        protected void finalize() throws SQLException {
            if (m_statement != null) {
                m_statement.close();
            }
            if (m_connection != null) {
                m_connection.close();
            }
        }
    }
    
    enum Status {
        OK,
        SKIPPED,
        EXISTS;
        
        Status combine(Status s) {
            if (this.ordinal() > s.ordinal()) {
                return this;
            } else {
                return s;
            }
        }
    }
    
    public class Insert {

        private String m_table;
        private String m_insertStatement;
        private String m_criteria;

        public Insert(String table, String line, String criteria) {
            m_table = table;
            m_insertStatement = line;
            m_criteria = criteria;
        }
        
        public String getTable() {
            return m_table;
        }

        public String getCriteria() {
            return m_criteria;
        }

        public String getInsertStatement() {
            return m_insertStatement;
        }

        Status execute() throws SQLException {
            if (isCriteriaMet()) {
                return doInsert();
            } else {
                return Status.SKIPPED;
            }
        }

        private boolean isCriteriaMet() throws SQLException {
            if (getCriteria() == null) {
                return true;
            }
            Statement st = null;
            try {
                st = getConnection().createStatement();
                ResultSet rs = null;
                try {
                    rs = st.executeQuery(getCriteria());
                    // if we find a row the first column must be 't'
                    if (rs.next()) {
                        return rs.getBoolean(1);
                    }
                    // other wise return false
                    return false;
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
            } finally {
                if (st != null) {
                    st.close();
                }
            }
        }

        private Status doInsert() throws SQLException {
            Statement st = null;
            try {
                st = getConnection().createStatement();
                st.execute(getInsertStatement());
            } catch (SQLException e) {
                /*
                 * SQL Status codes: 23505: ERROR: duplicate key violates
                 * unique constraint "%s"
                 */
                if (e.toString().indexOf("duplicate key") != -1
                        || "23505".equals(e.getSQLState())) {
                    return Status.EXISTS;
                } else {
                    throw e;
                }
            } finally {
                if (st != null) {
                    st.close();
                }
            }
            return Status.OK;
        }
        


    }

    /**
     * <p>vacuumDatabase</p>
     *
     * @param full a boolean.
     * @throws java.sql.SQLException if any.
     */
    public void vacuumDatabase(boolean full) throws SQLException {
        Statement st = getConnection().createStatement();
        m_out.print("- optimizing database (VACUUM ANALYZE)... ");
        st.execute("VACUUM ANALYZE");
        m_out.println("OK");
        
        if (full) {
            m_out.print("- recovering database disk space (VACUUM FULL)... ");
            st.execute("VACUUM FULL");
            m_out.println("OK");
        }
    }
}
