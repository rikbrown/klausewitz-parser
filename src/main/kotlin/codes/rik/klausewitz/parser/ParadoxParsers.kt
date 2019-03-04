package codes.rik.klausewitz.parser

import codes.rik.klausewitz.antlr.ParadoxLexer
import codes.rik.klausewitz.antlr.ParadoxParser
import codes.rik.klausewitz.parser.extensions.toCharStream
import codes.rik.klausewitz.parser.stellaris.ObjectVisitor
import org.antlr.v4.runtime.CommonTokenStream
import java.io.FileInputStream
import java.nio.file.Path

inline fun <reified T: Any> parseParadoxFile(file: Path): T {
    val fis = FileInputStream(file.toFile()).toCharStream()
    val config = ParadoxParser(CommonTokenStream(ParadoxLexer(fis))).config()
    return config.accept(ObjectVisitor(T::class))
}