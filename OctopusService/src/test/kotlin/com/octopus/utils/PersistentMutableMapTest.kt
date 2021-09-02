package com.octopus.utils

import org.apache.commons.io.FileUtils
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class PersistentMutableMapTest {
    private val filePath = "src/test/resources/store/test_map_1"

    @Test
    fun testAllInOrder() {
        testPersistentMutableMapCreationWithoutPreExistingFile()
        testPersistentMutableMapCreationWithPreExistingEmptyFile()
        testPersistentMutableMapCreationWithPreExistingBadFile()
        testPersistentMutableMapInsertion()
        testPersistentMutableMapPersistence()
    }

    private fun testPersistentMutableMapCreationWithoutPreExistingFile() {
        val file = File(filePath)
        FileUtils.deleteDirectory(file.parentFile)

        // Make sure the file does not exist to test creation
        assert(!file.exists())

        val persistentMap = PersistentMutableMap<String, Int>(filePath, true)

        assert(persistentMap.isEmpty())
        assert(file.exists())
    }

    private fun testPersistentMutableMapCreationWithPreExistingEmptyFile() {
        val file = File(filePath)
        // Delete any pre-existing file
        FileUtils.deleteDirectory(file.parentFile)
        // Create a new empty file
        file.parentFile.mkdirs()
        file.createNewFile()

        // Make sure the file exist
        assert(file.exists())

        val persistentMap = PersistentMutableMap<String, Int>(filePath, true)

        // Map should be empty
        assert(persistentMap.isEmpty())
    }

    private fun testPersistentMutableMapCreationWithPreExistingBadFile() {
        val file = File(filePath)
        // Delete any pre-existing file
        FileUtils.deleteDirectory(file.parentFile)
        // Create a new empty file
        file.parentFile.mkdirs()
        file.createNewFile()

        file.writeText("I bet you can't read this text as a map >:)")

        // Make sure the file exist
        assert(file.exists())

        val persistentMap = PersistentMutableMap<String, Int>(filePath, true)

        // Map should be empty
        assert(persistentMap.isEmpty())
    }

    private fun testPersistentMutableMapInsertion() {
        val persistentMap = PersistentMutableMap<String, Int>(filePath, true)

        // Map should be empty
        assert(persistentMap.isEmpty())

        persistentMap.put("key1", 1)
        persistentMap.put("key2", 2)

        assert(persistentMap.containsKey("key1"))
        assert(persistentMap.containsKey("key2"))

        assertEquals(1, persistentMap.get("key1"))
        assertEquals(2, persistentMap.get("key2"))
    }

    private fun testPersistentMutableMapPersistence() {
        val persistentMap = PersistentMutableMap<String, Int>(filePath, true)

        // Map should NOT be empty
        assert(!persistentMap.isEmpty())

        assert(persistentMap.containsKey("key1"))
        assert(persistentMap.containsKey("key2"))

        assertEquals(1, persistentMap.get("key1"))
        assertEquals(2, persistentMap.get("key2"))
    }
}