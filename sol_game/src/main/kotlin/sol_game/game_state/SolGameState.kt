package sol_game.game_state

import org.joml.Vector2f
import sol_engine.ecs.World
import sol_game.core_game.Ability


data class SolGameState(
        val gameStarted: Boolean,
        val gameEnded: Boolean,
        val playerIndexWon: Int,
        val staticGameState: SolStaticGameState,
        val charactersState: List<SolCharacterState>,
        val world: World
)

data class SolCharacterState(
        val physicalObject: CircleObjectState = CircleObjectState(Vector2f(), 0f),
        val velocity: Vector2f = Vector2f(),
        val acceleration: Vector2f = Vector2f(),
        val rotation: Float = 0f,
        val damage: Float = 0f,
        val stocks: Int = 0,
        val stateTag: SolCharacterStateTag = SolCharacterStateTag.NO_PLAYER_PRESENT,
        val abilities: List<Ability> = emptyList(),
        val currentHitboxes: List<SolHitboxState> = emptyList()
)

enum class SolCharacterStateTag {
    NO_PLAYER_PRESENT,
    CONTROLLED,
    HITSTUN,
    ABILITY_STARTUP,
    ABILITY_EXECUTION,
    ABILITY_ENDLAG
}

data class SolHitboxState(
        val entityName: String,
        val physicalObject: CircleObjectState,
        val velocity: Vector2f,
        val damage: Float,
        val baseKnockback: Float,
        val knockbackRatio: Float,
        val knockbackPoint: Float,
        val knockbackTowardsPoint: Boolean,
        val hitsGivenNow: List<HitboxHitState>
)

data class HitboxHitState(
        val interactionVector: Vector2f = Vector2f(),  // a vector representing the direction of knockback
        val damage: Float = 0f
)

data class SolStaticGameState(
        val worldSize: Vector2f,
        val walls: List<ObjectState>,
        val holes: List<ObjectState>
)

abstract class ObjectState {
    abstract val position: Vector2f
}

data class CircleObjectState(
        override val position: Vector2f,
        val radius: Float
) : ObjectState()

// center position
data class RectangleObjectState(
        override val position: Vector2f,
        val size: Vector2f
) : ObjectState()

fun emptyHitboxState() = SolHitboxState(
        entityName = "",
        physicalObject = CircleObjectState(Vector2f(), 0f),
        velocity = Vector2f(),
        damage = 0f,
        baseKnockback = 0f,
        knockbackRatio = 0f,
        knockbackPoint = 0f,
        knockbackTowardsPoint = false,
        hitsGivenNow = listOf()
)

fun emptyCharacterState() = SolCharacterState()

fun emptyStaticGameState() = SolStaticGameState(
        worldSize = Vector2f(),
        walls = listOf(),
        holes = listOf()
)

fun emptyGameState() = SolGameState(
        gameStarted = true,
        gameEnded = false,
        playerIndexWon = -1,
        staticGameState = emptyStaticGameState(),
        charactersState = listOf(emptyCharacterState(), emptyCharacterState()),
        world = World()
)