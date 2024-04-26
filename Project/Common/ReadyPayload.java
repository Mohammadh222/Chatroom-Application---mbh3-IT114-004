package Project.Common;
import Project.Common.PayloadType;

/// mbh3
 // 04/25/24 
 
public class ReadyPayload extends Payload {

    private boolean isReady;

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }
    public ReadyPayload() {
        setPayloadType(PayloadType.READY);
    }
}