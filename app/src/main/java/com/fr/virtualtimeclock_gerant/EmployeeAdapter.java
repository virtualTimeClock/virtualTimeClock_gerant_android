package com.fr.virtualtimeclock_gerant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EmployeeAdapter extends FirestoreRecyclerAdapter<Employee, EmployeeAdapter.NoteHolder> {

    private OnItemClickListener listener;

    public EmployeeAdapter(@NonNull FirestoreRecyclerOptions<Employee> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteHolder holder, int position, @NonNull Employee model) {
        holder.textViewNom.setText(model.getNom());
        holder.textViewPrenom.setText(model.getPrenom());
        holder.textViewDateNaissance.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(model.getDateNaissance()));
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.employee_item,
                parent, false);
        return new NoteHolder(v);
    }

    //Supprime un élément de la liste en glissant sur un coté
    public void deleteItem(int position){
        //récupère la position particulière du document
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class NoteHolder extends RecyclerView.ViewHolder {
        TextView textViewNom;
        TextView textViewPrenom;
        TextView textViewDateNaissance;

        public NoteHolder(View itemView) {
            super(itemView);
            textViewNom = itemView.findViewById(R.id.text_view_name);
            textViewPrenom = itemView.findViewById(R.id.text_view_firstname);
            textViewDateNaissance = itemView.findViewById(R.id.text_view_birthday);


            //Clique sur l'employé pour acceder à ses données
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }
    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }
    public void setOnClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}