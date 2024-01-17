/*
 * MIT License
 *
 * Copyright (c) 2020-2022 Fabio Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package kr.waymakers.tsid

import kr.waymakers.tsid.Tsid.Companion.RANDOM_BITS
import kr.waymakers.tsid.Tsid.Companion.RANDOM_MASK
import java.security.SecureRandom
import java.time.Clock
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.function.IntFunction
import java.util.function.IntSupplier
import java.util.function.LongSupplier
import kotlin.math.ceil
import kotlin.math.ln

/**
 * A factory that actually generates Time-Sorted Unique Identifiers (TSID).
 *
 *
 * This factory is used by the [TsidCreator] utility.
 *
 *
 * Most people just need [TsidCreator]. However, you can use this class if
 * you need to make some customizations, for example changing the default
 * [SecureRandom] random generator to a faster pseudo-random generator.
 *
 *
 * If a system property "tsidcreator.node" or environment variable
 * "TSIDCREATOR_NODE" is defined, its value is utilized as node identifier. One
 * of them **should** be defined to embed a machine ID in the generated TSID
 * in order to avoid TSID collisions. Using that property or variable is
 * **highly recommended**. If no property or variable is defined, a random
 * node ID is generated at initialization.
 *
 *
 * If a system property "tsidcreator.node.count" or environment variable
 * "TSIDCREATOR_NODE_COUNT" is defined, its value is utilized by the
 * constructors of this class to adjust the amount of bits needed to embed the
 * node ID. For example, if the number 50 is given, the node bit amount is
 * adjusted to 6, which is the minimum number of bits to accommodate 50 nodes.
 * If no property or variable is defined, the number of bits reserved for node
 * ID is set to 10, which can accommodate 1024 nodes.
 *
 *
 * A "node" as we call it in this library can be a physical machine, a virtual
 * machine, a container, a k8s pod, a running process, etc.
 *
 *
 * This class **should** be used as a singleton. Make sure that you create
 * and reuse a single instance of [TsidFactory] per node in your
 * distributed system.
 */
