package lukeworm.com.ceekayretailers;

import android.graphics.Movie;
import android.support.v7.widget.RecyclerView;


        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.TextView;

        import java.util.List;

public class RetailerListAdapter extends RecyclerView.Adapter<RetailerListAdapter.MyViewHolder> {

    private List<Retailer> retailerList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, code;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.retailer_name);
            code = (TextView) view.findViewById(R.id.retailer_code);
        }
    }


    public RetailerListAdapter(List<Retailer> retailerList) {
        this.retailerList = retailerList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.retailer_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Retailer retailer = retailerList.get(position);
        holder.name.setText(retailer.getName());
        holder.code.setText(retailer.getCode());
    }

    @Override
    public int getItemCount() {
        return retailerList.size();
    }
}