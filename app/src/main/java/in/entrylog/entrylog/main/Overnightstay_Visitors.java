package in.entrylog.entrylog.main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Surface;

import java.util.ArrayList;

import in.entrylog.entrylog.R;
import in.entrylog.entrylog.adapters.VisitorsAdapters;
import in.entrylog.entrylog.dataposting.ConnectingTask;
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
}
