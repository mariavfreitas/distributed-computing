package pt.tecnico.distledger.server;

import java.util.ArrayList;

public class VectorClock {
    private final ArrayList<Integer> timeStamps;

    public VectorClock() {
        timeStamps = new ArrayList<>();
    }

    public Integer getTS(Integer i) {
        return timeStamps.get(i);
    }

    public void setTS(Integer i, Integer value) {
        timeStamps.set(i, value);
    }

    public boolean isGreaterOrEqual(VectorClock v) {
        for (int i = 0; i < timeStamps.size(); i++) {
            if (timeStamps.get(i) >= timeStamps.get(i+1))
                return true;
        }
        return false;
    }
}
