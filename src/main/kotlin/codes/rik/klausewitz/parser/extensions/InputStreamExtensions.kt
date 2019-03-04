package codes.rik.klausewitz.parser.extensions

import org.antlr.v4.runtime.CharStreams
import java.io.InputStream

fun InputStream.toCharStream() = CharStreams.fromStream(this)