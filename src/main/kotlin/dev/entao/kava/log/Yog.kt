@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.kava.log

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by entaoyang@163.com on 2018/11/8.
 */
enum class LogLevel(val n: Int) {

	DISABLE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4), FATAIL(5);

	//>=
	fun ge(level: LogLevel): Boolean {
		return this.ordinal >= level.ordinal
	}
}

object Yog {
	var level = LogLevel.DEBUG
	var defaultPrinter: YogPrinter? = YogConsole()
	val tagPrinters = HashMap<String, YogPrinter>()

	fun setPrinter(p: YogPrinter) {
		defaultPrinter?.uninstall()
		defaultPrinter = p
	}

	fun setTagPrinter(tag: String, p: YogPrinter) {
		tagPrinters[tag]?.uninstall()
		tagPrinters[tag] = p
	}

	fun setTagPrinter(tag: String, dir: File) {
		setTagPrinter(tag, YogDir(dir, 15, tag, ".log"))
	}

	fun flush() {
		defaultPrinter?.flush()
		for (p in tagPrinters.values) {
			p.flush()
		}
	}

	fun uninstall() {
		flush()
		defaultPrinter?.uninstall()
		for (p in tagPrinters.values) {
			p.uninstall()
		}
	}

	fun d(vararg args: Any?) {
		printMessage(LogLevel.DEBUG, *args)
	}

	fun w(vararg args: Any?) {
		printMessage(LogLevel.WARN, *args)
	}

	fun e(vararg args: Any?) {
		printMessage(LogLevel.ERROR, *args)
		defaultPrinter?.flush()
	}

	fun i(vararg args: Any?) {
		printMessage(LogLevel.INFO, *args)
	}

	fun fatal(vararg args: Any?) {
		e(*args)
		throw RuntimeException("fatal error!")
	}

	fun formatMsg(level: LogLevel, msg: String): String {
		val sb = StringBuilder(msg.length + 64)
		val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(System.currentTimeMillis()))
		sb.append(date)
		sb.append(String.format(Locale.getDefault(), "%6d ", Thread.currentThread().id))
		sb.append(level.name)
		sb.append(" ")
		sb.append(msg)
		return sb.toString()
	}

	@Synchronized
	fun printMessage(level: LogLevel, vararg args: Any?) {
		if (level.ge(Yog.level)) {
			val s: String = args.joinToString(" ") {
				toLogString(it)
			}
			val p = if (args.isEmpty()) {
				defaultPrinter
			} else {
				tagPrinters[args[0].toString()] ?: defaultPrinter
			}
			p?.printLine(level, s)
		}
	}

	fun toLogString(obj: Any?): String {
		if (obj == null) {
			return "null"
		}
		if (obj is String) {
			return obj
		}
		if (obj.javaClass.isPrimitive) {
			return obj.toString()
		}

		if (obj is Throwable) {
			val sw = StringWriter(512)
			val pw = PrintWriter(sw)
			obj.printStackTrace(pw)
			return sw.toString()
		}

		if (obj is Array<*>) {
			val s = obj.joinToString(",") { toLogString(it) }
			return "ARRAY[$s]"
		}
		if (obj is List<*>) {
			val s = obj.joinToString(", ") { toLogString(it) }
			return "LIST[$s]"
		}
		if (obj is Map<*, *>) {
			val s = obj.map { "${toLogString(it.key)} = ${toLogString(it.value)}" }.joinToString(",")
			return "MAP{$s}"
		}
		if (obj is Iterable<*>) {
			val s = obj.joinToString(", ") { toLogString(it) }
			return "ITERABLE[$s]"
		}
		return obj.toString()
	}
}

interface YogPrinter {
	fun flush()
	fun printLine(level: LogLevel, msg: String)
	fun uninstall() {

	}
}

class YogTree(vararg ps: YogPrinter) : YogPrinter {
	val all = ArrayList<YogPrinter>()

	init {
		all += ps
	}

	override fun flush() {
		all.forEach { it.flush() }
	}

	override fun printLine(level: LogLevel, msg: String) {
		all.forEach { it.printLine(level, msg) }
	}

	override fun uninstall() {
		for (p in all) {
			p.uninstall()
		}
	}
}

class YogConsole : YogPrinter {
	override fun printLine(level: LogLevel, msg: String) {
		val s = Yog.formatMsg(level, msg)
		println(s)
	}

	override fun flush() {

	}
}