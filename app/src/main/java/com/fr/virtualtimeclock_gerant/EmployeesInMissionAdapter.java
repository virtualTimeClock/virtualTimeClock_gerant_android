package com.fr.virtualtimeclock_gerant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EmployeesInMissionAdapter extends RecyclerView.Adapter<EmployeesInMissionAdapter.MyRecyclerViewHolder> {

    EmployeesInMissionActivity employeesInMissionActivity;
    ArrayList<CompleteEmployeeInMission> userArrayList;
    Boolean imgInZone;

    public EmployeesInMissionAdapter(EmployeesInMissionActivity mainActivity, ArrayList<CompleteEmployeeInMission> userArrayList) {
        this.employeesInMissionActivity = mainActivity;
        this.userArrayList = userArrayList;
    }

    @NonNull
    @Override
    public MyRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(employeesInMissionActivity.getBaseContext());
        View view = layoutInflater.inflate(R.layout.employees_in_mission_item, parent, false);

        return new MyRecyclerViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull MyRecyclerViewHolder holder, final int position) {
        holder.mNom_EiM.setText(userArrayList.get(position).getNom());
        holder.mPrenom_EiM.setText(userArrayList.get(position).getPrenom());
        holder.mEstPresent_EiM.setText(String.valueOf(userArrayList.get(position).getEstPresent()));
//        holder.mDeleteRowBtn_EiM.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                deleteSelectedRow(position);
//            }
//        });
        if(userArrayList.get(position).getEstPresent()){
            holder.mDeleteRowBtn_EiM.setBackgroundResource(R.drawable.ic_in_zone);
            imgInZone = true;
        }else{
            holder.mDeleteRowBtn_EiM.setBackgroundResource(R.drawable.ic_out_zone);
            imgInZone = false;
        }
        holder.mDate_EiM.setText(new SimpleDateFormat("EEE, dd-MM-yy  HH:mm aaa", Locale.getDefault()).format(userArrayList.get(position).getDate()));
    }
//
//    private void deleteSelectedRow(int position) {
//        employeesInMissionActivity.db.collection("pointage").document("PUr56wkTxjT7pFSyGiPO").collection("pointageMission")
//                .document("mp0t5o1wp67A20D9edfW")
//    }


    @Override
    public int getItemCount() {
        return userArrayList.size();
    }


    public class MyRecyclerViewHolder extends RecyclerView.ViewHolder {

        public TextView mNom_EiM, mPrenom_EiM, mEstPresent_EiM, mDate_EiM;
        public Button mDeleteRowBtn_EiM;

        public MyRecyclerViewHolder(View itemView) {

            super(itemView);
            mNom_EiM = itemView.findViewById(R.id.mNom_EiM);
            mPrenom_EiM = itemView.findViewById(R.id.mPrenom_EiM);
            mEstPresent_EiM = itemView.findViewById(R.id.mEstPresent_EiM);
            mDate_EiM = itemView.findViewById(R.id.mDate_EiM);
            mDeleteRowBtn_EiM = itemView.findViewById(R.id.mDeleteRowBtn_EiM);
        }
    }
}