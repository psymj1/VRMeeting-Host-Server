# Instructions of Setting up VR Meeting Backend

The host server which proivdes communication between VRMeeting clients is written in Java and so should run on any system that supports the Java(TM) SE Runtime Environment 1.8 or later

# Compiling the source code

## Step 1: Install Java JDK 1.8 or later

To check your Java version you can open a command prompt and type `java -version`. If you're version is less than "1.8.0_11" then you'll need to install the latest version making sure to
install the JDK.

If your java version is up to date then skip this step

## Step 2: Installing Maven

The source code can be compiled via Maven as it's a mavne project. You'll need to first install maven which you can find a guide on how to do [here](https://maven.apache.org/install.html)
Once you have maven successfully installed you can move onto the next step

## Step 3: Get Source from Git

You can get source code simply by executing <br>
`git clone https://github.com/psymj1/VRMeeting-Host-Server.git`

## Step 4: Installing

After Step 3, you will find a new folder in current directory called `VRMeeting_Host_Server`, change your current working directory into it. `cd VRMeeting_Host_Server`

Then execute `mvn install`. You should see the tests run and succeed and a new file will be created under the directory `\target` called *vrmeeting-hostserver-3.0.0-SNAPSHOT.jar*

## Step 5: Setting up the properties
Before running the jar you'll need to locate `properties.txt` and open it. You'll need to setup the WebServerIP and port number. If you need more infromation you can find
the online installation guide in the documentation repository: https://github.com/psymj1/VRMeeting-Documentation , Under: Installation Guide 

## Step 6: All Done!
You've now compiled the source code to a jar. Follow the next steps for running the jar file

# Running the Host Server Jar

If you've followed the above steps then you should be in a directory similar to `VRMeeting_Host_Server\target` with a file called *vrmeeting-hostserver-3.0.0-SNAPSHOT.jar*

Running the jar file is simple. You'll need to execute the following command in the directory containing the *jar* file: `java -jar vrmeeting-hostserver-3.0.0-SNAPSHOT.jar
# Common Errors when attempting to run the jar

## Listening Port already in use
If you see the following output then you'll need to change the port in the properties as the port is already being used by another program. If you've
previously used the VRMeeting_Host_Server on the same port then you'll want to double check that you haven't left it running by accident.

**>\VRMeeting_Host_Server\target> java -jar vrmeeting-hostserver-3.0.0-SNAPSHOT.jar 25560 http://localhost:25560** <br>
INFO <Thu May 17 11:44:35 BST 2018> (Host Server Thread) Initialising server... <br>
INFO <Thu May 17 11:44:35 BST 2018> (Host Server Thread) Attempting to start host server listening on port 25560 <br>
Host Server will connect to the web server located at http://localhost:25560 <br>
INFO <Thu May 17 11:44:36 BST 2018> (TCP Connection Acceptor) Starting... <br>
ERROR <Thu May 17 11:44:36 BST 2018> (TCP Connection Acceptor) Address already in use: JVM_Bind <br>
[Ljava.lang.StackTraceElement;@4fca772d <br>
Exception in thread "main" main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException: Error starting TCPConnectionAcceptor, see logs for details <br>
        at main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor.TCPConnectionAcceptor.startUp(TCPConnectionAcceptor.java:101) <br>
        at main.java.com.hexcore.vrmeeting_hostserver.ServerComponent.start(ServerComponent.java:43) <br>
        at main.java.com.hexcore.vrmeeting_hostserver.HostServer.startUp(HostServer.java:162) <br>
        at main.java.com.hexcore.vrmeeting_hostserver.ServerComponent.start(ServerComponent.java:43) <br>
        at main.java.com.hexcore.vrmeeting_hostserver.HostServer.main(HostServer.java:59) <br>

## Web Server Unavailable
If you see the following output then the VRMeeting_Server Web Server is unavailable to the Host Server. This could be because:
+ You haven't got the web server running
+ You haven't entered the address of the web server correctly
+ You haven't installed the Web Server in which case you'll need to following the instructions found in the project [here](https://github.com/psymj1/VRMeeting-Web-Server)
+ Your firewall could be blocking the connection
+ The Web Server is hosted on a separate network and that network's port has not been forwarded for TCP

**>\VRMeeting_Host_Server\target>java -jar vrmeeting-hostserver-3.0.0-SNAPSHOT.jar 25565 http://localhost:25560** <br>
INFO <Thu May 17 11:48:19 BST 2018> (Host Server Thread) Initialising server... <br>
INFO <Thu May 17 11:48:19 BST 2018> (Host Server Thread) Attempting to start host server listening on port 25565 <br>
Host Server will connect to the web server located at http://localhost:25560 <br>
IGNORE <Thu May 17 11:48:20 BST 2018> (WebServerConnector to http://localhost:25560) Connection refused: connect <br>
ERROR <Thu May 17 11:48:20 BST 2018> (Host Server Thread) Cannot connect to the Web Server at http://localhost:25560 are you sure it's installed and running? Check the Instructions.md for more details <br>

## Missing Parameters
If you see the following output then you've run the jar file without the correct number of parameters. See the above instructions on Running the Host Server Jar

**>\VRMeeting_Host_Server\target>java -jar vrmeeting-hostserver-3.0.0-SNAPSHOT.jar** <br>
INFO <Thu May 17 11:52:28 BST 2018> (Host Server Thread) Initialising server... <br>
ERROR <Thu May 17 11:52:28 BST 2018> (Host Server Thread) Missing parameters <br>
 Execution Instructions: <br>
 <port number> <Web Server IP> <br>

# Extra Notes:
+ You can move the jar file to any directory it doesn't have to remain in the target directory.
+ If you're trying to connect to this server from an external IP you'll need to make sure you've forwarded the port you use as a runtime argument for TCP
<br>
<br>
<br>
<br>