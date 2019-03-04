package codes.rik.klausewitz.parser.stellaris

import codes.rik.klausewitz.antlr.ParadoxBaseVisitor
import codes.rik.klausewitz.antlr.ParadoxParser
import codes.rik.klausewitz.antlr.ParadoxParser.AssignmentContext
import codes.rik.klausewitz.antlr.ParadoxVisitor
import codes.rik.kotlinbits.strings.pluralise
import codes.rik.kotlinbits.strings.toCamelCase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

private fun resolveVisitor(name: String?, type: KType): ParadoxVisitor<*> {
    val classifierClass = type.classifier as KClass<*>

    return when (classifierClass) {
        // literals
        String::class -> StringVisitor
        Long::class -> IntegerVisitor
        Double::class -> RealVisitor
        Boolean::class -> BooleanVisitor
        LocalDate::class -> DateVisitor

        // collections
        List::class -> ArrayVisitor(name, type.typeArgument(0))
        Map::class -> MapVisitor(
                name,
                type.typeArgument(0),
                type.typeArgument(1)
        )

        else ->
            when {
                classifierClass.isData -> ObjectVisitor(classifierClass)
                else -> throw IllegalArgumentException("Unexpected type for $name ($type): $classifierClass")
            }
    }
}

class ObjectVisitor<T: Any>(private val clazz: KClass<T>) : ParadoxBaseVisitor<T>() {
    private val constructor = clazz.primaryConstructor ?: throw IllegalStateException("No constructor for $clazz")

    override fun visitConfig(ctx: ParadoxParser.ConfigContext) = visitAssignments(ctx.assignment())
    override fun visitMap(ctx: ParadoxParser.MapContext) = visitAssignments(ctx.assignment())

    /**
     * Iterates through the parameters available on this object, finding assignments to resolve them.
     */
    private fun visitAssignments(assignments: List<AssignmentContext>): T {
        val params = constructor.parameters

        val parameterValues: Map<KParameter, List<ParadoxParser.ValueContext>> = params
            .map { p -> p to assignments.findMatching(p.name).map { it.value() } }
            .toMap()

        val multiValues = parameterValues.filterValues { it.size > 1 }
        val singleValues = parameterValues.filterValues { it.size <= 1  }.mapValues { (_, ctxs) -> ctxs.getOrNull(0) }

        val singleResolvedValues: Map<KParameter, Any?> = singleValues
            .map { (parameter, value) -> parameter to value?.accept(
                resolveVisitor(
                    parameter.name,
                    parameter.type
                )
            ) }
            .toMap()
            .filterValues { it != null }

        val multiResolvedValues: Map<KParameter, List<Any>> = multiValues
            .map { (parameter, values) -> parameter to values.map { it.accept(
                resolveVisitor(
                    parameter.name,
                    parameter.typeArgument(0)
                )
            ) } }
            .toMap()

        val resolvedValues = singleResolvedValues + multiResolvedValues
        return try {
            constructor.callBy(resolvedValues)
        } catch (e: IllegalArgumentException) {
            val debug = resolvedValues.map { (k, v) -> k.name to v }
            throw RuntimeException("Error creating $clazz with $debug", e)
        }
    }

    private fun List<AssignmentContext>.findMatching(parameterName: String?): List<AssignmentContext> {
        return this.filter { assignment ->
            val field = assignment.field().text.stripped

            when (parameterName) {
                field -> true
                field.pluralise() -> true
                field.toCamelCase() -> true
                field.toCamelCase().pluralise() -> true
                else -> false
            }
        }
    }
}

private class ArrayVisitor(private val name: String?, type: KType): ParadoxBaseVisitor<Any>() {
    private val visitor = resolveVisitor(name, type)

    override fun visitArray(ctx: ParadoxParser.ArrayContext): List<Any> = ctx.value().map { it.accept(visitor) }

    override fun visitMap(ctx: ParadoxParser.MapContext): List<Any> {
        val distinctKeys = ctx.assignment().map { it.field().text }.distinct()
        if (distinctKeys.size > 1) throw(IllegalArgumentException("$name multimap has multiple keys: $distinctKeys"))
        return ctx.assignment().map { it.value() }.map { it.accept(visitor) }
    }
}

private class MapVisitor(name: String?, keyType: KType, valueType: KType): ParadoxBaseVisitor<Any>() {
    private val keyVisitor = resolveVisitor(name, keyType)
    private val valueVisitor = resolveVisitor(name, valueType)

    override fun visitMap(ctx: ParadoxParser.MapContext): Map<Any, Any> {
        return ctx.assignment()
            .map { it.field().accept(keyVisitor) to it.value().accept(valueVisitor) }
            .toMap()
    }
}

private object StringVisitor : ParadoxBaseVisitor<String>() {
    override fun visitString(ctx: ParadoxParser.StringContext): String = ctx.text.stripped
    override fun visitSymbol(ctx: ParadoxParser.SymbolContext): String = ctx.text.stripped
}

private object IntegerVisitor : ParadoxBaseVisitor<Long>() {
    override fun visitInteger(ctx: ParadoxParser.IntegerContext): Long = ctx.text.toLong()
    override fun visitSymbol(ctx: ParadoxParser.SymbolContext): Long = ctx.text.toLong()
    override fun visitPercent(ctx: ParadoxParser.PercentContext): Long = ctx.text.removeSuffix("%").toLong()
}

private object RealVisitor : ParadoxBaseVisitor<Double>() {
    override fun visitInteger(ctx: ParadoxParser.IntegerContext): Double = ctx.text.toDouble()
    override fun visitReal(ctx: ParadoxParser.RealContext): Double = ctx.text.toDouble()
    override fun visitSymbol(ctx: ParadoxParser.SymbolContext): Double = ctx.text.toDouble()
}

private object BooleanVisitor : ParadoxBaseVisitor<Boolean>() {
    override fun visitSymbol(ctx: ParadoxParser.SymbolContext): Boolean = when(ctx.text) {
        TRUE -> true
        FALSE -> false
        else -> throw IllegalArgumentException("Expected boolean but got ${ctx.text}")
    }
}

private object DateVisitor : ParadoxBaseVisitor<LocalDate>() {
    override fun visitDate(ctx: ParadoxParser.DateContext): LocalDate = LocalDate.parse(ctx.text,
        dateFormatter
    )
    override fun visitString(ctx: ParadoxParser.StringContext): LocalDate = LocalDate.parse(ctx.text.stripped,
        stellarisDateFormatter
    )
}

private fun KParameter.typeArgument(idx: Int) = type.typeArgument(idx)
private fun KType.typeArgument(idx: Int) = this.arguments[idx].type ?: throw java.lang.IllegalStateException("No type argument $idx on $this")

private val stellarisDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
