@file:Suppress("unused")

package dev.entao.kava.log


/**
 * Created by yangentao on 2015/11/21.
 * entaoyang@163.com
 */
//
//fun main() {
//	logdx("yang", "hello")
//	Yog.ix("YANG", "123", "Hello")
//
//}

fun log(vararg args: Any?) {
	Yog.d(*args)
}

fun logd(vararg args: Any?) {
	Yog.d(*args)
}

fun logi(vararg args: Any?) {
	Yog.i(*args)
}

fun loge(vararg args: Any?) {
	Yog.e(*args)
}

fun fatal(msg: String, vararg args: Any?) {
	loge(*args)
	throw RuntimeException(msg)
}

fun fatalIf(b: Boolean?, msg: String, vararg args: Any?) {
	if (b == null || b) {
		loge(*args)
		throw RuntimeException(msg)
	}
}


fun logx(tag: String, vararg args: Any?) {
	Yog.dx(tag, *args)
}

fun logdx(tag: String, vararg args: Any?) {
	Yog.dx(tag, *args)
}

fun logix(tag: String, vararg args: Any?) {
	Yog.ix(tag, *args)
}

fun logex(tag: String, vararg args: Any?) {
	Yog.ex(tag, *args)
}