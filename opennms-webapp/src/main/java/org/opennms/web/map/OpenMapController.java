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
package org.opennms.web.map;

/*
 * Created on 8-giu-2005
 *
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;

import org.opennms.core.utils.ThreadCategory;

import org.opennms.web.WebSecurityUtils;
import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.config.MapStartUpConfig;
import org.opennms.web.map.view.*;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


/**
 * <p>OpenMapController class.</p>
 *
 * @author mmigliore
 *
 * this class provides to create, manage and delete
 * proper session objects to use when working with maps
 * @version $Id: $
 * @since 1.6.12
 */
public class OpenMapController implements Controller {
	Category log;

	private Manager manager;
	
	
	/**
	 * <p>Getter for the field <code>manager</code>.</p>
	 *
	 * @return a {@link org.opennms.web.map.view.Manager} object.
	 */
	public Manager getManager() {
		return manager;
	}

	/**
	 * <p>Setter for the field <code>manager</code>.</p>
	 *
	 * @param manager a {@link org.opennms.web.map.view.Manager} object.
	 */
	public void setManager(Manager manager) {
		this.manager = manager;
	}

	/** {@inheritDoc} */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		String mapIdStr = request.getParameter("MapId");
		
		
		String user = request.getRemoteUser();
		
		String role = MapsConstants.ROLE_USER;

		if ((manager.isUserAdmin())) {
			role=MapsConstants.ROLE_ADMIN;
			log.info("Admin mode");
		}					

		float widthFactor = 1;
		float heightFactor =1;
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream()));

		try {
			MapStartUpConfig config = null;
			config=manager.getMapStartUpConfig();
			int mapWidth = config.getScreenWidth();
			int mapHeight = config.getScreenHeight();

			log.debug("Current mapWidth=" + mapWidth
						+ " and MapHeight=" + mapHeight);
			VMap map = null;
			if(mapIdStr!=null){
				int mapid = WebSecurityUtils.safeParseInt(mapIdStr);
				log.debug("Opening map "+mapid+" for user "+user);
				map = manager.openMap(mapid, user, !manager.isAdminMode());
			}else{
				log.debug("Opening map without passing mapid");
				map = manager.openMap();
			}
			 

			if(map != null){
				int oldMapWidth = map.getWidth();
				int oldMapHeight = map.getHeight();
				widthFactor = (float) mapWidth / oldMapWidth;
				heightFactor = (float) mapHeight / oldMapHeight;

				log.debug("Old saved mapWidth=" + oldMapWidth
						+ " and MapHeight=" + oldMapHeight);
				log.debug("widthFactor=" + widthFactor);
				log.debug("heightFactor=" + heightFactor);
				log.debug("Setting new width and height to the session map");
				
				map.setHeight(mapHeight);
				map.setWidth(mapWidth);
				map.setAccessMode(role);
				
			}
			
			bw.write(ResponseAssembler.getMapResponse(MapsConstants.OPENMAP_ACTION, map,widthFactor,heightFactor,true));

		} catch (Exception e) {
			log.error("Error while opening map with id:"+mapIdStr+", for user:"+user+", and role:"+role,e);
			bw.write(ResponseAssembler.getMapErrorResponse(MapsConstants.OPENMAP_ACTION));
		} finally {
			bw.close();
		}

		return null;
	}

}
