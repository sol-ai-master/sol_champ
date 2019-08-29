package sol_engine.physics_module;

import org.joml.Vector2f;
import sol_engine.core.TransformComp;
import sol_engine.ecs.SystemBase;

public class PhysicsSystem extends SystemBase {

    private final Vector2f tempVec = new Vector2f();

    @Override
    public void onStart() {
        usingComponents(PhysicsBodyComp.class);
    }

    @Override
    public void onUpdate() {
        groupEntities.forEach(entity -> {
            PhysicsBodyComp physComp = entity.getComponent(PhysicsBodyComp.class);
            TransformComp transComp = entity.getComponent(TransformComp.class);

            physComp.velocity.add(physComp.acceleration.mul(PhysicsConstants.FIXED_UPDATE_TIME, tempVec));
            physComp.velocity.add(physComp.impulse);

            physComp.velocity.mul(PhysicsConstants.FIXED_UPDATE_TIME, tempVec);
            transComp.x += tempVec.x;
            transComp.y += tempVec.y;

            physComp.acceleration.zero();
            physComp.impulse.zero();
        });
    }

    @Override
    public void onEnd() {

    }
}