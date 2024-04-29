package Project.Common;

import java.util.ArrayList;
import java.util.List;

//mbh3
//04/24/24 

public class RoomResultsPayload extends Payload {
    private List<String> rooms = new ArrayList<String>();
   
    private int limit = 10;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public RoomResultsPayload() {
        setPayloadType(PayloadType.LIST_ROOMS);
    }

    public List<String> getRooms() {
        return rooms;
    }

    public void setRooms(List<String> rooms) {
        this.rooms = rooms;
    }

    public void setRooms(String[] rooms2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setRooms'");
    }
}
