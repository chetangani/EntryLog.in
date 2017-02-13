package in.entrylog.entrylog.main.apponitments;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Parcelable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import in.entrylog.entrylog.R;
import in.entrylog.entrylog.adapters.AppointmentAdapters;
import in.entrylog.entrylog.dataposting.ConnectingTask;
import in.entrylog.entrylog.dataposting.ConnectingTask.AllAppointments;
import in.entrylog.entrylog.main.Visitors;
import in.entrylog.entrylog.values.DetailsValue;
import in.entrylog.entrylog.values.FunctionCalls;

public class Appointments extends AppCompatActivity implements View.OnClickListener {
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final int REQUEST_FOR_ACTIVITY_CODE = 1;
    public static final int APPOINTMENTS_DLG = 2;

    //region Declaration
    RecyclerView Appointmentview;
    ArrayList<DetailsValue> AppointmentsList;
    AppointmentAdapters Appointmentadapter;
    RecyclerView.LayoutManager layoutManager;
    ConnectingTask task;
    DetailsValue detailsValue;
    String Organization_ID, ContextView, CheckingUser, SearchName="", SearchMobile="", SearchTomeet="",
            SearchVehicle="", Device="", PrinterType = "";
    Button Search_btn, SearchByName_btn, SearchByMobile_btn, SearchByVehicle_btn, SearchByToMeet_btn, Reset_btn;
    boolean searchname = false, searchmobile = false, searchtomeet = false, searchvehicle = false, searchcheckin = false,
            searchcheckout = false, result = false;
    EditText et_SearchName, et_SearchMobile, et_SearchTomeet, et_SearchVehicle;
    TextInputLayout Til_SearchName, Til_SearchMobile, Til_SearchTomeet, Til_SearchVehicle;
    int year, month, date;
    SharedPreferences settings;
    Thread appointmentsthread;
    FunctionCalls functionCalls;
    static ProgressDialog dialog = null;
    NfcAdapter nfcAdapter;
    NfcManager nfcManager;
    boolean nfcavailable = false;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        detailsValue = new DetailsValue();
        task = new ConnectingTask();
        functionCalls = new FunctionCalls();

        functionCalls.OrientationView(Appointments.this);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        //region Get Intent results
        Organization_ID = settings.getString("OrganizationID", "");
        CheckingUser = settings.getString("GuardID", "");
        Device = settings.getString("Device", "");
        PrinterType = settings.getString("Printertype", "");
        //endregion

        //region Buttons Initialization
        Search_btn = (Button) findViewById(R.id.search_btn);
        Search_btn.setOnClickListener(this);
        Reset_btn = (Button) findViewById(R.id.reset_btn);
        Reset_btn.setOnClickListener(this);
        SearchByName_btn = (Button) findViewById(R.id.searchvisitor_name_btn);
        SearchByName_btn.setOnClickListener(this);
        SearchByMobile_btn = (Button) findViewById(R.id.searchvisitor_mobile_btn);
        SearchByMobile_btn.setOnClickListener(this);
        SearchByVehicle_btn = (Button) findViewById(R.id.searchvisitor_vehicle_btn);
        SearchByVehicle_btn.setOnClickListener(this);
        SearchByToMeet_btn = (Button) findViewById(R.id.searchvisitor_tomeet_btn);
        SearchByToMeet_btn.setOnClickListener(this);
        //endregion

        //region Text Input Layout Initialization
        Til_SearchName = (TextInputLayout) findViewById(R.id.searchvisitor_name_Til);
        Til_SearchMobile = (TextInputLayout) findViewById(R.id.searchvisitor_mobile_Til);
        Til_SearchTomeet = (TextInputLayout) findViewById(R.id.searchvisitor_tomeet_Til);
        Til_SearchVehicle = (TextInputLayout) findViewById(R.id.searchvisitor_vehicle_Til);
        //endregion

        //region Edit Text Initialization
        et_SearchName = (EditText) findViewById(R.id.searchvisitor_name);
        et_SearchMobile = (EditText) findViewById(R.id.searchvisitor_mobile);
        et_SearchTomeet = (EditText) findViewById(R.id.searchvisitor_tomeet);
        et_SearchVehicle = (EditText) findViewById(R.id.searchvisitor_vehicle);
        //endregion

