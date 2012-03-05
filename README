Hirschberg-Sinclair Leader Election Algorithm
===

This is an implementation of the Hirschberg-Sinclair Leader election algorithm. It's written in Java and uses Java RMI for communication between Nodes. 

This was developed on a Mac running Mac OS X 10.7.3

The build and run script uses osascript to open a terminal window for the `Ringer` process, and `n` terminal windows for `n Node` processes. 
You can choose to not have any terminal windows for the Node processes by editing the `run-hs` file. Inside the file is documentation to help you do that. 

The build script also generates a new java security policy file from `java.policy.default` that gives file writing, reading and executing permission for the current working directory.
`run.sh` automatically places the current working directory in the appropriate place in the policy file and outputs the new file as `java-security.policy`.

Since for this project I was required to keep track of average number of messages sent by nodes, I have some reporting functionality that writes a file named `stat.txt`.

This file contains a record of runs of this algorithm with the node count and average messages sent and average messages received.

To run the algorithm: 
>`$ ./run-hs <number of nodes>`
