package akshay.kumar.eatitserver.ViewHolder;


import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import akshay.kumar.eatitserver.R;



public class OrderViewHolder extends RecyclerView.ViewHolder{

    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtOrderAddres;

    public Button btnEdit,btnRemove,btnDetail,btnDirection;


    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderAddres = itemView.findViewById(R.id.order_addres);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        btnEdit=itemView.findViewById(R.id.btnEdit);
        btnRemove=itemView.findViewById(R.id.btnRemove);
        btnDetail=itemView.findViewById(R.id.btnDetail);
        btnDirection=itemView.findViewById(R.id.btnDirection);


    }

}
