package kr.waymakers.tsid.uniq

import kr.waymakers.tsid.TsidFactory
import java.util.*

/**
 * This is not included in the [TestSuite] because it may take a long
 * time to finish.
 * Initialize the test.
 *
 * @param threadCount
 * @param requestCount
 * @param verbose
 */
class UniquenessTest // Number of threads to run
(
	private val threadCount: Int, // Number of requests for thread
	private val requestCount: Int, // Show progress or not
	private val verbose: Boolean
) {

	private val hashSet = HashSet<Long>()

	/**
	 * Initialize and start the threads.
	 */
	fun start() {
		val threads = arrayOfNulls<Thread>(this.threadCount)

		// Instantiate and start many threads
		for (i in 0 until this.threadCount) {
			threads[i] = Thread(UniquenessTestThread(i, verbose))
			threads[i]!!.start()
		}

		// Wait all the threads to finish
		for (thread in threads) {
			try {
				thread!!.join()
			} catch (e: InterruptedException) {
				Thread.currentThread().interrupt()
			}
		}
	}

	inner class UniquenessTestThread(private val id: Int, private val verbose: Boolean) : Runnable {
		private val factory: TsidFactory = TsidFactory(nodeBits = id, random = Random())

		/**
		 * Run the test.
		 */
		override fun run() {
			var progress = 0
			val max = requestCount

			for (i in 0 until max) {
				// Request a TSID

				val tsid = factory.create().toLong()

				if (verbose && (i % (max / 100) == 0)) {
					// Calculate and show progress
					progress = ((i * 1.0 / max) * 100).toInt()
					println(String.format("[Thread %06d] %s %s %s%%", id, tsid, i, progress))
				}
				synchronized(hashSet) {
					// Insert the value in cache, if it does not exist in it.
					if (!hashSet.add(tsid)) {
						System.err.println(
							String.format("[Thread %06d] %s %s %s%% [DUPLICATE]", id, tsid, i, progress)
						)
					}
				}
			}

			if (verbose) {
				// Finished
				println(String.format("[Thread %06d] Done.", id))
			}
		}
	}

	companion object {
		private fun execute(threadCount: Int, requestCount: Int, verbose: Boolean) {
			val test = UniquenessTest(threadCount, requestCount, verbose)
			test.start()
		}

		@JvmStatic
		fun main(args: Array<String>) {
			val threadCount = 1024 // 2^10 (node bit length)
			val requestCount = 4096 // 2^12 (counter bit length)
			val verbose = true
			execute(threadCount, requestCount, verbose)
		}
	}
}
