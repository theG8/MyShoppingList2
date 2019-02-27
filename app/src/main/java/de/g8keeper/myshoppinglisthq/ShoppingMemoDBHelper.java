package de.g8keeper.myshoppinglisthq;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;



public class ShoppingMemoDBHelper extends SQLiteOpenHelper {

    private static final String TAG = ShoppingMemoDBHelper.class.getSimpleName();

    public static final String DB_NAME = "shoppinglist_db";
    public static final int DB_VERSION = 2;

    public static final String TBL_SHOPPING_LIST = "tblShopping_list";
    // id IMMER mit _ benennen... SimpleCursor z.B. benötigt zwingend diese benennung
    public static final String COL_ID = "_id";
    public static final String COL_PRODUCT = "product";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_CHECKED = "checked";

    public static final String SQL_CREATE = "CREATE TABLE " + TBL_SHOPPING_LIST + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_PRODUCT + " TEXT NOT NULL, " +
            COL_QUANTITY + " INTEGER NOT NULL, " +
            COL_CHECKED + " BOOLEAN NOT NULL DEFAULT 0);";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TBL_SHOPPING_LIST;



    public ShoppingMemoDBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(TAG, "DbHelper hat die Datenbank angelegt: " + getDatabaseName());
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        try{
            Log.d(TAG, "Tabelle wird mit " + SQL_CREATE + " erstellt...");
            db.execSQL(SQL_CREATE);
        } catch (RuntimeException e){
            Log.e(TAG, "Fehler beim Anlegen der Datenbank: ", e);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        try {

            db.execSQL(SQL_DROP);
            onCreate(db);
        } catch (RuntimeException e){
            Log.e(TAG, "Fehler beim Upgrade der Datenbank: ", e);
        }

    }


}
