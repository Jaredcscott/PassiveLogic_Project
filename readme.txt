This program relies on JavaFX which does not have native support on JDK versions 11 and on.

To run the program, open a command prompt and from within this directory run the command..
java -jar PassiveLogic_Project.jar 

if this does not work than I have also included the source code.
Uncompiled file is located at PassiveLogic\passivelogic_project\Main.java

This must be run with the JDK version 10.0.2 or another JRE that supports JavaFX 
Installation media for JDK 10.0.2 can be found at https://www.oracle.com/java/technologies/java-archive-javase10-downloads.html 
If JDK 10.0.2 is installed, from the command line run "set path=%path%;C:\Program Files\Java\jdk-10.0.2\bin".

To confirm that the environment is correctly set up run "java --version" the output should be...
'''
java 10.0.2 2018-07-17
Java(TM) SE Runtime Environment 18.3 (build 10.0.2+13)
Java HotSpot(TM) 64-Bit Server VM 18.3 (build 10.0.2+13, mixed mode)
'''
Once the environment is set up, navigate to PassiveLogic\passivelogic_project,
then run the command "java Main" this should execute the program and display the simulation application.  

If all of this fails and my java implementation is unusable. 
I have included a Python implementation within "PassiveLogic\In_Case_The_Java_Project_Wont_Run"
As well as a short video demonstrating what the Java application would have looked like had it run.
