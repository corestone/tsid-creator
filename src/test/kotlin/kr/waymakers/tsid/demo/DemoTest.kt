package kr.waymakers.tsid.demo

import kr.waymakers.tsid.TsidCreator

object DemoTest {

	private const val HORIZONTAL_LINE = "----------------------------------------"

	fun printList() {
		val max = 100

		println(HORIZONTAL_LINE)
		println("### TSID number")
		println(HORIZONTAL_LINE)

		for (i in 0 until max) {
			println(TsidCreator.tsid1024.toLong())
		}

		println(HORIZONTAL_LINE)
		println("### TSID string")
		println(HORIZONTAL_LINE)

		for (i in 0 until max) {
			println(TsidCreator.tsid1024.toString())
		}
	}

	@JvmStatic
	fun main(args: Array<String>) {
		printList()
	}

}
