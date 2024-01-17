package kr.waymakers.tsid

import org.junit.Assert.*
import org.junit.Test
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class CollisionTest {

	private fun newFactory(nodeBits: Int): TsidFactory {
		return TsidFactory(nodeBits = nodeBits)
	}

	@Test
	@Throws(InterruptedException::class)
	fun testCollision() {
		val nodeBits = 8
		val threadCount = 16
		val iterationCount = 100000

		val clashes = AtomicInteger()
		val endLatch = CountDownLatch(threadCount)
		val tsidMap: ConcurrentMap<Long, Int> = ConcurrentHashMap()

		// one generator shared by ALL THREADS
		val factory = newFactory(nodeBits)

		for (i in 0 until threadCount) {

			Thread {
				for (j in 0 until iterationCount) {
					val tsid = factory.create().toLong()
					if (Objects.nonNull(tsidMap.put(tsid, (i * iterationCount) + j))) {
						clashes.incrementAndGet()
						break
					}
				}
				endLatch.countDown()
			}.start()
		}
		endLatch.await()

		assertFalse("Collisions detected!", clashes.toInt() != 0)
	}

}
