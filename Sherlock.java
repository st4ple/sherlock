import java.io.*;
import java.security.*;
import java.util.*;
import java.lang.reflect.Array;
import java.nio.file.Path;

public class Sherlock {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        // set default arguments
        String path = System.getProperty("user.dir");
        boolean analyze = false;
        boolean verbose = false;

        // handle input arguments
        if (args.length > 4) {
            System.out.println("ERROR: Too many arguments!");
            printUsagesAndExit();
        }
        else {
            for (int i = 0; i<args.length; i++){
                if (args[i].equals("-h")){
                    printUsagesAndExit();
                }
                if (args[i].equals("-a")){
                    analyze = true;
                }
                if (args[i].equals("-v")){
                    verbose = true;
                }
                if (args[i].equals("-p")){
                    path = args[i+1];
                }
            }
        }

        // check if defined path is valid
        File d = new File(path);
        if(!d.isDirectory()) { 
            System.out.println("ERROR:    The entered path doesn't seem to exist!");
            System.out.println("SOLUTION: Please make sure you enter a valid path.");
            System.out.println("HINT:     If path is not set, the current directory is taken as default path.");
            System.exit(1);
        }

        // instantiate some resources
        Walker walker = new Walker();
        ArrayList<File> files = walker.getFiles(path);

        ArrayList<String> ignoreList = new ArrayList<String>();
        HashMap<String, String> newIndex = new HashMap<String, String>();

        ArrayList<String> modList = new ArrayList<String>();
        ArrayList<String> newList = new ArrayList<String>();
        ArrayList<String> deletedList = new ArrayList<String>();
        HashMap<String, String> oldIndex = new HashMap<String, String>();

