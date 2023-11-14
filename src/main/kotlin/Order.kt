sealed class Order

class NewOrder(
    val userId: Int,
    val ticker: String,
    val price: Int,
    val quantity: Int,
    val side: OrderSide,
    val orderId: Int
) : Order() {
    override fun toString(): String {
        return "NewOrder[userId: ${userId}, ticker: ${ticker}, price: ${price}, quantity: ${quantity}, side: ${side}, orderId: ${orderId}]"
    }
}

class CancelOrder(
    val userId: Int,
    val orderId: Int
) : Order() {
    override fun toString(): String {
        return "CancelOrder[userId: ${userId}, orderId: ${orderId}]"
    }
}

class FlushOrders : Order() {
    override fun toString(): String {
        return "FlushOrders[]"
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