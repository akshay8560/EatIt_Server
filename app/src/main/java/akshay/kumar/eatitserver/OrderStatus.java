package akshay.kumar.eatitserver;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.content.Intent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.concurrent.atomic.AtomicInteger;

import akshay.kumar.eatitserver.Common.Common;
import akshay.kumar.eatitserver.Model.MyResponse;
import akshay.kumar.eatitserver.Model.Notification;
import akshay.kumar.eatitserver.Model.Request;
import akshay.kumar.eatitserver.Model.Sender;
import akshay.kumar.eatitserver.Model.Token;
import akshay.kumar.eatitserver.Remote.APIService;
import akshay.kumar.eatitserver.ViewHolder.OrderViewHolder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    MaterialSpinner spinner;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
     APIService mService;
    FirebaseDatabase db;
    DatabaseReference requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //Init Firebase
        db = FirebaseDatabase.getInstance();
        requests = db.getReference("Requests");

        mService=Common.getFCMClint();
        //Init
        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        try {
            loadOrders(Common.currentUser.getPhone());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void loadOrders(String phone) {
        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>().setQuery(requests.orderByChild("phone").equalTo(phone), Request.class).build();

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {


            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, @SuppressLint("RecyclerView") int i, @NonNull Request model) {
                viewHolder.txtOrderId.setText(adapter.getRef(i).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddres.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());

                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener( ) {
                    @Override
                    public void onClick(View v) {

                        showUpdateDialog(adapter.getRef(i).getKey(), adapter.getItem(i));
                    }
                });
                viewHolder.btnRemove.setOnClickListener(new View.OnClickListener( ) {
                    @Override
                    public void onClick(View v) {

                        deleteOrder(adapter.getRef(i).getKey());
                    }


                });
                viewHolder.btnDetail.setOnClickListener(new View.OnClickListener( ) {
                    @Override
                    public void onClick(View v) {

                        Intent orderDetail = new Intent(OrderStatus.this, OrderDetail.class);
                        Common.currentRequest = model;

                        final Intent intent = orderDetail.putExtra("OrderId", adapter.getRef(i).getKey( ));
                        startActivity(orderDetail);
                    }
                });
                viewHolder.btnDirection.setOnClickListener(new View.OnClickListener( ) {
                    @Override
                    public void onClick(View v) {
                        Intent trackingOrder = new Intent(OrderStatus.this, TrackingOrder.class);
                        Common.currentRequest = model;
                        startActivity(trackingOrder);
                    }
                });

            }

            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_layout, parent, false);
                return new OrderViewHolder(view);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }


    private void deleteOrder(String key) {
        requests.child(key).removeValue();
        adapter.notifyDataSetChanged();
    }

    private void showUpdateDialog(String key, final Request item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please Choose Status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout, null);

        spinner = view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed", "On My Way", "Shipped");

        alertDialog.setView(view);

        final String localKey = key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                requests.child(localKey).setValue(item);
                adapter.notifyDataSetChanged();
                Toast.makeText(OrderStatus.this, "Order was updated !", Toast.LENGTH_SHORT).show( );

                sendOrderStatusToUser(localKey,item);
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void sendOrderStatusToUser(final String key,final Request item) {
        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");
        tokens.orderByKey().equalTo(item.getPhone()).addValueEventListener(new ValueEventListener( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapShot:snapshot.getChildren()){
                    Token serverToken=postSnapShot.getValue( Token.class );

                    Notification notification=new Notification( "EAT IT","Your Order "+key+"Was Updated"  );
                    Sender content=new Sender(serverToken.getToken(),notification);
                    mService.sendNotification(content).enqueue(new Callback<MyResponse>( ) {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {


                           if (response.code()==200){
                               if (response.body( ).success == 1) {
                                   Toast.makeText(OrderStatus.this, "Order was updated !", Toast.LENGTH_SHORT).show( );
                               }else {
                                   Toast.makeText(OrderStatus.this, "failed to send notification !", Toast.LENGTH_SHORT).show( );

                               }
                           }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                            Toast.makeText(OrderStatus.this, t.getMessage(), Toast.LENGTH_SHORT).show( );
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
