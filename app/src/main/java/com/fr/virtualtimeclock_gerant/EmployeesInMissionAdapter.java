package com.fr.virtualtimeclock_gerant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EmployeesInMissionAdapter extends RecyclerView.Adapter<EmployeesInMissionAdapter.MyRecyclerViewHolder> {

    EmployeesInMissionActivity employeesInMissionActivity;
    ArrayList<CompleteEmployeeInMission> userArrayList;
    String missionRef, pointageId;
    Boolean imgInZone;

    public EmployeesInMissionAdapter(EmployeesInMissionActivity mainActivity, ArrayList<CompleteEmployeeInMission> userArrayList, String missionRef, String pointageId) {
        this.employeesInMissionActivity = mainActivity;
        this.missionRef = missionRef;
        this.pointageId = pointageId;
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
        if(userArrayList.get(position).getDate() != null) {
            holder.mDate_EiM.setText(new SimpleDateFormat("EEE, dd-MM-yy  HH:mm aaa", Locale.getDefault()).format(userArrayList.get(position).getDate()));
        }
//        holder.mDeleteRowBtn_EiM.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                deleteSelectedRow(position);
//            }
//        });
        if(userArrayList.get(position).getEstPresent() != null) {
            if (userArrayList.get(position).getEstPresent()) {
                holder.mDeleteRowBtn_EiM.setBackgroundResource(R.drawable.ic_in_zone);
                imgInZone = true;
            } else if(!userArrayList.get(position).getEstPresent()) {
                holder.mDeleteRowBtn_EiM.setBackgroundResource(R.drawable.ic_out_zone);
                imgInZone = false;
            }
        }
        if(pointageId != null){
            deleteSelectedRow(pointageId);
        }
    }

    private void deleteSelectedRow(String position) {
        employeesInMissionActivity.showProgressDialog();
        employeesInMissionActivity.db.collection("pointage").document(missionRef).collection("pointageMission")
                .document(position)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        employeesInMissionActivity.hideProgressDialog();
                        Toast.makeText(employeesInMissionActivity.getBaseContext(), "Employé supprimé : success",  Toast.LENGTH_SHORT).show();
                        userArrayList.clear();
                        employeesInMissionActivity.loadDataFromFirebase();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(employeesInMissionActivity.getBaseContext(), "Employé supprimé : fail",  Toast.LENGTH_SHORT).show();

                    }
                });
    }


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