package sol_game.core_game.systems

import sol_engine.ecs.SystemBase
import sol_engine.game_utils.MoveByVelocityComp
import sol_game.core_game.components.AbilityComp
import sol_game.core_game.components.ControlDisabledComp
import sol_game.core_game.components.FaceAimComp
import sol_game.core_game.components.MovementComp

class ControlDisabledSystem : SystemBase() {

    override fun onSetup() {
        usingComponents(
                ControlDisabledComp::class.java,
                MovementComp::class.java,
                FaceAimComp::class.java,
                AbilityComp::class.java
        )
    }

    override fun onUpdate() {
        forEachWithComponents(
                ControlDisabledComp::class.java,
                MovementComp::class.java,
                FaceAimComp::class.java,
                AbilityComp::class.java
        ) { _, disabledComp, moveComp, aimComp, abComp ->
            if (disabledComp.disabledTimer > 0) {
                disabledComp.disabledTimer--

                moveComp.disabled = true
                aimComp.disabled = true
                abComp.executionDisabled = true
            } else if (disabledComp.disabledTimer == 0) {
                disabledComp.disabledTimer = -1
                moveComp.disabled = false
                aimComp.disabled = false
                abComp.executionDisabled = false
            }
        }
    }

}