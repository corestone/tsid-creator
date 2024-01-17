package kr.waymakers.tsid

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class TsidFormatTest {

	@Test
	fun testFormat() {
		val tsid = Tsid.fast()

		val string = arrayOf( //
			arrayOf("HEAD", "TAIL"),  //
			arrayOf("HEAD", ""),  //
			arrayOf("", "TAIL"),  //
			arrayOf("", "") //
		)

		var format: String
		var formatted: String

		// '%S': upper case
		for (i in string.indices) {
			val head = string[i][0]
			val tail = string[i][1]

			// '%S': canonical string in upper case
			format = "$head%S$tail"
			formatted = head + tsid.toString() + tail
			assertEquals(formatted, tsid.format(format))

			// '%s': canonical string in lower case
			format = "$head%s$tail"
			formatted = head + tsid.toLowerCase() + tail
			assertEquals(formatted, tsid.format(format))

			// '%X': hexadecimal in upper case
			format = "$head%X$tail"
			formatted = head + Tsid.BaseN.encode(tsid, 16) + tail
			assertEquals(formatted, tsid.format(format))

			// '%x': hexadecimal in lower case
			format = "$head%x$tail"
			formatted = head + Tsid.BaseN.encode(tsid, 16).lowercase(Locale.getDefault()) + tail
			assertEquals(formatted, tsid.format(format))

			// '%d': base-10
			format = "$head%d$tail"
			formatted = head + Tsid.BaseN.encode(tsid, 10) + tail
			assertEquals(formatted, tsid.format(format))

			// '%z': base-62
			format = "$head%z$tail"
			formatted = head + Tsid.BaseN.encode(tsid, 62) + tail
			assertEquals(formatted, tsid.format(format))
		}
	}

	@Test
	fun testUnformat() {
		val tsid = Tsid.fast()

		val string = arrayOf( //
			arrayOf("HEAD", "TAIL"),  //
			arrayOf("HEAD", ""),  //
			arrayOf("", "TAIL"),  //
			arrayOf("", "") //
		)

		var format: String
		var formatted: String

		for (i in string.indices) {
			val head = string[i][0]
			val tail = string[i][1]

			// '%S': canonical string in upper case
			format = "$head%S$tail"
			formatted = head + tsid.toString() + tail
			assertEquals(tsid, Tsid.unformat(formatted, format))

			// '%s': canonical string in lower case
			format = "$head%s$tail"
			formatted = head + tsid.toLowerCase() + tail
			assertEquals(tsid, Tsid.unformat(formatted, format))

			// '%X': hexadecimal in upper case
			format = "$head%X$tail"
			formatted = head + Tsid.BaseN.encode(tsid, 16) + tail
			assertEquals(tsid, Tsid.unformat(formatted, format))

			// '%x': hexadecimal in lower case
			format = "$head%x$tail"
			formatted = head + Tsid.BaseN.encode(tsid, 16).lowercase(Locale.getDefault()) + tail
			assertEquals(tsid, Tsid.unformat(formatted, format))

			// '%d': base-10
			format = "$head%d$tail"
			formatted = head + Tsid.BaseN.encode(tsid, 10) + tail
			assertEquals(tsid, Tsid.unformat(formatted, format))

			// '%z': base-62
			format = "$head%z$tail"
			formatted = head + Tsid.BaseN.encode(tsid, 62) + tail
			assertEquals(tsid, Tsid.unformat(formatted, format))
		}
	}

	@Test
	fun testIllegalArgumentException() {
		run {
			try {
				val string = Tsid.fast().format("%z")
				Tsid.unformat(string, "%z")
				// success
			} catch (e: IllegalArgumentException) {
				fail()
			}
		}

		run {
			try {
				Tsid.fast().format("")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}

			try {
				Tsid.fast().format("%")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}

			try {
				Tsid.fast().format("%a")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}

			try {
				Tsid.fast().format("INVALID")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}
			try {
				Tsid.fast().format("INVALID%")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}
		}

		run {
			try {
				Tsid.unformat("", "")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}

			try {
				Tsid.unformat("", "%s")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}
			try {
				Tsid.unformat("INVALID", "%s")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}
		}
		run {
			try {
				Tsid.unformat("HEAD" + Tsid.fast() + "TAIL", "HEAD%STOES")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}
			try {
				Tsid.unformat("HEAD" + Tsid.fast() + "TAIL", "BANG%STAIL")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}

			try {
				Tsid.unformat("" + Tsid.fast(), "%a")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}

			try {
				Tsid.unformat("INVALID" + Tsid.fast(), "INVALID%")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}

			try {
				Tsid.unformat("HEADzzzTAIL", "HEAD%STAIL")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}
			try {
				Tsid.unformat("HEADTAIL", "HEAD%STAIL")
				fail()
			} catch (e: IllegalArgumentException) {
				// success
			}
		}
	}

	@Test
	fun testFormatAndUnformat() {
		val tsid = Tsid.fast()

		val string = arrayOf( //
			arrayOf("HEAD", "TAIL"),  //
			arrayOf("HEAD", ""),  //
			arrayOf("", "TAIL"),  //
			arrayOf("", "") //
		)

		var format: String
		var formatted: String

		for (i in string.indices) {
			val head = string[i][0]
			val tail = string[i][1]

			// '%S': canonical string in upper case
			format = "$head%S$tail"
			formatted = head + tsid.toString() + tail
			assertEquals(formatted, Tsid.unformat(formatted, format).format(format))
			assertEquals(tsid, Tsid.unformat(tsid.format(format), format))

			// '%s': canonical string in lower case
			format = "$head%s$tail"
			formatted = head + tsid.toLowerCase() + tail
			assertEquals(formatted, Tsid.unformat(formatted, format).format(format))
			assertEquals(tsid, Tsid.unformat(tsid.format(format), format))

			// '%X': hexadecimal in upper case
			format = "$head%X$tail"
			formatted = head + Tsid.BaseN.encode(tsid, 16) + tail
			assertEquals(formatted, Tsid.unformat(formatted, format).format(format))
			assertEquals(tsid, Tsid.unformat(tsid.format(format), format))

			// '%x': hexadecimal in lower case
			format = "$head%x$tail"
			formatted = head + Tsid.BaseN.encode(tsid, 16).lowercase(Locale.getDefault()) + tail
			assertEquals(formatted, Tsid.unformat(formatted, format).format(format))
			assertEquals(tsid, Tsid.unformat(tsid.format(format), format))

			// '%z': base-62
			format = "$head%z$tail"
			formatted = head + Tsid.BaseN.encode(tsid, 62) + tail
			assertEquals(formatted, Tsid.unformat(formatted, format).format(format))
			assertEquals(tsid, Tsid.unformat(tsid.format(format), format))

			// '%d': base-10
			format = "$head%d$tail"
			formatted = head + Tsid.BaseN.encode(tsid, 10) + tail
			assertEquals(formatted, Tsid.unformat(formatted, format).format(format))
			assertEquals(tsid, Tsid.unformat(tsid.format(format), format))
		}
	}

}
