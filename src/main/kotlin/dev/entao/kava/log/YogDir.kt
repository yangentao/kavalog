package dev.entao.kava.log

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by entaoyang@163.com on 2016-10-28.
 */

@Suppress("unused")
class YogDir(
    private val logdir: File,
    private val keepDays: Int,
    private val prefix: String = "yog",
    private val ext: String = ".log",
    private val tagSeprateFile: Boolean = false
) : YogPrinter {

    private val mapWriter = HashMap<String, BufferedWriter>()
    private val mapDay = HashMap<String, Int>()

    private var installed = false

    private var KEY_DEF = "YOG"

    init {
        if (!logdir.exists()) {
            logdir.mkdirs()
            logdir.mkdir()
        }
    }

    @Synchronized
    override fun install() {
        installed = true
    }

    @Synchronized
    override fun uninstall() {
        installed = false
        val m = HashMap<String, BufferedWriter>(mapWriter)
        mapWriter.clear()
        for ((_, v) in m) {
            v.flush()
            v.close()
        }
        m.clear()
    }

    @Synchronized
    override fun flush() {
        try {
            for (e in this.mapWriter) {
                e.value.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun outOf(tag: String): BufferedWriter? {
        if (!installed) {
            return null
        }
        val tagKey = if (!tagSeprateFile || tag.isEmpty()) {
            this.KEY_DEF
        } else {
            tag
        }
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        if (mapDay[tagKey] == dayOfYear) {
            return mapWriter[tagKey]
        }
        var oldW = mapWriter.remove(tagKey)
        oldW?.flush()
        oldW?.close()
        oldW = null

        deleteOldLogs()

        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val ds = fmt.format(Date(System.currentTimeMillis()))

        val filename: String = if (tagSeprateFile) {
            "$prefix$ds$tagKey$ext"
        } else {
            "$prefix$ds$ext"
        }
        try {
            val writer = BufferedWriter(FileWriter(File(logdir, filename), true), 16 * 1024)
            mapWriter[tagKey] = writer
            mapDay[tagKey] = dayOfYear
            return writer
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return null
    }

    private fun deleteOldLogs() {
        var n = keepDays
        if (n < 1) {
            n = 1
        }
        val fs = logdir.listFiles() ?: return
        val ls = fs.filter { it.name.endsWith(ext) && it.name.startsWith(prefix) }.sortedByDescending { it.name }
        if (ls.size > n + 1) {
            for (i in (n + 1) until ls.size) {
                ls[i].delete()
            }
        }
    }


    override fun printItem(item: YogItem) {
        val w = outOf(item.tag) ?: return
        try {
            w.write(item.line)
            w.write("\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}