WHAT:
This applcaition is a java based voice over IP communicator.  The applcaition can transmit data via TCP or UDP packets and works cross platform (tested on Mac and PC).

HOW:
This readMe is for the Speak java executable.

The project was written in java on a Mac, using eclipse and compiled by the command line.

To compile:
    From this directory execute: javac -d bin src/speak_pkg/*.java

My project uses one main program, voIPClient, but uses a 2 client setup to establish the sockets properly.  When opening the program please provide a -clientA to one client, and -clientB to the other, this will establish the sockets correctly.

To execute:
    From this directory: java -cp ./bin speak_pkg.voIPClient -clientA {any input params}

The only input parameter that is required is the client order, clientA or client B.  No other input parameters are required because the program defults to usable parameters, but should you want to specify input parameters, they are as follows:
 - client setup: -clientA or -clientB
 - Destination ip Address: -ip:XX.XX.XXX.XXX
 - sample interval in milliseconds: -i:20-1000
 - transmission protocol: -tcp or -udp
 - Speech Detection: -speech:true/false
 - packet loss (as a percentage): -l:0-100

 Examples: 
    - standard execution: "java -cp ./bin speak_pkg.voIPClient -clientA"
    - with an IP address: "java -cp ./bin speak_pkg.voIPClient -clientA -ip:10.0.0.5"
    - with an IP address and protocol: "java -cp ./bin speak_pkg.voIPClient -clientA -ip:10.0.0.5 -tcp"
    - with all parameters: "java -cp ./bin speak_pkg.voIPClient -clientB -ip:10.0.0.5 -tcp -i:1000 -speech:true -l:10"

 NOTE: there is not particular order these must be entered in, and they are not required, so if you don'y supply an option, we use the following defaults: 
         ipAddress = "localhost";
         socketType = protocol.UDP;
         sampleInterval = 1000;
         detectSpeech = false;
         loss: 0%

 To test this project I mostly used localhost on my Mac OSX machine, but verified that it works n a MAC to MAC and MAC to PC environment. I have verified that both TCP and UDP network protocols work in both envirnments.