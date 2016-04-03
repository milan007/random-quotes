package com.mentatmobile.randomquotes.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String LOG_TAG = "RandomQuotesTag";

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.mentatmobile.randomquotes/databases/";
    private static String DB_NAME = "randomQuotes.db";

    private SQLiteDatabase myDataBase;
    private final Context myContext;

    private static final int DATABASE_VERSION = 1;

    // Quotes table name
    private static final String TABLE_QUOTES = "quotes";

    // Quotes Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_QUOTE = "quote";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_GENRE = "genre";

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

    public Quote getQuote(int id) {
        SQLiteDatabase db = null;
        Cursor cursor= null;
        Quote quote = null;

        try {
            db = this.getReadableDatabase();
            String[] params = new String[]{KEY_ID, KEY_QUOTE, KEY_AUTHOR, KEY_GENRE};
            cursor = db.query(TABLE_QUOTES, params, KEY_ID + "=?",
                    new String[]{String.valueOf(id)}, null, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
            }

            quote = new Quote(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3));
        }
        catch(Exception e){
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        finally{
            cursor.close();
            db.close();
        }

        return quote;
    }

    public Quote getNextQuote(int id) {
        SQLiteDatabase db = null;
        Cursor cursor= null;
        Quote quote = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM " + TABLE_QUOTES + " WHERE " + KEY_ID + " > " + id + " ORDER BY " + KEY_ID + " LIMIT 1", null);
            if (cursor != null) {
                cursor.moveToFirst();
            }

            quote = new Quote(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3));
        }
        catch(Exception e){
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        finally{
            cursor.close();
            db.close();
        }

        return quote;
    }

    public Quote getRandomQuote() {
        int randomId = 0;
        SQLiteDatabase db = null;
        Cursor cursor= null;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT count(*) FROM " + TABLE_QUOTES, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }

            Integer count = Integer.parseInt(cursor.getString(0));

            Random random = new Random();
            randomId = random.nextInt(count);
        }
        catch(Exception e){
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        finally{
            cursor.close();
            db.close();
        }

        Quote quote = getNextQuote(randomId);
        return quote;
    }

    public List<Quote> getAllQuotes() {
        SQLiteDatabase db = null;
        Cursor cursor= null;
        List<Quote> quoteList = new ArrayList<>();

        try {
            // Select All Query
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT  * FROM " + TABLE_QUOTES, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    Quote quote = new Quote(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                    quoteList.add(quote);
                } while (cursor.moveToNext());
            }
        }
        catch(Exception e){
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        finally{
            cursor.close();
            db.close();
        }

        // return contact list
        return quoteList;
    }
}