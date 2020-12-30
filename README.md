![logo](https://github.com/FlyLamb/MusicBlockMod/raw/master/other%20assets/logo.png)
# MusicBlockMod
A Forge mod that adds programmable music blocks!
Minecraft version: 1.16.4, should work for other 1.16.

## Content and Craftings

### Music Processor
It is the base component of all the other musical blocks. It can be crafted by using 3 redstone and 6 iron ingots

![](https://github.com/FlyLamb/MusicBlockMod/raw/master/other%20assets/crafting_2.png)

### Music Block
Used to record music.
Right-click it to start recording music in the area indicated by particles. Every sound that an **Advanced Note Block** makes will be recorded.
The maximum recording time is defined in the code *(TODO: Make it configurable)* as 12000 ticks, which is 10 minutes. After that the recording saves. You can also save it manually by right-clicking again.
To play the music again, all you need to do is to power it with redstone. Powering it when it is playing makes the music stop.
To record again, you have to double right-click the block.
When you break the block it will drop with it's music saved. You can rename it to change it's title.

![](https://github.com/FlyLamb/MusicBlockMod/raw/master/other%20assets/crafting_0.png)

### Advanced Note Block
Works just like a normal Note Block, but with a few tweaks:
+ displays the letter-note
+ doesn't have annoying particles
+ is registered by the **Music Block**

![](https://github.com/FlyLamb/MusicBlockMod/raw/master/other%20assets/crafting_1.png)

In crafting, you can make up to 8 **Advanced Note Blocks** using only one **Processor**

![](https://github.com/FlyLamb/MusicBlockMod/raw/master/other%20assets/crafting_3.png)

## Credits
@Bajtix - code stuff
@Dragster2401 - the entirety of the resources folder: textures, models, Polish and English translation

Huge "thank you" to Forge forums members:
+ diesieben07
+ Beethoven92
