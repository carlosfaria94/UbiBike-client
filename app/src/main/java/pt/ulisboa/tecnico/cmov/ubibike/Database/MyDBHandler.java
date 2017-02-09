package pt.ulisboa.tecnico.cmov.ubibike.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station;

public class MyDBHandler extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 19;
    private static final String DATABASE_NAME = "chat.db";

    public static final String TABLE_CHAT = "chatTable";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FRIEND = "friendName";
    public static final String COLUMN_CHAT = "chat";
    public static final String COLUMN_DATE = "date";

    public static final String TABLE_PENDING_POINTS = "pendingPointsTable";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_POINTS = "points";
    public static final String COLUMN_HASH = "hash";

    public static final String TABLE_PENDING_TRAJECTORY = "pendingTrajectory";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_POINTS_EARNED = "pointsEarned";
    public static final String COLUMN_TRAVEL_TIME = "travelTime";
    public static final String COLUMN_POINT_LIST = "pointList";
    public static final String COLUMN_IS_PENDING = "isPending";

    public static final String TABLE_STATIONS = "stations";
    public static final String COLUMN_ID_SERVER = "stationIdServer";
    public static final String COLUMN_NAME = "stationName";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_BIKESAVAILABLE = "bikesAvailable";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_CLIENT_DISTANCE = "clientDistance";

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_CHAT + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FRIEND + " TEXT, " +
                COLUMN_CHAT + " TEXT, " +
                COLUMN_DATE + " TEXT" +
                ");";
        db.execSQL(query);

        String query2 = "CREATE TABLE " + TABLE_PENDING_POINTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SENDER + " TEXT, " +
                COLUMN_HASH + " TEXT, " +
                COLUMN_TIMESTAMP + " INTEGER, " +
                COLUMN_POINTS + " INTEGER" +
                ");";
        db.execSQL(query2);

        String query3 = "CREATE TABLE " + TABLE_PENDING_TRAJECTORY + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DISTANCE + " INTEGER, " +
                COLUMN_POINTS_EARNED + " INTEGER, " +
                COLUMN_TRAVEL_TIME + " INTEGER, " +
                COLUMN_IS_PENDING + " INTEGER, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_POINT_LIST + " TEXT" +
                ");";
        db.execSQL(query3);

        String query4 = "CREATE TABLE " + TABLE_STATIONS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ID_SERVER + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_LOCATION + " TEXT, " +
                COLUMN_BIKESAVAILABLE + " INTEGER, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_CLIENT_DISTANCE + " REAL " +
                ");";
        db.execSQL(query4);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PENDING_POINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PENDING_TRAJECTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATIONS);
        onCreate(db);
    }

    /**
     * Insert station
     */
    public void insertStation(String id, String name, String location, int bikesAvailable, float latitude, float longitude, float distance) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_STATIONS + " WHERE stationIdServer='" + id + "'");

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID_SERVER, id);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_BIKESAVAILABLE, bikesAvailable);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_CLIENT_DISTANCE, distance);

        db.insert(TABLE_STATIONS, null, values);
        db.close();
    }

    /**
     * Get a list of all stations
     */
    public List<Station> getAllStations() {
        List<Station> listOfAllStations = new ArrayList<Station>();

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_STATIONS;
        Cursor c = db.rawQuery(query, null);
        while(c.moveToNext()) {
            Station entry = new Station(
                    c.getString(c.getColumnIndex(COLUMN_ID_SERVER)),
                    c.getString(c.getColumnIndex(COLUMN_NAME)),
                    c.getString(c.getColumnIndex(COLUMN_LOCATION)),
                    c.getInt(c.getColumnIndex(COLUMN_BIKESAVAILABLE)),
                    c.getFloat(c.getColumnIndex(COLUMN_LATITUDE)),
                    c.getFloat(c.getColumnIndex(COLUMN_LONGITUDE)),
                    c.getFloat(c.getColumnIndex(COLUMN_CLIENT_DISTANCE))
            );
            listOfAllStations.add(entry);
        }

        db.close();
        c.close();
        return listOfAllStations;
    }


    //para adicionar 'a tabela que contem as trajetorias pendentes
    public void insertPendingTrajectoriesTable(int distTravelled, int pointsEarned, long travelTime, JSONArray pointList)
    {
        //processo de converter JSON to string
        String[] pointListArray = new String[pointList.length()];
        for(int i = 0; i < pointList.length(); i++)
        {
            try {
                String aux = pointList.getJSONObject(i).toString();
                pointListArray[i] = aux;
            }
            catch(JSONException e)
            {
                Log.d("Exception", "JSON Exception ");
                e.printStackTrace();
            }
        }

        //converter array para String
        String converted = "";
        //separador usado para identifcar os strings quandof for feito o read
        String strSeparator = "__,__";

        for(int i = 0; i < pointListArray.length; i++)
        {
            converted = converted + pointListArray[i];
            //nao aplicamos o separador no ultimo elemento
            if(i < pointListArray.length - 1)
                converted = converted + strSeparator;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_DISTANCE, distTravelled);
        values.put(COLUMN_POINTS_EARNED, pointsEarned);
        values.put(COLUMN_TRAVEL_TIME, travelTime);
        values.put(COLUMN_POINT_LIST, converted);
        values.put(COLUMN_IS_PENDING, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
        values.put(COLUMN_DATE, date);

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_PENDING_TRAJECTORY, null, values);

        db.close();
    }

    //este metodo retorna todas as trajetorias que estao pendentes para enviar para o servidor
    public ArrayList<TupleTrajectory> getAllPendingTrajectories()
    {
        ArrayList<TupleTrajectory> listOfPendingTrajectories = new ArrayList<TupleTrajectory>();

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PENDING_TRAJECTORY + " WHERE isPending='" + 0 + "'";
        Cursor c = db.rawQuery(query, null);

        while(c.moveToNext())
        {
            String jsonString = c.getString(c.getColumnIndex(COLUMN_POINT_LIST));

            //temos que converter o jsonString de volta a um String array
            String strSeparator = "__,__";
            String[] jsonArray = jsonString.split(strSeparator);

            JSONArray jsonArrayFinal = new JSONArray();
            //e agora convertemos de volta para um JSON Array
            for(int i = 0; i < jsonArray.length ; i++)
            {
                try {
                    JSONObject jsonObject = new JSONObject(jsonArray[i]);
                    jsonArrayFinal.put(jsonObject);
                } catch(JSONException e)
                {
                    Log.d("Exception", "JSON Exception ");
                    e.printStackTrace();
                }
            }
            TupleTrajectory entry = new TupleTrajectory(c.getInt(c.getColumnIndex(COLUMN_DISTANCE)), c.getInt(c.getColumnIndex(COLUMN_POINTS_EARNED)), c.getLong(c.getColumnIndex(COLUMN_TRAVEL_TIME)),jsonArrayFinal,c.getInt(c.getColumnIndex(COLUMN_ID)), c.getString(c.getColumnIndex(COLUMN_DATE)));
            listOfPendingTrajectories.add(entry);
        }

        db.close();
        c.close();

        return listOfPendingTrajectories;
    }

    public ArrayList<TupleTrajectory> getAllTrajectories()
    {
        ArrayList<TupleTrajectory> listOfTrajectories = new ArrayList<TupleTrajectory>();

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PENDING_TRAJECTORY ;
        Cursor c = db.rawQuery(query, null);

        while(c.moveToNext())
        {
            String jsonString = c.getString(c.getColumnIndex(COLUMN_POINT_LIST));

            //temos que converter o jsonString de volta a um String array
            String strSeparator = "__,__";
            String[] jsonArray = jsonString.split(strSeparator);

            JSONArray jsonArrayFinal = new JSONArray();
            //e agora convertemos de volta para um JSON Array
            for(int i = 0; i < jsonArray.length ; i++)
            {
                try {
                    JSONObject jsonObject = new JSONObject(jsonArray[i]);
                    jsonArrayFinal.put(jsonObject);
                } catch(JSONException e)
                {
                    Log.d("Exception", "JSON Exception ");
                    e.printStackTrace();
                }
            }
            TupleTrajectory entry = new TupleTrajectory(c.getInt(c.getColumnIndex(COLUMN_DISTANCE)), c.getInt(c.getColumnIndex(COLUMN_POINTS_EARNED)), c.getLong(c.getColumnIndex(COLUMN_TRAVEL_TIME)),jsonArrayFinal,c.getInt(c.getColumnIndex(COLUMN_ID)), c.getString(c.getColumnIndex(COLUMN_DATE)));
            listOfTrajectories.add(entry);
        }

        db.close();
        c.close();

        return listOfTrajectories;
    }

    public void changeIsPending(int id)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        String query = "SELECT * FROM " + TABLE_PENDING_TRAJECTORY + " WHERE _id='" + id + "' AND isPending='" + 0 + "'";
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        values.put(COLUMN_IS_PENDING, 1);
        db.update(TABLE_PENDING_TRAJECTORY, values, "_id='" + id + "'", null);

        c.close();
        db.close();
    }

    //para adicionar 'a tabela que contem os pontos pendentes
    public void insertPendingPointsTable(String sender, int timestamp, int points, String hash)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_POINTS, points);
        values.put(COLUMN_HASH, hash);

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_PENDING_POINTS, null, values);

        db.close();
    }

    public void removeEntryPendingPointsTable(int timestamp, String idSender)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PENDING_POINTS + " WHERE timestamp='" + timestamp + "' AND sender='" + idSender + "'");

        db.close();
    }

    //este metodo retorna todos pontos que estao pendentes para validacao
    public ArrayList<TuplePlusHash> getAllPendingPoints()
    {
        ArrayList<TuplePlusHash> listOfPendingPoints = new ArrayList<TuplePlusHash>();

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PENDING_POINTS;
        Cursor c = db.rawQuery(query, null);

        while(c.moveToNext())
        {
            TuplePlusHash entry = new TuplePlusHash(c.getString(c.getColumnIndex(COLUMN_SENDER)), c.getInt(c.getColumnIndex(COLUMN_TIMESTAMP)), c.getInt(c.getColumnIndex(COLUMN_POINTS)),c.getString(c.getColumnIndex(COLUMN_HASH)));
            listOfPendingPoints.add(entry);
        }

        db.close();
        c.close();

        return listOfPendingPoints;
    }

    //adicionar uma linha à base de dados (a primeira)
    public void insertChatEntry(ChatEntry entry)
    {
        String chat_entry = entry.getFriendName() + ":"+entry.getChat(); //para windows: \r\n
        ContentValues values = new ContentValues();
        values.put(COLUMN_FRIEND, entry.getFriendName());
        values.put(COLUMN_CHAT, chat_entry);
        values.put(COLUMN_DATE, entry.getDate());
       // values.put(COLUMN_ID, entry.getId());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_CHAT, null, values);
        db.close();
    }

    //se ja existe uma conversa, entao fazemos update na conversa e na data
    public void updateChatEntry(ChatEntry entry)
    {
        ContentValues values = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_CHAT + " WHERE friendName='" + entry.getFriendName()+"'";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        String chat_entry = c.getString(c.getColumnIndex(COLUMN_CHAT));
        String name = c.getString(c.getColumnIndex(COLUMN_FRIEND));

        String newEntry = chat_entry + System.getProperty("line.separator") + name + ":"+ entry.getChat();

        values.put(COLUMN_CHAT, newEntry);

        db.update(TABLE_CHAT, values, "friendName='" + entry.getFriendName() + "'", null);
        db.close();
        c.close();
    }

    //este updateChatEntry é para o user local da app
    public void updateChatEntry2(ChatEntry entry)
    {
        ContentValues values = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_CHAT + " WHERE friendName='" + entry.getFriendName()+"'";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        String chat_entry = c.getString(c.getColumnIndex(COLUMN_CHAT));
        String name = c.getString(c.getColumnIndex(COLUMN_FRIEND));

        String newEntry = chat_entry + System.getProperty("line.separator") +  entry.getChat();

        values.put(COLUMN_CHAT, newEntry);

        db.update(TABLE_CHAT, values, "friendName='" + entry.getFriendName() + "'", null);
        db.close();
        c.close();
    }

    public ChatEntry getChatEntry(String friendName)
    {
        String chat, date;

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_CHAT + " WHERE friendName='" + friendName+"'";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        chat = c.getString(c.getColumnIndex(COLUMN_CHAT));

        db.close();
        c.close();

        ChatEntry entry = new ChatEntry(friendName,chat);
        return entry;
    }

    public boolean existEntry(String friendName)
    {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_CHAT + " WHERE friendName='" +friendName+"'";


        Cursor c = db.rawQuery(query, null);
        if(c.getCount() <= 0) //se for true quer dizer que ainda nao temos nada na db com este utilizador
        {
            db.close();
            c.close();
            return false;
        }
        else
        {
            db.close();
            c.close();
            return true;
        }

    }
    //obter todas as entradas na base de dados
    public ArrayList<ChatEntry> getAllEntries()
    {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_CHAT;
        Cursor c = db.rawQuery(query, null);

        ArrayList<ChatEntry> entries = new ArrayList<ChatEntry>();

        while(c.moveToNext())
        {
            ChatEntry entry = new ChatEntry(c.getString(c.getColumnIndex(COLUMN_FRIEND)), c.getString(c.getColumnIndex(COLUMN_CHAT)));
            entries.add(entry);
        }

        db.close();
        c.close();
        return entries;
    }
}
