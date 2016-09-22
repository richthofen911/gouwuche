package io.ap1.libbeaconmanagement.Utils;

import android.content.Context;
//import android.database.SQLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import io.ap1.libbeaconmanagement.Beacon;
import io.ap1.libbeaconmanagement.BeaconOperation;
import io.ap1.libbeaconmanagement.Company;

/**
 * Created by admin on 06/10/15.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper{
    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "beacons.db";
    private static final int DATABASE_VERSION = 1;

    private static DatabaseHelper instance;

    private static Dao<Beacon, Integer> beaconDao = null;
    private RuntimeExceptionDao<Beacon, Integer> beaconRuntimeExceptionDao = null;
    private static Dao<Company, Integer> companyDao = null;
    private RuntimeExceptionDao<Company, Integer> companyRuntimeExceptionDao = null;

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
    }

    public static synchronized DatabaseHelper getHelper(Context context){
        if(instance == null){
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource){
        try{

            TableUtils.createTable(connectionSource, Company.class);
            Log.e(TAG, "onCreate: CompanyTable");
            TableUtils.createTable(connectionSource, Beacon.class);
            Log.e(TAG, "onCreate: BeaconTable");
            companyDao = getCompanyDao();
            beaconDao = getBeaconDao();

            companyRuntimeExceptionDao = getCompanyRuntimeExceptionDao();
            beaconRuntimeExceptionDao = getBeaconRuntimeExceptionDao();
        } catch(SQLException e){
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Beacon.class, true);
            TableUtils.dropTable(connectionSource, Class.class, true);
        }
        catch (SQLException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public Dao<Beacon, Integer> getBeaconDao() throws SQLException{
        if (beaconDao == null){
            beaconDao = getDao(Beacon.class);
            Log.e("beaconDao", "null but created");
        }
        return beaconDao;
    }

    public Dao<Company, Integer> getCompanyDao() throws SQLException{
        if(companyDao == null){
            companyDao = getDao(Company.class);
            Log.e("companyDao", "null but created");
        }
        return companyDao;
    }

    public RuntimeExceptionDao<Beacon, Integer> getBeaconRuntimeExceptionDao(){
        if (beaconRuntimeExceptionDao == null){
            beaconRuntimeExceptionDao = getRuntimeExceptionDao(Beacon.class);
            Log.e("beaconRuntimeExDao", "null but created");
        }
        return beaconRuntimeExceptionDao;
    }

    public RuntimeExceptionDao<Company, Integer> getCompanyRuntimeExceptionDao(){
        if(companyRuntimeExceptionDao == null){
            companyRuntimeExceptionDao = getRuntimeExceptionDao(Company.class);
            Log.e("companyRuntimeExDao", "null but created");
        }
        return companyRuntimeExceptionDao;
    }

    public void saveBeacon(Beacon newBeacon){
        if(beaconDao == null){
            try{
                beaconDao = getBeaconDao();
            } catch(SQLException e){
                Log.e(TAG, e.toString());
            }
        }
        try {
            beaconDao.createIfNotExists(newBeacon);
        }catch (SQLException e){
            Log.e(TAG, "save beacon error " + e.toString());
        }
    }

    public void saveCompany(Company newCompany){
        if(companyDao == null){
            try {
                companyDao = getCompanyDao();
            }catch (SQLException e){
                Log.e(TAG, e.toString());
            }
        }
        try{
            companyDao.createIfNotExists(newCompany);
        }catch (SQLException e){
            Log.e(TAG, "save company err " + e.toString());
        }
    }

    public void deleteAllBeacons(Context context){
        try {
            if(beaconDao == null)
                beaconDao = getBeaconDao();
            beaconDao.delete(queryForAllBeacons());
        }catch (SQLException e){
            Log.e("del all beacons error", e.toString());
        }

    }

    public void deleteAllCompanies(){
        try{
            if(companyDao == null)
                companyDao = getCompanyDao();
            companyDao.delete(queryForAllCompanies());
        }catch (SQLException e){
            Log.e(TAG, "del all companies err " + e.toString());
        }
    }

    public List<Beacon> queryForAllBeacons(){
        try {
            if(beaconDao == null)
                beaconDao = getBeaconDao();
            return beaconDao.queryForAll();
        }catch (SQLException e){
            Log.e(TAG, "query all beacons err " + e.toString());
            return null;
        }
    }

    public List<Company> queryForAllCompanies(){
        try{
            if(companyDao == null)
                companyDao = getCompanyDao();
            return companyDao.queryForAll();
        }catch (SQLException e){
            Log.e(TAG, "query all companies err " + e.toString());
            return null;
        }
    }

    /**
     * check if a detected beacon is in local db or not. if so, it should have more properties
     * to use
     * @param beaconFromDetectedList
     * @return
     */
    public List<Beacon> queryBeacons(Beacon beaconFromDetectedList){
        try {
            if(beaconDao == null)
                beaconDao = getBeaconDao();
            List<Beacon> beaconWanted = beaconDao.queryBuilder().where().
                    eq("major", beaconFromDetectedList.getMajor()).and().
                    eq("minor", beaconFromDetectedList.getMinor()).query();
            if(beaconWanted.size() > 0){
                Log.e(TAG, "queryBeacons: success");
                return beaconWanted;
            }else {
                Log.e(TAG, "queryBeacons: not in local db");
                return null;
            }
        }catch (SQLException e){
            Log.e(TAG, "queryBeacons: error, " + e.toString());
            return null;
        }
    }

    public Company queryForOneCompany(String idcompany){
        try{
            if(companyDao == null)
                companyDao = getCompanyDao();
            List<Company> companyWanted = companyDao.queryBuilder().where().
                    eq("id", idcompany).query();
            if(companyWanted.size() > 0)
                return companyWanted.get(0);
            else
                return null;
        }catch (SQLException e){
            Log.e(TAG, "query one company err " + e.toString());
            return null;
        }
    }

    public List<Beacon> queryForBeaconsByCompanyId(String idcompany){
        try {
            List<Beacon> beaconByTheCompany = beaconDao.queryBuilder().where().
                    eq("idcompany", idcompany).query();
            return beaconByTheCompany;
        } catch (SQLException e){
            Log.e("queryByCompany", e.toString());
            return null;
        }
    }

    public List<Beacon> queryForBeaconsWithAppIdParent(String idparent){
        try {
            List<Beacon> beaconWithAppIdParent = beaconDao.queryBuilder().where().
                    eq("idparent", idparent).query();
            return beaconWithAppIdParent;
        } catch (SQLException e){
            Log.e("queryByCompany", e.toString());
            return null;
        }
    }

    public String[] queryDistinctBeaconTable(String columnName){
        try{
            List<Beacon> distinctSamples = beaconDao.queryBuilder().distinct().selectColumns(columnName).query();
            int resultsAmount = distinctSamples.size();
            String[] results = new String[resultsAmount];
            for(int i = 0; i < resultsAmount; i++){
                results[i] = ((Beacon)((ArrayList)distinctSamples).get(i)).getIdcompany();
            }
            return results;
        }catch (SQLException e) {
            return null;
        }
    }

    @Override
    public void close(){
        super.close();
        beaconRuntimeExceptionDao = null;
    }
}
