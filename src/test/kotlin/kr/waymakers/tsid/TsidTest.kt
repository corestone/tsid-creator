package kr.waymakers.tsid

import org.junit.Assert.*
import org.junit.Test
import java.math.BigInteger
import java.nio.ByteBuffer
import java.time.Instant
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow

class TsidTest {

	@Test
	fun testFromBytes() {
		for (i in 0 until LOOP_MAX) {
			val number0 = ThreadLocalRandom.current().nextLong()
			val buffer = ByteBuffer.allocate(8)
			buffer.putLong(number0)
			val bytes = buffer.array()

			val number1 = Tsid.from(bytes).toLong()
			assertEquals(number0, number1)
		}
	}

	@Test
	fun testToBytes() {
		for (i in 0 until LOOP_MAX) {
			val number = ThreadLocalRandom.current().nextLong()
			val buffer = ByteBuffer.allocate(8)
			buffer.putLong(number)
			val bytes0 = buffer.array()

			val string0 = toString(number)
			val bytes1 = Tsid.from(string0).toBytes()
			assertEquals(bytes0.contentToString(), bytes1.contentToString())
		}
	}

	@Test
	fun testFromString() {
		for (i in 0 until LOOP_MAX) {
			val number0 = ThreadLocalRandom.current().nextLong()
			val string0 = toString(number0)
			val number1 = Tsid.from(string0).toLong()
			assertEquals(number0, number1)
		}
	}

	@Test
	fun testToString() {
		for (i in 0 until LOOP_MAX) {
			val number = ThreadLocalRandom.current().nextLong()
			val string0 = toString(number)
			val string1 = Tsid.from(number).toString()
			assertEquals(string0, string1)
		}
	}

	@Test
	fun testToLowerCase() {
		for (i in 0 until LOOP_MAX) {
			val number = ThreadLocalRandom.current().nextLong()
			val string0 = toString(number).lowercase(Locale.getDefault())
			val string1 = Tsid.from(number).toLowerCase()
			assertEquals(string0, string1)
		}
	}

	@Test
	fun testFromString2() {
		val tsid1 = -0x1L
		val string1 = "FZZZZZZZZZZZZ"
		val result1 = Tsid.from(string1).toLong()
		assertEquals(tsid1, result1)

		val tsid2 = 0x000000000000000aL // 10
		val string2 = "000000000000A"
		val result2 = Tsid.from(string2).toLong()
		assertEquals(tsid2, result2)

		try {
			// Test the first extra bit added by the base32 encoding
			val string3 = "G000000000000"
			Tsid.from(string3)
			fail("Should throw an InvalidTsidException")
		} catch (e: IllegalArgumentException) {
			// success
		}
	}

	@Test
	fun testToString2() {
		val tsid1 = -0x1L
		val string1 = "FZZZZZZZZZZZZ"
		val result1 = Tsid.from(tsid1).toString()
		assertEquals(string1, result1)

		val tsid2 = 0x000000000000000aL // 10
		val string2 = "000000000000A"
		val result2 = Tsid.from(tsid2).toString()
		assertEquals(string2, result2)
	}

	@Test
	fun testToLowerCase2() {
		val tsid1 = -0x1L
		val string1 = "FZZZZZZZZZZZZ".lowercase(Locale.getDefault())
		val result1 = Tsid.from(tsid1).toLowerCase()
		assertEquals(string1, result1)

		val tsid2 = 0x000000000000000aL // 10
		val string2 = "000000000000A".lowercase(Locale.getDefault())
		val result2 = Tsid.from(tsid2).toLowerCase()
		assertEquals(string2, result2)
	}

	@Test
	fun testGetUnixMilliseconds() {
		val start = System.currentTimeMillis()
		val tsid = TsidCreator.tsid1024
		val middle = tsid.unixMilliseconds
		val end = System.currentTimeMillis()

		assertTrue(start <= middle)
		assertTrue(middle <= end + 1)
	}

