package org.school.association

import org.slf4j.{Logger, LoggerFactory}
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import org.school.core.{AbstractSupport}
import org.school.core.{ItemSet, Transaction, FrequentSet}
import org.school.utility.Stopwatch

/**
 * This is an implementation of the Minimum Support Prefix Span algorithm
 * outlined in WDM. Given a collection of transactions * and an item
 * support lookup table, it will generate a a listing of frequent
 * transactions as well as their support count.
 *
 * @param sequences The collection of transactions to process
 * @param support The support lookup table for each item
 */
class PrefixSpan[T](val sequences:List[Transaction[T]],
    val support:AbstractSupport[T]) {

    /** This is N represented in the gsp algorithm */
    private val sizeN = sequences.size.doubleValue
	private val logger = LoggerFactory.getLogger(this.getClass)
	private val stopwatch = new Stopwatch()

    /**
     * Processes the current sequence list to produce all the
     * frequent sequences with the supplied support.
     *
     * @return A list of frequent itemsets
     */
    def process() : List[FrequentSet[T]] = {
        stopwatch.start
        val frequent  = initialize()
        val frequents = buildFrequents(frequent)

        logger.debug("processing took " + stopwatch.toString)
        frequents
    }

    /**
     * Generate all the unique items in the list of transactions
     * Note: this comprises the following portions of the algorith:
     * * M: sort(I,MS)
     * * L: init-pass(M, S)
     *
     * @return A list of all the unique items
     */
    private def initialize() : FrequentSet[T] = {
        val allItems = sequences.map { _.unique }.flatten                           // I with repeats
        val counts   = allItems groupBy identity mapValues { _.size }               // I.count
        val actual   = HashMap[T,Double]()                                          // I.support
        val unique   = counts.keys.toList                                           // I
        val sorted   = unique.sortWith { (a,b) => support.get(a) < support.get(b) } // M
        sorted.foreach { s => actual(s) = counts(s) / sizeN }                       // populate supports

        val start    = sorted.findIndexOf { s  => actual(s) >= support.get(s) }     // first support > minsup
		val minsup   = support.get(sorted(start))                                   // its index

        val level1   = sorted.slice(start, sorted.size).filter { s =>               // L
            actual(s) >= minsup }
        val filtered = level1.filter { s => actual(s) >= support.get(s) }           // <F1>

        logger.debug("initialization took " + stopwatch.toString)
        logger.debug("generated initial candidates: " + filtered)
        FrequentSet(filtered.map { x => {                                           // <{F1}>
			val transaction = Transaction(List(ItemSet(x)), counts(x))
            transaction.support = actual(x)
            transaction
       }})
    }

    /**
     * Given a possible candidate set, search the sequences to see if any
     * of the candidates are frequent
     *
     * @param candidate The initial candidate to explore
     * @return The frequent candidate list
     */
    private def buildFrequents(candidate:FrequentSet[T]) : List[FrequentSet[T]] = {

        val frequents = ListBuffer[FrequentSet[T]](candidate)

		candidate.transactions.foreach { transaction =>
	        val sk = initializeProjections(transaction)                 // projections
            val count = math.ceil(transaction.minsup(support) * sizeN).intValue  // count(MIS(ik))
            val frequent = removeInfrequent(sk, count)                  // local frequent

            frequent.foreach { ik =>
                val result = restrictedPrefixSpan(ik, sk, count) 
            }
		}

        frequents.toList
    }

    /**
     * Helper method to test that two items exceed the support
     * difference constraint.
     *
     * @param left The left item to test
     * @param right The right item to test
     * @return The result of the test
     */
    private def evaluateSdc(left:Transaction[T], right:Transaction[T]) =
        math.abs { left.support - right.support } <= support.sdc

    /**
     * Helper method to test that two items exceed the support
     * difference constraint.
     *
     * @param left The left item to test
     * @param right The right item to test
     * @return The result of the test
     */
    private def evaluateSdc(left:T, right:T) =
        math.abs { support.get(left) - support.get(right) } <= support.sdc

    /**
     * Given a possible candidate set, search the sequences to see if any
     * of the candidates are frequent
     *
     * @param candidate The initial candidate to explore
     * @return The frequent candidate list
     */
    private def initializeProjections(transaction:Transaction[T], frequents:FrequentSet[T])
		: List[Transaction[T]] = {
		def extract(needle:ItemSet[T], source:ItemSet[T]) : ItemSet[T] = {

		}

        val possible = ListBuffer[Transaction[T]]()
        val ik = transaction.sets.head.items.head                       // current frequent item

		sequences.foreach { sequence =>                                 // check every sequence for possible match
			if (sequence contains transaction) {                        // only sequences with ik
                val pruned = sequence.sets.map { set =>                 // remove all items that don't meet
                    val filtered = set.items.filter { item =>           // the minimum support (sdc)
                        evaluateSdc(ik, item) }
                    ItemSet(filtered)                                   // new filtered itemset
                }
                if ((pruned.size > 1) || (pruned.head.size > 1)) {      // eliminate prefix only patterns
                    possible += Transaction(pruned)
                }
			}
		}

		possible.toList
	}

    /**
     * Given a possible candidate set, search the sequences to see if any
     * of the candidates are frequent
     *
     * @param candidate The initial candidate to explore
     * @return The frequent candidate list
     */
    private def restrictedPrefixSpan(ik:Transaction[T], sk:List[Transaction[T]],
        minsup:Int) : {
    }
}