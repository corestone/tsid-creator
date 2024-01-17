package kr.waymakers.tsid

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
	TsidTest::class,
	TsidBaseNTest::class,
	TsidFactoryTest::class,
	TsidFormatTest::class,
	TsidFactory00001Test::class,
	TsidFactory00064Test::class,
	TsidFactory00256Test::class,
	TsidFactory01024Test::class,
	TsidFactory04096Test::class,
	TsidFactory16384Test::class,
	IncrementalTest::class,
	CollisionTest::class
)
/**
 *
 * It bundles all JUnit test cases.
 *
 * Also see [UniquenesTest].
 *
 */
class TestSuite
