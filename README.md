# Minecraft Plugin: Gravebound Army

Gravebound Army is a Paper Minecraft plugin that turns fallen mobs into recruitable army units. When a living mob dies, the plugin removes its drops and experience, respawns a temporary fallen version nearby, and lets a player recruit and command it through the `/gravebound` Minecraft server command.

## Install

1. Build the plugin with Java 21 and Maven:

   ```text
   mvn package
   ```

2. Copy the generated `target/plugin.jar` file into your Paper server's `plugins` directory.

3. Start or restart the server.

4. Confirm that the plugin loaded successfully.

5. Players need the `gravebound.use` permission. This permission is enabled by default.

## Use

1. Defeat a living mob. The mob's drops and experience are removed, and a fallen version is spawned at the death location.

2. Move within 8 blocks of the fallen mob.

3. Run:

   ```text
   /gravebound recruit
   ```

4. Command recruited units with:

   ```text
   /gravebound follow
   /gravebound hold
   /gravebound attack
   /gravebound release
   /gravebound list
   ```

5. Use `/gravebound attack` while looking at a living enemy within 32 blocks. Monster army units will target that enemy.

6. Right-click one of your recruited units to toggle its artificial intelligence between active and holding position.

7. Recruited units do not target or attack their owner. Units that move more than 48 blocks from their owner are teleported back nearby.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/974f9686-5bce-4cc0-b4ea-3147d692509b?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=974f9686-5bce-4cc0-b4ea-3147d692509b
