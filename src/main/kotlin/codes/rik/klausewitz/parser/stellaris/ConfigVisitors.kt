package codes.rik.klausewitz.parser.stellaris

import codes.rik.klausewitz.antlr.ParadoxBaseVisitor
import codes.rik.klausewitz.antlr.ParadoxLexer
import codes.rik.klausewitz.antlr.ParadoxParser
import codes.rik.kotlinpieces.collections.toMultimap
import com.google.common.collect.Multimap
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Visitors which parse to untyped Multimaps
// WIP: misses a bunch of cases, use TypedVisitors if possible.

fun parseConfig(input: CharStream) = ParadoxParser(CommonTokenStream(ParadoxLexer(input))).config().toConfigBlock()
fun ParadoxParser.ConfigContext.toConfigBlock() = ConfigVisitor.visit(this)

internal object ConfigVisitor : ParadoxBaseVisitor<ConfigBlock>() {
    override fun visitConfig(ctx: ParadoxParser.ConfigContext) = ctx.assignment().toBlock()
}

internal object AssignmentVisitor : ParadoxBaseVisitor<ConfigAssignment>() {
    override fun visitAssignment(ctx: ParadoxParser.AssignmentContext): ConfigAssignment {
        val field = ctx.field().text.stripped
        val value = ctx.value().accept(ValueVisitor)
        return field to value
    }
}

object ValueVisitor : ParadoxBaseVisitor<Any>() {
    override fun visitSymbol(ctx: ParadoxParser.SymbolContext): Any = when(ctx.text) {
        TRUE -> true
        FALSE -> false
        else -> ctx.text
    }
    override fun visitString(ctx: ParadoxParser.StringContext): String = ctx.text.stripped
    override fun visitInteger(ctx: ParadoxParser.IntegerContext): Long = ctx.text.toLong()
    override fun visitReal(ctx: ParadoxParser.RealContext): Double = ctx.text.toDouble()
    override fun visitDate(ctx: ParadoxParser.DateContext): LocalDate = LocalDate.parse(ctx.text,
        dateFormatter
    )
    override fun visitPercent(ctx: ParadoxParser.PercentContext): Int = ctx.text.removeSuffix("%").toInt()
    override fun visitArray(ctx: ParadoxParser.ArrayContext) = ctx.value().map { it.accept(ValueVisitor) }
    override fun visitMap(ctx: ParadoxParser.MapContext) = ctx.assignment().toBlock()
}

private fun List<ParadoxParser.AssignmentContext>.toBlock(): ConfigBlock = this
    .map { it.accept(AssignmentVisitor) }
    .toMultimap()

val String.stripped get() = removeSurrounding("\"")

const val TRUE = "yes"
const val FALSE = "no"

val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

typealias ConfigBlock = Multimap<String, Any>
typealias ConfigAssignment = Pair<String, Any>