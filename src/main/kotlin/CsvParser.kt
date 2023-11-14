
class CsvParser {
    fun parseCsv(csvData: String): Order? {
        val type = csvData[0]

        return when (type) {
            'N' -> {
                val splitData = split(csvData.substring(2), ",")
                val userId = splitData[0].toInt()
                val ticker = splitData[1]
                val price = splitData[2].toInt()
                val quantity = splitData[3].toInt()
                val side = orderSideFromString(splitData[4]) ?: throw IllegalArgumentException("Invalid bid/ask")
                val userOrderId = splitData[5].toInt()
                NewOrder(userId, ticker, price, quantity, side, userOrderId)
            }
            'C' -> {
                val splitData = split(csvData.substring(2), ",")
                val userId = splitData[0].toInt()
                val userOrderId = splitData[1].toInt()
                CancelOrder(userId, userOrderId)
            }
            'F' -> FlushOrders()
            else -> {
                throw IllegalArgumentException("Invalid data")
                null
            }
        }
    }
}

fun split(s: String, delimiter: String): List<String> {
    return s.split(delimiter).filter { it.isNotEmpty() }
}
