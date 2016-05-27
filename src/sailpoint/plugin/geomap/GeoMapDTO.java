package sailpoint.plugin.geomap;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class GeoMapDTO {
    private String _message = new String();
    private String _session;
    private String _ip;
    
//    private String session = ""; //store the current jsessionID

    public String get_message() {
        return _message;
    }

    public void set_message(String _message) {
        this._message = _message;
    }

    public void set_ip(String _ip){
        this._ip = _ip;
    }

    public String get_ip(){
        return _ip;
    }

    public void set_session(String _session){
        this._session = _session;
    }

    public String get_session(){
        return _session;
    }
}

//    public boolean check_Session(String id){
//        return false;
//    }
