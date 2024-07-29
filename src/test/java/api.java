import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;

import java.time.Duration;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class api
{

    @Test(description="To trigger a job")

    public void api() throws InterruptedException, IOException {
        Properties prop = new Properties();
        
        try (InputStream input = new FileInputStream("config.properties")) {
            // Load properties file
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    
        OkHttpClient client = new OkHttpClient.Builder().build();
        MediaType mediaType = MediaType.parse("application/json");
    
        String licenseUser = prop.getProperty("license.user");
        String licensePassword = prop.getProperty("license.password");
        String licenseCloudServerID = prop.getProperty("license.cloudServerID");
        String licenseType = prop.getProperty("license.type");
    
        String projectUser = prop.getProperty("project.user");
        String projectPassword = prop.getProperty("project.password");
        String projectConnectionString = prop.getProperty("project.connectionString");
        String projectDb = prop.getProperty("project.db");
    
        String projectID = prop.getProperty("projectID");
        String dexAddress = prop.getProperty("dexAddress");
        String projectRootName = prop.getProperty("projectRootName");

        String concurrency = prop.getProperty("concurrency");
    
        String[] testDiscoveryResults = prop.getProperty("testDiscoveryResult").split(",");
        String[] jobLabels = prop.getProperty("jobLabel").split(",");
    
        // Construct JSON array for testDiscoveryResult
        StringBuilder testDiscoveryResultArray = new StringBuilder();
        for (String result : testDiscoveryResults) {
            if (testDiscoveryResultArray.length() > 0) {
                testDiscoveryResultArray.append(", ");
            }
            testDiscoveryResultArray.append("\"").append(result).append("\"");
        }
    
        String jsonBody = "{\n" +
                "    \"version\": \"0.2\",\n" +
                "    \"runson\": \"win\",\n" +
                "    \"concurrency\": "+concurrency+",\n" +
                "    \"autosplit\": true,\n" +
                "    \"framework\": {\n" +
                "        \"name\": \"tosca-dex\",\n" +
                "        \"toscaArgs\": {\n" +
                "            \"splitOn\": \"\",\n" +
                "            \"discoveryObject\": \"executionList\",\n" +
                "            \"license\": {\n" +
                "                \"user\": \"" + licenseUser + "\",\n" +
                "                \"password\": \"" + licensePassword + "\",\n" +
                "                \"cloudServerID\": \"" + licenseCloudServerID + "\",\n" +
                "                \"type\": \"" + licenseType + "\"\n" +
                "            },\n" +
                "            \"project\": {\n" +
                "                \"user\": \"" + projectUser + "\",\n" +
                "                \"password\": \"" + projectPassword + "\",\n" +
                "                \"connectionString\": \"" + projectConnectionString + "\",\n" +
                "                \"db\": \"" + projectDb + "\"\n" +
                "            },\n" +
                "            \"projectID\": \"" + projectID + "\",\n" +
                "            \"dexAddress\": \"" + dexAddress + "\",\n" +
                "            \"projectRootName\": \"" + projectRootName + "\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"testDiscoveryResult\": [" + testDiscoveryResultArray.toString() + "],\n" +
                "    \"sourcePayload\": {\n" +
                "        \"platform\": \"git\",\n" +
                "        \"link\": \"https://github.com/LambdaTest/HyperExecute-Playwright-Jest.git\",\n" +
                "        \"ref\": \"main\"\n" +
                "    },\n" +
                "    \"captureScreenRecordingForScenarios\": true,\n" +
                "    \"codeDirectory\": \"tosca\",\n" +
                "    \"retryOnFailure\": false,\n" +
                "    \"backgroundDirectives\": {\n" +
                "        \"commands\": [\n" +
                "            {\n" +
                "                \"name\": \"start-agents\",\n" +
                "                \"command\": \"start cmd.exe @cmd /k ToscaDistributionAgent.exe cmd-only http://4.246.225.253/DistributionServerService/CommunicationService.svc\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"jobLabel\": [\n" +
                "        \"" + String.join("\", \"", jobLabels) + "\"\n" +
                "    ],\n" +
                "    \"discoveryObject\": \"executionList\"\n" +
                "}";
    
        String auth = "tjx:s6ur01PrOLQo4mzhfVed3zkvI8Ff1Rf3fR1woPnqwDFOe6yUq5";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        System.out.println("encodedauth --------- > " + encodedAuth);
        
        Gson gson = new Gson();
        RequestBody body = RequestBody.create(mediaType, jsonBody);
        Request request = new Request.Builder()
                .url("https://api-hyperexecute.lambdatest.com/orchestrator/v1.0/job")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Basic " + encodedAuth)
                .build();
    
        String jobID = null;
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println(responseBody);
            
            // Extract jobID from the response using Gson
            
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            jobID = jsonResponse.getAsJsonObject("data").get("jobID").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        if (jobID != null) {
            // Polling for job status
            String statusUrl = "https://api.hyperexecute.cloud/v1.0/job/" + jobID + "/isFinished";
            Request statusRequest = new Request.Builder()
                    .url(statusUrl)
                    .addHeader("Authorization", "Basic " + encodedAuth)
                    .build();
            
            boolean isFinished = false;
            while (!isFinished) {
                try (Response response = client.newCall(statusRequest).execute()) {
                    String statusResponse = response.body().string();
                    System.out.println(statusResponse);
                    
                    // Parse the response to check if the job is finished using Gson
                    JsonObject statusJsonResponse = gson.fromJson(statusResponse, JsonObject.class);
                    isFinished = statusJsonResponse.get("finished").getAsBoolean();
                    
                    if (!isFinished) {
                        // Wait before polling again
                        TimeUnit.SECONDS.sleep(20);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            System.out.println("Job is finished!");
        }
    }
}


