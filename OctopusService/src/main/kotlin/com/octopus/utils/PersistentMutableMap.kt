package com.octopus.utils

import com.google.common.collect.ImmutableMap
import org.slf4j.LoggerFactory
import java.io.*
import java.util.concurrent.Executors

/**
 * This class acts as a normal map that persists its value to a file store.
 * Both the key and value must be [Serializable].
 */
class PersistentMutableMap<K, V>(private val filePath: String, private val shouldPersistOnUpdate: Boolean) {
    private val logger = LoggerFactory.getLogger(PersistentMutableMap::class.java)
    private val executor = Executors.newSingleThreadExecutor();

    val map: MutableMap<K, V>

    init {
        val file = File(filePath)

        if (!file.exists()) {
            // Create file or throw an exception
            if ((file.parentFile.exists() || file.parentFile.mkdirs()) && file.createNewFile()) {
                logger.info("File did not exist. Created new file: $filePath")
            } else {
                throw ExceptionInInitializerError("Failed to create a new file at $filePath")
            }

            // Initialize the file with an empty map
            initFile(file)
        }

        var tmpMap = mutableMapOf<K, V>()
        var fileInputStream: FileInputStream? = null
        var objectInputStream: ObjectInputStream? = null

        // Try to load the file or create a new map if not.
        try {
            fileInputStream = FileInputStream(file)
            objectInputStream = ObjectInputStream(fileInputStream)

            tmpMap = objectInputStream.readObject() as MutableMap<K, V>
        } catch (e: EOFException) {
            logger.warn("An empty file was found. Initializing the file with an empty map.")
            initFile(file)
        } catch (e: Exception) {
            // TODO: Consider throwing instead to avoid loss of old data.
            logger.error("Could not read file. Initializing the file with an empty map.")
            initFile(file)
        } finally {
            objectInputStream?.close()
            fileInputStream?.close()
        }

        // Load the map
        map = tmpMap
    }

    private fun initFile(file: File) {
        val fileOutputStream = FileOutputStream(file)
        val objectOutputStream = ObjectOutputStream(fileOutputStream)

        objectOutputStream.writeObject(mutableMapOf<K, V>())
        fileOutputStream.close()
        objectOutputStream.close()
    }

    fun put(key: K, value: V) {
        map[key] = value

        if (shouldPersistOnUpdate) {
            asyncPersist()
        }
    }

    fun remove(key: K) : V? {
        val oldValue = map.remove(key)

        if (shouldPersistOnUpdate) {
            asyncPersist()
        }

        return oldValue
    }

    fun get(key: K): V? {
        return map[key]
    }

    fun containsKey(key: K): Boolean {
        return map.containsKey(key)
    }

    fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    fun asyncPersist() {
        executor.execute { persist() }
    }

    @Synchronized
    fun persist() {
        val fileOutputStream = FileOutputStream(filePath)
        val objectOutputStream = ObjectOutputStream(fileOutputStream)

        objectOutputStream.writeObject(map)
        fileOutputStream.close()
        objectOutputStream.close()
    }

    /**
     * This only returns a snapshot of the map at the time of calling the method.
     * This should mainly be used for testing or to access native methods of underlying map.
     * It is best to avoid using this!
     */
    fun getImmutableMapSnapshot() : ImmutableMap<K, V> {
        return ImmutableMap.copyOf(map)
    }
}