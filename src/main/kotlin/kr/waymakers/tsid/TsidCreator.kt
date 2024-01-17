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

object TsidCreator {

	val tsid: Tsid
		/**
		 * Returns a new TSID.
		 *
		 *
		 * The node ID is is set by defining the system property "tsidcreator.node" or
		 * the environment variable "TSIDCREATOR_NODE". One of them **should** be
		 * used to embed a machine ID in the generated TSID in order to avoid TSID
		 * collisions. If that property or variable is not defined, the node ID is
		 * chosen randomly.
		 *
		 *
		 * The amount of nodes can be set by defining the system property
		 * "tsidcreator.node.count" or the environment variable
		 * "TSIDCREATOR_NODE_COUNT". That property or variable is used to adjust the
		 * minimum amount of bits to accommodate the node ID. If that property or
		 * variable is not defined, the default amount of nodes is 1024, which takes 10
		 * bits.
		 *
		 *
		 * The amount of bits needed to accommodate the node ID is calculated by this
		 * pseudo-code formula: `node_bits = ceil(log(node_count)/log(2))`.
		 *
		 *
		 * Random component settings:
		 *
		 *  * Node bits: node_bits
		 *  * Counter bits: 22-node_bits
		 *  * Maximum node: 2^node_bits
		 *  * Maximum counter: 2^(22-node_bits)
		 *
		 *
		 *
		 * The time component can be 1 ms or more ahead of the system time when
		 * necessary to maintain monotonicity and generation speed.
		 *
		 * @return a TSID
		 * @since 5.1.0
		 */
		get() = FactoryHolder.INSTANCE.create()

	val tsid256: Tsid
		/**
		 * Returns a new TSID.
		 *
		 *
		 * It supports up to 256 nodes.
		 *
		 *
		 * It can generate up to 16,384 TSIDs per millisecond per node.
		 *
		 *
		 * The node ID is is set by defining the system property "tsidcreator.node" or
		 * the environment variable "TSIDCREATOR_NODE". One of them **should** be
		 * used to embed a machine ID in the generated TSID in order to avoid TSID
		 * collisions. If that property or variable is not defined, the node ID is
		 * chosen randomly.
		 *
		 *
		 *
		 * Random component settings:
		 *
		 *  * Node bits: 8
		 *  * Counter bits: 14
		 *  * Maximum node: 256 (2^8)
		 *  * Maximum counter: 16,384 (2^14)
		 *
		 *
		 *
		 * The time component can be 1 ms or more ahead of the system time when
		 * necessary to maintain monotonicity and generation speed.
		 *
		 * @return a TSID
		 */
		get() = Factory256Holder.INSTANCE.create()

	val tsid1024: Tsid
		/**
		 * Returns a new TSID.
		 *
		 *
		 * It supports up to 1,024 nodes.
		 *
		 *
		 * It can generate up to 4,096 TSIDs per millisecond per node.
		 *
		 *
		 * The node ID is is set by defining the system property "tsidcreator.node" or
		 * the environment variable "TSIDCREATOR_NODE". One of them **should** be
		 * used to embed a machine ID in the generated TSID in order to avoid TSID
		 * collisions. If that property or variable is not defined, the node ID is
		 * chosen randomly.
		 *
		 *
		 * Random component settings:
		 *
		 *  * Node bits: 10
		 *  * Counter bits: 12
		 *  * Maximum node: 1,024 (2^10)
		 *  * Maximum counter: 4,096 (2^12)
		 *
		 *
		 *
		 * The time component can be 1 ms or more ahead of the system time when
		 * necessary to maintain monotonicity and generation speed.
		 *
		 * @return a TSID
		 */
		get() = Factory1024Holder.INSTANCE.create()

	val tsid4096: Tsid
		/**
		 * Returns a new TSID.
		 *
		 *
		 * It supports up to 4,096 nodes.
		 *
		 *
		 * It can generate up to 1,024 TSIDs per millisecond per node.
		 *
		 *
		 * The node ID is is set by defining the system property "tsidcreator.node" or
		 * the environment variable "TSIDCREATOR_NODE". One of them **should** be
		 * used to embed a machine ID in the generated TSID in order to avoid TSID
		 * collisions. If that property or variable is not defined, the node ID is
		 * chosen randomly.
		 *
		 *
		 * Random component settings:
		 *
		 *  * Node bits: 12
		 *  * Counter bits: 10
		 *  * Maximum node: 4,096 (2^12)
		 *  * Maximum counter: 1,024 (2^10)
		 *
		 *
		 *
		 * The time component can be 1 ms or more ahead of the system time when
		 * necessary to maintain monotonicity and generation speed.
		 *
		 * @return a TSID number
		 */
		get() = Factory4096Holder.INSTANCE.create()

	private object FactoryHolder {
		val INSTANCE: TsidFactory = TsidFactory()
	}

	private object Factory256Holder {
		val INSTANCE: TsidFactory = TsidFactory.create256()
	}

	private object Factory1024Holder {
		val INSTANCE: TsidFactory = TsidFactory.create1024()
	}

	private object Factory4096Holder {
		val INSTANCE: TsidFactory = TsidFactory.create4096()
	}

}
