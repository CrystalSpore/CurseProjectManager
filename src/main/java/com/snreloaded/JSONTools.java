package com.snreloaded;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JSONTools {

    public static String getModName(JSONObject jsonObject)
    {
        return (String)jsonObject.get("name");
    }

    public static JSONObject getJSONFromFile(String pathToJson)
    {
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject)(new JSONParser().parse(new FileReader(pathToJson)));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return  jsonObject;
    }

    public static void writeJSONFile(JSONObject jsonObject, String pathToJson)
    {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            FileWriter writer = new FileWriter( pathToJson );

            String prettyJSON = gson.toJson(jsonObject);

            writer.write( prettyJSON );
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject makeModJSONObject(long projectID, long fileID)
    {
        JSONObject modObject = new JSONObject();
        modObject.put("projectID",projectID);
        modObject.put("required",true);
        modObject.put("fileID",fileID);
        return modObject;
    }
}
