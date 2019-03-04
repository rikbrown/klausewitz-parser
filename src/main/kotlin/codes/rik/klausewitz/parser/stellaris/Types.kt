package codes.rik.klausewitz.parser.stellaris

import java.time.LocalDate

data class GameState(
    val version: String,
    val name: String,
    val date: LocalDate,
    val requiredDlcs: List<String>,
    val players: List<Player>,
    val species: List<Species>,
    val planets: Map<Long, Planet>,
    val countries: Map<Long, Country>
)

data class Player(
    val name: String,
    val country: Long)

data class Species(
    val name: String,
    val plural: String = name,
    val traits: List<String>)

data class Planet(
    val name: String,
    val planetClass: String,
    val planetSize: Long,
    val colonizeDate: LocalDate? = null,
    val pops: List<Long> = listOf(),
    val districts: List<String> = listOf(),
    val branchOfficeBuildings: List<String> = listOf(),
    val freeAmenities: Double,
    val freeHousing: Double,
    val growth: Double = 0.0,
    val growthSpecies: Long? = null,
    val stability: Double = 0.0,
    val migration: Double = 0.0,
    val crime: Double,
    val builtArmies: Long = 0
)

data class Country(
    val name: String,
    val flag: Flag,
    val techStatus: TechStatus,
    val autoShipDesigns: Boolean = false,
    val budget: Budget,
    val traditions: List<String> = listOf(),
    val ownedMegastructures: List<String> = listOf(),
    val ascensionPerks: List<String> = listOf(),
    val militaryPower: Double = 0.0,
    val economyPower: Double = 0.0,
    val victoryRank: Long? = null,
    val victoryScore: Double = 0.0,
    val techPower: Double = 0.0,
    val immigration: Double = 0.0,
    val emigration: Double = 0.0,
    val fleetSize: Double = 0.0,
    val empireSize: Long = 0,
    val empireCohesion: Double? = null,
    val graphicalCulture: String? = null,
    val ai: AiBehaviour? = null

) {
    data class Flag(
        val colors: List<String>,
        val icon: Icon? = null,
        val background: Background? = null
    ) {
        data class Icon(val file: String)
        data class Background(val file: String)
    }

    data class Budget(
        val lastMonth: BudgetMonth
    )

    data class BudgetMonth(
        val income: BudgetSheet,
        val expenses: BudgetSheet,
        val balance: BudgetSheet
    )

    data class BudgetSheet(
        val countryBase: BudgetValues = BudgetValues.ZERO,
        val tradeRoutes: BudgetValues = BudgetValues.ZERO,
        val colonies: BudgetValues = BudgetValues.ZERO,
        val ships: BudgetValues = BudgetValues.ZERO,
        val shipComponents: BudgetValues = BudgetValues.ZERO,
        val stationGatherers: BudgetValues = BudgetValues.ZERO,
        val stationResearchers: BudgetValues = BudgetValues.ZERO,
        val starbaseStations: BudgetValues = BudgetValues.ZERO,
        val starbaseBuildings: BudgetValues = BudgetValues.ZERO,
        val starbaseModules: BudgetValues = BudgetValues.ZERO,
        val planetBuildings: BudgetValues = BudgetValues.ZERO,
        val planetDistricts: BudgetValues = BudgetValues.ZERO,
        val planetJobs: BudgetValues = BudgetValues.ZERO,
        val planetPopAssemblers: BudgetValues = BudgetValues.ZERO,
        val planetFarmers: BudgetValues = BudgetValues.ZERO,
        val planetMiners: BudgetValues = BudgetValues.ZERO,
        val planetTechnicians: BudgetValues = BudgetValues.ZERO,
        val planetAdministrators: BudgetValues = BudgetValues.ZERO,
        val planetResearchers: BudgetValues = BudgetValues.ZERO,
        val planetMetallurgists: BudgetValues = BudgetValues.ZERO,
        val planetCultureWorkers: BudgetValues = BudgetValues.ZERO,
        val planetEntertainers: BudgetValues = BudgetValues.ZERO,
        val planetPriests: BudgetValues = BudgetValues.ZERO,
        val planetEnforcers: BudgetValues = BudgetValues.ZERO,
        val planetDoctors: BudgetValues = BudgetValues.ZERO,
        val planetTranslucers: BudgetValues = BudgetValues.ZERO,
        val planetChemists: BudgetValues = BudgetValues.ZERO,
        val planetArtisans: BudgetValues = BudgetValues.ZERO,
        val popCategorySlaves: BudgetValues = BudgetValues.ZERO,
        val popCategoryRobots: BudgetValues = BudgetValues.ZERO,
        val popCategoryWorkers: BudgetValues = BudgetValues.ZERO,
        val popCategorySpecialists: BudgetValues = BudgetValues.ZERO,
        val popCategoryRulers: BudgetValues = BudgetValues.ZERO,
        val orbitalMiningDeposits: BudgetValues = BudgetValues.ZERO,
        val orbitalResearchDeposits: BudgetValues = BudgetValues.ZERO,
        val armies: BudgetValues = BudgetValues.ZERO,
        val popFactions: BudgetValues = BudgetValues.ZERO,
        val rivalries: BudgetValues = BudgetValues.ZERO
    )

    data class BudgetValues(
        val energy: Double = 0.0,
        val minerals: Double = 0.0,
        val food: Double = 0.0,
        val physicsResearch: Double = 0.0,
        val societyResearch: Double = 0.0,
        val engineeringResearch: Double = 0.0,
        val influence: Double = 0.0,
        val unity: Double = 0.0,
        val alloys: Double = 0.0,
        val consumerGoods: Double = 0.0,
        val rareCrystals: Double = 0.0,
        val volatileMotes: Double = 0.0,
        val exoticGases: Double = 0.0,
        val srDarkMatter: Double = 0.0,
        val srZro: Double = 0.0,
        val nanites: Double = 0.0
    ) {
        companion object {
            val ZERO = BudgetValues()
        }
    }

    data class TechStatus(
        val technologies: List<String> = listOf()
    )

    data class AiBehaviour(
        val attitude: List<AiAttitude> = listOf()
    ) {
        data class AiAttitude(
            val country: Long,
            val attitude: String
        )
    }
}


