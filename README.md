Sherlock
========

Simple command line tool that indexes and analyzes changes of files and directories for a given root path.

## Usage

On first use, compile the Java code by running

    javac Sherlock.java
    
in terminal.

### Index mode

To just create an index file containing the absolute path and an md5 hash of each file in the directory located at <path>, run the following command in the terminal:

    java Sherlock -p <path>

**Note:** If `-p <path>` is omitted, Sherlock will use the path of the directory it is located as the root path.

### Analyze mode

To detect files (in the directory located at `<path>`) that have been modified, deleted or newly created since the index file was updated last, run the follwing command in terminal:

    java Sherlock -a -p <path>

**Note:** If `-p <path>` is omitted, Sherlock will use the path of the directory it is located as the root path.

**Note:** Before the analyze mode for a directory located at `<path>` can be executed, there must exist an index file in this directory. To initially create this, simply run the index mode beforehand.

## Ignore file

To specify certain files and diretories to be ignored by the index and analyze mode, create a file named `ignore.sk` in the directory located at `<path>`. 

### Syntax

##### Files
Lines not ending with / are treated as files. If Sherlock finds a file with the absolute path equal to this line, the file will be ignored.

##### Directories
Lines ending with / are treated as directories. All the files and subdirectories in this directory will be ignored by Sherlock.

##### Comments
Lines that start with # are skipped by Sherlock (not treated as file or directory).

## Verbose output

Simply add the option `-v` as an argument at the end of any command to receive verbose output while the programm is executing.
