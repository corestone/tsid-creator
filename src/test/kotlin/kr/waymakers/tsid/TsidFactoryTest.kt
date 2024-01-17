package kr.waymakers.tsid

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.nio.ByteBuffer
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.function.IntFunction
import java.util.function.IntSupplier
import kotlin.math.pow

class TsidFactoryTest {

	@Before
	fun before() {
		// clear properties
		System.clearProperty(TsidFactory.Settings.NODE)
		System.clearProperty(TsidFactory.Settings.NODE_COUNT)
	}

	@After
	fun after() {
		// clear properties
		System.clearProperty(TsidFactory.Settings.NODE)
		System.clearProperty(TsidFactory.Settings.NODE_COUNT)
	}

	@Test
	fun testGetInstant() {
		val start = Instant.now()
		val tsid = TsidCreator.tsid
		val middle = tsid.instant
		val end = Instant.now()

		assertTrue(start.toEpochMilli() <= middle.toEpochMilli())
		assertTrue(middle.toEpochMilli() <= end.toEpochMilli() + 1)
	}

	@Test
	fun testGetUnixMilliseconds() {
		val start = System.currentTimeMillis()
		val tsid = TsidFactory().create()
		val middle = tsid.unixMilliseconds
		val end = System.currentTimeMillis()

		assertTrue(start <= middle)
		assertTrue(middle <= end + 1)
	}

	@Test
	fun testGetInstantWithClock() {
		val bound = 2.0.pow(42.0).toLong()

		for (i in 0 until LOOP_MAX) {
			// instantiate a factory with a Clock that returns a fixed value

			val random = ThreadLocalRandom.current().nextLong(bound)
			val millis = random + Tsid.TSID_EPOCH // avoid dates before 2020
			val clock = Clock.fixed(Instant.ofEpochMilli(millis), ZoneOffset.UTC) // simulate a frozen clock
			val randomFunction = IntFunction { x: Int -> ByteArray(x) } // force to reinitialize the counter to ZERO
			val factory = TsidFactory(clock = clock, randomFunction = randomFunction)

			val result = factory.create().instant.toEpochMilli()
			assertEquals("The current instant is incorrect", millis, result)
		}
	}

	@Test
	fun testGetUnixMillisecondsWithClock() {
		val bound = 2.0.pow(42.0).toLong()

		for (i in 0 until LOOP_MAX) {
			// instantiate a factory with a Clock that returns a fixed value

			val random = ThreadLocalRandom.current().nextLong(bound)
			val millis = random + Tsid.TSID_EPOCH // avoid dates before 2020
			val clock = Clock.fixed(Instant.ofEpochMilli(millis), ZoneOffset.UTC) // simulate a frozen clock
			val randomFunction = IntFunction { x: Int -> ByteArray(x) } // force to reinitialize the counter to ZERO
			val factory = TsidFactory(clock = clock, randomFunction = randomFunction)

			val result = factory.create().unixMilliseconds
			assertEquals("The current millisecond is incorrect", millis, result)
		}
	}

	@Test
	fun testGetInstantWithCustomEpoch() {
		val customEpoch = Instant.parse("2015-10-23T00:00:00Z")

		val start = Instant.now()
		val tsid = TsidFactory(customEpoch = customEpoch.toEpochMilli()).create()
		val middle = tsid.getInstant(customEpoch)
		val end = Instant.now()

		assertTrue(start.toEpochMilli() <= middle.toEpochMilli())
		assertTrue(middle.toEpochMilli() <= end.toEpochMilli())
	}

	@Test
	fun testGetUnixMillisecondsWithCustomEpoch() {
		val customEpoch = Instant.parse("1984-01-01T00:00:00Z")

		val start = System.currentTimeMillis()
		val tsid = TsidFactory(customEpoch = customEpoch.toEpochMilli()).create()
		val middle = tsid.getInstant(customEpoch).toEpochMilli()
		val end = System.currentTimeMillis()

		assertTrue(start <= middle)
		assertTrue(middle <= end)
	}

