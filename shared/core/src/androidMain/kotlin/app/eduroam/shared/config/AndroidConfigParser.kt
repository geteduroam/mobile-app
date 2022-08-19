package app.eduroam.shared.config

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser

class AndroidConfigParser : ConfigParser {
    override suspend fun parse(source: ByteArray): WifiConfigData = withContext(Dispatchers.IO) {
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        }

        val xml = String(source, Charsets.UTF_16)
        xml.reader().use { reader ->
            parser.setInput(reader)
            var tag = parser.nextTag()
            while (tag != XmlPullParser.START_TAG && parser.name != "EAPIdentityProviderList") {
                skip(parser)
                tag = parser.next()
            }
            parser.nextTag()
        }

        WifiConfigData(emptyList(), emptyList(), null, null, null)
    }

    private fun readConfig(parser: XmlPullParser) {
        parser.require(XmlPullParser.START_TAG, null, "EAPIdentityProvider")
        skip(parser)
        parser.next()


        var title: String? = null
        var link: String? = null
        var description: String? = null
        var imageUrl: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "title" -> title = readTagText("title", parser)
                "link" -> link = readTagText("link", parser)
                "description" -> description = readTagText("description", parser)
                else -> skip(parser)
            }
        }
    }

    private fun readTagText(tagName: String, parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, tagName)
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, null, tagName)
        return title
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun skip(parser: XmlPullParser) {
        parser.require(XmlPullParser.START_TAG, null, null)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}

fun main() {
    println("Test")
}