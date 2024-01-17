package kr.waymakers.tsid

import org.junit.Assert.*
import java.util.*

abstract class TsidFactory00000Test {

	protected class TestThread(private val creator: TsidFactory, private val loopLimit: Int) : Thread() {
		override fun run() {
			for (i in 0 until loopLimit) {
				synchronized(hashSet) {
					hashSet.add(creator.create().toLong())
				}
			}
		}

		companion object {
			@JvmField
			val hashSet: MutableSet<Long> = HashSet()

			@JvmStatic
			fun clearHashSet() {
				synchronized(hashSet) {
					hashSet.clear()
				}
			}
		}
	}

	protected fun checkNullOrInvalid(list: LongArray): Boolean {
		for (tsid in list) {
			assertNotEquals("TSID is zero", tsid, 0)
		}
		return true // success
	}

	protected fun checkNullOrInvalid(list: List<String>): Boolean {
		for (tsid in list) {
			assertNotNull("TSID is null", tsid)
			assertFalse("TSID is empty", tsid.isEmpty())
			assertFalse("TSID is blank", tsid.replace(" ", "").isEmpty())
			assertEquals("TSID length is wrong " + tsid.length, TSID_LENGTH.toLong(), tsid.length.toLong())
			assertTrue("TSID is not valid", Tsid.isValid(tsid))
		}
		return true // success
	}

	protected fun checkUniqueness(list: LongArray): Boolean {
		val set = HashSet<Long>()

		for (tsid in list) {
			assertTrue(String.format("TSID is duplicated %s", tsid), set.add(tsid))
		}

		assertEquals("There are duplicated TSIDs", set.size.toLong(), list.size.toLong())
		return true // success
	}

	protected fun checkUniqueness(list: List<String>): Boolean {
		val set = HashSet<String>()

		for (tsid in list) {
			assertTrue(String.format("TSID is duplicated %s", tsid), set.add(tsid))
		}

		assertEquals("There are duplicated TSIDs", set.size.toLong(), list.size.toLong())
		return true // success
	}

	protected fun checkOrdering(list: LongArray): Boolean {
		val other = list.copyOf(list.size)
		Arrays.sort(other)

		for (i in list.indices) {
			assertEquals("The TSID list is not ordered", list[i], other[i])
		}
		return true // success
	}

	protected fun checkOrdering(list: List<String>): Boolean {
		val other = list.stream()
			.sorted()
			.collect(java.util.stream.Collectors.toList())

		for (i in list.indices) {
			assertEquals("The TSID list is not ordered", list[i], other[i])
		}
		return true // success
	}

	protected fun checkMaximumPerMs(list: LongArray, max: Int): Boolean {
		val map = HashMap<Long, ArrayList<Long>?>()

		for (tsid in list) {
			val key = Tsid.from(tsid).time
			if (map[key] == null) {
				map[key] = ArrayList()
			}
			map[key]!!.add(tsid)
			val size = map[key]!!.size
			assertTrue(String.format("Too many TSIDs per milliecond %s", size), size <= max)
		}

		return true // success
	}

	protected fun checkMaximumPerMs(list: List<String>, max: Int): Boolean {
		val map = HashMap<Long, HashSet<String>?>()

		for (tsid in list) {
			val key = Tsid.from(tsid).time
			if (map[key] == null) {
				map[key] = HashSet()
			}
			map[key]!!.add(tsid)
			val size = map[key]!!.size
			assertTrue(String.format("Too many TSIDs per milliecond %s", size), size <= max)
		}

		return true // success
	}

	protected fun checkCreationTime(list: LongArray, startTime: Long, endTime: Long): Boolean {
		assertTrue("Start time was after end time", startTime <= endTime)

		for (tsid in list) {
			val creationTime = Tsid.from(tsid).instant.toEpochMilli()
			assertTrue("Creation time was before start time", creationTime >= startTime)
			assertTrue("Creation time was after end time", creationTime <= endTime + LOOP_MAX)
		}
		return true // success
	}

	protected fun checkCreationTime(list: List<String>, startTime: Long, endTime: Long): Boolean {
		assertTrue("Start time was after end time", startTime <= endTime)

		for (tsid in list) {
			val creationTime = Tsid.from(tsid).instant.toEpochMilli()
			assertTrue("Creation time was before start time ", creationTime >= startTime)
			assertTrue("Creation time was after end time", creationTime <= endTime + LOOP_MAX)
		}
		return true // success
	}

	companion object {
		protected const val TSID_LENGTH: Int = 13

		@JvmStatic
		protected val LOOP_MAX: Int = 10000

		@JvmStatic
		protected var random: Random = Random()

		@JvmStatic
		protected val DUPLICATE_UUID_MSG: String = "A duplicate TSID was created"

		@JvmStatic
		protected val THREAD_TOTAL: Int = availableProcessors()

		private fun availableProcessors(): Int {
			var processors = Runtime.getRuntime().availableProcessors()
			if (processors < 4) {
				processors = 4
			}
			return processors
		}
	}

}
