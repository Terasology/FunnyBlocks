// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.speedboostblocks;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterImpulseEvent;
import org.terasology.engine.logic.characters.CharacterMoveInputEvent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.items.OnBlockItemPlaced;
import org.terasology.math.geom.Vector3f;
import org.terasology.speedboostblocks.component.SpeedBoostComponent;

import java.util.List;

/**
 * This class manages and controls SpeedBoost blocks.
 *
 * <p>SpeedBlocks push you in a certain direction based on the orientation of the block.
 * To change the orientation interact with the block using the interaction key (usually E).</p>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SpeedBoostSystem extends BaseComponentSystem {
    private Vector3f boostDirection = Vector3f.zero(); //This holds the direction to boost in
    private int moveForce = 1; //This is the force that the boost will have. The value should be between 1 and 5 to 
    // prevent damage to the player.

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    /**
     * This is called when a block is placed.
     *
     * @param event Details about the block placed.
     * @param entity The entity of the block placed.
     */
    @ReceiveEvent
    public void onBlockItemPlaced(OnBlockItemPlaced event, EntityRef entity) {
        if (!event.getPlacedBlock().hasComponent(SpeedBoostComponent.class)) {
            return;
        }

        //Get the visible block instance
        BlockComponent blockComponent = event.getPlacedBlock().getComponent(BlockComponent.class);

        //Get all the directions that the block could take
        List<Block> blocks = getBlockDirections(blockComponent);

        //Iterate through the possible block directions
        for (Block block : blocks) {
            //If the direction of the block is equal to one of the possible directions
            if (block.toString().equals(blockComponent.getBlock().toString())) {
                //Get the block's direction number and apply it to the boost properties
                event.getPlacedBlock().getComponent(SpeedBoostComponent.class).boostDirection =
                        getDirection(block.toString());
            }
        }
    }

    /**
     * This is called when the SpeedBoost block is interacted with (by default the E key).
     *
     * @param event Details about the use.
     * @param entity The block being used.
     */
    @ReceiveEvent(components = {SpeedBoostComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        //Get the boost properties
        entity.getComponent(SpeedBoostComponent.class).changeDirection();
        //Obtain the direction of the boost
        int direction = entity.getComponent(SpeedBoostComponent.class).boostDirection;

        //Get the visible block instance
        BlockComponent blockComponent = entity.getComponent(BlockComponent.class);

        //Get all the directions that the block could take
        List<Block> blocks = getBlockDirections(blockComponent);

        worldProvider.setBlock(blockComponent.getPosition(), blocks.get(direction)); //Change the block seen in the 
        // world
    }

    /**
     * This is called when the character is moving.
     *
     * @param moveInputEvent The details of the movement.
     * @param player The player entity.
     * @param location The player's location.
     */
    @ReceiveEvent(components = {LocationComponent.class})
    public void onCharacterMovement(CharacterMoveInputEvent moveInputEvent, EntityRef player,
                                    LocationComponent location) {
        //Reset the boost properties
        moveForce = 0;
        boostDirection = Vector3f.zero();

        //Get the player's location
        Vector3f playerWorldLocation = location.getWorldPosition();

        //If it doesn't exist
        if (playerWorldLocation == null) {
            //Something is very wrong so return
            return;
        }

        //Iterate through all the SpeedBoost blocks
        for (EntityRef entity : entityManager.getEntitiesWith(SpeedBoostComponent.class)) {
            //Get the block's location data
            LocationComponent entityLocation = entity.getComponent(LocationComponent.class);

            //If it does not exist
            if (entityLocation == null) {
                //This block is not in the world so move onto the next one
                continue;
            }

            //Get the block's position
            Vector3f entityWorldLocation = entityLocation.getWorldPosition();

            //If it does not exist
            if (entityWorldLocation == null) {
                //Move on (this should never be called)
                continue;
            }

            //If the player's location matches the block's on the x and z axis (we round it to make the numbers usable)
            if (Math.round(playerWorldLocation.x) == entityWorldLocation.x && Math.round(playerWorldLocation.z) == entityWorldLocation.z) {
                //If the block is underneath the player
                if (Math.round(playerWorldLocation.y - 1) == entityWorldLocation.y) {
                    //Get the SpeedBoost block's properties
                    SpeedBoostComponent component = entity.getComponent(SpeedBoostComponent.class);
                    //Assign the properties to a local variable
                    moveForce = component.speedIncrease;
                    boostDirection = getDirection(component.boostDirection, playerWorldLocation);
                    //We've found a block underneath us so there is no need to continue looking
                    break;
                }
            }
        }

        //If the player is set to be boosted
        if (boostDirection != Vector3f.zero() && moveForce != 0) {
            //Calculate the impulse to be applied
            Vector3f impulse = boostDirection.mul(moveForce);
            //Apply the impulse to the player
            player.send(new CharacterImpulseEvent(impulse));
            //Reset the force of the boost to prevent a duplicate
            moveForce = 0;
        } else {
            //There is no force to apply so return
            return;
        }
    }

    /**
     * This method converts the numerical value of the direction to its Vector3f equivalent.
     *
     * @param direction The direction in integer form.
     * @param playerDirection The direction that the player is traveling in.
     * @return A vector version of the direction.
     */
    private Vector3f getDirection(int direction, Vector3f playerDirection) {
        //Check the directions against preset values
        switch (direction) {
            case 0:
                return new Vector3f(0, 0, 1); //Up
            case 1:
                return new Vector3f(-1, 0, 0); //Right
            case 2:
                return new Vector3f(0, 0, -1); //Down
            case 3:
                return new Vector3f(1, 0, 0); //Left
            case 4:
                return playerDirection; //This applies an impulse in the direction the player is moving in
            default:
                return Vector3f.zero(); //This should never happen but if the direction is unknown then do not move
        }
    }

    /**
     * This method obtains the blocks direction in its numerical equivalent based in the name of its type.
     *
     * @param blockName The name of the block's type (includes direction).
     * @return The integer version of the block's direction.
     */
    private int getDirection(String blockName) {
        //Check the block names (internal) against preset values
        switch (blockName) {
            case "FunnyBlocks:SpeedBoost.FRONT":
                return 0; //Forwards
            case "FunnyBlocks:SpeedBoost.RIGHT":
                return 1; //Right
            case "FunnyBlocks:SpeedBoost.BACK":
                return 2; //Backwards
            case "FunnyBlocks:SpeedBoost.LEFT":
                return 3; //Left
            default:
                return 4; //Player direction
        }
    }

    /**
     * This method gets all the possible directions that a block could be rotated in
     *
     * @param blockComponent The block to get the rotations from
     * @return All the possible rotations of the block
     */
    private List<Block> getBlockDirections(BlockComponent blockComponent) {
        //Assign all the possible block rotations to an array
        List<Block> blocks = Lists.newArrayList(blockComponent.getBlock().getBlockFamily().getBlocks());

        //Obtain the blocks per direction to reassign later in the correct order
        Block upBlock = blocks.get(2); //This gets the instance of the block facing forwards
        Block downBlock = blocks.get(3); //This gets the instance of the block facing backwards
        Block leftBlock = blocks.get(0); //This gets the instance of the block facing left
        Block rightBlock = blocks.get(1); //This gets the instance of the block facing right

        //Reassign the blocks to their correct indexes
        blocks.set(0, upBlock); //Assign the forward-facing block to the first index
        blocks.set(1, rightBlock); //Assign the backward-facing block to the second index
        blocks.set(2, downBlock); //Assign the left-facing block to the third index
        blocks.set(3, leftBlock); //Assign the right-facing block to the fourth index

        //Return the results
        return blocks;
    }
}
