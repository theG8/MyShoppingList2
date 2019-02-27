package de.g8keeper.myshoppinglisthq;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ShoppingMemoDataSource {

    private static final String TAG = ShoppingMemoDataSource.class.getSimpleName();

    private SQLiteDatabase db;
    private ShoppingMemoDBHelper dbHelper;

    private String[] columns = {
            ShoppingMemoDBHelper.COL_ID,
            ShoppingMemoDBHelper.COL_PRODUCT,
            ShoppingMemoDBHelper.COL_QUANTITY,
            ShoppingMemoDBHelper.COL_CHECKED
    };


    public ShoppingMemoDataSource(Context context) {

        Log.d(TAG, "DataSource erzeugt jetzt den dbHelper");
        dbHelper = new ShoppingMemoDBHelper(context);

    }


    public void open() {

        Log.d(TAG, "Eine Referenz auf die Datenbank wird angefragt.");
        db = dbHelper.getWritableDatabase();
        Log.d(TAG, "open: Referenz erhalten. Pfad zur DB -> " + db.getPath());

    }


    public void close() {

        dbHelper.close();
        Log.d(TAG, "Datenbank mit DBHelper geschlossen");

    }


    public ShoppingMemo createShoppingMemo(String product, int quantity) {

        ContentValues values = new ContentValues();
        values.put(ShoppingMemoDBHelper.COL_PRODUCT, product);
        values.put(ShoppingMemoDBHelper.COL_QUANTITY, quantity);

        long insertId = db.insert(ShoppingMemoDBHelper.TBL_SHOPPING_LIST, null, values);

        Cursor cursor = db.query(ShoppingMemoDBHelper.TBL_SHOPPING_LIST, columns,
                ShoppingMemoDBHelper.COL_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();

        ShoppingMemo shoppingMemo = cursorToShoppingMemo(cursor);

        cursor.close();

        return shoppingMemo;

    }

    public ShoppingMemo updateShoppingMemo(long id, String newProduct, int newQuantity, boolean newChecked) {

        int iNewChecked = newChecked ? 1 : 0;
        ContentValues values = new ContentValues();

        values.put(ShoppingMemoDBHelper.COL_PRODUCT, newProduct);
        values.put(ShoppingMemoDBHelper.COL_QUANTITY, newQuantity);
        values.put(ShoppingMemoDBHelper.COL_CHECKED, iNewChecked);

        db.update(ShoppingMemoDBHelper.TBL_SHOPPING_LIST, values,
                ShoppingMemoDBHelper.COL_ID + "=" + id, null);


        Cursor cursor = db.query(ShoppingMemoDBHelper.TBL_SHOPPING_LIST, columns,
                ShoppingMemoDBHelper.COL_ID + "=" + id,
                null, null, null, null);

        cursor.moveToFirst();
        ShoppingMemo sm = cursorToShoppingMemo(cursor);
        cursor.close();

        return sm;
    }

    public void deleteShoppingMemo(ShoppingMemo shoppingMemo) {

        long id = shoppingMemo.getId();

        db.delete(ShoppingMemoDBHelper.TBL_SHOPPING_LIST, ShoppingMemoDBHelper.COL_ID + "=" + id, null);

        Log.d(TAG, "Eintrag gelöscht! ID: " + id + " Inhalt: " + shoppingMemo.toString());

    }


    private ShoppingMemo cursorToShoppingMemo(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(ShoppingMemoDBHelper.COL_ID);
        int idProduct = cursor.getColumnIndex(ShoppingMemoDBHelper.COL_PRODUCT);
        int idQuantity = cursor.getColumnIndex(ShoppingMemoDBHelper.COL_QUANTITY);
        int idChecked = cursor.getColumnIndex(ShoppingMemoDBHelper.COL_CHECKED);

        long index = cursor.getInt(idIndex);
        String product = cursor.getString(idProduct);
        int quantity = cursor.getInt(idQuantity);
        boolean checked = (cursor.getInt(idChecked) != 0);

        ShoppingMemo shoppingMemo = new ShoppingMemo(product, quantity, index, checked);

        return shoppingMemo;

    }


    public List<ShoppingMemo> getAllShoppingMemos() {

        List<ShoppingMemo> shoppingMemoList = new ArrayList<>();
        Cursor cursor = db.query(ShoppingMemoDBHelper.TBL_SHOPPING_LIST, columns,
                null, null, null, null, null);

        cursor.moveToFirst();

        ShoppingMemo shoppingMemo;

        while (!cursor.isAfterLast()) {
            shoppingMemo = cursorToShoppingMemo(cursor);
            shoppingMemoList.add(shoppingMemo);
            Log.d(TAG, "ID: " + shoppingMemo.getId() +
                    " Inhalt: " + shoppingMemo.toString());
            cursor.moveToNext();
        }

        cursor.close();

        return shoppingMemoList;

    }


}
