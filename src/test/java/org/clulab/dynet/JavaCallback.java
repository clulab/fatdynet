package org.clulab.dynet;

import edu.cmu.dynet.internal.Callback;

class JavaCallback extends Callback {
	int count = 0;

	public JavaCallback() {
		super();
	}

	public void run() {
		count++;
		System.out.println("JavaCallback.run()");
	}
}
