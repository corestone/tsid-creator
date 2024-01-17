package kr.waymakers.tsid

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.pow

class TsidFactory04096Test : TsidFactory00000Test() {
	@Test
	fun testGetTsid4096() {
		val startTime = System.currentTimeMillis()

		val factory = TsidFactory(nodeBits = NODE_BITS, random = random)

		val list = LongArray(LOOP_MAX)
		for (i in 0 until LOOP_MAX) {
			list[i] = factory.create().toLong()
		}

		val endTime = System.currentTimeMillis()

		assertTrue(checkNullOrInvalid(list))
		assertTrue(checkUniqueness(list))
		assertTrue(checkOrdering(list))
		assertTrue(checkMaximumPerMs(list, COUNTER_MAX))
		assertTrue(checkCreationTime(list, startTime, endTime))
	}

	@Test
	fun testGetTsid4096WithNode() {
		val startTime = System.currentTimeMillis()

		val node = random.nextInt(NODE_MAX)
		val factory = TsidFactory(node = node, nodeBits = NODE_BITS, random = random)

		val list = LongArray(LOOP_MAX)
		for (i in 0 until LOOP_MAX) {
			list[i] = factory.create().toLong()
		}

		val endTime = System.currentTimeMillis()

		assertTrue(checkNullOrInvalid(list))
		assertTrue(checkUniqueness(list))
		assertTrue(checkOrdering(list))
		assertTrue(checkMaximumPerMs(list, COUNTER_MAX))
		assertTrue(checkCreationTime(list, startTime, endTime))
	}

	@Test
	fun testGetTsidString4096() {
		val startTime = System.currentTimeMillis()

		val factory = TsidFactory(nodeBits = NODE_BITS, random = random)

		val list = mutableListOf<String>()
		for (i in 0 until LOOP_MAX) {
			list.add(factory.create().toString())
		}

		val endTime = System.currentTimeMillis()

		assertTrue(checkNullOrInvalid(list))
		assertTrue(checkUniqueness(list))
		assertTrue(checkOrdering(list))
		assertTrue(checkMaximumPerMs(list, COUNTER_MAX))
		assertTrue(checkCreationTime(list, startTime, endTime))
	}

	@Test
	fun testGetTsidString4096WithNode() {
		val startTime = System.currentTimeMillis()

		val node = random.nextInt(NODE_MAX)
		val factory = TsidFactory(node = node, nodeBits = NODE_BITS, random = random)

		val list = mutableListOf<String>()
		for (i in 0 until LOOP_MAX) {
			list.add(factory.create().toString())
		}

		val endTime = System.currentTimeMillis()

		assertTrue(checkNullOrInvalid(list))
		assertTrue(checkUniqueness(list))
		assertTrue(checkOrdering(list))
		assertTrue(checkMaximumPerMs(list, COUNTER_MAX))
		assertTrue(checkCreationTime(list, startTime, endTime))
	}

	@Test
	@Throws(InterruptedException::class)
	fun testGetTsid4096Parallel() {
		TestThread.clearHashSet()
		val threads = arrayOfNulls<Thread>(THREAD_TOTAL)

		// Instantiate and start many threads
		for (i in 0 until THREAD_TOTAL) {
			val factory = TsidFactory(node = i, nodeBits = NODE_BITS, random = random)
			threads[i] = TestThread(factory, COUNTER_MAX)
			threads[i]?.start()
		}

		// Wait all the threads to finish
		for (thread in threads) {
			thread!!.join()
		}

		// Check if the quantity of unique UUIDs is correct
		assertEquals(DUPLICATE_UUID_MSG, (COUNTER_MAX * THREAD_TOTAL).toLong(), TestThread.hashSet.size.toLong())
	}

	companion object {
		private const val NODE_BITS = 12
		private const val COUNTER_BITS = 10

		private val NODE_MAX = 2.0.pow(NODE_BITS.toDouble()).toInt()
		private val COUNTER_MAX = 2.0.pow(COUNTER_BITS.toDouble()).toInt()
	}
}
