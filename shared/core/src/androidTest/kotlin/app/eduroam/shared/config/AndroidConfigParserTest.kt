package app.eduroam.shared.config

import app.eduroam.shared.Greeting
import junit.framework.Assert.assertTrue
import org.junit.Test

class AndroidConfigParserTest {

    @Test
    fun testExample() {
        assertTrue("Check Android is mentioned", Greeting().greeting().contains("Android"))
    }
}