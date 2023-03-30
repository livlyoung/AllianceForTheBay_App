package dill.group.riparianreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
