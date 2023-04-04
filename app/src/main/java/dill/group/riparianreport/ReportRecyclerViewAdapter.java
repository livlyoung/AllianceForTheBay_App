package dill.group.riparianreport;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.io.Resources;

import org.w3c.dom.Text;

import java.util.ArrayList;

//To edit the items shown on the RecyclerView go to the recyclerview_row layout file
class ReportRecyclerViewAdapter extends RecyclerView.Adapter<ReportRecyclerViewAdapter.MyViewHolder> { // Custom RecyclerView (ListView basically) that shows question and answer
    Context context;
    ArrayList<ReportModel> reportModels;

    private final RecyclerViewInterface recyclerViewInterface;


    public ReportRecyclerViewAdapter(Context context, ArrayList<ReportModel> reportModels, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.reportModels = reportModels;
        this.recyclerViewInterface = recyclerViewInterface;
    }



    @NonNull
    @Override
    public ReportRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ReportRecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }


    @Override
    public void onBindViewHolder(@NonNull ReportRecyclerViewAdapter.MyViewHolder holder, int position) { //Important function that shows what is displayed
        holder.question.setText(reportModels.get(position).getQuestion());
        if (reportModels.get(position).isAnswered()) {
            Log.d("Bind", String.valueOf(position));
            holder.answer.setText(reportModels.get(position).getAnswer());
            holder.answer.setVisibility(View.VISIBLE);
            holder.cardView.setBackground(this.context.getDrawable(R.drawable.question_answered_green));
        } else {
            holder.answer.setVisibility(View.INVISIBLE);
            holder.cardView.setBackground(this.context.getDrawable(R.drawable.question_not_answered));
        }
            // what other UI changes should happen when a question is Answered?

    }

    @Override
    public int getItemCount() {
        return reportModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {


        TextView question;
        TextView answer;
        CardView cardView; // This is the background of each item


        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            question = itemView.findViewById(R.id.textView_Q);
            answer = itemView.findViewById(R.id.textView_A);
            cardView = itemView.findViewById(R.id.cardView);

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
