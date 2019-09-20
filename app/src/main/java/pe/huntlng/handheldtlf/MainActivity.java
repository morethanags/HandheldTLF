package pe.huntlng.handheldtlf;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton scanImageButton, searchImageButton;
    private EditText dniEditText,lastnamesEditText, namesEditText, companyEditText ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dniEditText = (EditText) findViewById(R.id.dniEditText);
        lastnamesEditText = (EditText) findViewById(R.id.lastnamesEditText);
        namesEditText = (EditText) findViewById(R.id.namesEditText);
        companyEditText = (EditText) findViewById(R.id.companyEditText);
        scanImageButton = (ImageButton) findViewById(R.id.scanImageButton);
        scanImageButton.setOnClickListener(this);
        searchImageButton = (ImageButton) findViewById(R.id.searchImageButton);
        searchImageButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scanImageButton:
                scan();
                break;
            case R.id.searchImageButton:
                search();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(
                    requestCode, resultCode, intent);
            if (scanResult != null) {
                this.dniEditText.setText(scanResult.getContents());
                search();
            }
        }
    }

    private void search(){
        if (TextUtils.isEmpty(dniEditText.getText().toString().trim())) {
            dniEditText.setError("Ingrese DNI");
            return;
        }
        dniEditText.setError(null);
        Log.d("DNI", dniEditText.getText().toString().trim());
        new SearchPersonnelTask().execute(getResources().getString(R.string.url_path) + dniEditText.getText().toString().trim());
    }
    private void scan() {
        dniEditText.setError(null);
        Intent intent = new Intent(
                "com.google.zxing.client.android.SCAN");
        startActivityForResult(intent,
                IntentIntegrator.REQUEST_CODE);
    }
    private void displayPersonnelNotFound(){
        dniEditText.setError("No se encontró información");
        lastnamesEditText.setText(null);
        namesEditText.setText(null);
        companyEditText.setText(null);
    }
    private void displayPersonnel(JSONObject jsonObject){
        dniEditText.setError(null);
        if (jsonObject != null) {
            Log.d("jsonObject", jsonObject.toString());
            String lastname1 = !jsonObject.isNull("lastName1")? jsonObject.optString("lastName1"):"";
            String lastname2 = !jsonObject.isNull("lastName2")? jsonObject.optString("lastName2"):"";
            lastnamesEditText.setText(lastname1+" "+lastname2);
            String name = !jsonObject.isNull("name")? jsonObject.optString("name"):"";
            namesEditText.setText(name);
            String company = !jsonObject.isNull("companyAreaName")? jsonObject.optString("companyAreaName"):"";
            companyEditText.setText(company);
        }

    }
    private class SearchPersonnelTask extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;

        @SuppressWarnings("unchecked")
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(args[0]);
                Log.d("url", args[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                int code = urlConnection.getResponseCode();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                Log.d("Exception", e.getMessage());
            } finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }

        protected void onPostExecute(String result) {
            try {
                if (result != null && !result.equals("")) {
                    JSONObject jsonObject = new JSONObject(result);
                    MainActivity.this.displayPersonnel(jsonObject);
                } else {
                    MainActivity.this.displayPersonnelNotFound();
                }
            } catch (Exception ex) {
                Log.d("Exception", ex.getMessage());
            }
        }
    }
}
