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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao;

import java.io.Serializable;
import java.util.List;

import org.opennms.netmgt.model.OnmsCriteria;

/**
 * <p>OnmsDao interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface OnmsDao<T, K extends Serializable> {

    /**
     * <p>initialize</p>
     *
     * @param obj a {@link java.lang.Object} object.
     * @param <T> a T object.
     * @param <K> a K object.
     */
    public abstract void initialize(Object obj);

    /**
     * <p>flush</p>
     */
    public abstract void flush();

    /**
     * <p>clear</p>
     */
    public abstract void clear();

    /**
     * <p>countAll</p>
     *
     * @return a int.
     */
    public abstract int countAll();

    /**
     * <p>delete</p>
     *
     * @param entity a T object.
     */
    public abstract void delete(T entity);

    /**
     * <p>findAll</p>
     *
     * @return a {@link java.util.List} object.
     */
    public abstract List<T> findAll();
    
    /**
     * <p>findMatching</p>
     *
     * @param criteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @return a {@link java.util.List} object.
     */
    public abstract List<T> findMatching(OnmsCriteria criteria);

    /**
     * <p>get</p>
     *
     * @param id a K object.
     * @return a T object.
     */
    public abstract T get(K id);

    /**
     * <p>load</p>
     *
     * @param id a K object.
     * @return a T object.
     */
    public abstract T load(K id);

    /**
     * <p>save</p>
     *
     * @param entity a T object.
     */
    public abstract void save(T entity);

    /**
     * <p>saveOrUpdate</p>
     *
     * @param entity a T object.
     */
    public abstract void saveOrUpdate(T entity);

    /**
     * <p>update</p>
     *
     * @param entity a T object.
     */
    public abstract void update(T entity);

}
