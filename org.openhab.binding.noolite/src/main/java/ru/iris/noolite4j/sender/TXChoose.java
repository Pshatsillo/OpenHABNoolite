package ru.iris.noolite4j.sender;

import java.util.HashMap;
import java.util.Map;

public class TXChoose extends PC11xx{
	
	public TXChoose(byte channels) {

        super();

        Map<Short, ?> devices = new HashMap<Short, Object>(channels);
        availableChannels = channels;
    }

}
