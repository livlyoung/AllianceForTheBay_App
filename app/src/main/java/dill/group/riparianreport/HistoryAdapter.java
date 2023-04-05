package dill.group.riparianreport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.OuterViewHolder>  {
    Context context;
    ArrayList<String> dates;

    private final RecyclerViewInterface recyclerViewInterface;


    public HistoryAdapter(Context context, ArrayList<String> dates, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.dates = dates;
        this.recyclerViewInterface = recyclerViewInterface;
    }



    @NonNull
    @Override
    public HistoryAdapter.OuterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.history_row, parent, false);
        return new HistoryAdapter.OuterViewHolder(view, recyclerViewInterface);
    }


    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.OuterViewHolder holder, int position) { //Important function that shows what is displayed
        holder.date.setText(dates.get(position));
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public static class OuterViewHolder extends RecyclerView.ViewHolder {


        TextView date;

        public OuterViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            date = itemView.findViewById(R.id.date_textView);


            itemView.setOnClickListener(view -> {
                if (recyclerViewInterface != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(pos);
                    }
                }
            });
        }
    }
}
