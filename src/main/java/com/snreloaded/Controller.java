package com.snreloaded;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    public static void hFlag()
    {
        System.out.println("List of available flags:");
        System.out.println("-h                         : List this help menu.");
        System.out.println("-u <path to manifest.json> : Check for mod updates.");
        System.out.println("-a <path to manifest.json> : Add mod(s) to pack.");
        System.out.println("-r <path to manifest.json> : Remove mod(s) from pack.");
        //System.out.println("-c <path to directory>     : Create a manifest.json in directory path.");
    }

    public static void uFlag(String pathToManifest)
    {
        JSONObject jsonObject = JSONTools.getJSONFromFile(pathToManifest);

        Scanner kin = new Scanner(System.in);

        JSONObject minecraftObject = (JSONObject)jsonObject.get("minecraft");
        String minecraftVersion = minecraftObject.get("version").toString();

        JSONArray files = (JSONArray)jsonObject.get("files");
        for (int i = 0; i < files.size(); i++) {
            System.out.println("Checking for updates on mod " + (i+1) + "/" + files.size());
            JSONObject curObject = (JSONObject)files.get(i);
            String projectID = ((Long)curObject.get("projectID")).toString();
            String fileID = ((Long)curObject.get("fileID")).toString();

            JSONObject modObject = NetworkTools.getModJSON(projectID);

            String latestFileID = NetworkTools.getLatestFileID(modObject, minecraftVersion);
            String modName = JSONTools.getModName(modObject);

            if (latestFileID == null)
            {
                System.err.println("Problem with projectID: " + projectID + "; mod name: " + modName);
                System.err.println("\tSkipping this mod");
                continue;
            }

            if ( !(latestFileID.equals(fileID)) )
            {
                System.out.println( "Do you wish to update " + modName + " to the most recent version? [Yn]" );
                String input = kin.nextLine();
                if ( input.isEmpty() || input.equals("Y") || input.equals("y") || input.equals("yes") || input.equals("Yes") || input.equals("YES") )
                {
                    curObject.replace("fileID", Long.parseLong(latestFileID));
                    System.out.println("\tUpdated " + modName);
                }
                else
                {
                    System.out.println("\tNot updating");
                }
            }
        }

        JSONTools.writeJSONFile(jsonObject, pathToManifest);
        System.out.println("Finished updating!");
    }

    public static void aFlag(String pathToManifest)
    {
        JSONObject jsonObject = JSONTools.getJSONFromFile(pathToManifest);
        JSONArray files = (JSONArray)jsonObject.get("files");

        Long projectID = 0L;
        Long fileID = 0L;

        Scanner kin = new Scanner(System.in);

        JSONObject minecraftObject = (JSONObject)jsonObject.get("minecraft");
        String minecraftVersion = minecraftObject.get("version").toString();

        while ( true )
        {
            System.out.println("Enter projectID that you wish to add (or \"q\" to quit):");
            String input = kin.nextLine();

            Pattern p = Pattern.compile("[0-9]{"+input.length()+"}");
            Matcher m = p.matcher(input);

            if ( !m.find() )
            {
                break;
            }

            projectID = Long.parseLong(input);

            JSONObject modJSON = NetworkTools.getModJSON(projectID.toString());
            String fID = NetworkTools.getLatestFileID(modJSON,minecraftVersion);
            fileID = Long.parseLong(fID);

            System.out.println("Adding mod \"" + JSONTools.getModName(modJSON) + "\" to project.");

            files.add(JSONTools.makeModJSONObject(projectID,fileID));
        }
        JSONTools.writeJSONFile(jsonObject,pathToManifest);
        System.out.println("Finished updating!");
    }

    public static void rFlag(String pathToManifest)
    {
        JSONObject jsonObject = JSONTools.getJSONFromFile(pathToManifest);
        JSONArray files = (JSONArray)jsonObject.get("files");

        Long projectID = 0L;

        Scanner kin = new Scanner(System.in);

        while ( true ) {
            System.out.println("Enter projectID that you wish to remove (or \"q\" to quit):");
            String input = kin.nextLine();

            Pattern p = Pattern.compile("[0-9]{" + input.length() + "}");
            Matcher m = p.matcher(input);

            if (!m.find()) {
                break;
            }

            projectID = Long.parseLong(input);

            JSONObject modJSON = NetworkTools.getModJSON(projectID.toString());

            System.out.println("Removing mod \"" + JSONTools.getModName(modJSON) + "\" from project.");

            for (Object object : files)
            {
                JSONObject curObject = (JSONObject)object;
                if ( ((Long)curObject.get("projectID")).equals(projectID) )
                {
                    files.remove(object);
                    break;
                }
            }
        }
        JSONTools.writeJSONFile(jsonObject,pathToManifest);
        System.out.println("Finished updating!");
    }

    public static void main(String[] args) {
        if ( args.length < 1 )
        {
            System.out.println("Please use at least 1 flag.");
            hFlag();
            return;
        }

        if ( args[0].equals("-h") )
        {
            hFlag();
            return;
        }

        if ( args[0].equals("-u") )
        {
            if ( args.length < 2 )
            {
                System.out.println("Please provide path to manifest.json.");
                return;
            }

            uFlag(args[1]);
        }

        if ( args[0].equals("-a") )
        {
            if ( args.length < 2 )
            {
                System.out.println("Please provide path to manifest.json.");
                return;
            }

            aFlag(args[1]);
        }

        if ( args[0].equals("-r") )
        {
            if ( args.length < 2 )
            {
                System.out.println("Please provide path to manifest.json.");
                return;
            }

            rFlag(args[1]);
        }

    }

}
