class MatchingService(private val orderBooks: MutableMap<String, OrderBook>) {

    private val orderToTicker = mutableMapOf<String, String>()

    fun getOrderBookByTicker(ticker: String): OrderBook {
        return orderBooks.getOrPut(ticker) { OrderBook() }
    }

    fun flush() {
        orderBooks.clear()
    }

    fun indexTickerByOrderId(userId: Int, orderId: Int, ticker: String) {
        orderToTicker["$userId#$orderId"] = ticker
    }

    fun getTickerByOrderId(userId: Int, orderId: Int): String {
        return orderToTicker["$userId#$orderId"] ?: throw IllegalArgumentException("Order ID not found")
    }
}