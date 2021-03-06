package teamdynamite.pizzapoint;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;


public class DBHandler extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 10;
    // Database Name
    public static final String DATABASE_NAME = "Orders";

    // Order table name
    public static final String TABLE_ORDER = "Orders";
    // Order table Columns names
    public static final String KEY_ORDER_NUMBER = "order_num";
    public static final String KEY_DATE = "open_date";
    public static final String KEY_ISOPEN = "is_open";
    public static final String KEY_USER = "user";

    //Item table name
    public static final String TABLE_ITEM = "Item";
    //ITem table column names
    public static final String KEY_ORDER_NUM = "forder_num";
    public static final String KEY_ITEM_NAME = "item_name";
    public static final String KEY_PRICE = "price";
    public static final String KEY_COMMENTS = "comments";
    public static final String KEY_EXTRAS = "extras";
    public static final String KEY_ID = "_id";

    //Constructor
    public DBHandler(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Creates the 'Order' table
        String CREATE_ORDER_TABLE = "CREATE TABLE " + TABLE_ORDER + "("
                + KEY_ORDER_NUMBER + " INTEGER PRIMARY KEY, "
                + KEY_DATE + " TEXT, "
                + KEY_ISOPEN + " INTEGER, "
                + KEY_USER + "INTEGER )";
        db.execSQL(CREATE_ORDER_TABLE);

        //Creates the 'Item' table
        String CREATE_TABLE_ITEM = "CREATE TABLE " + TABLE_ITEM + "("
                + KEY_ORDER_NUM + " INTEGER, "
                + KEY_ITEM_NAME + " TEXT, "
                + KEY_PRICE + " REAL, "
                + KEY_COMMENTS + " TEXT, "
                + KEY_EXTRAS + " TEXT, "
                + KEY_ID + " INTEGER, "
                + "FOREIGN KEY (" + KEY_ORDER_NUM + ") REFERENCES "
                + TABLE_ORDER + "(" + KEY_ORDER_NUMBER + ")" + ");";
        db.execSQL(CREATE_TABLE_ITEM);


    }

    @Override
    //If a new database version is created
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM);
        onCreate(db);

    }

    public DBHandler open() {
        SQLiteDatabase db = this.getWritableDatabase();
        return this;
    }


    // Adding new order
    public void addOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ORDER_NUMBER, order.getOrderNumber()); // Order Number
        values.put(KEY_DATE, order.getOpenDate()); // open date
        values.put(KEY_ISOPEN, order.getIsOpen());// bool is open
        // Inserting Row
        db.insert(TABLE_ORDER, null, values);

    }
    //Adds an item to the database
    public void addItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ORDER_NUM, item.getOrderNum());
        values.put(KEY_ITEM_NAME, item.getItemName());
        values.put(KEY_PRICE, item.getPrice());
        values.put(KEY_COMMENTS, item.getComments());
        values.put(KEY_EXTRAS, item.getExtras());
        values.put(KEY_ID, item.getId());

        //Inserting row
        db.insert(TABLE_ITEM, null, values);

    }

    // Getting one Order
    public int orderTotal() {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT * FROM " + TABLE_ORDER;
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null && !cursor.isClosed()) {
            count = cursor.getCount();
            cursor.close();

        }
        return count;
    }
    //Set the new item number. This value is not visible to the user
    public int newItemNum(){
        SQLiteDatabase db = this.getReadableDatabase();

        final SQLiteStatement stmt = db
                .compileStatement("SELECT MAX(_id) FROM Item");

        return (int) stmt.simpleQueryForLong();

    }
    //Gets the highest orderNum
    public int newOrderNum() {

        SQLiteDatabase db = this.getReadableDatabase();

        final SQLiteStatement stmt = db
                .compileStatement("SELECT MAX(order_num) FROM Orders");

        return (int) stmt.simpleQueryForLong();

    }
    //SQL query that tells the cursor which orders to put in the 'Open order' Listview
    public Cursor orderListView() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT order_num AS _id, open_date FROM " + TABLE_ORDER + " WHERE is_open = 1 ";
        Cursor c = db.rawQuery(query, null);
        return c;
    }
    //SQL query that tells the cursor which orders to put in the 'View Order' Listview
    public Cursor itemListView(int n) {

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT "+KEY_ID+" AS _id, "+KEY_ITEM_NAME+ " , "
                +KEY_COMMENTS+" , "+KEY_PRICE+" , forder_num FROM " + TABLE_ITEM
                + " WHERE forder_num = " + n;
        Cursor c = db.rawQuery(query, null);
        return c;
    }

    public void deleteItem(int n){
        SQLiteDatabase db = this.getWritableDatabase();
        System.out.println("Deleting itemNum: " + n);

        String strSQL = "DELETE FROM "+ TABLE_ITEM+" WHERE "+KEY_ID+" = "+ n;
        db.execSQL(strSQL);

    }

    //Closes an order.
    //Sets isOpen to 0
    public void closeOrder(int n){
        SQLiteDatabase db = this.getReadableDatabase();
        String strSQL = "UPDATE "+ TABLE_ORDER+" SET "+KEY_ISOPEN+" = 0 WHERE "+KEY_ORDER_NUMBER+" = "+ n;
        db.execSQL(strSQL);

    }

    //Gets the order total to display in View Order
    public double orderTotal(int n){
        double total;
        SQLiteDatabase db = this.getReadableDatabase();
        final SQLiteStatement stmt = db
                .compileStatement("SELECT SUM(Item.price) FROM Item WHERE Item.forder_num = "+ n);

        String s = (String) stmt.simpleQueryForString();

        //Null value check
        //If there is nothing in the order, set the total price to 0
        if (s == null){
           total = 0.00;
        }
        else {
            total = Double.parseDouble(s);

        }
        return total;
    }

}