        //region RecyclerView with Adapter
        StaggeredRotationChanged();
        Appointmentview = (RecyclerView) findViewById(R.id.appointmentview);
        AppointmentsList = new ArrayList<DetailsValue>();
        Appointmentadapter = new AppointmentAdapters(Appointments.this, AppointmentsList, ContextView, Organization_ID,
                Device, PrinterType);
        Appointmentview.setHasFixedSize(true);
        Appointmentview.setLayoutManager(layoutManager);
        Appointmentview.setAdapter(Appointmentadapter);
        //endregion

        AllAppointments checkAppointments = task.new AllAppointments(AppointmentsList, Appointmentadapter, detailsValue,
                Organization_ID);
        checkAppointments.execute();
        dialog = ProgressDialog.show(Appointments.this, "", "Searching for a appointments..", true);
        appointmentsthread = null;
        Runnable runnable = new AppointmentsTimer();
        appointmentsthread = new Thread(runnable);
        appointmentsthread.start();

        et_SearchMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String number = editable.toString();
                int test = number.length();
                if (test >= 1) {
                    String trimnumber = number.substring(0, 1);
                    int num = Integer.parseInt(trimnumber);
                    if (num == 7 || num == 8 || num == 9) {
                    } else {
                        et_SearchMobile.setText("");
                    }
                }
            }
        });

        if (settings.getString("RFID", "").equals("true")) {
            nfcManager = (NfcManager) getSystemService(NFC_SERVICE);
            nfcAdapter = nfcManager.getDefaultAdapter();
            if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                nfcavailable = true;
            }
        }
    }

    private void StaggeredRotationChanged() {
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                switch (rotation) {
                    case Surface.ROTATION_0:
                        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
                        break;
                    case Surface.ROTATION_90:
                        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                        break;
                    case Surface.ROTATION_270:
                        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                        break;
                }
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
                break;
            default:
                layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                reload();
            }
        }
    }

    private void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private void searchbtn() {
        if (Search_btn.getVisibility() != View.VISIBLE) {
            Search_btn.setVisibility(View.VISIBLE);
            Reset_btn.setVisibility(View.VISIBLE);
            Appointmentview.setVisibility(View.GONE);
        }
    }

    protected void showdialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case APPOINTMENTS_DLG:
                AlertDialog.Builder novisitors = new AlertDialog.Builder(this);
                novisitors.setTitle("Visitor Details");
                novisitors.setCancelable(false);
                novisitors.setMessage("No Visitors Found to display..");
                novisitors.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                AlertDialog alertdialog = novisitors.create();
                alertdialog.show();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchvisitor_name_btn:
                SearchName = "";
                SearchByName_btn.setVisibility(View.GONE);
                searchbtn();
                reset();
                searchname = true;
                if (Til_SearchName.getVisibility() != View.VISIBLE) {
                    Til_SearchName.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.searchvisitor_mobile_btn:
                SearchMobile = "";
                SearchByMobile_btn.setVisibility(View.GONE);
                searchbtn();
                reset();
                searchmobile = true;
                if (Til_SearchMobile.getVisibility() != View.VISIBLE) {
                    Til_SearchMobile.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.searchvisitor_tomeet_btn:
                SearchTomeet = "";
                SearchByToMeet_btn.setVisibility(View.GONE);
                searchbtn();
                reset();
                searchtomeet = true;
                if (Til_SearchTomeet.getVisibility() != View.VISIBLE) {
                    Til_SearchTomeet.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.searchvisitor_vehicle_btn:
                SearchVehicle = "";
                SearchByVehicle_btn.setVisibility(View.GONE);
                searchbtn();
                reset();
                searchvehicle = true;
                if (Til_SearchVehicle.getVisibility() != View.VISIBLE) {
                    Til_SearchVehicle.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.reset_btn:
                fullreset();
                break;
        }
    }

    private void reset() {
        if (searchname) {
            if (et_SearchName.getText().toString().equals("")) {
                searchname = false;
                Til_SearchName.setVisibility(View.GONE);
                SearchByName_btn.setVisibility(View.VISIBLE);
                SearchName = "";
            }
        }
        if (searchmobile) {
            if (et_SearchMobile.getText().toString().equals("")) {
                searchmobile = false;
                Til_SearchMobile.setVisibility(View.GONE);
                SearchByMobile_btn.setVisibility(View.VISIBLE);
                SearchMobile = "";
            }
        }
        if (searchtomeet) {
            if (et_SearchTomeet.getText().toString().equals("")) {
                searchtomeet = false;
                Til_SearchTomeet.setVisibility(View.GONE);
                SearchByToMeet_btn.setVisibility(View.VISIBLE);
                SearchTomeet = "";
            }
        }
        if (searchvehicle) {
            if (et_SearchVehicle.getText().toString().equals("")) {
                searchvehicle = false;
                Til_SearchVehicle.setVisibility(View.GONE);
                SearchByVehicle_btn.setVisibility(View.VISIBLE);
                SearchVehicle = "";
            }
        }
    }

    private void fullreset() {
        Search_btn.setVisibility(View.GONE);
        Reset_btn.setVisibility(View.GONE);
        Appointmentview.setVisibility(View.VISIBLE);
        if (searchname) {
            searchname = false;
            Til_SearchName.setVisibility(View.GONE);
            et_SearchName.setText("");
            SearchByName_btn.setVisibility(View.VISIBLE);
            SearchName = "";
        }
        if (searchmobile) {
            searchmobile = false;
            Til_SearchMobile.setVisibility(View.GONE);
            et_SearchMobile.setText("");
            SearchByMobile_btn.setVisibility(View.VISIBLE);
            SearchMobile = "";
        }
        if (searchtomeet) {
            searchtomeet = false;
            Til_SearchTomeet.setVisibility(View.GONE);
            et_SearchTomeet.setText("");
            SearchByToMeet_btn.setVisibility(View.VISIBLE);
            SearchTomeet = "";
        }
        if (searchvehicle) {
            searchvehicle = false;
            Til_SearchVehicle.setVisibility(View.GONE);
            et_SearchVehicle.setText("");
            SearchByVehicle_btn.setVisibility(View.VISIBLE);
            SearchVehicle = "";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcavailable) {
            enableForegroundDispatchSystem();
        }
    }

    private void enableForegroundDispatchSystem() {
        Intent intent = new Intent(this, Visitors.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[] {};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Toast.makeText(Appointments.this, "Smart Card Intent", Toast.LENGTH_SHORT).show();
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (parcelables != null && parcelables.length > 0) {
                readTextFromMessage((NdefMessage) parcelables[0]);
            } else {
                Toast.makeText(Appointments.this, "No Ndef Message Found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void readTextFromMessage(NdefMessage ndefMessage) {
        NdefRecord[] ndefRecords = ndefMessage.getRecords();
        if (ndefRecords != null && ndefRecords.length > 0) {
            NdefRecord ndefRecord = ndefRecords[0];
            String tagcontent = getTextfromNdefRecord(ndefRecord);
            checkingout(tagcontent);
        } else {
            Toast.makeText(Appointments.this, "No Ndef Records Found", Toast.LENGTH_SHORT).show();
        }
    }

    public String getTextfromNdefRecord(NdefRecord ndefRecord) {
        String tagContent = null;
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1, payload.length - languageSize - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {

        }
        return tagContent;
    }

    public void checkingout(String result) {
        ConnectingTask.SmartCheckinout checkOut = task.new SmartCheckinout(detailsValue, result, Organization_ID, CheckingUser);
        checkOut.execute();
        dialog = ProgressDialog.show(Appointments.this, "", "Checking...", true);
        appointmentsthread = null;
        Runnable runnable = new AppointmentsTimer();
        appointmentsthread = new Thread(runnable);
        appointmentsthread.start();
    }

    class AppointmentsTimer implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    visiting();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }

    public void visiting() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (detailsValue.isAppointmentsFound()) {
                        detailsValue.setAppointmentsFound(false);
                        dialog.dismiss();
                        appointmentsthread.interrupt();
                    }
                    if (detailsValue.isAppointmentsNotFound()) {
                        detailsValue.setAppointmentsNotFound(false);
                        dialog.dismiss();
                        appointmentsthread.interrupt();
                        showdialog(APPOINTMENTS_DLG);
                    }
                    String Message = "";
                    if (detailsValue.isSmartIn()) {
                        appointmentsthread.interrupt();
                        detailsValue.setSmartIn(false);
                        dialog.dismiss();
                        Message = "Successfully Checked In";
                        functionCalls.smartCardStatus(Appointments.this, Message);
                    }
                    if (detailsValue.isSmartOut()) {
                        appointmentsthread.interrupt();
                        detailsValue.setSmartOut(false);
                        dialog.dismiss();
                        Message = "Successfully Checked Out";
                        functionCalls.smartCardStatus(Appointments.this, Message);
                    }
                    if (detailsValue.isSmartError()) {
                        appointmentsthread.interrupt();
                        detailsValue.setSmartError(false);
                        dialog.dismiss();
                        Message = "Checking Error.. Please swipe again..";
                        functionCalls.smartCardStatus(Appointments.this, Message);
                    }
                } catch (Exception e) {
                }
            }
        });
    }
}