	@Test
	fun testWithNode() {
		run {
			for (i in 0..20) {
				val bits = TsidFactory.NodeBits.NODE_256.bits
				val shif = Tsid.RANDOM_BITS - bits
				val mask = ((1 shl bits) - 1)
				val node = ThreadLocalRandom.current().nextInt() and mask
				val factory = TsidFactory(node)
				assertEquals(node.toLong(), (factory.create().random ushr shif) and mask.toLong())
			}
		}
		run {
			for (i in 0..20) {
				val bits = TsidFactory.NodeBits.NODE_256.bits
				val shif = Tsid.RANDOM_BITS - bits
				val mask = ((1 shl bits) - 1)
				val node = ThreadLocalRandom.current().nextInt() and mask
				System.setProperty(TsidFactory.Settings.NODE, node.toString())
				val factory = TsidFactory()
				assertEquals(node.toLong(), (factory.create().random ushr shif) and mask.toLong())
			}
		}
		run {
			for (i in 0..20) {
				val bits = TsidFactory.NodeBits.NODE_256.bits
				val shif = Tsid.RANDOM_BITS - bits
				val mask = ((1 shl bits) - 1)
				val node = ThreadLocalRandom.current().nextInt() and mask
				val factory = TsidFactory(node = node)
				assertEquals(node.toLong(), (factory.create().random ushr shif) and mask.toLong())
			}
		}
		run {
			for (i in 0..20) {
				val bits = TsidFactory.NodeBits.NODE_256.bits
				val shif = Tsid.RANDOM_BITS - bits
				val mask = ((1 shl bits) - 1)
				val node = ThreadLocalRandom.current().nextInt() and mask
				val factory = TsidFactory.create256(node)
				assertEquals(node.toLong(), (factory.create().random ushr shif) and mask.toLong())
			}
		}
		run {
			for (i in 0..20) {
				val bits = TsidFactory.NodeBits.NODE_1024.bits
				val shif = Tsid.RANDOM_BITS - bits
				val mask = ((1 shl bits) - 1)
				val node = ThreadLocalRandom.current().nextInt() and mask
				val factory = TsidFactory.create1024(node)
				assertEquals(node.toLong(), (factory.create().random ushr shif) and mask.toLong())
			}
		}
		run {
			for (i in 0..20) {
				val bits = TsidFactory.NodeBits.NODE_4096.bits
				val shif = Tsid.RANDOM_BITS - bits
				val mask = ((1 shl bits) - 1)
				val node = ThreadLocalRandom.current().nextInt() and mask
				val factory = TsidFactory.create4096(node)
				assertEquals(node.toLong(), (factory.create().random ushr shif) and mask.toLong())
			}
		}
	}

	@Test
	fun testWithNodeBits() {
		val randomBits = 22
		// test all allowed values of node bits
		for (i in 0..20) {
			val counterBits = randomBits - i
			val node = (1 shl i) - 1 // max: 2^nodeBits - 1
			val tsid = TsidFactory(nodeBits = i, node = node).create()
			val actual = tsid.random.toInt() ushr counterBits
			assertEquals(node.toLong(), actual.toLong())
		}
	}

	@Test
	fun testWithNodeCount() {
		val randomBits = 22
		// test all allowed values of node bits
		for (i in 0..20) {
			val counterBits = randomBits - i
			val node = (1 shl i) - 1 // max: 2^nodeBits - 1
			val nodeCount = 2.0.pow(i.toDouble()).toInt()
			System.setProperty(TsidFactory.Settings.NODE_COUNT, nodeCount.toString())
			val tsid = TsidFactory(node = node).create()
			val actual = tsid.random.toInt() ushr counterBits
			assertEquals(node.toLong(), actual.toLong())
		}
	}

	@Test
	fun testWithRandom() {
		val random = Random()
		val factory = TsidFactory(random = random)
		assertNotNull(factory.create())
	}

