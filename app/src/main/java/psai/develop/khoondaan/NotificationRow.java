package psai.develop.khoondaan;

/**
 * Created by psai on 1/21/2016.
 */
public class NotificationRow {

    //private variables
    int _id, _count;
    String _date;

    // Empty constructor
    public NotificationRow(){

    }
    // constructor
    public NotificationRow(int id, String _date, int _count){
        this._id = id;

        this._date = _date;
        this._count = _count;
    }


    // getting id
    public int getId(){
        return this._id;
    }

    // setting id
    public void setId(int id){
        this._id = id;
    }

    // getting date
    public String getDate(){
        return this._date;
    }

    // setting Date
    public void setDate(String date){
        this._date = date;
    }

    //getting Count
    public int getCount(){
        return this._count;
    }

    //setting Count
    public void setCount(int count){
        this._count = count;
    }
}
