package com.example.getparsespecificmessage;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public class SmsReceiverService extends Service {

    private BroadcastReceiver smsReceiver;
    private DatabaseHelper databaseHelper;


    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new DatabaseHelper(this);
        // Créez et enregistrez le BroadcastReceiver pour les SMS
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // Traitez les SMS entrants ici
                if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
                    // Récupérez les informations du SMS
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        if (pdus != null) {
                            for (Object pdu : pdus) {
                                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                                String sender = smsMessage.getOriginatingAddress();
                                String body = smsMessage.getMessageBody();
                                long timestamp = smsMessage.getTimestampMillis();

                                // Créez un objet Message
                                Message message = new Message(sender, body, timestamp);

                                // Ajoutez le message à la base de données
                                databaseHelper.addMessage(message);
                                // Faites ce que vous voulez avec le SMS reçu
                                // Par exemple, vous pouvez le stocker dans une base de données, l'afficher dans une notification, etc.
                            }
                        }
                    }
                }
            }
        };

        // Enregistrez le BroadcastReceiver pour les SMS
        IntentFilter filter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(smsReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
        // Désenregistrez le BroadcastReceiver lorsque le service est détruit
        unregisterReceiver(smsReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Ce service n'a pas besoin d'une interface de liaison, retournez simplement null
        return null;
    }
}