        FileInputStream fileIn;
        File f;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (analyze){
            // load old index if it exists, otherwise output an error
            f = new File(path+"/index.sk");
            if(f.exists()) { 
                try {
                    fileIn = new FileInputStream(path+"/index.sk");
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    oldIndex = (HashMap<String, String>) in.readObject();
                    in.close();
                    fileIn.close();
                } catch (IOException|ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println("ERROR:    index.sk file doesn't exist. Cannot analyse!");
                System.out.println("SOLUTION: Please run 'java Sherlock' or 'java Sherlock -p <path>' first.");
                System.exit(1);
            }		
            if (verbose) System.out.println("Sherlock is analyzing changes in directory "+path+":");
        }
        else {
            if (verbose) System.out.println("Sherlock is indexing directory "+path+":");
        }

        // load ignore file if it exists, otherwise output a warning (only in verbose mode)
        f = new File(path+"/ignore.sk");
        if(f.exists() && !f.isDirectory()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(path+"/ignore.sk"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                String line = br.readLine();
                while (line != null) {
                    // ignore empty lines
                    if (line.length()>0){
                        // ignore commented lines beginning with #
                        if (!line.substring(0,1).equals("#")){
                            ignoreList.add(line);
                        }
                    }
                    line = br.readLine();
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
        else {
            if (verbose) System.out.println("WARNING: No ignore.sk file found.");
        }

        // iterate through files in directory
        for (File file : files) {
            boolean ignore = false;
            String absPath = file.getAbsolutePath();
            // set ignore = true if the file appears on the ignore list
            for (String ignoreString : ignoreList){

                if (matching(ignoreString, absPath)){
                    ignore = true;
                    if (verbose) System.out.println("IGNORING      "+absPath);
                }
            }

            // create a hash for all files not ignored.
            if (ignore==false){
                // calculate md5 hash
                byte[] dataBytes = new byte[1024];

                int nread = 0;
                try {
                    fileIn = new FileInputStream(file);
                    while ((nread = fileIn.read(dataBytes)) != -1) {
                        md.update(dataBytes, 0, nread);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] mdbytes = md.digest();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < mdbytes.length; i++) {
                    sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
                }
                String absolutePath = file.getAbsolutePath();
                String hash = sb.toString();
                // put filename and hash in newIndex
                newIndex.put(absolutePath, hash);

                if (analyze){
                    // analyze if the file is unchanged, modified or new
                    if (oldIndex.containsKey(absolutePath)){
                        String oldHash = oldIndex.remove(absolutePath);
                        if (hash.equals(oldHash)){
                            if (verbose) System.out.printf("UNCHANGED     ");
                        }
                        else {
                            if (verbose) System.out.printf("MODIFIED      ");
                            modList.add(absolutePath);
                        }
                    }
                    else {
                        if (verbose) System.out.printf("NEW           ");
                        newList.add(absolutePath);
                    }
                    if (verbose) System.out.println(absolutePath);
                }
                else {
                    // for indexing in verbose mode, print file and its hash 
                    if (verbose) System.out.println("HASHING       " + absolutePath + "  =>  " + hash);
                }
            }
        }

        // write all files and their hash to new index for use next time in analyzing mode
        try {
            FileOutputStream indexOut = new FileOutputStream(path+"/index.sk");
            ObjectOutputStream out = new ObjectOutputStream(indexOut);
            out.writeObject(newIndex);
            out.close();
            indexOut.close();
        } catch(IOException i) {
            i.printStackTrace();
        }

        // create final output for analyzing and indexing mode
        if (analyze){

            // all files that were on the oldIndex but havent appeared in the files list must have been deleted
            Iterator it = oldIndex.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                if (verbose) System.out.println("DELETED       "+pairs.getKey());
                deletedList.add(pairs.getKey().toString());
                it.remove();
            }       

            // output all modified, new and deleted files
            if (modList.size()>0||newList.size()>0||deletedList.size()>0){
                if (newList.size()>0) {
                    System.out.println("New files:");
                    for (int i=0; i<newList.size();i++){
                        System.out.println("+ "+newList.get(i));
                    }
                }
                if (modList.size()>0) {
                    System.out.println("Modified files:");
                    for (int i=0; i<modList.size();i++){
                        System.out.println("▲ "+modList.get(i));
                    }
                }
                if (deletedList.size()>0) {
                    System.out.println("Deleted files:");
                    for (int i=0; i<deletedList.size();i++){
                        System.out.println("- "+deletedList.get(i));
                    }
                }
            }
            else {
                System.out.println("No changes since last index or analyze.");
            }
        }

        else {
            System.out.println("Indexing complete.");
        }
    }

    public static boolean matching(String ignore, String filePath){
        String filePathShort = filePath;
        // check if ignore string is a directory that has shorter path than path
        if (ignore.substring(ignore.length()-1).equals("/") && filePath.length()>ignore.length()){
            filePathShort = filePath.substring(0,ignore.length());
        }
        return (ignore.equals(filePath)||ignore.equals(filePathShort));
    }

    // helper method that prints how to use Sherlock from command line
    public static void printUsagesAndExit(){
        System.out.println("USAGES: 'java Sherlock' or 'java Sherlock -p <path>' for indexing mode");
        System.out.println("        'java Sherlock -a' or 'java Sherlock -a -p <path>' for analyzing mode");
        System.out.println();
        System.out.println("HINTS:  default path is current directory");
        System.out.println("        ignore file is ignore.sk in path directory");
        System.out.println("        index file is index.sk in path directory");
        System.out.println("        add -v as argument for verbose output");
        System.exit(1);
    }
}

// helper class that recursively walks through all directories in the declared root path and adds all files to a list
class Walker {
	public ArrayList<File> getFiles (String path){
		ArrayList<File> files = new ArrayList<File>(); 
		walk(path, files);
		return files;
	}
			
   public void walk(String path, ArrayList<File> files) {
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) return;

        for (File f : list) {
            if (f.isDirectory()) {
        		walk(f.getAbsolutePath(), files);
            }
            else {
 	    	   	files.add(f);
            }
        }
    }
}