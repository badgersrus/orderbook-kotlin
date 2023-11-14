import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestOrderBook {

    private val csvParser = CsvParser()

    private fun runOrderBook(lines: List<String>): List<String> {
        val results = mutableListOf<String>()
        val orderBooks = mutableMapOf<String, OrderBook>()
        val matchingService = MatchingService(orderBooks)

        lines.forEach { line ->
            val order = csvParser.parse(line)
            val executionResults = order!!.execute(matchingService)
            results.addAll(executionResults)
        }

        return results
    }

    @Test
    fun `balanced book`() {
        val res = runOrderBook(listOf(
            "N,1,IBM,10,100,B,1",
            "N,1,IBM,12,100,S,2",
            "N,2,IBM,9,100,B,101",
            "N,2,IBM,11,100,S,102",
            "N,1,IBM,11,100,B,3",
            "N,2,IBM,10,100,S,103",
            "N,1,IBM,10,100,B,4",
            "N,2,IBM,11,100,S,104"
        ))
        assertEquals(listOf(
            "A, 1, 1",
            "B, B, 10, 100",
            "A, 1, 2",
            "B, S, 12, 100",
            "A, 2, 101",
            "A, 2, 102",
            "B, S, 11, 100",
            "A, 1, 3",
            "T, 1, 3, 2, 102, 11, 100",
            "B, S, 12, 100",
            "A, 2, 103",
            "T, 1, 1, 2, 103, 10, 100",
            "B, B, 9, 100",
            "A, 1, 4",
            "B, B, 10, 100",
            "A, 2, 104",
            "B, S, 11, 100"
            // ... other expected results
        ), res)
    }
    @Test
    fun `shallow ask`() {
        val res = runOrderBook(listOf(
            "N,1,VAL,10,100,B,1",
            "N,2,VAL,9,100,B,101",
            "N,2,VAL,11,100,S,102",
            "N,1,VAL,11,100,B,2",
            "N,2,VAL,11,100,S,103"
        ))
        assertEquals(listOf(
            "A, 1, 1",
            "B, B, 10, 100",
            "A, 2, 101",
            "A, 2, 102",
            "B, S, 11, 100",
            "A, 1, 2",
            "T, 1, 2, 2, 102, 11, 100",
            "B, S, -, -",
            "A, 2, 103",
            "B, S, 11, 100"
        ), res)
    }

    @Test
    fun `balanced book limit above best ask`() {
        val res = runOrderBook(listOf(
            "N,1,IBM,10,100,B,1",
            "N,1,IBM,12,100,S,2",
            "N,2,IBM,9,100,B,101",
            "N,2,IBM,11,100,S,102",
            "N,1,IBM,12,100,B,103"
        ))
        assertEquals(listOf(
            "A, 1, 1",
            "B, B, 10, 100",
            "A, 1, 2",
            "B, S, 12, 100",
            "A, 2, 101",
            "A, 2, 102",
            "B, S, 11, 100",
            "A, 1, 103",
            "T, 1, 103, 2, 102, 11, 100",
            "B, S, 12, 100"
        ), res)
    }

    @Test
    fun `balanced book market buy`() {
        val res = runOrderBook(listOf(
            "N,1,IBM,10,100,B,1",
            "N,1,IBM,12,100,S,2",
            "N,2,IBM,9,100,B,101",
            "N,2,IBM,11,100,S,102",
            "N,1,IBM,0,100,B,3"
        ))
        assertEquals(listOf(
            "A, 1, 1",
            "B, B, 10, 100",
            "A, 1, 2",
            "B, S, 12, 100",
            "A, 2, 101",
            "A, 2, 102",
            "B, S, 11, 100",
            "A, 1, 3",
            "T, 1, 3, 2, 102, 11, 100",
            "B, S, 12, 100"
        ), res)
    }

    @Test
    fun `balanced book market sell partial`() {
        val res = runOrderBook(listOf(
            "N,1,IBM,10,100,B,1",
            "N,1,IBM,12,100,S,2",
            "N,2,IBM,9,100,B,101",
            "N,2,IBM,11,100,S,102",
            "N,2,IBM,0,20,S,103",
        ))
        assertEquals(listOf(
            "A, 1, 1",
            "B, B, 10, 100",
            "A, 1, 2",
            "B, S, 12, 100",
            "A, 2, 101",
            "A, 2, 102",
            "B, S, 11, 100",
            "A, 2, 103",
            "T, 1, 1, 2, 103, 10, 20",
            "B, B, 10, 80"
        ), res)
    }

    @Test
    fun `balanced book limit sell partial`() {
        val res = runOrderBook(listOf(
            "N,1,IBM,10,100,B,1",
            "N,1,IBM,12,100,S,2",
            "N,2,IBM,9,100,B,101",
            "N,2,IBM,11,100,S,102",
            "N,2,IBM,10,20,S,103"
        ))
        assertEquals(listOf(
            "A, 1, 1",
            "B, B, 10, 100",
            "A, 1, 2",
            "B, S, 12, 100",
            "A, 2, 101",
            "A, 2, 102",
            "B, S, 11, 100",
            "A, 2, 103",
            "T, 1, 1, 2, 103, 10, 20",
            "B, B, 10, 80",
        ), res)
    }

    @Test
    fun `balanced book multiple offers best bid`() {
        val res = runOrderBook(listOf(
            "N,1,IBM,10,100,B,1",
            "N,1,IBM,12,100,S,2",
            "N,2,IBM,9,100,B,101",
            "N,2,IBM,11,100,S,102",
            "N,2,IBM,10,50,B,103",
            "N,1,IBM,11,50,S,3",
            "N,1,IBM,11,100,B,4",
            "N,2,IBM,10,100,S,104",
        ))
        assertEquals(listOf(
            "A, 1, 1", "B, B, 10, 100",
            "A, 1, 2", "B, S, 12, 100",
            "A, 2, 101",
            "A, 2, 102",
            "B, S, 11, 100",
            "A, 2, 103",
            "A, 1, 3",
            "A, 1, 4",
            "T, 1, 4, 2, 102, 11, 100",
            "B, S, 11, 50", "A, 2, 104",
            "T, 1, 1, 2, 104, 10, 100",
            "B, B, 10, 50"
        ), res)
    }

    @Test
    fun `balanced book cancel behind best bid`() {
        val res = runOrderBook(listOf(
            "N,1,IBM,10,100,B,1",
            "N,1,IBM,12,100,S,2",
            "N,2,IBM,9,100,B,101",
            "N,2,IBM,11,100,S,102",
            "C,1,2",
            "C,2,101"
        ))
        assertEquals(listOf(
            "A, 1, 1",
            "B, B, 10, 100",
            "A, 1, 2",
            "B, S, 12, 100",
            "A, 2, 101",
            "A, 2, 102",
            "B, S, 11, 100",
            "C, 1, 2",
            "C, 2, 101",
        ), res)
    }
}