class TsidFactory(
	nodeBits: Int? = null,
	node: Int? = null,
	random: Random? = null,
	randomSupplier: IntSupplier? = null,
	randomFunction: IntFunction<ByteArray>? = null,
	customEpoch: Long? = null,
	timeFunction: LongSupplier? = null,
	clock: Clock? = null,
	) {

	private var counter: Int
	private var lastTime = 0L

	private val node: Int

	private val nodeBits: Int
	private val counterBits: Int

	private val nodeMask: Int
	private val counterMask: Int

	private val customEpoch: Long = customEpoch ?: Tsid.TSID_EPOCH
	private val timeFunction: LongSupplier?

	private val random: IRandom
	private val randomBytes: Int

	private val lock = ReentrantLock()

	init {
		this.nodeBits = nodeBits ?: if (Settings.nodeCount != null) {
			ceil(ln(Settings.nodeCount!!.toDouble()) / ln(2.0)).toInt()
		} else {
			NodeBits.NODE_256.bits
		}

		this.random = run {
			if (random != null) {
				if (random is SecureRandom) {
					ByteRandom(random)
				} else {
					IntRandom(random)
				}
			} else if (randomFunction != null) {
				ByteRandom(randomFunction)
			} else if (randomSupplier != null) {
				IntRandom(randomSupplier)
			} else {
				ByteRandom(SecureRandom())
			}
		}
		this.timeFunction = timeFunction ?: LongSupplier { clock?.millis() ?: System.currentTimeMillis() }

		// setup constants that depend on node bits
		this.counterBits = RANDOM_BITS - this.nodeBits
		this.counterMask = RANDOM_MASK ushr this.nodeBits
		this.nodeMask = RANDOM_MASK ushr counterBits

		// setup how many bytes to get from the random function
		this.randomBytes = ((this.counterBits - 1) / 8) + 1

		// setup the node identifier
		this.node = run {
			val max = (1 shl this.nodeBits) - 1
			if (node == null) {
				Settings.node ?: (this.random.nextInt() and max)
			} else {
				require(node in 0..max) { "Node identifier must be $node between 0 and $max" }
				node
			}
		} and nodeMask

		// finally initialize inner state
		// 1970-01-01
		this.counter = this.randomCounter
	}

	enum class NodeBits(val bits: Int) {
		NODE_256(8),
		NODE_1024(10),
		NODE_4096(12),
		;
	}

	// ******************************
	// Public methods
	// ******************************
	/**
	 * Returns a TSID.
	 *
	 * @return a TSID.
	 */
	fun create(): Tsid {
		lock.lock()
		try {
			val time = time shl RANDOM_BITS
			val node = node.toLong() shl this.counterBits
			val counter = counter.toLong() and counterMask.toLong()

			return Tsid(time or node or counter)
		} finally {
			lock.unlock()
		}
	}

	private val time: Long
		/**
		 * Returns the current time.
		 *
		 *
		 * If the current time is equal to the previous time, the counter is incremented
		 * by one. Otherwise the counter is reset to a random value.
		 *
		 *
		 * The maximum number of increment operations depend on the counter bits. For
		 * example, if the counter bits is 12, the maximum number of increment
		 * operations is 2^12 = 4096.
		 *
		 * @return the current time
		 */
		get() {
			var time = timeFunction!!.asLong

			if (time <= this.lastTime) {
				counter++
				// Carry is 1 if an overflow occurs after ++.
				val carry = this.counter ushr this.counterBits
				this.counter = this.counter and this.counterMask
				time = this.lastTime + carry // increment time
			} else {
				// If the system clock has advanced as expected,
				// simply reset the counter to a new random value.
				this.counter = this.randomCounter
			}

			// save current time
			this.lastTime = time

			// adjust to the custom epoch
			return time - this.customEpoch
		}

	private val randomCounter: Int
		/**
		 * Returns a random counter value from 0 to 0x3fffff (2^22-1 = 4,194,303).
		 *
		 *
		 * The counter maximum value depends on the node identifier bits. For example,
		 * if the node identifier has 10 bits, the counter has 12 bits.
		 *
		 * @return a number
		 */
		get() {
			if (random is ByteRandom) {
				val bytes = random.nextBytes(this.randomBytes)

				return when (bytes.size) {
					1 -> bytes[0].toInt() and 0xff and this.counterMask
					2 -> ((bytes[0].toInt() and 0xff) shl 8) or (bytes[1].toInt() and 0xff) and this.counterMask
					else -> ((bytes[0].toInt() and 0xff) shl 16) or ((bytes[1].toInt() and 0xff) shl 8) or (bytes[2].toInt() and 0xff) and this.counterMask
				}
			} else {
				return random.nextInt() and this.counterMask
			}
		}

	interface IRandom {

		fun nextInt(): Int

		fun nextBytes(length: Int): ByteArray

	}

	internal class IntRandom @JvmOverloads constructor(
		randomFunction: IntSupplier? = newRandomFunction(null)
	) : IRandom {

		private val randomFunction = randomFunction ?: newRandomFunction(null)

		constructor(random: Random?) : this(newRandomFunction(random))
		override fun nextInt(): Int {
			return randomFunction.asInt
		}

		override fun nextBytes(length: Int): ByteArray {
			var shift = 0
			var random: Long = 0
			val bytes = ByteArray(length)

			for (i in 0 until length) {
				if (shift < java.lang.Byte.SIZE) {
					shift = Integer.SIZE
					random = randomFunction.asInt.toLong()
				}
				shift -= java.lang.Byte.SIZE // 56, 48, 40...
				bytes[i] = (random ushr shift).toByte()
			}

			return bytes
		}

		companion object {
			private fun newRandomFunction(random: Random?): IntSupplier {
				val entropy = random ?: SecureRandom()
				return IntSupplier { entropy.nextInt() }
			}
		}

	}

	internal class ByteRandom @JvmOverloads constructor(
		randomFunction: IntFunction<ByteArray>? = newRandomFunction(null)
	) : IRandom {

		private val randomFunction = randomFunction ?: newRandomFunction(null)

		constructor(random: Random?) : this(newRandomFunction(random))

		override fun nextInt(): Int {
			var number = 0
			val bytes = randomFunction.apply(Integer.BYTES)
			for (i in 0 until Integer.BYTES) {
				number = (number shl 8) or (bytes[i].toInt() and 0xff)
			}
			return number
		}

		override fun nextBytes(length: Int): ByteArray {
			return randomFunction.apply(length)
		}

		companion object {
			private fun newRandomFunction(random: Random?): IntFunction<ByteArray> {
				val entropy = random ?: SecureRandom()
				return IntFunction<ByteArray> { length: Int ->
					val bytes = ByteArray(length)
					entropy.nextBytes(bytes)
					bytes
				}
			}
		}

	}

	internal object Settings {
		const val NODE: String = "tsidcreator.node"
		const val NODE_COUNT: String = "tsidcreator.node.count"

		val node: Int?
			get() = getPropertyAsInteger(NODE)

		val nodeCount: Int?
			get() = getPropertyAsInteger(NODE_COUNT)

		private fun getPropertyAsInteger(property: String): Int? {
			return try {
				Integer.decode(getProperty(property))
			} catch (e: NumberFormatException) {
				null
			} catch (e: NullPointerException) {
				null
			}
		}

		private fun getProperty(name: String): String? {
			val property = System.getProperty(name)
			if (property != null && property.isNotEmpty()) {
				return property
			}
			val variable = System.getenv(name.uppercase(Locale.getDefault()).replace(".", "_"))
			if (variable != null && variable.isNotEmpty()) {
				return variable
			}
			return null
		}

	}

	companion object {

		fun create256(node: Int? = null) : TsidFactory {
			return TsidFactory(nodeBits = NodeBits.NODE_256.bits, node = node)
		}

		fun create1024(node: Int? = null) : TsidFactory {
			return TsidFactory(nodeBits = NodeBits.NODE_1024.bits, node = node)
		}

		fun create4096(node: Int? = null) : TsidFactory {
			return TsidFactory(nodeBits = NodeBits.NODE_4096.bits, node = node)
		}

	}

}
