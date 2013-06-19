package com.rit.sucy.service;

import org.bukkit.Material;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Tests for MaterialsParser
 * @author Diemex
 */
public class MaterialsParserTest
{
    /**
     * Test if simple enum values get processed correctly
     */
    @Test
    public void toMaterial()
    {
        assertArrayEquals(new Material[]{Material.DIAMOND_SWORD, Material.DIAMOND}, MaterialsParser.toMaterial(new String[]{Material.DIAMOND_SWORD.name(), Material.DIAMOND.name()}));
    }

    /**
     * Test if a simple Material array gets converted correctly
     */
    @Test
    public void toStringList()
    {
        assertArrayEquals(new String[] {Material.BEDROCK.name(), Material.ANVIL.name()}, MaterialsParser.toStringArray(new Material[]{Material.BEDROCK, Material.ANVIL}));
    }

    /**
     * Test that no error is thrown when invalid input is inserted
     */
    @Test
    public void invalid_toMaterial()
    {
        assertArrayEquals(new Material[]{}, MaterialsParser.toMaterial(new String[] {"bogus", "dogus:::", "@€9())/(7", "I'm stupid", "noooo000b"}));
    }

    /**
     * Should still parse if whitespace is inserted
     */
    @Test
    public void whitespace_toMaterial()
    {
        assertArrayEquals(new Material[]{Material.MAP}, MaterialsParser.toMaterial(new String[]{" M a p "}));
    }

    /**
     * Still parse if there are stray numbers
     */
    @Test
    public void numbers_toMaterial()
    {
        assertArrayEquals(new Material[]{Material.BONE}, MaterialsParser.toMaterial(new String[] {"b1o2n3e"}));
    }

    /**
     * Recognize numbers and convert to Materials
     */
    @Test
    public void blockid_toMaterial()
    {
        assertArrayEquals(new Material[]{Material.STONE_PICKAXE}, MaterialsParser.toMaterial(new String[]{"" + Material.STONE_PICKAXE.getId()}));
    }
}