package dill.group.riparianreport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class GoogleSheetsAPI {
    private static final String APPLICATION_NAME = "Your Application Name";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static final String SERVICE_ACCOUNT_ID = "Your Service Account ID";
    private static final String P12_FILE_PATH = "path/to/your/key.p12";

    private Sheets sheetsService;

    public GoogleSheetsAPI() throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(new File("path/to/your/credentials.json")))
                .createScoped(Arrays.asList(SheetsScopes.SPREADSHEETS));
        sheetsService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public void appendToSheet(String spreadsheetId, String range, List<List<Object>> values) throws IOException {
        ValueRange body = new ValueRange()
                .setValues(values);
        AppendValuesResponse result = sheetsService.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();
    }

    /*
    This function takes three parameters:
    1. jsonUrl: The URL of the Firebase Realtime Database endpoint containing the JSON data.
    2. sheetId: The ID of the Google Sheets spreadsheet to append the data to.
    3. range: The cell range in the spreadsheet to append the data to (e.g. "Sheet1!A1").
     */
    public void updateSheetWithJson(String jsonUrl, String sheetId, String range) throws IOException, GeneralSecurityException {
        // Read JSON data from the Firebase Realtime Database
        URL url = new URL(jsonUrl);
        InputStream inputStream = url.openStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        String jsonData = stringBuilder.toString();

        // Parse the JSON data into a List of Maps
        Gson gson = new Gson();
        List<Map<String, Object>> data = gson.fromJson(jsonData, new TypeToken<List<Map<String, Object>>>(){}.getType());

        // Authenticate with Google Sheets API using a service account
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("path/to/credentials.json"))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        Sheets sheetsService = new Sheets.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
                .setApplicationName("My App Name")
                .build();

        // Append the data to the specified sheet and range
        List<List<Object>> values = new ArrayList<>();
        for (Map<String, Object> row : data) {
            List<Object> rowValues = new ArrayList<>();
            for (String key : row.keySet()) {
                rowValues.add(row.get(key));
            }
            values.add(rowValues);
        }
        ValueRange body = new ValueRange().setValues(values);
        sheetsService.spreadsheets().values()
                .append(sheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
    }


    public void appendDataFromDictionary(String spreadsheetId, String range, Map<String, Object> data) throws IOException {
        List<List<Object>> values = new ArrayList<>();
        List<Object> row = new ArrayList<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            row.add(entry.getValue());
        }
        values.add(row);
        appendToSheet(spreadsheetId, range, values);
    }
}
