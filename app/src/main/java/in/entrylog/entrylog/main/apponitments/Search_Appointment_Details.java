package in.entrylog.entrylog.main.apponitments;

import android.app.Activity;
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
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Surface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import in.entrylog.entrylog.R;
import in.entrylog.entrylog.adapters.AppointmentAdapters;
import in.entrylog.entrylog.dataposting.ConnectingTask;
import in.entrylog.entrylog.dataposting.ConnectingTask.SmartCheckinout;
import in.entrylog.entrylog.dataposting.ConnectingTask.SearchAppointments;
import in.entrylog.entrylog.values.DetailsValue;
import in.entrylog.entrylog.values.FunctionCalls;

public class Search_Appointment_Details extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final int VISITORS_DLG = 1;

    //region Declaration
    RecyclerView SearchAppointmentsView;
    ArrayList<DetailsValue> SearchAppointmentList;
    AppointmentAdapters SearchAppointmentsadapter;
    RecyclerView.LayoutManager layoutManager;
    ConnectingTask task;
    DetailsValue detailsValue;
    String Organization_ID, ContextView, CheckingUser, CheckinDate="", CheckoutDate="", SearchName="", SearchEmail="",
            SearchMobile="", SearchTomeet="", SearchVehicle="", Device="", PrinterType;
    LinearLayout SearchVehicle_layout, SearchDetails_layout, SearchDetails2_layout, SearchCheckdate_layout, Searchtomeet_layout,
        SearchMobile_layout, SearchName_layout, SearchCheckin_layout, SearchCheckout_layout;
    TextView tv_CheckinDate, tv_CheckoutDate, tv_VisitorName, tv_VisitorMobile, tv_VisitorMeet, tv_VisitorVehicle;
    SharedPreferences settings;
    Thread visitorsthread;
    FunctionCalls functionCalls;
    static ProgressDialog dialog = null;
    NfcAdapter nfcAdapter;
    NfcManager nfcManager;
    boolean nfcavailable = false;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_appointment_details);

        detailsValue = new DetailsValue();
        task = new ConnectingTask();
        functionCalls = new FunctionCalls();

        functionCalls.OrientationView(Search_Appointment_Details.this);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        //region Get Intent Results
        Intent intent = getIntent();
        Bundle bnd = intent.getExtras();
        Organization_ID = bnd.getString("ID");
        ContextView = bnd.getString("VIEW");
        CheckinDate = bnd.getString("ACHECKINDATE");
        SearchName = bnd.getString("ASEARCHNAME");
        SearchMobile = bnd.getString("ASEARCHMOBILE");
        SearchTomeet = bnd.getString("ASEARCHTOMEET");
          //endregion

        //region Search Linear Layouts Initialization
         SearchDetails_layout = (LinearLayout) findViewById(R.id.searchdetails_layout);
        SearchDetails2_layout = (LinearLayout) findViewById(R.id.searchdetails2_layout);
        SearchCheckdate_layout = (LinearLayout) findViewById(R.id.searchcheckdate_layout);
        Searchtomeet_layout = (LinearLayout) findViewById(R.id.searchtomeet_layout);
        SearchMobile_layout = (LinearLayout) findViewById(R.id.searchmobile_layout);
        SearchName_layout = (LinearLayout) findViewById(R.id.searchname_layout);
        SearchCheckin_layout = (LinearLayout) findViewById(R.id.searchcheckin_layout);
        //endregion

        //region Search TextView Initialization
        tv_CheckinDate = (TextView) findViewById(R.id.tv_checkindate);
        tv_VisitorName = (TextView) findViewById(R.id.tv_visitorname);
        tv_VisitorMobile = (TextView) findViewById(R.id.tv_visitormobile);
        tv_VisitorMeet = (TextView) findViewById(R.id.tv_visitormeet);
        //endregion

        SearchVisibility();

        //region RecyclerView with Adapter
        StaggeredRotationChanged();
        SearchAppointmentsView = (RecyclerView) findViewById(R.id.searchappointmentsview);
        SearchAppointmentList = new ArrayList<DetailsValue>();
        SearchAppointmentsadapter = new AppointmentAdapters(Search_Appointment_Details.this, SearchAppointmentList, ContextView, Organization_ID,
                Device, PrinterType);

        SearchAppointmentsView.setHasFixedSize(true);
        SearchAppointmentsView.setLayoutManager(layoutManager);
        SearchAppointmentsView.setAdapter(SearchAppointmentsadapter);
        //endregion

        if (settings.getString("RFID", "").equals("true")) {
            nfcManager = (NfcManager) getSystemService(NFC_SERVICE);
            nfcAdapter = nfcManager.getDefaultAdapter();
            if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                nfcavailable = true;
            }
        }
        SearchAppointments searchAppointments = task.new SearchAppointments(SearchAppointmentList, SearchAppointmentsadapter,
                detailsValue, Organization_ID, SearchName, SearchMobile, SearchTomeet, convertdate(CheckinDate));
        searchAppointments.execute();
        dialog = ProgressDialog.show(Search_Appointment_Details.this, "", "Searching for a Appointments..", true);
        visitorsthread = null;
        Runnable runnable = new VisitorsTimer();
        visitorsthread = new Thread(runnable);
        visitorsthread.start();

    }

    class VisitorsTimer implements Runnable {

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
                        detailsValue.setAppointmentsNotFound(false);
                        dialog.dismiss();
                        visitorsthread.interrupt();
                    }
                    if (detailsValue.isAppointmentsNotFound()) {
                        detailsValue.setAppointmentsNotFound(false);
                        dialog.dismiss();
                        visitorsthread.interrupt();
                        showdialog(VISITORS_DLG);
                    }
                    String Message = "";
                    if (detailsValue.isSmartIn()) {
                        visitorsthread.interrupt();
                        detailsValue.setSmartIn(false);
                        dialog.dismiss();
                        Message = "Successfully Checked In";
                        functionCalls.smartCardStatus(Search_Appointment_Details.this, Message);
                    }
                    if (detailsValue.isSmartOut()) {
                        visitorsthread.interrupt();
                        detailsValue.setSmartOut(false);
                        dialog.dismiss();
                        Message = "Successfully Checked Out";
                        functionCalls.smartCardStatus(Search_Appointment_Details.this, Message);
                    }
                    if (detailsValue.isSmartError()) {
                        visitorsthread.interrupt();
                        detailsValue.setSmartError(false);
                        dialog.dismiss();
                        Message = "Checking Error.. Please swipe again..";
                        functionCalls.smartCardStatus(Search_Appointment_Details.this, Message);
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    private void SearchVisibility() {
        //region Search Visibility Details
        if (!CheckinDate.equals("")) {
            if (SearchCheckdate_layout.getVisibility() != View.VISIBLE) {
                SearchCheckdate_layout.setVisibility(View.VISIBLE);
            }
            if (SearchCheckin_layout.getVisibility() != View.VISIBLE) {
                SearchCheckin_layout.setVisibility(View.VISIBLE);
                tv_CheckinDate.setText(CheckinDate);
            }
        }
        if (!CheckoutDate.equals("")) {
            if (SearchCheckdate_layout.getVisibility() != View.VISIBLE) {
                SearchCheckdate_layout.setVisibility(View.VISIBLE);
            }
            if (SearchCheckout_layout.getVisibility() != View.VISIBLE) {
                SearchCheckout_layout.setVisibility(View.VISIBLE);
                tv_CheckoutDate.setText(CheckoutDate);
            }
        }
        if (!SearchName.equals("")) {
            if (SearchDetails_layout.getVisibility() != View.VISIBLE) {
                SearchDetails_layout.setVisibility(View.VISIBLE);
            }
            if (SearchName_layout.getVisibility() != View.VISIBLE) {
                SearchName_layout.setVisibility(View.VISIBLE);
                tv_VisitorName.setText(SearchName);
            }
        }
        if (!SearchMobile.equals("")) {
            if (SearchDetails_layout.getVisibility() != View.VISIBLE) {
                SearchDetails_layout.setVisibility(View.VISIBLE);
            }
            if (SearchMobile_layout.getVisibility() != View.VISIBLE) {
                SearchMobile_layout.setVisibility(View.VISIBLE);
                tv_VisitorMobile.setText(SearchMobile);
            }
        }
        if (!SearchTomeet.equals("")) {
            if (SearchDetails2_layout.getVisibility() != View.VISIBLE) {
                SearchDetails2_layout.setVisibility(View.VISIBLE);
            }
            if (Searchtomeet_layout.getVisibility() != View.VISIBLE) {
                Searchtomeet_layout.setVisibility(View.VISIBLE);
                tv_VisitorMeet.setText(SearchTomeet);
            }
        }
        if (!SearchVehicle.equals("")) {
            if (SearchDetails2_layout.getVisibility() != View.VISIBLE) {
                SearchDetails2_layout.setVisibility(View.VISIBLE);
            }
            if (SearchVehicle_layout.getVisibility() != View.VISIBLE) {
                SearchVehicle_layout.setVisibility(View.VISIBLE);
                tv_VisitorVehicle.setText(SearchVehicle);
            }
        }
        //endregion
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
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }
    }

    private String convertdate(String Date) {
        String date = "";
        if (!Date.equals("")) {
            String date1 = Date.substring(0, 2);
            String date2 = Date.substring(3, 5);
            String date3 = Date.substring(6, 10);
            date = date3 + "-" + date2 + "-" + date1;
        } else {
            date = Date;
        }
        return date;
    }

    protected void showdialog(int id) {
        switch (id) {
            case VISITORS_DLG:
                AlertDialog.Builder novisitors = new AlertDialog.Builder(this);
                novisitors.setTitle("Visitor Details");
                novisitors.setCancelable(false);
                novisitors.setMessage("No Visitors Found to display..");
                novisitors.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                });
                AlertDialog alertdialog = novisitors.create();
                alertdialog.show();
                break;
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
        Intent intent = new Intent(this, Search_Appointment_Details.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[] {};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Toast.makeText(Search_Appointment_Details.this, "Smart Card Intent", Toast.LENGTH_SHORT).show();
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (parcelables != null && parcelables.length > 0) {
                readTextFromMessage((NdefMessage) parcelables[0]);
            } else {
                Toast.makeText(Search_Appointment_Details.this, "No Ndef Message Found", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(Search_Appointment_Details.this, "No Ndef Records Found", Toast.LENGTH_SHORT).show();
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
        SmartCheckinout checkOut = task.new SmartCheckinout(detailsValue, result,
                Organization_ID, CheckingUser);
        checkOut.execute();
        dialog = ProgressDialog.show(Search_Appointment_Details.this, "", "Checking Out...", true);
        visitorsthread = null;
        Runnable runnable = new VisitorsTimer();
        visitorsthread = new Thread(runnable);
        visitorsthread.start();
    }


}
