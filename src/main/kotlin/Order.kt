import java.util.concurrent.atomic.AtomicInteger

class OrderMetadata(
    val orderId: Int,
    val userId: Int,
    var price: Int,
    var quantity: Int,
    val arrivedAt: Int
) {
    override fun equals(other: Any?): Boolean {
        return other is OrderMetadata && orderId == other.orderId && userId == other.userId
    }

    override fun hashCode(): Int {
        return 31 * orderId + userId
    }
}
sealed class Order{
    abstract fun execute(matchingService: MatchingService): List<String>
}

class NewOrder(
    val userId: Int,
    val ticker: String,
    val price: Int,
    val quantity: Int,
    val side: OrderSide,
    val orderId: Int
) : Order() {

    companion object {
        private val counter = AtomicInteger(0)
    }
    override fun toString(): String {
        return "NewOrder[userId: ${userId}, ticker: ${ticker}, price: ${price}, quantity: ${quantity}, side: ${side}, orderId: ${orderId}]"
    }
    override fun execute(service: MatchingService): List<String> {
        val orderBook = service.getOrderBookByTicker(ticker)
        service.indexTickerByOrderId(userId, orderId, ticker)
        val logs = mutableListOf<String>()

        acknowledgeNewOrder(logs)
        addOrder(orderBook, logs)
        val settlementLogs = orderBook.settleTransactions()
        logs.addAll(settlementLogs)

        return logs
    }

    private fun addOrder(orderBook: OrderBook, logs: MutableList<String>) {
        val topOfBookChange = when (side) {
            OrderSide.BID -> orderBook.addBid(OrderMetadata(orderId, userId, if (price == 0) Int.MAX_VALUE else price, quantity, counter.get()))
            OrderSide.ASK -> orderBook.addAsk(OrderMetadata(orderId, userId, price, quantity, counter.get()))
        }
        if (topOfBookChange.isNotEmpty()) {
            logs.add(topOfBookChange)
        }
    }

    private fun acknowledgeNewOrder(logs: MutableList<String>) {
        logs.add("A, $userId, $orderId")
    }
}

class CancelOrder(
    val userId: Int,
    val orderId: Int
) : Order() {
    override fun toString(): String {
        return "CancelOrder[userId: ${userId}, orderId: ${orderId}]"
    }
    override fun execute(service: MatchingService): List<String> {
        val ticker = service.getTickerByOrderId(userId, orderId)
        val orderBook = service.getOrderBookByTicker(ticker)
        val logs = mutableListOf<String>()

        acknowledgeCancelOrder(logs)
        val topOfBookChange = orderBook.cancelOrder(orderId, userId)
        if (topOfBookChange.isNotEmpty()) {
            logs.add(topOfBookChange)
        }
        return logs
    }

    private fun acknowledgeCancelOrder(logs: MutableList<String>) {
        logs.add("C, $userId, $orderId")
    }
}

class FlushOrders : Order() {
    override fun toString(): String {
        return "FlushOrders[]"
    }
    override fun execute(service: MatchingService): List<String> {
        service.flush()
        return emptyList()
    }
}

enum class OrderSide {
    BID, ASK
}

fun orderSideFromString(orderStr: String): OrderSide? {
    return when (orderStr) {
        "B" -> OrderSide.BID
        "S" -> OrderSide.ASK
        else -> null
    }
}