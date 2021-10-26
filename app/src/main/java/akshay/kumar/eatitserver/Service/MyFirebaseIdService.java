package akshay.kumar.eatitserver.Service;


import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

import akshay.kumar.eatitserver.Common.Common;
import akshay.kumar.eatitserver.Model.Token;


public class MyFirebaseIdService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKEN", s);
        if (Common.currentRequest!=null){
            updateToServer();
        }

    }

    private void updateToServer() {
       if (Common.currentUser!=null){
           FirebaseMessaging.getInstance ().getToken ()
                   .addOnCompleteListener ( task -> {
                       if (!task.isSuccessful ()) {
                           //Could not get FirebaseMessagingToken
                           return;
                       }
                       if (null != task.getResult ()) {
                           //Got FirebaseMessagingToken
                           String firebaseMessagingToken = Objects.requireNonNull ( task.getResult () );
                           //Use firebaseMessagingToken further
                           FirebaseDatabase db=FirebaseDatabase.getInstance();
                           DatabaseReference tokens=db.getReference("Tokens");
                           Token data= new Token(firebaseMessagingToken,true );
                           tokens.child(Common.currentUser.getPhone()).setValue(data);
                       }
                   } );
       }
       }

}