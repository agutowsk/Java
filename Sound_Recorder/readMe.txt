WHAT:
    This project is a voice/sound recording applciation, the application records all sound in analog waves and coverts it to digital readings so that it can be filtered, saved and played back.  The application has optional background noise reduction filtering provided by basic wave analysis.

HOW:
    I wrote this project on a Macbook pro using Java, and tested it on a Linux machine to verify it worked.  I have supplied a precomplied jar file, as well as all my complied classes under the bin directory, and all my source under the src directory.  While the program executes all output files such as sound.raw or energy.data can be found in the output directory.

To record data use a command line option like this:
    java -jar project1.jar record

To play back the data you recorded use a command line option like this:
    java -jar project1.jar play "output/sound.raw"


Should you want to compile my code on to run on your own, here are some rough compiling instructions:
    1. from this directory call 
        javac -d bin project1/*.java
    2. now call
        java -cp ./bin project1.voiceRecorder