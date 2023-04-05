package dill.group.riparianreport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryInnerAdapter extends RecyclerView.Adapter<HistoryInnerAdapter.InnerViewHolder> {
    Context context;
    ArrayList<String> questions;
    ArrayList<String> answers;

    private final RecyclerViewInterface recyclerViewInterface;


    public HistoryInnerAdapter(Context context, ArrayList<String> questions, ArrayList<String> answers, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.questions = questions;
        this.answers = answers;
        this.recyclerViewInterface = recyclerViewInterface;
    }



    @NonNull
    @Override
    public HistoryInnerAdapter.InnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.history_inner_row, parent, false);
        return new HistoryInnerAdapter.InnerViewHolder(view, recyclerViewInterface);
    }


    @Override
    public void onBindViewHolder(@NonNull HistoryInnerAdapter.InnerViewHolder holder, int position) { //Important function that shows what is displayed
        holder.question_tv.setText(questions.get(position));
        holder.answer_tv.setText(answers.get(position));
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public static class InnerViewHolder extends RecyclerView.ViewHolder {


        TextView question_tv;
        TextView answer_tv;

        public InnerViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            question_tv = itemView.findViewById(R.id.history_inner_Q);
            answer_tv = itemView.findViewById(R.id.history_inner_A);

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
