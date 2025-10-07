package com.example.elite.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elite.R;
import com.example.elite.models.Building;

import java.util.List;

public class BuildingAdapter extends RecyclerView.Adapter<BuildingAdapter.ViewHolder> {
    private List<Building> buildingList;
    private Context context;
    private OnBuildingActionListener listener;

    public interface OnBuildingActionListener {
        void onDeleteBuilding(Building building);
        void onEditBuilding(Building building);
        void onCopyAddress(Building building);
        void onOpenMaps(Building building);
        void onBuildingClick(Building building); // Добавляем клик по элементу
    }

    public BuildingAdapter(Context context, List<Building> buildingList, OnBuildingActionListener listener) {
        this.context = context;
        this.buildingList = buildingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_building, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Building building = buildingList.get(position);
        holder.name.setText(building.getName());
        
        // Set street address
        if (building.getStreet() != null && !building.getStreet().trim().isEmpty()) {
            holder.street.setText(building.getStreet());
            holder.street.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.street.setVisibility(android.view.View.GONE);
        }
        
        // Set city and postal code
        StringBuilder cityAndCode = new StringBuilder();
        if (building.getCity() != null && !building.getCity().trim().isEmpty()) {
            cityAndCode.append(building.getCity());
        }
        if (building.getCode() != null && !building.getCode().trim().isEmpty()) {
            if (cityAndCode.length() > 0) cityAndCode.append(", ");
            cityAndCode.append(building.getCode());
        }
        
        if (cityAndCode.length() > 0) {
            holder.cityAndCode.setText(cityAndCode.toString());
            holder.cityAndCode.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.cityAndCode.setVisibility(android.view.View.GONE);
        }
        
        // Show Google Maps info if URL is available
        if (building.hasValidGoogleMapsUrl()) {
            holder.mapsInfo.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.mapsInfo.setVisibility(android.view.View.GONE);
        }
        
        // Всегда показываем кнопку Maps (проверка URL будет при клике)
        holder.buttonOpenMaps.setVisibility(android.view.View.VISIBLE);

        // Set click listeners
        holder.buttonOpenMaps.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOpenMaps(building);
            }
        });

        holder.buttonCopy.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCopyAddress(building);
            }
        });

        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditBuilding(building);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteBuilding(building);
            }
        });

        // Добавляем клик на весь элемент для открытия детальной информации
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBuildingClick(building);
            }
        });
    }

    @Override
    public int getItemCount() {
        return buildingList.size();
    }

    public void updateBuildings(List<Building> newBuildings) {
        this.buildingList = newBuildings;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, street, cityAndCode, mapsInfo;
        ImageButton buttonOpenMaps, buttonCopy, buttonEdit, buttonDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.buildingName);
            street = itemView.findViewById(R.id.buildingStreet);
            cityAndCode = itemView.findViewById(R.id.buildingCityAndCode);
            mapsInfo = itemView.findViewById(R.id.buildingMapsInfo);
            buttonOpenMaps = itemView.findViewById(R.id.button_open_maps);
            buttonCopy = itemView.findViewById(R.id.button_copy_address);
            buttonEdit = itemView.findViewById(R.id.button_edit_building);
            buttonDelete = itemView.findViewById(R.id.button_delete_building);
        }
    }
}



