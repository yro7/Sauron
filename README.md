<div align="center">

# Sauron - Dupe Preventer

[![Minecraft Versions](https://img.shields.io/badge/Minecraft-1.12%20--%201.20-2d2d2d?style=for-the-badge&logo=minecraft)](https://www.spigotmc.org/resources/sauron-dupe-preventer-1-12-1-20-6.115810/)
[![Spigot Downloads](https://img.shields.io/spiget/downloads/115810?style=for-the-badge&color=blue)](https://www.spigotmc.org/resources/sauron-dupe-preventer-1-12-1-20-6.115810/)
[![Spigot Rating](https://img.shields.io/spiget/rating/115810?style=for-the-badge&color=yellow)](https://www.spigotmc.org/resources/sauron-dupe-preventer-1-12-1-20-6.115810/)
[![Spigot Version](https://img.shields.io/spiget/version/115810?style=for-the-badge&color=brightgreen)](https://www.spigotmc.org/resources/sauron-dupe-preventer-1-12-1-20-6.115810/)

<br>

**A lightweight, high-performance plugin designed to catch item duplication on Minecraft servers.**

[**View Resource on SpigotMC**](https://www.spigotmc.org/resources/sauron-dupe-preventer-1-12-1-20-6.115810/)

</div>


### Keep an eye on your player's item

An anti-dupe and tracking plugin allowing you to make items non-fongible.

Sauron lets you define "tracking rules" and items corresponding to these rules will be tracked.
Tracked items have an additional timestamp and unique UUID identifier.
By comparing the timestamp-UUID association of the item with the one in the database, you can detect duplicates and delete them from your server.

Sauron can also help you refund your players items after a bug, without having the doubt that maybe they hid the item from your sight.
Any refunded item is blacklisted and will be cleared (and logged) if used again.
The plugin is great with Inventory Restore plugins : you can get the exact item that the user was cleared, do a /sauron refund on it and be sure that if the player lied, the plugin will find him and blacklist him.

<img width="1024" height="559" alt="image" src="https://github.com/user-attachments/assets/a883b6d9-cc4c-47a6-a70e-09b28cae05b8" />