	@Test
	fun testGetUnixMillisecondsMinimum() {
		// 2020-01-01T00:00:00.000Z

		val expected = Tsid.TSID_EPOCH

		val time1: Long = 0 // the same as 2^42
		val tsid1 = time1 shl RANDOM_BITS
		assertEquals(expected, Tsid.from(tsid1).unixMilliseconds)

		val time2 = 2.0.pow(42.0).toLong()
		val tsid2 = time2 shl RANDOM_BITS
		assertEquals(expected, Tsid.from(tsid2).unixMilliseconds)
	}

	@Test
	fun testGetUnixMillisecondsMaximum() {
		// 2159-05-15T07:35:11.103Z

		val expected = Tsid.TSID_EPOCH + 2.0.pow(42.0).toLong() - 1

		val time1 = 2.0.pow(42.0).toLong() - 1
		val tsid1 = time1 shl RANDOM_BITS
		assertEquals(expected, Tsid.from(tsid1).unixMilliseconds)

		val time2: Long = -1 // the same as 2^42-1
		val tsid2 = time2 shl RANDOM_BITS
		assertEquals(expected, Tsid.from(tsid2).unixMilliseconds)
	}

	@Test
	fun testGetInstant() {
		val start = Instant.now()
		val tsid = TsidCreator.tsid1024
		val middle = tsid.instant
		val end = Instant.now()

		assertTrue(start.toEpochMilli() <= middle.toEpochMilli())
		assertTrue(middle.toEpochMilli() <= end.toEpochMilli())
	}

	@Test
	fun testGetInstantMinimum() {
		val expected = Instant.parse("2020-01-01T00:00:00.000Z")

		val time1: Long = 0 // the same as 2^42
		val tsid1 = time1 shl RANDOM_BITS
		assertEquals(expected, Tsid.from(tsid1).instant)

		val time2 = 2.0.pow(42.0).toLong()
		val tsid2 = time2 shl RANDOM_BITS
		assertEquals(expected, Tsid.from(tsid2).instant)
	}

	@Test
	fun testGetInstantMaximum() {
		val expected = Instant.parse("2159-05-15T07:35:11.103Z")

		val time1 = 2.0.pow(42.0).toLong() - 1
		val tsid1 = time1 shl RANDOM_BITS
		assertEquals(expected, Tsid.from(tsid1).instant)

		val time2: Long = -1 // the same as 2^42-1
		val tsid2 = time2 shl RANDOM_BITS
		assertEquals(expected, Tsid.from(tsid2).instant)
	}

	@Test
	fun testGetTime() {
		for (i in 0 until LOOP_MAX) {
			val number = ThreadLocalRandom.current().nextLong()
			val tsid = Tsid.from(number)

			val time0 = number ushr RANDOM_BITS
			val time1 = tsid.time

			assertEquals(time0, time1)
		}
	}

	@Test
	fun testGetRandom() {
		for (i in 0 until LOOP_MAX) {
			val number = ThreadLocalRandom.current().nextLong()
			val tsid = Tsid.from(number)

			val random0 = number shl TIME_BITS ushr TIME_BITS
			val random1 = tsid.random

			assertEquals(random0, random1)
		}
	}

	@Test
	fun testFastTime() {
		for (i in 0 until LOOP_MAX) {
			val a = System.currentTimeMillis()
			val tsid = Tsid.fast()
			val b = System.currentTimeMillis()

			val time = tsid.unixMilliseconds
			assertTrue(time >= a)
			assertTrue(time <= b + 1)
		}
	}

	@Test
	fun testFastMonotonicity() {
		var prev: Long = 0
		for (i in 0 until LOOP_MAX) {
			val tsid = Tsid.fast()
			// minus to ignore counter rollover
			val next = tsid.toLong() - LOOP_MAX
			assertTrue(next > prev)
			prev = next
		}
	}

