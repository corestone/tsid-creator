package kr.waymakers.tsid

import org.junit.Assert.*
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class IncrementalTest {

	private class ClockMock : Clock() {
		private var instant: Instant = Instant.ofEpochSecond(0)

		override fun getZone(): ZoneId {
			throw UnsupportedOperationException("getZone")
		}

		override fun withZone(zoneid: ZoneId): Clock {
			throw UnsupportedOperationException("withZone")
		}

		override fun instant(): Instant {
			return instant
		}

		fun incrementMillis(millis: Long) {
			this.instant = instant.plusMillis(millis)
		}

		fun decrementMillis(millis: Long) {
			this.instant = instant.minusMillis(millis)
		}
	}

	@Test
	fun shouldGenerateIncrementalValuesInCaseOfBackwardAGlitch() {
		val clock = ClockMock()

		val factory = TsidFactory(nodeBits = 20, node = 0, clock = clock)

		val prev = factory.create()

		clock.decrementMillis(TEN_SECONDS - 1)

		val next = factory.create()

		assertIncremental(prev, next)
	}

	@Test
	fun shouldGenerateIncrementalValuesInCaseOfForwardAGlitch() {
		val clock = ClockMock()

		val factory = TsidFactory(nodeBits = 20, node =  0, clock = clock)

		val prev = factory.create()

		clock.incrementMillis(TEN_SECONDS - 1)

		val next = factory.create()

		assertIncremental(prev, next)
	}

	@Test
	fun shouldManageAGlitch() {
		val factory = TsidFactory(nodeBits = 20, node = 0, clock = ClockMock(), timeFunction = { 0 })

		val advanceTimeUpTODriftTolerance = TEN_SECONDS * 4 - 1

		var last = Long.MIN_VALUE
		for (i in 0 until advanceTimeUpTODriftTolerance) {
			val tsid = factory.create().toLong()
			assertTrue(last < tsid)
			last = tsid
		}

		val prev = factory.create()
		assertTrue(last < prev.toLong())

		val next = factory.create()
		assertIncremental(prev, next)
	}

	@Test
	fun shouldAlwaysBeIncremental() {
		val factory = TsidFactory(nodeBits = 20, node = 0)

		var last: Long = 0
		for (i in 0..<1000000) {
			val tsid = factory.create().toLong()
			if (last != 0L && tsid < last) {
				fail(
					"""
						generated TSID value is less that the previous one:
						iteration: $i
						previous: ${Tsid.from(last)}  long= $last
						actual  : ${Tsid.from(tsid)}  long= $tsid
					""".trimIndent())
			}
			last = tsid
		}
	}

	private fun assertIncremental(prev: Tsid, next: Tsid) {
		assertTrue(
			"""
				generated TSID value is less that the previous one:
				previous: $prev  long= ${prev.toLong()}
				actual  : $next  long= ${next.toLong()}
			""".trimIndent(),
			prev.toLong() < next.toLong()
		)
	}

	companion object {
		private const val TEN_SECONDS = 10000L
	}

}
