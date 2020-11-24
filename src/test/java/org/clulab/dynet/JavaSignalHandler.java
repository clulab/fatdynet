package org.clulab.dynet;

import edu.cmu.dynet.internal.SignalHandler;

public class JavaSignalHandler extends SignalHandler {
	int count = 0;

	public int run(int signal) {
		count++;
		System.out.println("JavaSignalHandler.run()");
		return 0;
	}
}