	@Test
	fun testIsValid() {
		println(Tsid(Long.MIN_VALUE).instant.toString())
		var tsid: String? = null // Null
		assertFalse("Null tsid should be invalid.", Tsid.isValid(tsid))

		tsid = "" // length: 0
		assertFalse("tsid with empty string should be invalid.", Tsid.isValid(tsid))

		tsid = "0123456789ABC" // All upper case
		assertTrue("tsid in upper case should valid.", Tsid.isValid(tsid))

		tsid = "0123456789abc" // All lower case
		assertTrue("tsid in lower case should be valid.", Tsid.isValid(tsid))

		tsid = "0123456789AbC" // Mixed case
		assertTrue("tsid in upper and lower case should valid.", Tsid.isValid(tsid))

		tsid = "0123456789AB" // length: 12
		assertFalse("tsid length lower than 13 should be invalid.", Tsid.isValid(tsid))

		tsid = "0123456789ABCC" // length: 14
		assertFalse("tsid length greater than 13 should be invalid.", Tsid.isValid(tsid))

		tsid = "0123456789ABi" // Letter I
		assertTrue("tsid with 'i' or 'I' should be valid.", Tsid.isValid(tsid))

		tsid = "0123456789ABl" // Letter L
		assertTrue("tsid with 'i' or 'L' should be valid.", Tsid.isValid(tsid))

		tsid = "0123456789ABo" // Letter O
		assertTrue("tsid with 'o' or 'O' should be valid.", Tsid.isValid(tsid))

		tsid = "0123456789ABu" // Letter U
		assertFalse("tsid with 'u' or 'U' should be invalid.", Tsid.isValid(tsid))

		tsid = "0123456789AB#" // Special char
		assertFalse("tsid with special chars should be invalid.", Tsid.isValid(tsid))

		for (i in 0 until LOOP_MAX) {
			val string = TsidCreator.tsid1024.toString()
			assertTrue(Tsid.isValid(string))
		}
	}

	@Test
	fun testTsidMax256() {
		val maxTsid = 16384
		val maxLoop = 20000

		val list = arrayOfNulls<Tsid>(maxLoop)

		for (i in 0 until maxLoop) {
			// can generate up to 16384 per msec
			list[i] = TsidCreator.tsid256
		}

		var n = 0
		var prevTime: Long = 0
		for (i in 0 until maxLoop) {
			val time = list[i]!!.time
			if (time != prevTime) {
				n = 0
			}
			n++
			prevTime = time
			assertFalse("Too many TSIDs: $n", n > maxTsid)
		}
	}

	@Test
	fun testTsidMax1024() {
		val maxTsid = 4096
		val maxLoop = 10000

		val list = arrayOfNulls<Tsid>(maxLoop)

		for (i in 0 until maxLoop) {
			// can generate up to 4096 per msec
			list[i] = TsidCreator.tsid1024
		}

		var n = 0
		var prevTime: Long = 0
		for (i in 0 until maxLoop) {
			val time = list[i]!!.time
			if (time != prevTime) {
				n = 0
			}
			n++
			prevTime = time
			assertFalse("Too many TSIDs: $n", n > maxTsid)
		}
	}

	@Test
	fun testEquals() {
		val bytes = ByteArray(Tsid.TSID_BYTES)

		for (i in 0 until LOOP_MAX) {
			ThreadLocalRandom.current().nextBytes(bytes)
			val tsid1 = Tsid.from(bytes)
			val tsid2 = Tsid.from(bytes)
			assertEquals(tsid1, tsid2)
			assertEquals(tsid1.toString(), tsid2.toString())
			assertEquals(tsid1.toBytes().contentToString(), tsid2.toBytes().contentToString())

			// change all bytes
			for (j in bytes.indices) {
				bytes[j]++
			}
			val tsid3 = Tsid.from(bytes)
			assertNotEquals(tsid1, tsid3)
			assertNotEquals(tsid1.toString(), tsid3.toString())
			assertNotEquals(tsid1.toBytes().contentToString(), tsid3.toBytes().contentToString())
		}
	}

