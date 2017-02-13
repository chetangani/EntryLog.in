package in.entrylog.entrylog.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import in.entrylog.entrylog.R;
import in.entrylog.entrylog.main.apponitments.Appointment_Details;
import in.entrylog.entrylog.values.DetailsValue;
import in.entrylog.entrylog.values.EL101_102;
import in.entrylog.entrylog.values.FunctionCalls;

/**
 * Created by Chetan Gani on 2/13/2017.
 */

public class AppointmentAdapters extends RecyclerView.Adapter<AppointmentAdapters.AppointmentViewHolder> {

    private static final int REQUEST_FOR_ACTIVITY_CODE = 1;
    ArrayList<DetailsValue> arrayList = new ArrayList<DetailsValue>();
    Context context;
    String ContextView, Organization_ID, Device, PrinterType;
    FunctionCalls functionCalls = new FunctionCalls();
    EL101_102 el101_102device = new EL101_102();

    public AppointmentAdapters(Context context, ArrayList<DetailsValue> arrayList, String contextview, String organization_ID,
                               String device, String printertype) {
        this.context = context;
        this.arrayList = arrayList;
        this.ContextView = contextview;
        this.Organization_ID = organization_ID;
        this.Device = device;
        this.PrinterType = printertype;
    }

    @Override
    public AppointmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_cardview, parent, false);
        AppointmentViewHolder viewHolder = new AppointmentViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AppointmentViewHolder holder, int position) {
        DetailsValue details = arrayList.get(position);
        holder.tv_appointername.setText(details.getVisitors_Name());
        holder.tv_appointer_tomeet.setText(details.getVisitors_tomeet());
        holder.tv_appointerdate.setText(functionCalls.ConvertApointmentDate(details.getAppointmentDate()));
        holder.tv_appointertime.setText(details.getAppointmentTime());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class AppointmentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_appointername, tv_appointer_tomeet, tv_appointerdate, tv_appointertime;

        public AppointmentViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tv_appointername = (TextView) itemView.findViewById(R.id.appointer_card_name);
            tv_appointer_tomeet = (TextView) itemView.findViewById(R.id.appointer_card_tomeet);
            tv_appointerdate = (TextView) itemView.findViewById(R.id.appointer_card_date);
            tv_appointertime = (TextView) itemView.findViewById(R.id.appointer_card_time);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            DetailsValue details = arrayList.get(pos);
            Intent intent = null;
            intent = new Intent(context, Appointment_Details.class);
            intent.putExtra("NAME", details.getVisitors_Name());
            intent.putExtra("MOBILE", details.getVisitors_Mobile());
            intent.putExtra("EMAIL", details.getVisitors_Email());
            intent.putExtra("TOMEET", details.getVisitors_tomeet());
            intent.putExtra("DATE", functionCalls.ConvertApointmentDate(details.getAppointmentDate()));
            intent.putExtra("TIME", details.getAppointmentTime());
            intent.putExtra("PURPOSE", details.getPurpose());
            ((Activity) context).startActivityForResult(intent, REQUEST_FOR_ACTIVITY_CODE);
        }
    }
}
