package com.gymfuel.app.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalSeedFoodsTest {
    @Test
    fun seedLibrary_containsEightFoodsWithUniqueStableIds() {
        val foods = LocalSeedFoods.all

        assertEquals(8, foods.size)
        assertEquals(foods.size, foods.map { it.id }.distinct().size)
        assertTrue(foods.all { it.sourceTemplateId == it.id })
    }

    @Test
    fun generatedImages_areAssignedToMatchingFoods() {
        val foodsByName = LocalSeedFoods.all.associateBy { it.name }

        assertNotNull(foodsByName.getValue("Chicken breast").imageReference)
        assertNotNull(foodsByName.getValue("White rice").imageReference)
        assertNotNull(foodsByName.getValue("Rolled oats").imageReference)
    }
}
