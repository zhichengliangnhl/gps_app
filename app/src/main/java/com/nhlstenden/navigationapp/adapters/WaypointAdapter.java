package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nhlstenden.navigationapp.R;

import java.util.List;

import models.Waypoint;

public class WaypointAdapter extends RecyclerView.Adapter<WaypointAdapter.ViewHolder> {

    private List<Waypoint> waypointList;

    public WaypointAdapter(List<Waypoint> waypointList) {
        this.waypointList = waypointList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_waypoint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Waypoint waypoint = waypointList.get(position);
        holder.nameTextView.setText(waypoint.getName());
        holder.descriptionTextView.setText(waypoint.getDescription());

        if (waypoint.getPhotoUri() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(waypoint.getPhotoUri())
                    .into(holder.photoImageView);
        }
    }

    @Override
    public int getItemCount() {
        return waypointList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView;
        ImageView photoImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.waypointNameTextView);
            descriptionTextView = itemView.findViewById(R.id.waypointDescriptionTextView);
            photoImageView = itemView.findViewById(R.id.waypointPhotoImageView);
        }
    }
}
