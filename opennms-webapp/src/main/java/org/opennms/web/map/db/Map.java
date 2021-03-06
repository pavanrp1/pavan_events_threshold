/*
 * Created on 9-dic-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.db;

import java.sql.Timestamp;

// FIXME: We really need to rename this class so that it doesn't have the same class name as java.util.Map
/**
 * <p>Map class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class Map {

    private int id;

    private String name;

    private String background;

    private String owner;

    private String accessMode;

    private String userLastModifies;

    private Timestamp createTime;

    private Timestamp lastModifiedTime;

    private float scale;

    private int offsetX;

    private int offsetY;

    private String type;
    
    private int width;
    
    private int height;

    /** Constant <code>USER_GENERATED_MAP="U"</code> */
    public static final String USER_GENERATED_MAP = "U";

    /** Constant <code>AUTOMATICALLY_GENERATED_MAP="A"</code> */
    public static final String AUTOMATICALLY_GENERATED_MAP = "A";

    /** Constant <code>DELETED_MAP="D"</code> */
    public static final String DELETED_MAP = "D"; //for future use

    private boolean isNew = false;

    /**
     * <p>Constructor for Map.</p>
     */
    public Map() {
        this.isNew = true;
    }
    
    /**
     * <p>Constructor for Map.</p>
     *
     * @param id a int.
     * @param name a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     */
    public Map(int id, String name, String owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }
  
    
    /**
     * <p>Constructor for Map.</p>
     *
     * @param id a int.
     * @param name a {@link java.lang.String} object.
     * @param background a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     * @param accessMode a {@link java.lang.String} object.
     * @param userLastModifies a {@link java.lang.String} object.
     * @param scale a float.
     * @param offsetX a int.
     * @param offsetY a int.
     * @param type a {@link java.lang.String} object.
     * @param width a int.
     * @param height a int.
     */
    public Map(int id, String name, String background, String owner,
            String accessMode, String userLastModifies, float scale,
            int offsetX, int offsetY, String type, int width, int height) {
        this.id = id;
        this.name = name;
        this.background = background;
        this.owner = owner;
        this.accessMode = accessMode;
        this.userLastModifies = userLastModifies;
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.type = type;
        this.width=width;
        this.height=height;
    }

    /**
     * <p>Getter for the field <code>accessMode</code>.</p>
     *
     * @return Returns the accessMode.
     */
    public String getAccessMode() {
        return accessMode;
    }

    /**
     * <p>Setter for the field <code>accessMode</code>.</p>
     *
     * @param accessMode
     *            The accessMode to set.
     */
    public void setAccessMode(String accessMode) {
        this.accessMode = accessMode;
    }

    /**
     * <p>Getter for the field <code>background</code>.</p>
     *
     * @return Returns the background.
     */
    public String getBackground() {
        return background;
    }

    /**
     * <p>Setter for the field <code>background</code>.</p>
     *
     * @param background
     *            The background to set.
     */
    public void setBackground(String background) {
        this.background = background;
    }

    /**
     * <p>Getter for the field <code>createTime</code>.</p>
     *
     * @return Returns the createTime.
     */
    public Timestamp getCreateTime() {
        return createTime;
    }

    /**
     * <p>Setter for the field <code>createTime</code>.</p>
     *
     * @param createTime
     *            The createTime to set.
     */
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    /**
     * <p>Getter for the field <code>lastModifiedTime</code>.</p>
     *
     * @return Returns the lastModifiedTime.
     */
    public Timestamp getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * <p>Setter for the field <code>lastModifiedTime</code>.</p>
     *
     * @param lastModifiedTime
     *            The lastModifiedTime to set.
     */
    public void setLastModifiedTime(Timestamp lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * <p>Getter for the field <code>name</code>.</p>
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * <p>Setter for the field <code>name</code>.</p>
     *
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p>Getter for the field <code>offsetX</code>.</p>
     *
     * @return Returns the offsetX.
     */
    public int getOffsetX() {
        return offsetX;
    }

    /**
     * <p>Setter for the field <code>offsetX</code>.</p>
     *
     * @param offsetX
     *            The offsetX to set.
     */
    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * <p>Getter for the field <code>offsetY</code>.</p>
     *
     * @return Returns the offsetY.
     */
    public int getOffsetY() {
        return offsetY;
    }

    /**
     * <p>Setter for the field <code>offsetY</code>.</p>
     *
     * @param offsetY
     *            The offsetY to set.
     */
    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * <p>Getter for the field <code>owner</code>.</p>
     *
     * @return Returns the owner.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * <p>Setter for the field <code>owner</code>.</p>
     *
     * @param owner
     *            The owner to set.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * <p>Getter for the field <code>scale</code>.</p>
     *
     * @return Returns the scale.
     */
    public float getScale() {
        return scale;
    }

    /**
     * <p>Setter for the field <code>scale</code>.</p>
     *
     * @param scale
     *            The scale to set.
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * <p>Getter for the field <code>userLastModifies</code>.</p>
     *
     * @return Returns the userLastModifies.
     */
    public String getUserLastModifies() {
        return userLastModifies;
    }

    /**
     * <p>Setter for the field <code>userLastModifies</code>.</p>
     *
     * @param userLastModifies
     *            The userLastModifies to set.
     */
    public void setUserLastModifies(String userLastModifies) {
        this.userLastModifies = userLastModifies;
    }

    /**
     * <p>Getter for the field <code>type</code>.</p>
     *
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * <p>Setter for the field <code>type</code>.</p>
     *
     * @param type
     *            The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return a int.
     */
    public int getId() {
        return id;
    }

    /**
     * <p>Setter for the field <code>id</code>.</p>
     *
     * @param id a int.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * <p>isNew</p>
     *
     * @return a boolean.
     */
    public boolean isNew() {
        return this.isNew;
    }

    /**
     * <p>setAsNew</p>
     *
     * @param v a boolean.
     */
    public void setAsNew(boolean v) {
        this.isNew = v;
    }
	/**
	 * <p>Getter for the field <code>height</code>.</p>
	 *
	 * @return a int.
	 */
	public int getHeight() {
		return height;
	}
	/**
	 * <p>Setter for the field <code>height</code>.</p>
	 *
	 * @param height a int.
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	/**
	 * <p>Getter for the field <code>width</code>.</p>
	 *
	 * @return a int.
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * <p>Setter for the field <code>width</code>.</p>
	 *
	 * @param width a int.
	 */
	public void setWidth(int width) {
		this.width = width;
	}
}
