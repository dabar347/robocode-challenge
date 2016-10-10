Hut Group University Challenge
------------------------------
#Intro
This project is a platform for running the Hut Group Dev Challenge 2016 by building on the `Robocode` platform. It setups up a knockout tournament with free-for-all style matches from a given list of participants.

#Format:
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
Vlad will contribute to this section though I imagine it will consist of the following steps:
 * Download and install robocode
 * Run robocode with the sample bots
 * Clone the sample repository
 * Build the sample bot
 * Run it with the sample bot
 * Add a feature and repeat the above

##Running `THGEngine`:
WIP not ready yet
 * Clone and unpack the solution
 * Add your jar to the robots directory
 * Run sanity_check.sh (to check whether your setup is fine)
 * Run `thgengine.sh <list of participating bots>`
 
#Prizes:
TBD




#Notes

Teams should have distinct names

#Todo

 * Run a check that the team names of all jars are distinct
 * Develop a tool to check the sanity of a solution
