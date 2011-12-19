package org.brain2.test.js;

import org.mozilla.javascript.*;

/**
 * RunScript: simplest example of controlling execution of Rhino.
 * 
 * Collects its arguments from the command line, executes the script, and prints
 * the result.
 * 
 * @author Norris Boyd
 */
public class RunScript {

	class Counter extends ScriptableObject {
		private static final long serialVersionUID = 438270592527335642L;

		// The zero-argument constructor used by Rhino runtime to create
		// instances
		public Counter() {
		}

		// Method jsConstructor defines the JavaScript constructor
		public void jsConstructor(int a) {
			count = a;
		}

		// The class name is defined by the getClassName method
		@Override
		public String getClassName() {
			return "Counter";
		}

		// The method jsGet_count defines the count property.
		public int jsGet_count() {
			return count++;
		}

		// Methods can be defined using the jsFunction_ prefix. Here we define
		// resetCount for JavaScript.
		public void jsFunction_resetCount() {
			count = 0;
		}

		private int count;
	}

	public static void main(String args[]) {
		// Creates and enters a Context. The Context stores information
		// about the execution environment of a script.
		Context cx = Context.enter();
		try {
			// Initialize the standard objects (Object, Function, etc.)
			// This must be done before scripts can be executed. Returns
			// a scope object that we use in later calls.
			Scriptable scope = cx.initStandardObjects();
			Object wrappedOut = Context.javaToJS(System.out, scope);
			Object wrappedErr = Context.javaToJS(System.err, scope);
			ScriptableObject.putProperty(scope, "out", wrappedOut);
			ScriptableObject.putProperty(scope, "err", wrappedErr);

			// Collect the arguments into a single string.
			String s = "var f = function(x){return x+1;}; out.println('f(7)='+f(7)); out.println('f(9)='+f(9)); null";

			// Now evaluate the string we've colected.
			Object result = cx.evaluateString(scope, s, "", 1, null);

			// Convert the result to a string and print it.
			System.out.println(Context.toString(result));

		} finally {
			// Exit from the context.
			Context.exit();
		}
	}
}
