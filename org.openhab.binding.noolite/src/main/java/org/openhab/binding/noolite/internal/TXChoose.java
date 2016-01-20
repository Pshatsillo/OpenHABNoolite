package org.openhab.binding.noolite.internal;

import java.util.HashMap;
import java.util.Map;

import ru.iris.noolite4j.sender.PC11xx;

public class TXChoose extends PC11xx{
	
	public TXChoose(byte channels) {

        super();

        Map<Short, ?> devices = new HashMap<Short, Object>(channels);
        availableChannels = channels;
    }

}
