Hut Group University Challenge
------------------------------
#Intro
Artificial intelligence is set to revolutionise the world around us. The scope for its application in business is huge, which is why The Hut Group needs to harness its full potential to stay ahead of the pack.
 
Using AI, we’d like you to program a robot tank to battle others in real time. Develop your bot to learn from its mistakes and automate its own decisions on your journey to total domination.
 
The Hut Group will provide the API to get you started and a training bot to test your skills. Live streams will be organised to showcase your performance against other applicants, as well as our very own house robots. For those who manage to annihilate the competition, there’s a £10k prize up for grabs.  

#Format:
Arena battle tournament. Optimize your bots for colosseum style battles. `Arena size 1000x1000` 

##Knockout Round:
Bots randomly grouped into brackets. Each bracket competes in a free-for-all. Winners from each bracket will compete in a final free-for-all battle.

##Scoring:
There will be 100 rounds. More emphasis will be given to later rounds, you should use the initial rounds to learn. (Weighting of the score of a round will increase linearly).
The score for a round is based on:
 * Energy left
 * Ram damage
 * Gun damage
(Note: accuracy is irrelevant) 

#Setup:
##Developing a `Bot`:  
`THIS REPO IS DESIGNED TO BE RUN IN INTELLIJ IDEA`   
To develop your bot follow these steps:    
*Clone this repo locally  
*Download and install robocode from the [official sourceforge website](https://sourceforge.net/projects/robocode/files/robocode/1.9.2.5/robocode-1.9.2.5-setup.jar/download)  
*In IntelliJ add to your project all jarsfound in {YOUR_ROBOCODE_INSTALL_PATH}/libs   
*You should now be able to run robocode using SHIFT+F10 or hitting the play button.  
*From the robocode instance go to OPTIONS-> PREFERENCES ->DEVELOPMENT OPTIONS and add your intellij output folder to the list  
*CONGRATULATIONS! You are now setup to develop bots.    

##Setup Notes  
*This is just a template bot which does nothing, it is up to you to architect it's behaviour.   
*If you want to see more bots to challenge just go into development options again and add the {YOUR_ROBOCODE_INSTALL_PATH}/robots  

#Submission:  
##Packaging your bot:    
*From your running instance of robocode: ROBOT -> PACKAGE ROBOT OR TEAM and follow the instructions  
*During packaging check both boxes for data files and source files  
*Names, Descriptions - Have fun ;)  

##Making your submission:  
*Get your robot jarfile, your java classes ( if you want you can add a readme describing who you are and why your bot is this way)

#Prizes:  
*£10,000.  
*Immediate job offers for the winners.
