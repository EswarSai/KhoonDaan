package psai.develop.khoondaan;

/**
 * Created by psai on 8/19/2015.
 */
public class User {

    //private variables
    String _username;
    String _mobilenumber, _bloodgroup, _landmark, _city;

    // Empty constructor
    public User(){

    }
    // constructor
    public User(String name, String _bloodgroup, String _number, String _landmark, String _city){
        this._username = name;
        this._mobilenumber = _number;
        this._bloodgroup = _bloodgroup;
        this._landmark = _landmark;
        this._city = _city;
    }

    public User(String name, String _bloodgroup, String _landmark, String _city){
        this._username = name;
        this._mobilenumber = "";
        this._bloodgroup = _bloodgroup;
        this._landmark = _landmark;
        this._city = _city;
    }


    // getting name
    public String getName(){
        return this._username;
    }

    // setting id
    public void setName(String name){
        this._username = name;
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

    // setting phone number
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
}