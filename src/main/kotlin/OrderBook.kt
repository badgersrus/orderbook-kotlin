import java.util.*

class OrderBook {
    private val bids = sortedSetOf<OrderMetadata>(compareByDescending { it.price })
    private val asks = sortedSetOf<OrderMetadata>(compareBy { it.price })

    fun addBid(bidOrderMetadata: OrderMetadata): String {
        val firstBid = bids.firstOrNull()
        bids.add(bidOrderMetadata)
        val firstBidAfterInsert = bids.firstOrNull()
        return if (firstBid == firstBidAfterInsert || isOrderExecutionPossible()) {
            ""
        } else {
            "B, B, ${firstBidAfterInsert?.price}, ${firstBidAfterInsert?.quantity}"
        }
    }

    fun addAsk(askOrderMetadata: OrderMetadata): String {
        val firstAsk = asks.firstOrNull()
        asks.add(askOrderMetadata)
        val firstAskAfterInsert = asks.firstOrNull()
        return if (firstAsk == firstAskAfterInsert || isOrderExecutionPossible()) {
            ""
        } else {
            "B, S, ${firstAskAfterInsert?.price}, ${firstAskAfterInsert?.quantity}"
        }
    }

    fun cancelOrder(orderId: Int, userId: Int): String {
        val searchKey = OrderMetadata(orderId, userId, 0, 0, 0)
        bids.find { it == searchKey }?.let {
            val firstBid = bids.first()
            bids.remove(it)
            val firstBidAfterRemoval = bids.firstOrNull()
            return if (firstBid == firstBidAfterRemoval) {
                ""
            } else {
                "B, B, ${firstBidAfterRemoval?.price}, ${firstBidAfterRemoval?.quantity}"
            }
        }

        asks.find { it == searchKey }?.let {
            val firstAsk = asks.first()
            asks.remove(it)
            val firstAskAfterRemoval = asks.firstOrNull()
            return if (firstAsk == firstAskAfterRemoval) {
                ""
            } else {
                "B, S, ${firstAskAfterRemoval?.price}, ${firstAskAfterRemoval?.quantity}"
            }
        }

        return ""
    }

    fun settleTransactions(): List<String> {
        val results = mutableListOf<String>()
        while (isOrderExecutionPossible()) {
            val bid = bids.first()
            val ask = asks.first()

            if (bid.price == Int.MAX_VALUE && ask.price == 0) {
                throw IllegalArgumentException("Market-market orders are not supported")
            }

            val price = if (bid.arrivedAt > ask.arrivedAt) ask.price else bid.price
            bids.remove(bid)
            asks.remove(ask)

            when {
                bid.quantity == ask.quantity -> {
                    results.add(getFormattedTransaction(bid, ask, price, bid.quantity))
                    if (bid.arrivedAt > ask.arrivedAt) {
                        updateAsksTopOfBook(asks, results)
                    } else {
                        updateBidsTopOfBook(bids, results)
                    }
                }
                bid.quantity > ask.quantity -> {
                    results.add(getFormattedTransaction(bid, ask, price, ask.quantity))
                    bids.add(OrderMetadata(bid.orderId, bid.userId, bid.price, bid.quantity - ask.quantity, bid.arrivedAt))
                    updateBidsTopOfBook(bids, results)
                }
                else -> {
                    results.add(getFormattedTransaction(bid, ask, price, bid.quantity))
                    asks.add(OrderMetadata(ask.orderId, ask.userId, ask.price, ask.quantity - bid.quantity, ask.arrivedAt))
                    updateAsksTopOfBook(asks, results)
                }
            }
        }
        return results
    }

    private fun isOrderExecutionPossible(): Boolean {
        val firstBid = bids.firstOrNull()
        val firstAsk = asks.firstOrNull()
        return firstBid != null && firstAsk != null && (firstAsk.price <= firstBid.price || firstAsk.price == 0 || firstBid.price == 0)
    }

    private fun getFormattedTransaction(bid: OrderMetadata, ask: OrderMetadata, price: Int, quantity: Int): String {
        return "T, ${bid.userId}, ${bid.orderId}, ${ask.userId}, ${ask.orderId}, $price, $quantity"
    }

    private fun updateAsksTopOfBook(asks: SortedSet<OrderMetadata>, results: MutableList<String>) {
        if (asks.isEmpty()) {
            results.add("B, S, -, -")
        } else {
            val newAsk = asks.first()
            results.add("B, S, ${newAsk.price}, ${newAsk.quantity}")
        }
    }

    private fun updateBidsTopOfBook(bids: SortedSet<OrderMetadata>, results: MutableList<String>) {
        if (bids.isEmpty()) {
            results.add("B, B, -, -")
        } else {
            val newBid = bids.first()
            results.add("B, B, ${newBid.price}, ${newBid.quantity}")
        }
    }
}