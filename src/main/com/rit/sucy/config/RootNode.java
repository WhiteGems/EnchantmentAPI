/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rit.sucy.config;

import com.rit.sucy.service.ConfigNode;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;

/**
* Configuration options of the root config.yml file.
*/
//The class is here so that if we need to add configuration we have it
public enum RootNode implements ConfigNode
{
    /**
     * Is unbreaking on swords legitimately obtainable through the Enchanting Table
     */
    //UNBR_ON_SWORDS  ("Unbreaking on swords", VarType.BOOLEAN, false),
    /**
     * Is unbreaking on hoes legitimately obtainable through the Enchanting Table
     */
    //UNBR_ON_HOES    ("Unbreaking on Hoes", VarType.BOOLEAN, false)
    ;
    /**
     * Path.
     */
    private final String path;
    /**
     * Variable type.
     */
    private final VarType type;
    /**
     * Subtype like percentage, y-value, health
     */
    private SubType subType = null;
    /**
     * Default value.
     */
    private final Object defaultValue;

    /**
     * Constructor.
     *
     * @param path - Configuration path.
     * @param type - Variable type.
     * @param def  - Default value.
     */
    private RootNode(String path, VarType type, Object def)
    {
        this.path = path;
        this.type = type;
        this.defaultValue = def;
    }

    private RootNode(String path, VarType type, SubType subType, Object def)
    {
        this.path = path;
        this.type = type;
        this.defaultValue = def;
        this.subType = subType;
    }

    @Override
    public String getPath()
    {
        return RootConfig.baseNode + getNode() + path;
    }

    @Override
    public VarType getVarType()
    {
        return type;
    }

    @Override
    public Object getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public SubType getSubType()
    {
        return subType;
    }

    public static String getNode()
    {
        return "Settings.";
    }
}
