package de.g8keeper.myshoppinglisthq;


import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


    private ListView mShoppingMemosListView;
    private boolean isKeyboardClick = false;
    ShoppingMemoDataSource dataSource;


    /***********************************************************************************************
     Callbacks
     ***********************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataSource = new ShoppingMemoDataSource(this);

        initializeShoppingMemosListView();

        activateAddButton();
        inizializeContextualActionBar();
    }


    @Override
    protected void onResume() {

        super.onResume();
        Log.d(TAG, "Datenquelle wird geöffnet");
        dataSource.open();

        showAllListEntries();
    }

    @Override
    protected void onPause() {

        super.onPause();
        Log.d(TAG, "Datenquelle wird geschlossen");
        dataSource.close();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, "Settings wurde gedrückt", Toast.LENGTH_SHORT).show();
                //todo Settings einbinden
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /***********************************************************************************************
     ***********************************************************************************************/


    private void initializeShoppingMemosListView() {
        List<ShoppingMemo> emptyListForInitialisation = new ArrayList<>();

        mShoppingMemosListView = findViewById(R.id.lv_shopping_memos);

        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = new ArrayAdapter<ShoppingMemo>(this,
                android.R.layout.simple_list_item_multiple_choice, emptyListForInitialisation) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;



                ShoppingMemo memo = (ShoppingMemo) mShoppingMemosListView.getItemAtPosition(position);
                if (memo.isChecked()) {
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    textView.setTextColor(getColor(R.color.colorChecked));
                } else {
                    textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    textView.setTextColor(getColor(R.color.colorUnchecked));
                }

                return view;
            }
        };

        mShoppingMemosListView.setAdapter(shoppingMemoArrayAdapter);

        mShoppingMemosListView.setOnItemClickListener((parent, view, position, id) -> {
            ShoppingMemo memo = (ShoppingMemo) parent.getItemAtPosition(position);
            ShoppingMemo updateMemo = dataSource.updateShoppingMemo(memo.getId(), memo.getProduct(),
                    memo.getQuantity(), !memo.isChecked());

            showAllListEntries();

        });

    }


    private void activateAddButton() {
        final EditText etQuantity = findViewById(R.id.et_quantity);
        final EditText etProduct = findViewById(R.id.et_product);
        Button btAddProduct = findViewById(R.id.bt_add_product);

        btAddProduct.setOnClickListener(v -> {

            String quantityString = etQuantity.getText().toString();
            String product = etProduct.getText().toString();

            if (TextUtils.isEmpty(quantityString)) {
                etQuantity.setError(getString(R.string.editText_errorMessage));
                return;
            }

            if (TextUtils.isEmpty(product)) {
                etProduct.setError(getString(R.string.editText_errorMessage));
                return;
            }

            int quantity = Integer.parseInt(quantityString);

            etProduct.setText("");
            etQuantity.setText("");

            dataSource.createShoppingMemo(product, quantity);

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

            if (getCurrentFocus() != null && !isKeyboardClick) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                mShoppingMemosListView.requestFocus();
            }

            showAllListEntries();

            mShoppingMemosListView.post(() -> mShoppingMemosListView.smoothScrollToPosition(mShoppingMemosListView.getCount() - 1));


        });

        etProduct.setOnEditorActionListener((textView, pos, keyEvent) -> {
            isKeyboardClick = true;
            btAddProduct.performClick();
            etQuantity.requestFocus();
            isKeyboardClick = false;
            return true;
        });

    }

    private void inizializeContextualActionBar() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mShoppingMemosListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);


        mShoppingMemosListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            int selCount = 0;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                if (checked) {
                    selCount++;
                } else {
                    selCount--;
                }

                String cabTitel = selCount + " " + getString(R.string.cab_checked_string);
                mode.setTitle(cabTitel);
                mode.invalidate();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_contextual_action_bar, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem item = menu.findItem(R.id.cab_change);
                if (selCount == 1) {
                    item.setVisible(true);
                } else {
                    item.setVisible(false);
                }

                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean returnValue = true;

                SparseBooleanArray touchedShoppingMemosPosition = mShoppingMemosListView.getCheckedItemPositions();

                switch (item.getItemId()) {

                    case R.id.cab_delete: // löschen wurde geklickt

                        for (int i = 0; i < touchedShoppingMemosPosition.size(); i++) {
                            boolean isChecked = touchedShoppingMemosPosition.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedShoppingMemosPosition.keyAt(i);
                                ShoppingMemo shoppingMemo = (ShoppingMemo) mShoppingMemosListView.
                                        getItemAtPosition(positionInListView);

                                Log.d(TAG, "Position im ListView: " + positionInListView + " Inhalt: " + shoppingMemo.toString());
                                dataSource.deleteShoppingMemo(shoppingMemo);
                            }
                        }

                        showAllListEntries();

                        // beendet mehrfachauswahl-modus
                        mode.finish();
                        break;

                    case R.id.cab_change:

                        for (int i = 0; i < touchedShoppingMemosPosition.size(); i++) {
                            boolean isChecked = touchedShoppingMemosPosition.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedShoppingMemosPosition.keyAt(i);
                                ShoppingMemo shoppingMemo = (ShoppingMemo) mShoppingMemosListView.
                                        getItemAtPosition(positionInListView);

                                AlertDialog editShopingMemoDialog = createShoppingMemoDialog(shoppingMemo);
                                editShopingMemoDialog.show();

                                // zeigt das soft-keyboard an

//                                imm.showSoftInput(getCurrentFocus(),InputMethodManager.SHOW_IMPLICIT);
                            }
                        }

                        mode.finish();
                        break;

                    default:

                        return returnValue = false;

                }


                return returnValue;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selCount = 0;

            }
        });

    }

    private AlertDialog createShoppingMemoDialog(final ShoppingMemo shoppingMemo) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogsView = inflater.inflate(R.layout.dialog_edit_shopping_memo, null);

        final EditText etNewQuantity = dialogsView.findViewById(R.id.et_new_quantity);
        etNewQuantity.setText(String.valueOf(shoppingMemo.getQuantity()));


        final EditText etNewProduct = dialogsView.findViewById(R.id.et_new_product);
        etNewProduct.setText(shoppingMemo.getProduct());


        builder.setView(dialogsView).
                setTitle(R.string.dialog_title).
                setPositiveButton(R.string.dialog_bt_positiv, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String quantityString = etNewQuantity.getText().toString();
                        String product = etNewProduct.getText().toString();

                        if (TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(product)) {
                            Toast.makeText(MainActivity.this, "Felder dürfen nicht leer sein",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int quantity = Integer.parseInt(quantityString);

                        ShoppingMemo temp = dataSource.updateShoppingMemo(shoppingMemo.getId(),
                                product, quantity, shoppingMemo.isChecked());

                        Log.d(TAG, "Alter Eintrag.ID: " + shoppingMemo.getId() + " Inhalt: " + shoppingMemo.toString());
                        Log.d(TAG, "Neuer Eintrag.ID: " + temp.getId() + " Inhalt: " + temp.toString());

                        showAllListEntries();
                        dialog.dismiss();
                    }
                }).
                setNegativeButton(R.string.dialog_bt_negativ, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


        etNewQuantity.setSelection(0, etNewQuantity.length());

        return builder.create();

    }

    private void showAllListEntries() {
        List<ShoppingMemo> shoppingMemoList = dataSource.getAllShoppingMemos();
        ArrayAdapter<ShoppingMemo> adapter = (ArrayAdapter<ShoppingMemo>) mShoppingMemosListView.getAdapter();

        adapter.clear();
        adapter.addAll(shoppingMemoList);
        adapter.notifyDataSetChanged();

    }


}
