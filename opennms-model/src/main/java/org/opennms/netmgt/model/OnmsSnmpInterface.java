//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Category;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.springframework.core.style.ToStringCreator;

@Entity
/**
 * <p>OnmsSnmpInterface class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Table(name = "snmpInterface")
public class OnmsSnmpInterface extends OnmsEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5396189389666285305L;

    private Integer m_id;

    /** identifier field */
    private String m_ipAddr;

    /** identifier field */
    private String m_netMask;

    /** identifier field */
    private String m_physAddr;

    /** identifier field */
    private Integer m_ifIndex;

    /** identifier field */
    private String m_ifDescr;

    /** identifier field */
    private Integer m_ifType;

    /** identifier field */
    private String m_ifName;

    /** identifier field */
    private Long m_ifSpeed;

    /** identifier field */
    private Integer m_ifAdminStatus;

    /** identifier field */
    private Integer m_ifOperStatus;

    /** identifier field */
    private String m_ifAlias;

    private OnmsNode m_node;

    private Set<OnmsIpInterface> m_ipInterfaces = new HashSet<OnmsIpInterface>();

    /**
     * <p>Constructor for OnmsSnmpInterface.</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @param ifIndex a int.
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public OnmsSnmpInterface(String ipAddr, int ifIndex, OnmsNode node) {
        this(ipAddr, new Integer(ifIndex), node);
    }

    /**
     * <p>Constructor for OnmsSnmpInterface.</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     * @param ifIndex a {@link java.lang.Integer} object.
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public OnmsSnmpInterface(String ipaddr, Integer ifIndex, OnmsNode node) {
        m_ipAddr = ipaddr;
        m_ifIndex = ifIndex;
        m_node = node;
        node.getSnmpInterfaces().add(this);
    }

    /**
     * default constructor
     */
    public OnmsSnmpInterface() {
    }

    /**
     * Unique identifier for snmpInterface.
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
    }

    /*
     * TODO this doesn't belong on SnmpInterface
     */
    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "ipAddr", length = 16)
    public String getIpAddress() {
        return m_ipAddr;
    }

    /**
     * <p>setIpAddress</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     */
    public void setIpAddress(String ipaddr) {
        m_ipAddr = ipaddr;
    }

    /*
     * TODO this doesn't belong on SnmpInterface
     */
    /**
     * <p>getNetMask</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "snmpIpAdEntNetMask", length = 16)
    public String getNetMask() {
        return m_netMask;
    }

    /**
     * <p>setNetMask</p>
     *
     * @param snmpipadentnetmask a {@link java.lang.String} object.
     */
    public void setNetMask(String snmpipadentnetmask) {
        m_netMask = snmpipadentnetmask;
    }

    /**
     * <p>getPhysAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "snmpPhysAddr", length = 32)
    public String getPhysAddr() {
        return m_physAddr;
    }

    /**
     * <p>setPhysAddr</p>
     *
     * @param snmpphysaddr a {@link java.lang.String} object.
     */
    public void setPhysAddr(String snmpphysaddr) {
        m_physAddr = snmpphysaddr;
    }

    /**
     * <p>getIfIndex</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name = "snmpIfIndex")
    public Integer getIfIndex() {
        return m_ifIndex;
    }

    /**
     * <p>setIfIndex</p>
     *
     * @param snmpifindex a {@link java.lang.Integer} object.
     */
    public void setIfIndex(Integer snmpifindex) {
        m_ifIndex = snmpifindex;
    }

    /**
     * <p>getIfDescr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "snmpIfDescr", length = 256)
    public String getIfDescr() {
        return m_ifDescr;
    }

    /**
     * <p>setIfDescr</p>
     *
     * @param snmpifdescr a {@link java.lang.String} object.
     */
    public void setIfDescr(String snmpifdescr) {
        m_ifDescr = snmpifdescr;
    }

    /**
     * <p>getIfType</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name = "snmpIfType")
    public Integer getIfType() {
        return m_ifType;
    }

    /**
     * <p>setIfType</p>
     *
     * @param snmpiftype a {@link java.lang.Integer} object.
     */
    public void setIfType(Integer snmpiftype) {
        m_ifType = snmpiftype;
    }

    /**
     * <p>getIfName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "snmpIfName", length = 32)
    public String getIfName() {
        return m_ifName;
    }

    /**
     * <p>setIfName</p>
     *
     * @param snmpifname a {@link java.lang.String} object.
     */
    public void setIfName(String snmpifname) {
        m_ifName = snmpifname;
    }

    /**
     * <p>getIfSpeed</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    @Column(name = "snmpIfSpeed")
    public Long getIfSpeed() {
        return m_ifSpeed;
    }

    /**
     * <p>setIfSpeed</p>
     *
     * @param snmpifspeed a {@link java.lang.Long} object.
     */
    public void setIfSpeed(Long snmpifspeed) {
        m_ifSpeed = snmpifspeed;
    }

    /**
     * <p>getIfAdminStatus</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name = "snmpIfAdminStatus")
    public Integer getIfAdminStatus() {
        return m_ifAdminStatus;
    }

    /**
     * <p>setIfAdminStatus</p>
     *
     * @param snmpifadminstatus a {@link java.lang.Integer} object.
     */
    public void setIfAdminStatus(Integer snmpifadminstatus) {
        m_ifAdminStatus = snmpifadminstatus;
    }

    /**
     * <p>getIfOperStatus</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name = "snmpIfOperStatus")
    public Integer getIfOperStatus() {
        return m_ifOperStatus;
    }

    /**
     * <p>setIfOperStatus</p>
     *
     * @param snmpifoperstatus a {@link java.lang.Integer} object.
     */
    public void setIfOperStatus(Integer snmpifoperstatus) {
        m_ifOperStatus = snmpifoperstatus;
    }

    /**
     * <p>getIfAlias</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "snmpIfAlias", length = 256)
    public String getIfAlias() {
        return m_ifAlias;
    }

    /**
     * <p>setIfAlias</p>
     *
     * @param snmpifalias a {@link java.lang.String} object.
     */
    public void setIfAlias(String snmpifalias) {
        m_ifAlias = snmpifalias;
    }

    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeId")
    public OnmsNode getNode() {
        return m_node;
    }

    /**
     * <p>setNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(OnmsNode node) {
        m_node = node;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return new ToStringCreator(this)
            .append("ipaddr", getIpAddress())
            .append("snmpipadentnetmask", getNetMask())
            .append("snmpphysaddr", getPhysAddr())
            .append("snmpifindex", getIfIndex())
            .append("snmpifdescr", getIfDescr())
            .append("snmpiftype", getIfType())
            .append("snmpifname", getIfName())
            .append("snmpifspeed", getIfSpeed())
            .append("snmpifadminstatus", getIfAdminStatus())
            .append("snmpifoperstatus", getIfOperStatus())
            .append("snmpifalias", getIfAlias())
            .toString();
    }

    /** {@inheritDoc} */
    public void visit(EntityVisitor visitor) {
        visitor.visitSnmpInterface(this);
        visitor.visitSnmpInterfaceComplete(this);
    }

    /**
     * <p>getIpInterfaces</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @OneToMany(mappedBy = "snmpInterface", fetch = FetchType.LAZY)
    public Set<OnmsIpInterface> getIpInterfaces() {
        return m_ipInterfaces;
    }

    /**
     * <p>setIpInterfaces</p>
     *
     * @param ipInterfaces a {@link java.util.Set} object.
     */
    public void setIpInterfaces(Set<OnmsIpInterface> ipInterfaces) {
        m_ipInterfaces = ipInterfaces;
    }

    // @Transient
    // public Set getIpInterfaces() {
    //		
    // Set ifsForSnmpIface = new LinkedHashSet();
    // for (Iterator it = getNode().getIpInterfaces().iterator();
    // it.hasNext();) {
    // OnmsIpInterface iface = (OnmsIpInterface) it.next();
    // if (getIfIndex().equals(iface.getIfIndex()))
    // ifsForSnmpIface.add(iface);
    // }
    // return ifsForSnmpIface;
    // }

    /**
     * <p>getCollectionType</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface.CollectionType} object.
     */
    @Transient
    public CollectionType getCollectionType() {
        CollectionType maxCollType = CollectionType.NO_COLLECT;
        for (Iterator<OnmsIpInterface> it = getIpInterfaces().iterator(); it.hasNext();) {
            OnmsIpInterface ipIface = it.next();
            if (ipIface.getIsSnmpPrimary() != null) {
                maxCollType = maxCollType.max(ipIface.getIsSnmpPrimary());
            }
        }
        return maxCollType;
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.apache.log4j.Category} object.
     */
    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * <p>computePhysAddrForRRD</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String computePhysAddrForRRD() {
        /*
         * In order to assure the uniqueness of the RRD file names we now
         * append the MAC/physical address to the end of label if it is
         * available.
         */
        String physAddrForRRD = null;

        if (getPhysAddr() != null) {
            String parsedPhysAddr = AlphaNumeric.parseAndTrim(getPhysAddr());
            if (parsedPhysAddr.length() == 12) {
                physAddrForRRD = parsedPhysAddr;
            } else {
                if (log().isDebugEnabled()) {
                    log().debug(
                                "physAddrForRRD: physical address len "
                                        + "is NOT 12, physAddr="
                                        + parsedPhysAddr);
                }
            }
        }
        log().debug(
                    "computed physAddr for " + this + " to be "
                            + physAddrForRRD);
        return physAddrForRRD;
    }

    /**
     * <p>computeNameForRRD</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String computeNameForRRD() {
        /*
         * Determine the label for this interface. The label will be used to
         * create the RRD file name which holds SNMP data retreived from the
         * remote agent. If available ifName is used to generate the label
         * since it is guaranteed to be unique. Otherwise ifDescr is used. In
         * either case, all non alpha numeric characters are converted to
         * underscores to ensure that the resuling string will make a decent
         * file name and that RRD won't have any problems using it
         */
        String label = null;
        if (getIfName() != null) {
            label = AlphaNumeric.parseAndReplace(getIfName(), '_');
        } else if (getIfDescr() != null) {
            label = AlphaNumeric.parseAndReplace(getIfDescr(), '_');
        } else {
            log().info(
                       "Interface ("
                               + this
                               + ") has no ifName and no ifDescr...setting to label to 'no_ifLabel'.");
            label = "no_ifLabel";
        }
        return label;
    }

    /**
     * <p>computeLabelForRRD</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String computeLabelForRRD() {
        String name = computeNameForRRD();
        String physAddrForRRD = computePhysAddrForRRD();
        return (physAddrForRRD == null ? name : name + '-' + physAddrForRRD);
    }

    /**
     * <p>addIpInterface</p>
     *
     * @param iface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    public void addIpInterface(OnmsIpInterface iface) {
        m_ipInterfaces.add(iface);
    }

}
