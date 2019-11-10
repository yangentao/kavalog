package dev.entao.kava.log

/**
 * Created by entaoyang@163.com on 2016-10-28.
 */

/**
 * Created by yangentao on 2015/11/21.
 * entaoyang@163.com
 */

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
