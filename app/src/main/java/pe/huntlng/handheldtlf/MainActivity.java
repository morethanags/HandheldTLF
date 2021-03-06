package pe.huntlng.handheldtlf;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton scanImageButton, searchImageButton;
    private EditText dniEditText, lastnamesEditText, namesEditText, companyEditText, camoTypeEditText,
            camoStatusEditText, camoExpirationEditText,
            sctrStatusEditText, sctrExpirationEditText;

    private EditText formStatusEditText, formExpirationEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dniEditText = (EditText) findViewById(R.id.dniEditText);
        lastnamesEditText = (EditText) findViewById(R.id.lastnamesEditText);
        namesEditText = (EditText) findViewById(R.id.namesEditText);
        companyEditText = (EditText) findViewById(R.id.companyEditText);
        camoTypeEditText = (EditText) findViewById(R.id.camoTypeEditText);
        camoStatusEditText = (EditText) findViewById(R.id.camoStatusEditText);
        camoExpirationEditText = (EditText) findViewById(R.id.camoExpirationEditText);

        sctrStatusEditText = (EditText) findViewById(R.id.sctrStatusEditText);
        sctrExpirationEditText = (EditText) findViewById(R.id.sctrExpirationEditText);

        scanImageButton = (ImageButton) findViewById(R.id.scanImageButton);
        scanImageButton.setOnClickListener(this);
        searchImageButton = (ImageButton) findViewById(R.id.searchImageButton);
        searchImageButton.setOnClickListener(this);


        formStatusEditText = (EditText) findViewById(R.id.formStatusEditText);
        formExpirationEditText = (EditText) findViewById(R.id.formExpirationEditText);
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

    private void search() {
        if (TextUtils.isEmpty(dniEditText.getText().toString().trim())) {
            dniEditText.setError("Ingrese DNI");
            return;
        }
        dniEditText.setError(null);
        Log.d("DNI", dniEditText.getText().toString().trim());
        new SearchPersonnelTask().execute(getResources().getString(R.string.url_path) + dniEditText.getText().toString().trim());
        new SearchPersonnelFormTask().execute(getResources().getString(R.string.url_path_form) + dniEditText.getText().toString().trim() + "?includeForm=0");
    }

    private void scan() {
        dniEditText.setError(null);
        Intent intent = new Intent(
                "com.google.zxing.client.android.SCAN");
        startActivityForResult(intent,
                IntentIntegrator.REQUEST_CODE);
    }

    private void displayPersonnelNotFound() {
        dniEditText.setError("No se encontró información");
        clearPersonnel();
    }

    private void clearPersonnel() {
        lastnamesEditText.setText(null);
        lastnamesEditText.setError(null);
        namesEditText.setText(null);
        namesEditText.setError(null);
        companyEditText.setText(null);
        companyEditText.setError(null);
        camoTypeEditText.setText(null);
        camoTypeEditText.setError(null);
        camoStatusEditText.setText(null);
        camoStatusEditText.setError(null);
        camoExpirationEditText.setText(null);
        camoExpirationEditText.setError(null);
        sctrStatusEditText.setText(null);
        sctrStatusEditText.setError(null);
        sctrExpirationEditText.setText(null);
        sctrExpirationEditText.setError(null);
        //limpiar tambien campos de formulario
        formStatusEditText.setText(null);
        formStatusEditText.setError(null);
        formExpirationEditText.setText(null);
        formExpirationEditText.setError(null);

    }

    private void displayPersonnel(JSONObject jsonObject) throws JSONException {
        dniEditText.setError(null);
        if (jsonObject != null) {
            Log.d("jsonObject", jsonObject.toString());
            String lastname1 = !jsonObject.isNull("lastName1") ? jsonObject.optString("lastName1") : "";
            String lastname2 = !jsonObject.isNull("lastName2") ? jsonObject.optString("lastName2") : "";
            lastnamesEditText.setText(lastname1 + " " + lastname2);
            String name = !jsonObject.isNull("name") ? jsonObject.optString("name") : "";
            namesEditText.setText(name);
            String company = !jsonObject.isNull("companyAreaName") ? jsonObject.optString("companyAreaName") : "";
            companyEditText.setText(company);
            if (!jsonObject.isNull("camo")) {
                camoTypeEditText.setError(null);
                camoStatusEditText.setError(null);
                camoExpirationEditText.setError(null);
                JSONObject jsonObjectCamo = jsonObject.getJSONObject("camo");
                String camotype = !jsonObjectCamo.isNull("documentType") ? jsonObjectCamo.optString("documentType") : "";
                camoTypeEditText.setText(camotype);
                String camostatus;
                if (jsonObjectCamo.optBoolean("validity")) {
                    camostatus = "VIGENTE";
                } else if (jsonObjectCamo.optBoolean("expired")) {
                    camostatus = "EXPIRADO";
                    camoStatusEditText.setError("");
                } else {
                    camostatus = "NO HAY INFORMACION";
                    camoStatusEditText.setError("");
                }
                camoStatusEditText.setText(camostatus);
                if (!jsonObjectCamo.isNull("expirationDate")) {
                    camoExpirationEditText.setText(jsonObjectCamo.optString("expirationDate"));
                }
            } else {
                camoTypeEditText.setText(null);
                camoStatusEditText.setText(null);
                camoExpirationEditText.setText(null);

                camoTypeEditText.setError("");
                camoStatusEditText.setError("");
                camoExpirationEditText.setError("");
            }
            if (!jsonObject.isNull("sctr")) {
                sctrStatusEditText.setError(null);
                sctrExpirationEditText.setError(null);
                JSONObject jsonObjectSctr = jsonObject.getJSONObject("sctr");
                String sctrstatus;
                if (jsonObjectSctr.optBoolean("validity")) {
                    sctrstatus = "VIGENTE";
                } else if (jsonObjectSctr.optBoolean("expired")) {
                    sctrstatus = "EXPIRADO";
                    sctrStatusEditText.setError("");
                } else {
                    sctrstatus = "NO HAY INFORMACION";
                    sctrStatusEditText.setError("");
                }
                sctrStatusEditText.setText(sctrstatus);
                if (!jsonObjectSctr.isNull("expirationDate")) {
                    sctrExpirationEditText.setText(jsonObjectSctr.optString("expirationDate"));
                }
            } else {
                sctrStatusEditText.setText(null);
                sctrExpirationEditText.setText(null);

                sctrStatusEditText.setError("");
                sctrExpirationEditText.setError("");
            }
        }

    }

    private void displayPersonnelFormNotFound() {
        //persona encontrada pero no info de formulario
        formStatusEditText.setText(null);
        formStatusEditText.setError("");
        formExpirationEditText.setText(null);
        formExpirationEditText.setError("");

    }

    private void displayPersonnelForm(JSONObject jsonObject) throws JSONException {
        if (jsonObject != null) {
            Log.d("jsonObjectForm", jsonObject.toString());

            JSONObject personForm = jsonObject.getJSONObject("personForm");

            String formstatus = personForm.optString("statusStr");
            formStatusEditText.setText(formstatus);
            if (!personForm.optBoolean("status")) {
                formStatusEditText.setError("");
            } else {
                formStatusEditText.setError(null);
            }
            String formexpiration = personForm.optString("expirationDate");
            formExpirationEditText.setText(formexpiration);
            if (personForm.optBoolean("expired")) {
                formExpirationEditText.setError("");
            } else {
                formExpirationEditText.setError(null);
            }
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

                final String basicAuth = "Basic " + Base64.encodeToString(getResources().getString(R.string.url_credentials).getBytes(), Base64.NO_WRAP);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", basicAuth);
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

    private class SearchPersonnelFormTask extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;

        @SuppressWarnings("unchecked")
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(args[0]);
                Log.d("url", args[0]);

                final String basicAuth = "Basic " + Base64.encodeToString(getResources().getString(R.string.url_credentials).getBytes(), Base64.NO_WRAP);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", basicAuth);
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
                if (result != null && !result.equals("") && !new JSONObject(result).isNull("personForm")) {
                    JSONObject jsonObject = new JSONObject(result);
                    MainActivity.this.displayPersonnelForm(jsonObject);
                } else {
                    MainActivity.this.displayPersonnelFormNotFound();
                }
            } catch (Exception ex) {
                Log.d("Exception", ex.getMessage());
            }
        }
    }

}
