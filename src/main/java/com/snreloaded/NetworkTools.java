package com.snreloaded;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class NetworkTools {

    public static JSONObject getModJSON(String projectID)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("https://curse.nikky.moe/api/addon/"+projectID);
        String response = target.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);

        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject)(new JSONParser().parse(response));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        client.close();
        return jsonObject;
    }

    public static String getLatestFileID(JSONObject jsonObject, String minecraftVersion)
    {
        JSONArray latestFiles = (JSONArray)jsonObject.get("gameVersionLatestFiles");

        for (Object latestFile : latestFiles) {
            JSONObject curObject = (JSONObject)latestFile;
            String fileVersion = (String) curObject.get("gameVersion");

            if (fileVersion.equals(minecraftVersion)) {
                return ((Long) curObject.get("projectFileId")).toString();
            }
        }

        return null;

    }

    /**
     * Handles the actual download of the file
     * @param imgURL - URL of the server file
     * @param imgSavePath - path & name of file to save as
     * @return success status
     */
    public static boolean saveFile(String imgURL, String imgSavePath, boolean silent) {

        boolean isSucceed = true;

        CloseableHttpClient httpClient = HttpClients.createDefault();

        URL url = null;

        try {
            url = new URL(imgURL);
        }
        catch ( MalformedURLException e )
        {
            isSucceed = false;
        }

        if (isSucceed) {
            HttpGet httpGet = new HttpGet(url.toString());
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.11 Safari/537.36");

            try {
                CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity imageEntity = httpResponse.getEntity();

                if (imageEntity != null) {
                    File outputFile = new File(imgSavePath);
                    BufferedInputStream inputStream = new BufferedInputStream(imageEntity.getContent());
                    URLConnection tempConnect = url.openConnection();
                    int size = tempConnect.getContentLength();
                    FileOutputStream fout = new FileOutputStream(outputFile);
                    byte buffer[] = new byte[524288];

                    // Read from server into buffer.
                    if (!silent)
                    {
                        System.out.println("Download:\n\t"+String.format("%10.2f",0.0)+"%");
                    }
                    int byteContent;
                    double percent = 0;
                    int ctr = 0;
                    while ((byteContent = inputStream.read(buffer, 0, 524288)) != -1) {
                        fout.write(buffer, 0, byteContent);
                        percent += (((double)byteContent)/size);
                        if (!silent)
                        {
                            if ( ctr == 2048 )
                            {
                                String is = String.format("%10.2f", (percent*100));
                                System.out.println("\t"+is+"%");
                                ctr = 0;
                            }
                            ctr++;
                        }
                    }
                    if (!silent)
                    {
                        System.out.println("\t"+String.format("%10.2f",100.00)+"%");
                        System.out.println("Download is complete!");
                    }
                }

                httpGet.releaseConnection();
                httpClient.close();
            } catch (IOException e) {
                isSucceed = false;
            }
        }

        return isSucceed;
    }

}