	@Test
	fun testWithRandomNull() {
		val factory = TsidFactory(random = null)
		assertNotNull(factory.create())
	}

	@Test
	fun testWithRandomFunction() {
		run {
			val random = SplittableRandom()
			val function = IntSupplier { random.nextInt() }
			val factory = TsidFactory(randomSupplier = function)
			assertNotNull(factory.create())
		}
		run {
			val function = IntFunction { length: Int ->
				val bytes = ByteArray(length)
				ThreadLocalRandom.current().nextBytes(bytes)
				bytes
			}
			val factory = TsidFactory(randomFunction = function)
			assertNotNull(factory.create())
		}
	}

	@Test
	@Throws(InterruptedException::class)
	fun testWithRandomFunctionReturningZero() {
		// a random function that returns a fixed array filled with ZEROS

		val randomFunction = IntFunction { x: Int -> ByteArray(x) }

		val factory = TsidFactory(randomFunction = randomFunction)

		val mask: Long = 4095 // counter bits: 12

		// test it 5 times, waiting 1ms each time
		for (i in 0..4) {
			Thread.sleep(1) // wait 1ms
			val expected: Long = 0
			val counter = factory.create().random and mask
			assertEquals("The counter should be equal to ZERO when the ms changes", expected, counter)
		}
	}

	@Test
	@Throws(InterruptedException::class)
	fun testWithRandomFunctionReturningNonZero() {
		// a random function that returns a fixed array

		val fixed = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 127)
		val randomFunction = IntFunction { _: Int -> fixed }

		val factory = TsidFactory(randomFunction = randomFunction)

		val mask: Long = 4095 // counter bits: 12

