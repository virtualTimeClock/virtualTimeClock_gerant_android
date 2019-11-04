package com.fr.virtualtimeclock_gerant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MissionAdapter extends FirestoreRecyclerAdapter<Mission, MissionAdapter.NoteHolder> {

    private OnItemClickListener listener;

    public MissionAdapter(@NonNull FirestoreRecyclerOptions<Mission> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteHolder holder, int position, @NonNull Mission model) {
        holder.textViewTitle.setText(model.getTitre());
        holder.textViewDescription.setText(model.getDescription());
        holder.textViewLocation.setText(model.getLieu());
        holder.textViewDebut.setText(new SimpleDateFormat("EEE, dd-MM-yy  HH:mm aaa").format(model.getDebut()));
        holder.textViewFin.setText(new SimpleDateFormat("EEE, dd-MM-yy  HH:mm aaa").format(model.getFin()));
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mission_item,
                parent, false);
        return new NoteHolder(v);
    }

    //Supprime un élément de la liste en glissant sur un coté
    public void deleteItem(int position){
        //récupère la position particulière du document
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class NoteHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewLocation;
        TextView textViewDebut;
        TextView textViewFin;

        public NoteHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            textViewLocation = itemView.findViewById(R.id.text_view_location);
            textViewDebut = itemView.findViewById(R.id.text_view_date_start);
            textViewFin = itemView.findViewById(R.id.text_view_date_stop);

            //Clique sur la mission pour acceder à ses données
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
