package io.ap1.libbeaconmanagement;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "company")

public class Company {

    private final String LID = "lid";
    private final String ID = "id";
    private final String COMPANY = "company";
    private final String COLOR = "color";
    private final String LATITUDE = "lat";
    private final String LONGITUDE = "lng";
    private final String HASH = "hash";
    private final String IDPARENT = "idparent";

    @DatabaseField(generatedId=true, useGetSet=true, columnName=LID)
    private int lID; // *** this field is for local auto increment, not contained in the remote db ****
    @DatabaseField(useGetSet=true, columnName=ID)
    private String id;
    @DatabaseField(useGetSet = true, columnName = COMPANY)
    private String company;
    @DatabaseField(useGetSet = true, columnName = COLOR)
    private String color;
    @DatabaseField(useGetSet = true, columnName = LATITUDE)
    private String lat;
    @DatabaseField(useGetSet = true, columnName = LONGITUDE)
    private String lng;
    @DatabaseField(useGetSet = true, columnName = HASH)
    private String hash;
    @DatabaseField(useGetSet = true, columnDefinition = IDPARENT)
    private String idparent;

    public int getLID(){
        return lID;
    }

    public void setLID(int lID){
        this.lID = lID;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getCompany(){
        return company;
    }

    public void setCompany(String company){
        this.company = company;
    }

    public String getColor(){
        return color;
    }

    public void setColor(String color){
        this.color = color;
    }

    public String getLat(){
        return lat;
    }

    public void setLat(String lat){
        this.lat = lat;
    }

    public String getLng(){
        return lng;
    }

    public void setLng(String lng){
        this.lng = lng;
    }

    public String getHash(){
        return hash;
    }

    public void setHash(String hash){
        this.hash = hash;
    }

    public String getIdparent(){
        return idparent;
    }

    public void setIdparent(String idparent){
        this.idparent = idparent;
    }
}
