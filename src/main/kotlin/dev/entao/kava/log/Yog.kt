@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.kava.log

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by entaoyang@163.com on 2018/11/8.
 */
enum class YogLevel(val n: Int) {
    DEBUG(0), VERBOSE(1), INFO(2), WARN(3), ERROR(4), FATAIL(5);
}

data class YogItem(val level: YogLevel, val tag: String, val message: String) {
    val line: String = makeLine()

    private fun makeLine(): String {
        val sb = StringBuilder(message.length + 64)
        sb.append("TIM:")
        val date =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(System.currentTimeMillis()))
        sb.append(date)
        sb.append(" TID:")
        sb.append(String.format(Locale.getDefault(), "%6d", Thread.currentThread().id))
        sb.append(" LVL:")
        sb.append(level.name)
        sb.append(" TAG:")
        sb.append(tag)
        sb.append(" MSG:")
        sb.append(message)
        return sb.toString()
    }

}

private class PrinterItem(val printer: YogPrinter, val accepter: (YogItem) -> Boolean)

object Yog {
    private val printerItems: ArrayList<PrinterItem> = ArrayList()
    var enabled = true
    var TAG: String = "YOG"

    init {
        setPrinter(YogConsole())
    }

    @Synchronized
    fun addPrinter(p: YogPrinter, accepter: (YogItem) -> Boolean) {
        this.printerItems.add(PrinterItem(p, accepter))
		p.install()
    }

    @Synchronized
    fun addPrinter(p: YogPrinter) {
        this.addPrinter(p) { true }
    }

    @Synchronized
    fun clearPrinter() {
        for (a in this.printerItems) {
            a.printer.flush()
            a.printer.uninstall()
        }
        this.printerItems.clear()
    }

    @Synchronized
    fun removePrinter(p: YogPrinter) {
        p.flush()
        p.uninstall()
        this.printerItems.removeIf {
            it.printer === p
        }
    }

    @Synchronized
    fun setPrinter(p: YogPrinter) {
        this.clearPrinter()
        addPrinter(p)
    }

    @Synchronized
    fun flush() {
        for (a in this.printerItems) {
            a.printer.flush()
        }
    }

    @Synchronized
    fun printItem(item: YogItem) {
        if (enabled) {
            for (a in this.printerItems) {
                if (a.accepter(item)) {
                    a.printer.printItem(item)
                }
            }
        }
    }

    fun printItem(level: YogLevel, tag: String, vararg args: Any?) {
        printItem(YogItem(level, tag, anyArrayToString(args)))
    }

    fun d(vararg args: Any?) {
        printItem(YogItem(YogLevel.DEBUG, TAG, anyArrayToString(args)))
    }

    fun w(vararg args: Any?) {
        printItem(YogItem(YogLevel.WARN, TAG, anyArrayToString(args)))
    }

    fun e(vararg args: Any?) {
        printItem(YogItem(YogLevel.ERROR, TAG, anyArrayToString(args)))
        this.flush()
    }

    fun i(vararg args: Any?) {
        printItem(YogItem(YogLevel.INFO, TAG, anyArrayToString(args)))
    }

    fun fatal(vararg args: Any?) {
        printItem(YogItem(YogLevel.FATAIL, TAG, anyArrayToString(args)))
        this.flush()
        throw RuntimeException("fatal error!")
    }


    fun dx(tag: String, vararg args: Any?) {
        printItem(YogItem(YogLevel.DEBUG, tag, anyArrayToString(args)))
    }

    fun wx(tag: String, vararg args: Any?) {
        printItem(YogItem(YogLevel.WARN, tag, anyArrayToString(args)))
    }

    fun ex(tag: String, vararg args: Any?) {
        printItem(YogItem(YogLevel.ERROR, tag, anyArrayToString(args)))
        this.flush()
    }

    fun ix(tag: String, vararg args: Any?) {
        printItem(YogItem(YogLevel.INFO, tag, anyArrayToString(args)))
    }

    fun fatalX(tag: String, vararg args: Any?) {
        printItem(YogItem(YogLevel.FATAIL, tag, anyArrayToString(args)))
        this.flush()
        throw RuntimeException("fatal error!")
    }
}

interface YogPrinter {
    fun printItem(item: YogItem)
    fun flush() {}
    fun uninstall() {}
    fun install() {}
}


class YogConsole : YogPrinter {
    override fun printItem(item: YogItem) {
        println(item.line)
    }

}