		// test it 5 times, waiting 1ms each time
		for (i in 0..4) {
			Thread.sleep(1) // wait 1ms
			val expected = fixed[2].toLong()
			val counter = factory.create().random and mask
			assertEquals("The counter should be equal to a fixed value when the ms changes", expected, counter)
		}
	}

	@Test
	@Throws(InterruptedException::class)
	fun testMonotonicityAfterClockDrift() {
		val diff: Long = 10000
		val time = Instant.parse("2021-12-31T23:59:59.000Z").toEpochMilli()
		val times = longArrayOf(time, time + 0, time + 1, time + 2, time + 3 - diff, time + 4 - diff, time + 5)

		val clock: Clock = object : Clock() {
			private var i = 0

			override fun millis(): Long {
				return times[i++ % times.size]
			}

			override fun getZone(): ZoneId? {
				return null
			}

			override fun withZone(zone: ZoneId): Clock? {
				return null
			}

			override fun instant(): Instant? {
				return null
			}
		}

		// a function that forces the clock to restart to ZERO
		val randomFunction = IntFunction { x: Int -> ByteArray(x) }

		val factory = TsidFactory(clock = clock, randomFunction = randomFunction)

		val ms1 = factory.create().unixMilliseconds // time
		val ms2 = factory.create().unixMilliseconds // time + 0
		val ms3 = factory.create().unixMilliseconds // time + 1
		val ms4 = factory.create().unixMilliseconds // time + 2
		val ms5 = factory.create().unixMilliseconds // time + 3 - 10000 (CLOCK DRIFT)
		val ms6 = factory.create().unixMilliseconds // time + 4 - 10000 (CLOCK DRIFT)
		val ms7 = factory.create().unixMilliseconds // time + 5
		assertEquals(ms1 + 0, ms2) // clock repeats.
		assertEquals(ms1 + 1, ms3) // clock advanced.
		assertEquals(ms1 + 2, ms4) // clock advanced.
		assertEquals(ms1 + 2, ms5) // CLOCK DRIFT! DON'T MOVE BACKWARDS!
		assertEquals(ms1 + 2, ms6) // CLOCK DRIFT! DON'T MOVE BACKWARDS!
		assertEquals(ms1 + 5, ms7) // clock advanced.
	}

	@Test
	@Throws(InterruptedException::class)
	fun testMonotonicityAfterLeapSecond() {
		val second = Instant.parse("2021-12-31T23:59:59.000Z").epochSecond
		val leapSecond = second - 1 // simulate a leap second
		val times = longArrayOf(second, leapSecond)

		val clock: Clock = object : Clock() {
			private var i = 0

			override fun millis(): Long {
				return times[i++ % times.size] * 1000
			}

			override fun getZone(): ZoneId? {
				return null
			}

			override fun withZone(zone: ZoneId): Clock? {
				return null
			}

			override fun instant(): Instant? {
				return null
			}
		}

		// a function that forces the clock to restart to ZERO
		// val randomFunction = IntFunction { x: Int -> ByteArray(x) }
		val randomFunction = IntFunction { x: Int -> ByteArray(x) }

		val factory = TsidFactory(clock = clock, randomFunction = randomFunction)

		val ms1 = factory.create().unixMilliseconds // second
		val ms2 = factory.create().unixMilliseconds // leap second

		assertEquals(ms1, ms2) // LEAP SECOND! DON'T MOVE BACKWARDS!
	}

	@Test
	fun testByteRandomNextInt() {
		for (i in 0..9) {
			val bytes = ByteArray(Integer.BYTES)
			Random().nextBytes(bytes)
			val number = ByteBuffer.wrap(bytes).getInt()
			val random: TsidFactory.IRandom = TsidFactory.ByteRandom { _: Int -> bytes }
			assertEquals(number.toLong(), random.nextInt().toLong())
		}

		for (i in 0..9) {
			val ints = 10
			val size = Integer.BYTES * ints

			val bytes = ByteArray(size)
			Random().nextBytes(bytes)
			val buffer1 = ByteBuffer.wrap(bytes)
			val buffer2 = ByteBuffer.wrap(bytes)

			val random: TsidFactory.IRandom = TsidFactory.ByteRandom { x: Int ->
				val octects = ByteArray(x)
				buffer1[octects]
				octects
			}

			for (j in 0 until ints) {
				assertEquals(buffer2.getInt().toLong(), random.nextInt().toLong())
			}
		}
	}

	@Test
	fun testByteRandomNextBytes() {
		for (i in 0..9) {
			val bytes = ByteArray(Integer.BYTES)
			Random().nextBytes(bytes)
			val random: TsidFactory.IRandom = TsidFactory.ByteRandom { _: Int -> bytes }
			assertEquals(bytes.contentToString(), random.nextBytes(Integer.BYTES).contentToString())
		}

		for (i in 0..9) {
			val ints = 10
			val size = Integer.BYTES * ints

			val bytes = ByteArray(size)
			Random().nextBytes(bytes)
			val buffer1 = ByteBuffer.wrap(bytes)
			val buffer2 = ByteBuffer.wrap(bytes)

			val random: TsidFactory.IRandom = TsidFactory.ByteRandom { x: Int ->
				val octects = ByteArray(x)
				buffer1[octects]
				octects
			}

			for (j in 0 until ints) {
				val octects = ByteArray(Integer.BYTES)
				buffer2[octects]
				assertEquals(octects.contentToString(), random.nextBytes(Integer.BYTES).contentToString())
			}
		}
	}

	@Test
	fun testLogRandomNextInt() {
		for (i in 0..9) {
			val bytes = ByteArray(Integer.BYTES)
			Random().nextBytes(bytes)
			val number = ByteBuffer.wrap(bytes).getInt()
			val random: TsidFactory.IRandom = TsidFactory.IntRandom { number }
			assertEquals(number.toLong(), random.nextInt().toLong())
		}

		for (i in 0..9) {
			val ints = 10
			val size = Integer.BYTES * ints

			val bytes = ByteArray(size)
			Random().nextBytes(bytes)
			val buffer1 = ByteBuffer.wrap(bytes)
			val buffer2 = ByteBuffer.wrap(bytes)

			val random: TsidFactory.IRandom = TsidFactory.IntRandom { buffer1.getInt() }

			for (j in 0 until ints) {
				assertEquals(buffer2.getInt().toLong(), random.nextInt().toLong())
			}
		}
	}

	@Test
	fun testLogRandomNextBytes() {
		for (i in 0..9) {
			val bytes = ByteArray(Integer.BYTES)
			Random().nextBytes(bytes)
			val number = ByteBuffer.wrap(bytes).getInt()
			val random: TsidFactory.IRandom = TsidFactory.IntRandom { number }
			assertEquals(bytes.contentToString(), random.nextBytes(Integer.BYTES).contentToString())
		}

		for (i in 0..9) {
			val ints = 10
			val size = Integer.BYTES * ints

			val bytes = ByteArray(size)
			Random().nextBytes(bytes)
			val buffer1 = ByteBuffer.wrap(bytes)
			val buffer2 = ByteBuffer.wrap(bytes)

			val random: TsidFactory.IRandom = TsidFactory.IntRandom { buffer1.getInt() }

			for (j in 0 until ints) {
				val octects = ByteArray(Integer.BYTES)
				buffer2[octects]
				assertEquals(octects.contentToString(), random.nextBytes(Integer.BYTES).contentToString())
			}
		}
	}

	@Test
	fun testSettingsGetNode() {
		for (i in 0..99) {
			val number = ThreadLocalRandom.current().nextInt()
			System.setProperty(TsidFactory.Settings.NODE, number.toString())
			val result = TsidFactory.Settings.node?.toLong()
			assertEquals(number.toLong(), result)
		}
	}

	@Test
	fun testSettingsGetNodeCount() {
		for (i in 0..99) {
			val number = ThreadLocalRandom.current().nextInt()
			System.setProperty(TsidFactory.Settings.NODE_COUNT, number.toString())
			val result = TsidFactory.Settings.nodeCount?.toLong()
			assertEquals(number.toLong(), result)
		}
	}

	@Test
	fun testSettingsGetNodeInvalid() {
		var string = "0xx11223344" // typo
		System.setProperty(TsidFactory.Settings.NODE, string)
		var result = TsidFactory.Settings.node
		assertNull(result)

		string = " 0x11223344" // space
		System.setProperty(TsidFactory.Settings.NODE, string)
		result = TsidFactory.Settings.node
		assertNull(result)

		string = "0x112233zz" // non hexadecimal
		System.setProperty(TsidFactory.Settings.NODE, string)
		result = TsidFactory.Settings.node
		assertNull(result)

		string = "" // empty
		System.setProperty(TsidFactory.Settings.NODE, string)
		result = TsidFactory.Settings.node
		assertNull(result)

		string = " " // blank
		System.setProperty(TsidFactory.Settings.NODE, string)
		result = TsidFactory.Settings.node
		assertNull(result)
	}

	@Test
	fun testSettingsGetNodeCountInvalid() {
		var string = "0xx11223344" // typo
		System.setProperty(TsidFactory.Settings.NODE_COUNT, string)
		var result = TsidFactory.Settings.nodeCount
		assertNull(result)

		string = " 0x11223344" // space
		System.setProperty(TsidFactory.Settings.NODE_COUNT, string)
		result = TsidFactory.Settings.nodeCount
		assertNull(result)

		string = "0x112233zz" // non hexadecimal
		System.setProperty(TsidFactory.Settings.NODE_COUNT, string)
		result = TsidFactory.Settings.nodeCount
		assertNull(result)

		string = "" // empty
		System.setProperty(TsidFactory.Settings.NODE_COUNT, string)
		result = TsidFactory.Settings.nodeCount
		assertNull(result)

		string = " " // blank
		System.setProperty(TsidFactory.Settings.NODE_COUNT, string)
		result = TsidFactory.Settings.nodeCount
		assertNull(result)
	}

	companion object {
		private const val LOOP_MAX = 1000
	}

}
