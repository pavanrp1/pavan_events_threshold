package org.opennms.core.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Category;

/**
 * <p>DBUtils class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DBUtils {
    private final Set<Statement> m_statements;
    private final Set<ResultSet> m_resultSets;
    private final Set<Connection> m_connections;
    private Class<?> m_loggingClass;

    /**
     * <p>Constructor for DBUtils.</p>
     */
    public DBUtils() {
        this(DBUtils.class);
    }
    
    /**
     * <p>Constructor for DBUtils.</p>
     *
     * @param loggingClass a {@link java.lang.Class} object.
     */
    public DBUtils(Class<?> loggingClass) {
        m_statements = Collections.synchronizedSet(new HashSet<Statement>());
        m_resultSets = Collections.synchronizedSet(new HashSet<ResultSet>());
        m_connections = Collections.synchronizedSet(new HashSet<Connection>());
        m_loggingClass = loggingClass;
    }

    /**
     * <p>setLoggingClass</p>
     *
     * @param c a {@link java.lang.Class} object.
     * @return a {@link org.opennms.core.utils.DBUtils} object.
     */
    public DBUtils setLoggingClass(Class<?> c) {
        m_loggingClass = c;
        return this;
    }

    /**
     * <p>watch</p>
     *
     * @param o a {@link java.lang.Object} object.
     * @return a {@link org.opennms.core.utils.DBUtils} object.
     */
    public DBUtils watch(Object o) {
        if (o instanceof Statement) {
            m_statements.add((Statement)o);
        } else if (o instanceof ResultSet) {
            m_resultSets.add((ResultSet)o);
        } else if (o instanceof Connection) {
            m_connections.add((Connection)o);
        }
        return this;
    }

    /**
     * <p>cleanUp</p>
     */
    public void cleanUp() {
        for (ResultSet rs : m_resultSets) {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    log().warn("Unable to close result set", e);
                }
            }
        }
        m_resultSets.clear();
        
        for (Statement s : m_statements) {
            if (s != null) {
                try {
                    s.close();
                } catch (Exception e) {
                    log().warn("Unable to close statement", e);
                }
            }
        }
        m_statements.clear();
        
        for (Connection c : m_connections) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    log().warn("Unable to close connection", e);
                }
            }
        }
        m_connections.clear();
    }
    
    /**
     * <p>log</p>
     *
     * @return a {@link org.apache.log4j.Category} object.
     */
    public Category log() {
        return ThreadCategory.getInstance(m_loggingClass);
    }
}
