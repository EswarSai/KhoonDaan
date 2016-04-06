package psai.develop.khoondaan;

/**
 * Created by psai on 11/3/2015.
 */
public class UserHeap {

    //private variables
    String _emailid;
    String _mobilenumber, _bloodgroup, _landmark, _city, _lat, _long, _date, _remnotif;

    // Empty constructor
    public UserHeap(){

    }
    // constructor
    public UserHeap(String _emailid, String _bloodgroup, String _number, String _landmark, String _city, String _lat, String _long, String _date, String _remnotif){
        this._emailid = _emailid;
        this._mobilenumber = _number;
        this._bloodgroup = _bloodgroup;
        this._landmark = _landmark;
        this._city = _city;
        this._lat = _lat; this._long = _long;
        this._date = _date;
        this._remnotif = _remnotif;
    }


    // getting name
    public String getEmailId(){
        return this._emailid;
    }

    // setting id
    public void setEmailId(String name){
        this._emailid = name;
    }

    // getting bloodgroup
    public String getBloodgroup(){
        return this._bloodgroup;
    }

    // setting bloodgroup
    public void setBloodgroup(String bloodgroup){
        this._bloodgroup = bloodgroup;
    }

    // getting phone number
    public String getMobilenumber(){
        return this._mobilenumber;
    }

    // setting phone number
    public void setMobilenumber(String phone_number){
        this._mobilenumber = phone_number;
    }

    // getting landmark
    public String getLandmark(){
        return this._landmark;
    }

    // setting landmark
    public void setLandmark(String landmark){
        this._landmark = landmark;
    }

    // getting city
    public String getCity(){
        return this._city;
    }

    // setting city
    public void setCity(String city){
        this._city = city;
    }

    // getting latitude
    public String getLatitude(){
        return this._lat;
    }

    // setting latitude
    public void setLatitude(String lat){
        this._lat = lat;
    }

    // getting longitude
    public String getLongitude(){
        return this._long;
    }

    // setting longitude
    public void setLongitude(String _long){
        this._long = _long;
    }

    // getting date
    public String getDate(){
        return this._date;
    }

    // setting Date
    public void setDate(String _date){
        this._date = _date;
    }

    // getting Remaining notifications
    public String getRemainingNotifications(){
        return this._remnotif;
    }

    // setting remaining notifications
    public void setRemainingNotifications(String _remnotif){
        this._remnotif = _remnotif;
    }

}
