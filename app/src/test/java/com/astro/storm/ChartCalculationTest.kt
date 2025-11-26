package com.astro.storm

import com.astro.storm.data.model.BirthData
import com.astro.storm.data.model.HouseSystem
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.ephemeris.ChartCalculator
import com.astro.storm.ephemeris.SwissEphemerisEngine
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
class ChartCalculationTest {

    private lateinit var swissEphemerisEngine: SwissEphemerisEngine
    private lateinit var chartCalculator: ChartCalculator

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        swissEphemerisEngine = SwissEphemerisEngine(context)
        chartCalculator = ChartCalculator()
    }

    @Test
    fun testRashiChartCalculation() {
        // Sample birth data (Albert Einstein)
        val birthData = BirthData(
            name = "Test Person",
            dateTime = LocalDateTime.of(1879, 3, 14, 11, 30),
            location = "Ulm, Germany",
            latitude = 48.4011,
            longitude = 9.9876,
            timezone = "Europe/Berlin"
        )

        val vedicChart = swissEphemerisEngine.calculateVedicChart(birthData, HouseSystem.PLACIDUS)
        val rashiChart = chartCalculator.calculateRashiChart(vedicChart)

        // Expected Ascendant for this birth data is Gemini
        assertEquals(ZodiacSign.GEMINI, rashiChart.ascendantSign)

        // Verify the signs in each house
        assertEquals(ZodiacSign.GEMINI, rashiChart.houses[0].sign) // House 1
        assertEquals(ZodiacSign.CANCER, rashiChart.houses[1].sign) // House 2
        assertEquals(ZodiacSign.LEO, rashiChart.houses[2].sign) // House 3
        assertEquals(ZodiacSign.VIRGO, rashiChart.houses[3].sign) // House 4
        assertEquals(ZodiacSign.LIBRA, rashiChart.houses[4].sign) // House 5
        assertEquals(ZodiacSign.SCORPIO, rashiChart.houses[5].sign) // House 6
        assertEquals(ZodiacSign.SAGITTARIUS, rashiChart.houses[6].sign) // House 7
        assertEquals(ZodiacSign.CAPRICORN, rashiChart.houses[7].sign) // House 8
        assertEquals(ZodiacSign.AQUARIUS, rashiChart.houses[8].sign) // House 9
        assertEquals(ZodiacSign.PISCES, rashiChart.houses[9].sign) // House 10
        assertEquals(ZodiacSign.ARIES, rashiChart.houses[10].sign) // House 11
        assertEquals(ZodiacSign.TAURUS, rashiChart.houses[11].sign) // House 12

        // You can add more assertions here to verify planet placements, etc.
    }
}
