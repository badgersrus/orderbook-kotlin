import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class TestMatchingService {

    @Test
    fun `flushOrderbook`() {
        val orderBooks = HashMap<String, OrderBook>()
        val matchingService = MatchingService(orderBooks)

        val executionResults = FlushOrders().execute(matchingService)

        assertTrue(executionResults.isEmpty())
        assertTrue(orderBooks.isEmpty())
    }
}