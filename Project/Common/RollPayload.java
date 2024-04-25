package Project.Common;

public class RollPayload extends Payload {
    private int numberOfDice;
    private int sidesPerDie;

    public RollPayload() {
        super.setPayloadType(PayloadType.ROLL);
    }

    public int getNumberOfDice() {
        return numberOfDice;
    }

    public void setNumberOfDice(int numberOfDice) {
        this.numberOfDice = numberOfDice;
    }

    public int getSidesPerDie() {
        return sidesPerDie;
    }

    public void setSidesPerDie(int sidesPerDie) {
        this.sidesPerDie = sidesPerDie;
    }
}
