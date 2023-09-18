package com.example.getparsespecificmessage;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "sms_database";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "messages";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SENDER = "sender";
    private static final String COLUMN_BODY = "body";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Créez la table pour stocker les messages
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SENDER + " TEXT,"
                + COLUMN_BODY + " TEXT,"
                + COLUMN_TIMESTAMP + " INTEGER)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Mettez à jour la base de données si nécessaire
        String dropTableQuery = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(dropTableQuery);
        onCreate(db);
    }

    public void addMessage(Message message) {
        // Ajoutez un nouveau message à la base de données
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, message.getSender());
        values.put(COLUMN_BODY, message.getBody());
        values.put(COLUMN_TIMESTAMP, message.getTimestamp());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // Ajoutez d'autres méthodes nécessaires pour récupérer les messages de la base de données
    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String sender = cursor.getString(cursor.getColumnIndex(COLUMN_SENDER));
                @SuppressLint("Range") String body = cursor.getString(cursor.getColumnIndex(COLUMN_BODY));
                @SuppressLint("Range") long timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP));

                Message message = new Message(sender, body, timestamp);
                messages.add(message);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return messages;
    }
}