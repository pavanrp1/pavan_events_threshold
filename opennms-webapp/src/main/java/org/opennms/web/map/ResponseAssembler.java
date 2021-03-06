/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 6, 2007
 *
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

/**
 * <p>ResponseAssembler class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.6.12
 */
package org.opennms.web.map;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;
import org.opennms.web.map.view.VMapInfo;
public class ResponseAssembler {
	private static Category log;
	
	/**
	 * <p>getRefreshResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @param map a {@link org.opennms.web.map.view.VMap} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getRefreshResponse(String action, VMap map){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		VElement[] velements = map.getAllElements();
		
		
		//checks for only changed velements 
		String response=getActionOKMapResponse(action);
		if (velements != null) {
			for(int k=0; k<velements.length;k++){
				VElement ve = velements[k];
				response += "&" + ve.getId() + ve.getType() + "+"
						+ ve.getIcon() + "+" + ve.getLabel();
				response += "+" + ve.getRtc() + "+"
						+ ve.getStatus() + "+" + ve.getSeverity()+ "+" + ve.getX()+ "+" + ve.getY();
			}
		}
		VLink[] links = map.getAllLinks();
		// construct string response considering links also
		if (links != null) {
			for(int k=0; k<links.length;k++){
				VLink vl = (VLink) links[k];
					response += "&" + vl.getFirst().getId()
					+ vl.getFirst().getType() + "+"
					+ vl.getSecond().getId()
					+ vl.getSecond().getType()+"+"+vl.getLinkTypeId()+"+"+vl.getLinkOperStatusString();
			}
		} 
		log.debug("getRefreshResponse: String assembled: "+response);
		return response;
	}
	
	/**
	 * <p>getLoadInfosResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @param infos a java$util$Map object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getLoadInfosResponse(String action, Map<String , String> infos){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		String str=getActionOKMapResponse(action);
		if(infos!=null){
			Iterator it = infos.keySet().iterator();
			while(it.hasNext()){
				String key = (String) it.next();
				str+="+"+key+": "+(String)infos.get(key);
			}
		}
		log.debug("getLoadInfosResponse: String assembled: "+str);
		return str;
	}
	
	/**
	 * <p>getAddMapsResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @param mapsWithLoopInfo a {@link java.util.List} object.
	 * @param velems a {@link java.util.List} object.
	 * @param links a {@link java.util.List} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getAddMapsResponse(String action,  List<Integer> mapsWithLoopInfo, List<VElement> velems, List<VLink> links){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		String response = getActionOKMapResponse(action);
		
		Iterator<Integer>  ite = mapsWithLoopInfo.iterator();
		while(ite.hasNext()){
			Integer entry = ite.next();
			//if loop is found
			response += "&loopfound" + entry;
			log.debug("found loop for map "+entry);
		}
		
		Iterator<VElement>it = velems.iterator();
		while(it.hasNext()){
			VElement ve = it.next();
			response += "&" + ve.getId() + ve.getType() + "+"
			+ ve.getIcon() + "+" + ve.getLabel();
			response += "+" + ve.getRtc() + "+"
			+ ve.getStatus() + "+" + ve.getSeverity();
		}
		
		// add String to return containing Links
		if (velems != null) {
			Iterator<VLink> sub_ite = links.iterator();
			while (sub_ite.hasNext()) {
				VLink vl = sub_ite.next();
				response += "&" + vl.getFirst().getId()
						+ vl.getFirst().getType() + "+"
						+ vl.getSecond().getId()
						+ vl.getSecond().getType()+"+"+vl.getLinkTypeId()+"+"+vl.getLinkOperStatusString();
			}
		}		
		log.debug("getAddMapsResponse: String assembled: "+response);
		return response;		
	}
	
	/**
	 * <p>getDeleteElementsResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @param velems a {@link java.util.List} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getDeleteElementsResponse(String action, List<VElement> velems){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		String response = getActionOKMapResponse(action);
		Iterator<VElement>it = velems.iterator();
		while(it.hasNext()){
			VElement ve = it.next();
			response += "&" + ve.getId() + ve.getType();
		}
		log.debug("getDeleteElementsResponse: String assembled: "+response);
		return response;
	}
	
	/**
	 * <p>getAddElementResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @param velems a {@link java.util.List} object.
	 * @param links a {@link java.util.List} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getAddElementResponse(String action, List<VElement> velems, List<VLink> links){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		String response = getActionOKMapResponse(action);
		Iterator<VElement>it = velems.iterator();
		while(it.hasNext()){
			VElement ve = it.next();
			response += "&" + ve.getId() + ve.getType() + "+"
			+ ve.getIcon() + "+" + ve.getLabel();
			response += "+" + ve.getRtc() + "+"
			+ ve.getStatus() + "+" + ve.getSeverity();
		}
		
		// add String to return containing Links
		if (velems != null) {
			Iterator<VLink> sub_ite = links.iterator();
			while (sub_ite.hasNext()) {
				VLink vl = sub_ite.next();
				response += "&" + vl.getFirst().getId()
						+ vl.getFirst().getType() + "+"
						+ vl.getSecond().getId()
						+ vl.getSecond().getType()+"+"+vl.getLinkTypeId()+"+"+vl.getLinkOperStatusString();
			}
		}		
		log.debug("getAddElementResponse: String assembled: "+response);
		return response;
	}

	
	/**
	 * <p>getLoadNodesResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @param elemInfos an array of {@link org.opennms.web.map.view.VElementInfo} objects.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getLoadNodesResponse(String action, VElementInfo[] elemInfos){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		String strToSend =getActionOKMapResponse(action);
		if(elemInfos!=null){
			for (int i = 0; i < elemInfos.length; i++) {
				VElementInfo n = elemInfos[i];
				if (i > 0) {
					strToSend += "&";
				}

				String nodeStr = n.getId() + "+" + n.getLabel();
				strToSend += nodeStr;
			}
		}
		log.debug("getLoadNodesResponse: String assembled: "+strToSend);
		return strToSend;
	}
	
	/**
	 * <p>getSaveMapResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @param map a {@link org.opennms.web.map.view.VMap} object.
	 * @param currPacket a {@link java.lang.String} object.
	 * @param totalPackets a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getSaveMapResponse(String action,VMap map, String currPacket, String totalPackets){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd/MM/yy");
	 String strToSend =getActionOKMapResponse(action)+map.getId()
		+ "+"
		+ map.getBackground()
		+ "+"
		+ map.getAccessMode()
		+ "+"
		+ map.getName()
		+ "+"
		+ map.getOwner()
		+ "+"
		+ map.getUserLastModifies()
		+ "+"
		+ ((map.getCreateTime() != null) ? formatter.format(map
				.getCreateTime()) : "")
		+ "+"
		+ ((map.getLastModifiedTime() != null) ? formatter
				.format(map.getLastModifiedTime()) : "") + "+"
		+ currPacket + "+" + totalPackets;
		log.debug("getSaveMapResponse: String assembled: "+strToSend);
	 return strToSend;
	}
	/**
	 * <p>getMapResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @param map a {@link org.opennms.web.map.view.VMap} object.
	 * @param widthFactor a float.
	 * @param heightFactor a float.
	 * @param sendElements a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getMapResponse(String action, VMap map, float widthFactor, float heightFactor,boolean sendElements) {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		
		String strToSend=null;
		String lastModTime = "";
		String createTime = "";
		String role = "";
				
		SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd/MM/yy");
				
		if(map!=null){
			strToSend = action + "OK";
			
			if (map.getCreateTime() != null)
				createTime = formatter.format(map.getCreateTime());
			
			if (map.getLastModifiedTime() != null)
				lastModTime = formatter.format(map
						.getLastModifiedTime());
			if (map.getAccessMode() != null)
				role = map.getAccessMode();

			strToSend += map.getId() + "+" + map.getBackground();
			strToSend +="+" + role + "+"
				+ map.getName() + "+" + map.getOwner() + "+"
				+ map.getUserLastModifies() + "+" + createTime
				+ "+" + lastModTime;
				
			VElement[] elems = map.getAllElements();
			if (elems != null) {
				for (int i = 0; i < elems.length; i++) {
					int x = (int) (elems[i].getX() * widthFactor);
					int y = (int) (elems[i].getY() * heightFactor);
	
					strToSend += "&" + elems[i].getId()
							+ elems[i].getType() + "+" + x + "+"
							+ y + "+" + elems[i].getIcon() + "+"
							+ elems[i].getLabel();
					strToSend += "+" + elems[i].getRtc() + "+"
							+ elems[i].getStatus() + "+"
							+ elems[i].getSeverity();
				}
			}
			
			VLink[] links = map.getAllLinks();
			if (links != null) {
				for (int i = 0; i < links.length; i++) {
					strToSend += "&" + links[i].getFirst().getId()
							+ links[i].getFirst().getType() + "+"
							+ links[i].getSecond().getId()
							+ links[i].getSecond().getType()+ "+"
							+links[i].getLinkTypeId() + "+"
							+links[i].getLinkOperStatusString();
				}
			}	
		} else {
			strToSend=action + "Failed";
		}
		log.debug("getMapResponse: String assembled: "+strToSend);
		return strToSend;
	}
	
	/**
	 * <p>getMapsResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @param maps a {@link java.util.List} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getMapsResponse(String action, List<VMapInfo> maps) {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		
		String strToSend=getActionOKMapResponse(action);
				
		if(maps!=null){
			// create the string containing the main informations about maps
			// the string will have the form:
			// mapid1+mapname1+mapowner1&mapid2+mapname2+mapowner2...
			for (int i = 0; i < maps.size(); i++) {
				if (i > 0) {
					strToSend += "&";
				}
				VMapInfo map = (VMapInfo) maps.get(i);
				strToSend += map.getId() + "+" + map.getName() + "+" + map.getOwner();
			}
		} else {
			strToSend=getMapErrorResponse(action);
		}
		log.debug("getMapsResponse: String assembled: "+strToSend);
		return strToSend;
	}
	
	/**
	 * <p>getCloseMapResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getCloseMapResponse(String action) {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		
		String strToSend = action+"OK";
		strToSend += MapsConstants.MAP_NOT_OPENED + "+" + MapsConstants.DEFAULT_BACKGROUND_COLOR;
		log.debug("getCloseMapResponse: String assembled: "+strToSend);
		return strToSend;
		
	}

	/**
	 * <p>getActionOKMapResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getActionOKMapResponse(String action) {

		return action+"OK";
		
	}

	/**
	 * <p>getMapErrorResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getMapErrorResponse(String action) {
		return action+"Failed";
	}


}