	@Test
	fun testCompareTo() {
		val bytes = ByteArray(Tsid.TSID_BYTES)

		for (i in 0 until LOOP_MAX) {
			ThreadLocalRandom.current().nextBytes(bytes)
			val tsid1 = Tsid.from(bytes)
			val number1 = BigInteger(1, bytes)

			ThreadLocalRandom.current().nextBytes(bytes)
			val tsid2 = Tsid.from(bytes)
			val tsid3 = Tsid.from(bytes)
			val number2 = BigInteger(1, bytes)
			val number3 = BigInteger(1, bytes)

			// compare numerically
			assertEquals(number1 > number2, tsid1 > tsid2)
			assertEquals(number1 < number2, tsid1 < tsid2)
			assertEquals(number2.compareTo(number3) == 0, tsid2.compareTo(tsid3) == 0)

			// compare lexicographically
			assertEquals(number1 > number2, tsid1.toString() > tsid2.toString())
			assertEquals(number1 < number2, tsid1.toString() < tsid2.toString())
			assertEquals(number2.compareTo(number3) == 0, tsid2.toString().compareTo(tsid3.toString()) == 0)
		}
	}

	@Test
	fun testHashCode() {
		// invoked on the same object

		for (i in 0 until LOOP_MAX) {
			val number = ThreadLocalRandom.current().nextLong()
			val tsid1 = Tsid.from(number)
			assertEquals(tsid1.hashCode().toLong(), tsid1.hashCode().toLong())
		}

		// invoked on two equal objects
		for (i in 0 until LOOP_MAX) {
			val number = ThreadLocalRandom.current().nextLong()
			val tsid1 = Tsid.from(number)
			val tsid2 = Tsid.from(number)
			assertEquals(tsid1.hashCode().toLong(), tsid2.hashCode().toLong())
		}
	}

	@Test
	fun testTsidMax4096() {
		val maxTsid = 1024
		val maxLoop = 10000

		val list = arrayOfNulls<Tsid>(maxLoop)

		for (i in 0 until maxLoop) {
			// can generate up to 1024 per msec
			list[i] = TsidCreator.tsid4096
		}

		var n = 0
		var prevTime: Long = 0
		for (i in 0 until maxLoop) {
			val time = list[i]!!.time
			if (time != prevTime) {
				n = 0
			}
			n++
			prevTime = time
			assertFalse("Too many TSIDs: $n", n > maxTsid)
		}
	}

	fun fromString(tsid: String): Long {
		var number = tsid.substring(0, 10)
		number = transliterate(number, ALPHABET_CROCKFORD, ALPHABET_JAVA)

		return java.lang.Long.parseUnsignedLong(number, 32)
	}

	fun toString(tsid: Long): String {
		val zero = "0000000000000"

		var number = java.lang.Long.toUnsignedString(tsid, 32)
		number = zero.substring(0, zero.length - number.length) + number

		return transliterate(number, ALPHABET_JAVA, ALPHABET_CROCKFORD)
	}

	companion object {
		private const val TIME_BITS = 42
		private const val RANDOM_BITS = 22
		private const val LOOP_MAX = 500

		private val ALPHABET_CROCKFORD = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray()
		private val ALPHABET_JAVA = "0123456789abcdefghijklmnopqrstuv".toCharArray() // Long.parseUnsignedLong()

		private fun transliterate(string: String, alphabet1: CharArray, alphabet2: CharArray): String {
			val output = string.toCharArray()
			for (i in output.indices) {
				for (j in alphabet1.indices) {
					if (output[i] == alphabet1[j]) {
						output[i] = alphabet2[j]
						break
					}
				}
			}
			return String(output)
		}
	}

}
