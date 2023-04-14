package dill.group.riparianreport;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

//To edit the items shown on the RecyclerView go to the recyclerview_row layout file
class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.MyViewHolder> { // Custom RecyclerView (ListView basically) that shows question and answer
    Context context;
    ArrayList<ReportModel> reportModels;

    private final RecyclerViewInterface recyclerViewInterface;


    public ReportAdapter(Context context, ArrayList<ReportModel> reportModels, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.reportModels = reportModels;
        this.recyclerViewInterface = recyclerViewInterface;
    }



    @NonNull
    @Override
    public ReportAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ReportAdapter.MyViewHolder(view, recyclerViewInterface);
    }


    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.MyViewHolder holder, int position) { //Important function that shows what is displayed
        holder.question.setText(reportModels.get(position).getQuestion());
        if (reportModels.get(position).isAnswered()) {
            Log.d("Bind", String.valueOf(position));
            holder.question.setTextColor(this.context.getColor(R.color.palete_2_3));
            holder.answer.setText(reportModels.get(position).getAnswer());
            holder.answer.setVisibility(View.VISIBLE);
            holder.cardView.setBackground(this.context.getDrawable(R.drawable.question_answered_green));
        } else {
            holder.answer.setVisibility(View.INVISIBLE);
            holder.question.setTextColor(this.context.getColor(R.color.matte_black));
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
