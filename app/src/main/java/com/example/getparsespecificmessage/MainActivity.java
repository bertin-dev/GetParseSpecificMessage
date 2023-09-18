package com.example.getparsespecificmessage;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.getparsespecificmessage.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView smsListView;
    private ArrayAdapter<String> smsAdapter;
    private ArrayList<String> smsList;

    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    // Récupérer les SMS reçus
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        for (Object pdu : pdus) {
                            // Convertir le PDU en un objet SmsMessage
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);

                            // Récupérer le corps du SMS
                            String messageBody = smsMessage.getMessageBody();

                            // Ajouter le SMS à la liste
                            smsList.add(messageBody);
                        }

                        // Mettre à jour la ListView
                        smsAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smsListView = findViewById(R.id.sms_listview);

        // Vérifier et demander la permission RECEIVE_SMS si elle n'est pas accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
        }

        // Initialiser la liste des SMS
        smsList = new ArrayList<>();

        // Initialiser l'adaptateur de la ListView
        smsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsList);
        smsListView.setAdapter(smsAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Enregistrer le BroadcastReceiver pour recevoir les SMS
        IntentFilter filter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(smsReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Désenregistrer le BroadcastReceiver
        unregisterReceiver(smsReceiver);
    }
}




//----------------------------------------------------------------------------------
/*
package com.example.getparsespecificmessage;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private ListView smsListView;
    private ArrayAdapter<Sms> smsAdapter;
    private ArrayList<Sms> smsList;

    private Button filterButton;
    private Button deleteButton;

    private Button btnRetrieveMessages;

    private DatabaseHelper databaseHelper;
    private String filterSender = "";

    private static class Sms {
        public String sender;
        public String body;
        public long timestamp;

        public Sms(String sender, String body, long timestamp) {
            this.sender = sender;
            this.body = body;
            this.timestamp = timestamp;
        }

        public String getFormattedTimestamp() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }

        @Override
        public String toString() {
            return "[" + getFormattedTimestamp() + "] " + sender + ": " + body;
        }
    }

    private void updateListView() {
        smsAdapter.clear();
        if (filterSender.isEmpty()) {
            smsAdapter.addAll(smsList);
        } else {
            for (Sms sms : smsList) {
                if (sms.sender.equals(filterSender)) {
                    smsAdapter.add(sms);
                }
            }
        }
        smsAdapter.notifyDataSetChanged();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smsListView = findViewById(R.id.sms_listview);
        filterButton = findViewById(R.id.btn_filter);
        deleteButton = findViewById(R.id.btn_delete);
        btnRetrieveMessages = findViewById(R.id.btnRetrieveMessages);

        databaseHelper = new DatabaseHelper(this);

        // Vérifiez et demandez la permission RECEIVE_SMS si elle n'est pas accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
        }

        // Démarrez le service pour écouter les SMS
        startService(new Intent(this, SmsReceiverService.class));

        // Vérifier et demander la permission RECEIVE_SMS si elle n'est pas accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
        }

        // Initialiser la liste des SMS
        smsList = new ArrayList<>();

        // Initialiser l'adaptateur de la ListView
        smsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsList);
        smsListView.setAdapter(smsAdapter);

        // Filtrer les SMS par expéditeur
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Filtrer par expéditeur");

                final EditText input = new EditText(MainActivity.this);
                input.setHint("Entrez le nom de l'expéditeur");
                builder.setView(input);

                builder.setPositiveButton("Filtrer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        filterSender = input.getText().toString();
                        updateListView();
                    }
                });

                builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        // Supprimer un SMS
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!filterSender.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Veuillez annuler le filtre pour supprimer un SMS.", Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Supprimer un SMS");
                builder.setMessage("Voulez-vous supprimer tous les SMS affichés ?");

                builder.setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        smsList.clear();
                        smsAdapter.clear();
                        smsAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "SMS supprimés.", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });


        btnRetrieveMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Récupérez tous les messages persistés
                List<Message> allMessages = databaseHelper.getAllMessages();

                // Faites ce que vous voulez avec la liste de messages
                // Par exemple, vous pouvez les afficher dans un TextView

                StringBuilder stringBuilder = new StringBuilder();
                for (Message message : allMessages) {
                    stringBuilder.append("Expéditeur: ").append(message.getSender()).append("\n");
                    stringBuilder.append("Message: ").append(message.getBody()).append("\n");
                    stringBuilder.append("Horodatage: ").append(message.getTimestamp()).append("\n");
                    stringBuilder.append("\n");
                }

                // Affichez les messages dans un TextView
                TextView textView = findViewById(R.id.txtMessages);
                textView.setText(stringBuilder.toString());
            }
        });


        // Capturer les SMS reçus
        BroadcastReceiver smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        for (Object pdu : pdus) {
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                            String sender = smsMessage.getOriginatingAddress();
                            String body = smsMessage.getMessageBody();
                            long timestamp = smsMessage.getTimestampMillis();

                            Sms sms = new Sms(sender, body, timestamp);
                            smsList.add(sms);
                            updateListView();
                        }
                    }
                }
            }
        };

        // Enregistrer le BroadcastReceiver
        IntentFilter filter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(smsReceiver, filter);

        // Gérer la sélection d'un SMS dans la ListView
        smsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sms sms = smsAdapter.getItem(position);
                Toast.makeText(MainActivity.this, "SMS sélectionné: " + sms.body, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
 */