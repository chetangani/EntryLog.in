package in.entrylog.entrylog.main;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Surface;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import in.entrylog.entrylog.R;
import in.entrylog.entrylog.adapters.VisitorsAdapters;
import in.entrylog.entrylog.dataposting.ConnectingTask;
import in.entrylog.entrylog.dataposting.ConnectingTask.SmartCheckinout;
import in.entrylog.entrylog.dataposting.ConnectingTask.OvernightStay_Visitors;
import in.entrylog.entrylog.values.DetailsValue;
import in.entrylog.entrylog.values.FunctionCalls;

public class Overnightstay_Visitors extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final int VISITORS_DLG = 1;
    public static final int NOTIFY_DLG = 2;
    public static final int NOTEXIST_DLG = 3;

    RecyclerView OverNightVisitorsView;
    ArrayList<DetailsValue> OverNightVisitorsList;
    VisitorsAdapters OverNightVisitorsadapter;
    RecyclerView.LayoutManager layoutManager;
    ConnectingTask task;
    DetailsValue detailsValue;
    String Organization_ID="", ContextView, CheckingUser, Device, PrinterType;
    SharedPreferences settings;
    FunctionCalls functionCalls;
    static ProgressDialog dialog = null;
    Thread nightstaythread;
    NfcAdapter nfcAdapter;
    NfcManager nfcManager;
    boolean nfcavailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overnightstay_visitors);

        detailsValue = new DetailsValue();
        task = new ConnectingTask();
        functionCalls = new FunctionCalls();
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        functionCalls.OrientationView(Overnightstay_Visitors.this);

        ContextView = "OverNightStay";
        Organization_ID = settings.getString("OrganizationID", "");
        CheckingUser = settings.getString("User", "");
        Device = settings.getString("Device", "");
        PrinterType = settings.getString("Printertype", "");

        StaggeredRotationChanged();
        OverNightVisitorsView = (RecyclerView) findViewById(R.id.overnight_visitorsview);
        OverNightVisitorsList = new ArrayList<DetailsValue>();
        OverNightVisitorsadapter = new VisitorsAdapters(OverNightVisitorsList, Overnightstay_Visitors.this, ContextView,
                Organization_ID, CheckingUser, Device, PrinterType);
        OverNightVisitorsView.setHasFixedSize(true);
        OverNightVisitorsView.setLayoutManager(layoutManager);
        OverNightVisitorsView.setAdapter(OverNightVisitorsadapter);

        if (settings.getString("RFID", "").equals("true")) {
            nfcManager = (NfcManager) getSystemService(NFC_SERVICE);
            nfcAdapter = nfcManager.getDefaultAdapter();
            if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                nfcavailable = true;
            }
        }

        nightstaythread = null;
        Runnable runnable = new VisitorsTimer();
        nightstaythread = new Thread(runnable);
        nightstaythread.start();

        if (!Organization_ID.equals("")) {
            showdialog(NOTIFY_DLG);
        } else {
            showdialog(NOTEXIST_DLG);
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
                    if (detailsValue.isVisitorsFound()) {
                        detailsValue.setVisitorsFound(false);
                        dialog.dismiss();
                        nightstaythread.interrupt();
                    }
                    if (detailsValue.isNoVisitorsFound()) {
                        detailsValue.setNoVisitorsFound(false);
                        dialog.dismiss();
                        nightstaythread.interrupt();
                        showdialog(VISITORS_DLG);
                    }
                    String Message = "";
                    if (detailsValue.isSmartIn()) {
                        nightstaythread.interrupt();
                        detailsValue.setSmartIn(false);
                        dialog.dismiss();
                        Message = "Successfully Checked In";
                        functionCalls.smartCardStatus(Overnightstay_Visitors.this, Message);
                    }
                    if (detailsValue.isSmartOut()) {
                        nightstaythread.interrupt();
                        detailsValue.setSmartOut(false);
                        dialog.dismiss();
                        Message = "Successfully Checked Out";
                        functionCalls.smartCardStatus(Overnightstay_Visitors.this, Message);
                    }
                    if (detailsValue.isSmartError()) {
                        nightstaythread.interrupt();
                        detailsValue.setSmartError(false);
                        dialog.dismiss();
                        Message = "Checking Error.. Please swipe again..";
                        functionCalls.smartCardStatus(Overnightstay_Visitors.this, Message);
                    }
                } catch (Exception e) {
                }
            }
        });
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
                        finish();
                    }
                });
                AlertDialog alertdialog = novisitors.create();
                alertdialog.show();
                break;

            case NOTIFY_DLG:
                functionCalls.ringtone(Overnightstay_Visitors.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Over Night Stay Visitors");
                builder.setMessage("You have visitors who did not checkout in cut off time");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        OvernightStay_Visitors overnightStay_visitors = task.new OvernightStay_Visitors(OverNightVisitorsList,
                                OverNightVisitorsadapter, detailsValue, Organization_ID, Overnightstay_Visitors.this);
                        overnightStay_visitors.execute();
                        dialog = ProgressDialog.show(Overnightstay_Visitors.this, "", "Searching for a visitors..", true);
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                AlertDialog alert1 = builder.create();
                alert1.show();
                break;

            case NOTEXIST_DLG:
                functionCalls.ringtone(Overnightstay_Visitors.this);
                AlertDialog.Builder notexist = new AlertDialog.Builder(this);
                notexist.setTitle("Over Night Stay Visitors");
                notexist.setMessage("Please login to view Visitors who did not checkout in cut off time");
                notexist.setCancelable(false);
                notexist.setPositiveButton("LOGIN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Overnightstay_Visitors.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
                notexist.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                AlertDialog alert2 = notexist.create();
                alert2.show();
                ((AlertDialog) alert2).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                ((AlertDialog) alert2).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (nightstaythread.isAlive()) {
            nightstaythread.interrupt();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcavailable) {
            enableForegroundDispatchSystem();
        }
    }

    private void enableForegroundDispatchSystem() {
        Intent intent = new Intent(this, Overnightstay_Visitors.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[] {};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Toast.makeText(Overnightstay_Visitors.this, "Smart Card Intent", Toast.LENGTH_SHORT).show();
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (parcelables != null && parcelables.length > 0) {
                readTextFromMessage((NdefMessage) parcelables[0]);
            } else {
                Toast.makeText(Overnightstay_Visitors.this, "No Ndef Message Found", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(Overnightstay_Visitors.this, "No Ndef Records Found", Toast.LENGTH_SHORT).show();
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
        SmartCheckinout checkOut = task.new SmartCheckinout(detailsValue, result, Organization_ID, CheckingUser);
        checkOut.execute();
        dialog = ProgressDialog.show(Overnightstay_Visitors.this, "", "Checking Out...", true);
        nightstaythread = null;
        Runnable runnable = new VisitorsTimer();
        nightstaythread = new Thread(runnable);
        nightstaythread.start();
    }
}
