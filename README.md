Redis lost my data! 
=======================================

This application is for demonstrating how Redis (out-of-box configuration) lose data when master node crashes on peak load.


How to run
==========
To run web app; run `mvn jetty:run` command on the command line.

Notes:
------
Please note that you need to run a local Redis with master-slave setup before you run this application. 


Usage
=====
1. Click on `Start inserting data` button
2. After ~20 secs, force shutdown master node of Redis
3. Click on `Show my data` button to see how much data you inserted.
4. Fill the `IP` and `port` fields with IP and port information of Redis slave. 
5. Click on `Validate` button to see which of the data exists on slave node. Green means data saved, red means data lost. 