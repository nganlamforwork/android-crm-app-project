package hcmus.android.crm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomBaseAdapter extends BaseAdapter {

    Context context;
    String listCustomer[];
    String listCustomerId[];
    int listImages[];
    LayoutInflater inflater;
    public CustomBaseAdapter(Context ctx, String[] customerList, String[] customerListId, int[] images){
        this.context = ctx;
        this.listCustomer = customerList;
        this.listCustomerId = customerListId;
        this.listImages = images;
        inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return listCustomer.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.activity_custom_list_view, null);
        TextView txtView = (TextView) convertView.findViewById(R.id.nameLabel);
        TextView idView = (TextView) convertView.findViewById(R.id.idLabel);
        ImageView customerImg = (ImageView) convertView.findViewById(R.id.imageIcon);
        txtView.setText(listCustomer[position]);
        idView.setText("Customer ID#" + listCustomerId[position]);
        customerImg.setImageResource(listImages[position]);
        return convertView;
    }
}
