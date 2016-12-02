package org.terasology.funnyblocks.system;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;

import java.io.Console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.logic.characters.CharacterImpulseEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.entitySystem.systems.RegisterMode;

import org.terasology.funnyblocks.component.BouncyBlockComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class BouncySystem extends BaseComponentSystem implements UpdateSubscriberSystem 
{

	@In
	private BlockEntityRegistry blockEntityProvider;

	@In
	private Time time;

	@In
	private EntityManager entityManager;

	@In
	private WorldProvider worldProvider;

	@Override
	public void update(float delta) 
	{

	}
	
	private static final Logger logger = LoggerFactory.getLogger(BouncySystem.class);

	@ReceiveEvent
	public void onEnterBlock(OnEnterBlockEvent event, EntityRef player) 
	{
		if (!isAtHeadLevel(event.getCharacterRelativePosition(), player)) 
		{
			return;
		}
		
		Block block = event.getNewBlock();
		if(isBouncyBlock(block)){
			Vector3f playerPosition = new Vector3f(player.getComponent(LocationComponent.class).getWorldPosition());
			if (worldProvider.getBlock(playerPosition.subY(1)).getEntity().getComponent(BouncyBlockComponent.class) != null)
			{
				Vector3f impulse = playerPosition.normalize();
				impulse.set(impulse.subY(1));
				player.send(new CharacterImpulseEvent(impulse));
			}			
		}
	}
	
	/**
	 * Check is the entity at head level
	 * @param relativePosition the specified position 
	 * @param entity                the enity
	 * @return                          true if the enitity is at head level of the specified position 
	 */
	private boolean isAtHeadLevel(Vector3i relativePosition, EntityRef entity) 
	{
		CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
		return (int) Math.ceil(characterMovementComponent.height) - 1 == relativePosition.y;
	}
	
	/**
	 * Check the block type
	 * @param block  the block
	 * @return           true if the block has BouncyBlockComponent
	 */
	private boolean isBouncyBlock(Block block)
	{
        return block.getEntity().hasComponent(BouncyBlockComponent.class);
	}
}