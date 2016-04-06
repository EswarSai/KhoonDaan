package psai.develop.khoondaan;

/**
 * Created by psai on 11/24/2015.
 */
public class Entry {

    //private variables
    int _id;
    String _bloodgroup, _helpemail, _date;

    // Empty constructor
    public Entry(){

    }
    // constructor
    public Entry(int id, String _bloodgroup, String _helpemail, String _date){
        this._id = id;
        this._bloodgroup = _bloodgroup;
        this._helpemail = _helpemail;
        this._date = _date;
    }


    // getting id
    public int getId(){
        return this._id;
    }

    // setting id
    public void setId(int id){
        this._id = id;
    }

    // getting bloodgroup
    public String getBloodgroup(){
        return this._bloodgroup;
    }

    // setting bloodgroup
    public void setBloodgroup(String bloodgroup){
        this._bloodgroup = bloodgroup;
    }

    // getting Helper Email
    public String getHelperEmail(){
        return this._helpemail;
    }

    // setting Helper email
    public void setHelperEmail(String help_email){
        this._helpemail = help_email;
    }

    // getting date
    public String getDate(){
        return this._date;
    }

    // setting Date
    public void setDate(String date){
        this._date = date;
    }

}
