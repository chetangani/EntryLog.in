package in.entrylog.entrylog.main.apponitments;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import in.entrylog.entrylog.R;

public class Appointment_Details extends AppCompatActivity {

    String Appointment_Name, Appointment_Mobile, Appointment_Email, Appointment_Tomeet, Appointment_Date,
            Appointment_Time, Appointment_Purpose;
    TextView tv_appointment_name, tv_appointment_mobile, tv_appointment_email, tv_appointment_tomeet,
            tv_appointment_date, tv_appointment_time, tv_appointment_purpose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Appointment_Name = bundle.getString("NAME");
        Appointment_Mobile = bundle.getString("MOBILE");
        Appointment_Email = bundle.getString("EMAIL");
        Appointment_Tomeet = bundle.getString("TOMEET");
        Appointment_Date = bundle.getString("DATE");
        Appointment_Time = bundle.getString("TIME");
        Appointment_Purpose = bundle.getString("PURPOSE");

        tv_appointment_name = (TextView) findViewById(R.id.appointment_name);
        tv_appointment_mobile = (TextView) findViewById(R.id.appointment_mobile);
        tv_appointment_email = (TextView) findViewById(R.id.appointment_email);
        tv_appointment_tomeet = (TextView) findViewById(R.id.appointment_tomeet);
        tv_appointment_date = (TextView) findViewById(R.id.appointment_date);
        tv_appointment_time = (TextView) findViewById(R.id.appointment_time);
        tv_appointment_purpose = (TextView) findViewById(R.id.appointment_purpose);

        tv_appointment_name.setText(Appointment_Name);
        tv_appointment_mobile.setText(Appointment_Mobile);
        tv_appointment_email.setText(Appointment_Email);
        tv_appointment_tomeet.setText(Appointment_Tomeet);
        tv_appointment_date.setText(Appointment_Date);
        tv_appointment_time.setText(Appointment_Time);
        tv_appointment_purpose.setText(Appointment_Purpose);
    }
}
