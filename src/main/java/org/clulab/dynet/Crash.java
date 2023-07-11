package org.clulab.dynet;

import edu.cmu.dynet.internal.dynet_swig;

public class Crash {

	public static void main(String[] args) {
		dynet_swig.readNullPtr();
		System.out.println("I should not get here.");
	}
}
