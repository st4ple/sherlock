sherlock
========

Simple command line tool that indexes and analyzes changes of files and directories for a given root path.

### Usage

On first use, compile the Java code by running
    javac Sherlock.java
in terminal.

#### Index mode

To just create an index file containing the absolute path and an md5 hash of each file in the directory located at <path>, run the following command in the terminal:
   java Sherlock -p <path>

**Note:** If `-p <path>` is omitted, Sherlock will use the path of the directory it is located as the root path.

#### Analyze mode

To detect files (in the directory located at `<path>`) that have been modified, deleted or newly created since the index file was updated last, run the follwing command in terminal:
   java Sherlock -a -p <path>

**Note:** If `-p <path>` is omitted, Sherlock will use the path of the directory it is located as the root path.
**Note:** Before the analyzing mode for a directory located at `<path>` can be executed, there must exist an index file in this directory. To initially create this, simply run the index mode beforehand.

#### Verbose output

Simply add the option `-v` as an argument at the end of any command to receive verbose output while the programm is executing.
