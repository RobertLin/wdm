package org.school.core

import java.io.Serializable

/**
 * Represents an ordered sequence of itemsets
 *
 * @param sets The itemsets composing this Transaction
 */
class Transaction[T] private (val sets:List[ItemSet[T]])
    extends Serializable {

    /** The number of itemsets in this transaction */
    def size()     = sets.size

    /** The total number of items in all the itemsets */
    def length()   = sets.foldLeft(0) { (t,s) => t + s.items.size }

    /** A flat list of all the unique items in this transaction */
    def unique()   = sets.map { _.items }.flatten.toSet

    /** A flat list of every item in this transaction */
    def allItems() = sets.map { _.items }.flatten.toList

    /**
     * Checks if the supplied transaction is a contiguous
     * subset of this transaction.
     *
     * @param other The other transaction to test
     * @return true if successful, false otherwise
     */
	def contains(other:Transaction[T]) : Boolean = {
        sets.zip(other.sets).foldLeft(true) { (result, itemset) =>
            result & itemset._2.items.subsetOf(itemset._1.items)
        }
    }

    /**
     * Retrieve the minimum support for this transaction set
     *
     * @param support The support lookup table
     * @return The minimum support for this collection
     */
    def minsup(support:AbstractSupport[T]) =
        sets.map { s => s.minsup(support) }.min
}

object Transaction {
    def apply[T](items:List[ItemSet[T]]) = new Transaction[T](items)
    def apply[T](items:ItemSet[T]*) = new Transaction[T](items.toList)
}
