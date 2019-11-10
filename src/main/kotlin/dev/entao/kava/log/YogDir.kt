package dev.entao.kava.log

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by entaoyang@163.com on 2016-10-28.
 */

@Suppress("unused")
class YogDir(
		private val logdir: File,
		private val keepDays: Int,
		private val prefix: String = "yog",
		private val ext: String = ".log"
) : YogPrinter {


	private var writer: BufferedWriter? = null
	private var dateStr: String = ""
	private var dayYear: Int = -1
	private var preTime: Long = System.currentTimeMillis()
	private var flushTime: Long = 10 * 1000  // 10s flush一次

	init {
		if (!logdir.exists()) {
			logdir.mkdirs()
			logdir.mkdir()
		}
	}

	private val out: BufferedWriter?
		get() {
			val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
			if (dayOfYear == this.dayYear) {
				return writer
			}
			val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
			val ds = fmt.format(Date(System.currentTimeMillis()))
			writer?.flush()
			writer?.close()
			writer = null
			dateStr = ds
			deleteOldLogs()
			try {
				writer = BufferedWriter(FileWriter(File(logdir, "$prefix$dateStr$ext"), true), 32 * 1024)
			} catch (ex: IOException) {
				ex.printStackTrace()
			}
			return writer
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

	override fun uninstall() {
		out?.flush()
		out?.close()
		writer = null
	}

	override fun flush() {
		try {
			out?.flush()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	override fun printItem(item: YogItem) {
		val w = out ?: return
		try {
			w.write(item.line)
			w.write("\n")
		} catch (e: IOException) {
			e.printStackTrace()
		}
		this.checkFlush()
	}

	private fun checkFlush() {
		val cur = System.currentTimeMillis()
		if (cur - preTime > flushTime) {
			flush()
			preTime = cur
		}
	}

}