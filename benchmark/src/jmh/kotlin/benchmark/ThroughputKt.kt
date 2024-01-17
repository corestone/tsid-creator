package benchmark

import kr.waymakers.tsid.Tsid
import kr.waymakers.tsid.TsidCreator
import org.openjdk.jmh.annotations.*
import java.util.*
import java.util.concurrent.TimeUnit

@Fork(1)
@Threads(4)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 3)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class ThroughputKt {

	@Benchmark
	fun UUID_randomUUID(): UUID {
		return UUID.randomUUID()
	}

	@Benchmark
	fun UUID_randomUUID_toString(): String {
		return UUID.randomUUID().toString()
	}

	@Benchmark
	fun Tsid_fast(): Tsid {
		return Tsid.fast()
	}

	@Benchmark
	fun Tsid_fast_toString(): String {
		return Tsid.fast().toString()
	}

	@Benchmark
	fun TsidCreator_getTsid0256(): Tsid {
		return TsidCreator.tsid256
	}

	@Benchmark
	fun TsidCreator_getTsid0256_toString(): String {
		return TsidCreator.tsid256.toString()
	}

	@Benchmark
	fun TsidCreator_getTsid1024(): Tsid {
		return TsidCreator.tsid1024
	}

	@Benchmark
	fun TsidCreator_getTsid1024_toString(): String {
		return TsidCreator.tsid1024.toString()
	}

	@Benchmark
	fun TsidCreator_getTsid4096(): Tsid {
		return TsidCreator.tsid4096
	}

	@Benchmark
	fun TsidCreator_getTsid4096_toString(): String {
		return TsidCreator.tsid4096.toString()
	}

}
