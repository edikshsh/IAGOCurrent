How to Install IAGO for Game_Edition only development

Prereqs:
a) Install Java 11 or newer from their website
b) Install Tomcat 8.5 or newer from their website
c) Install Eclipse 2019-03 (EE Edition!) or newer from their website
d) Recommended: install notepad++, and cygwin 

Main install:

1) Unzip the IAGO_Game_Edition zip into a new folder, called myIago (or whatever you'd like)
2) Open eclipse.  Immediately switch workspace to myIago
3) Server tab, add server, Apache --> Tomcat 8.5
4) Browse to C:/Program Files/Apache Software Foundation/Tomcat 8.5 --> Finish
5) Right click, New Project --> Web --> Dynamic Web Project --> Next --> uncheck default location --> browse to myIago/IAGO_Game_Edition
6) Project name = IAGO_Game_Edition. Target Runtime Tomcat 8.5. Finish
7) (If you forget to set the Target Runtime in 6), then: Right-click project --> Configure Build Path --> Libraries tab --> Add Library --> Server Runtime --> Tomcat 8.5
8) Move (don't copy) the file IAGO_Core.jar from IAGO_Game_Edition/WEB-INF/lib to C:/Program Files/Apache Software Foundation/Tomcat 8.5/shared/lib
9) Back to servers.  Tomcat v8.5 server--> right click --> Start
10) Double click Tomcat v8.5 server --> Deploy path = webapps, Server location = "Use Tomcat Installation"  Save
11a) If no error, continue to 12
11b) If error, make sure permissions for the Apache Software Foundation (or at least Tomcat 8.5) are set to all for all users.  Make a change (e.g., retype the deploy path again), save, restart server.
12) Go to modules tab on the server.  Add Web Module --> selecte IGE, set deploy path to "IAGO" Ok
13) Table should now read: Path: /IAGO_Game_Edition | Document Base: IAGO_Game_Edition | Module: IAGO_Game_Edition | Auto Reload: Enabled. 
14) Edit.  Change path to /IAGO because Eclipse sometimes has a bug here.
15) Save.  Start server.
16) You should now be able to access IAGO in a web browser at localhost:8080/IAGO.

How to use the config file:

If you want to simulate two agents, use two agents.  If you want the standard human vs. agent, only include one agent name.
If you want multiple negotiations, include multiple comma-separated GameSpec classes.  Otherwise, include only one.


