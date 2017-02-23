package jp.ac.it_college.std.s15009.attend;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Recyclerview adapter
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ArrayList<Stu_info> mData;
    private Context mContext;

    public RecyclerAdapter(Context context, ArrayList<Stu_info> data){
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.attend_student, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int posi) {
        if(mData !=  null && mData.size() > posi && mData.get(posi) != null){
            holder.num_stu.setText(mData.get(posi).getNum_stu());
            holder.stu_name.setText(mData.get(posi).getName_stu());
//            holder.attending_time.setText(mData.get(posi).getAttend_time());
//            holder.back_sc_time.setText(mData.get(posi).getBack_sc_time());
        }
    }
    @Override
    public int getItemCount() {
        if(mData != null){
            return mData.size();
        } else {
            return 0;
        }

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView num_stu;
        private TextView stu_name;
//        private TextView attending_time;
//        private TextView back_sc_time;

        public ViewHolder(View itemView) {
            super(itemView);
            num_stu = (TextView)itemView.findViewById(R.id.num_stu);
            stu_name = (TextView)itemView.findViewById(R.id.stu_name);
//            attending_time = (TextView)itemView.findViewById(R.id.attending_time);
//            back_sc_time = (TextView)itemView.findViewById(R.id.back_sc_time);

        }

    }
}
