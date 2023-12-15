package app.eduroam.geteduroam.extensions

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

@Throws(IOException::class)
fun InputStream.readBytes(): ByteArray {
    val bufLen = 4 * 0x400 // 4KB
    val buf = ByteArray(bufLen)
    var readLen: Int = 0

    ByteArrayOutputStream().use { o ->
        this.use { i ->
            while (i.read(buf, 0, bufLen).also { readLen = it } != -1)
                o.write(buf, 0, readLen)
        }

        return o.toByteArray()
    }
}
