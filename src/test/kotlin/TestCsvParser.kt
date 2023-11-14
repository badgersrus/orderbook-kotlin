import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestCsvParser {

    @Test
    fun `new order`(){
        val order = "N,123,APPL,200,10,B,1"
        val parsed = CsvParser().parseCsv(order)
        assertEquals("NewOrder[userId: 123, ticker: APPL, price: 200, quantity: 10, side: BID, orderId: 1]", parsed.toString())
    }

    @Test
    fun `cancel order`(){
        val order = "C,123,1"
        val parsed = CsvParser().parseCsv(order)
        assertEquals("CancelOrder[userId: 123, orderId: 1]", parsed.toString())
    }

    @Test
    fun `flush book`(){
        val order = "F"
        val parsed = CsvParser().parseCsv(order)
        assertEquals("FlushOrders[]", parsed.toString())
    }
}