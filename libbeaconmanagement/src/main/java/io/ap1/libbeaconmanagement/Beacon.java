package io.ap1.libbeaconmanagement;

import android.support.annotation.NonNull;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "beacon")
public class Beacon implements Comparable<Beacon>{

    private final String LID = "lid";
    private final String ID = "id";
    private final String UUID = "uuid";
    private final String MAJOR = "major";
    private final String MINOR = "minor";
    private final String LATITUDE = "lat";
    private final String LONGITUDE = "lng";
    private final String URLNEAR = "urlnear";
    private final String URLFAR = "urlfar";
    private final String NOTIFYTEXT = "notifytext";
    private final String NOTIFYTITLE = "notifytitle";
    private final String NICKNAME = "nickName";
    private final String RSSI = "rssi";
    private final String ACCURACY = "accuracy";
    private final String IDCOMPANY = "idcompany";
    private final String MACADDRESS = "macaddress";
    private final String IDPARENT = "idparent";

    @DatabaseField(generatedId=true, useGetSet=true, columnName=LID)
    private int lID; // *** this field is for local auto increment, not contained in the remote db ****
    @DatabaseField(useGetSet=true, columnName=ID)
    private String id;
    @DatabaseField(useGetSet = true, columnName = UUID)
    private String uuid;
    @DatabaseField(useGetSet = true, columnName = MAJOR)
    private String major;
    @DatabaseField(useGetSet = true, columnName = MINOR)
    private String minor;
    @DatabaseField(useGetSet = true, columnName = LATITUDE)
    private String lat;
    @DatabaseField(useGetSet = true, columnName = LONGITUDE)
    private String lng;
    @DatabaseField(useGetSet = true, columnName = URLNEAR)
    private String urlnear;
    @DatabaseField(useGetSet = true, columnName = URLFAR)
    private String urlfar;
    @DatabaseField(useGetSet = true, columnName = NOTIFYTEXT)
    private String notifytext;
    @DatabaseField(useGetSet = true, columnName = NOTIFYTITLE)
    private String notifytitle;
    @DatabaseField(useGetSet = true, columnName = NICKNAME)
    private String nickname;
    @DatabaseField(useGetSet = true, columnName = RSSI)
    private String rssi;
    @DatabaseField(useGetSet = true, columnName = ACCURACY)
    private String accuracy;
    @DatabaseField(useGetSet = true, columnName = IDCOMPANY)
    private String idcompany;
    @DatabaseField(useGetSet = true, columnName = MACADDRESS)
    private String macaddress;
    @DatabaseField(useGetSet = true, columnDefinition = IDPARENT)
    private String idparent;

    public Beacon(){
    }

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

    public String getUuid(){
        return uuid;
    }

    public void setUuid(String uuid){
        this.uuid = uuid;
    }

    public String getMajor(){
        return major;
    }

    public void setMajor(String major){
        this.major = major;
    }

    public String getMinor(){
        return minor;
    }

    public void setMinor(String minor){
        this.minor = minor;
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

    public String getUrlnear(){
        return urlnear;
    }

    public void setUrlnear(String urlnear){
        this.urlnear = urlnear;
    }

    public String getUrlfar(){
        return urlfar;
    }

    public void setUrlfar(String urlfar){
        this.urlfar = urlfar;
    }

    public String getNotifytext(){
        return notifytext;
    }

    public void setNotifytext(String notifytext){
        this.notifytext = notifytext;
    }

    public String getNotifytitle(){
        return notifytitle;
    }

    public void setNotifytitle(String notifytitle){
        this.notifytitle = notifytitle;
    }

    public String getNickname(){
        return nickname;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public String getRssi(){
        return rssi;
    }

    public void setRssi(String rssi){
        this.rssi = rssi;
    }

    public String getAccuracy(){
        return accuracy;
    }

    public void setAccuracy(String accuracy){
        this.accuracy = accuracy;
    }

    public String getIdcompany(){
        return idcompany;
    }

    public void setIdcompany(String idcompany){
        this.idcompany = idcompany;
    }

    public String getMacaddress(){
        return macaddress;
    }

    public void setMacaddress(String macaddress){
        this.macaddress = macaddress;
    }

    public String getIdparent(){
        return idparent;
    }

    public void setIdparent(String idparent){
        this.idparent = idparent;
    }

    @Override

    public int compareTo(@NonNull Beacon beacon){
        return (Integer.parseInt(getRssi()) - Integer.parseInt(beacon.getRssi()));
    }
}
