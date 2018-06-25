package main.java.com.hexcore.vrmeeting_hostserver;

/**
 * Describes the current state of a Server Component
 * @author Psymj1 (Marcus)
 *
 */
public enum ServerComponentState {
	NEW, //Hasn't been started yet
	//STARTING, //When the component has had start() called but has not finished starting yet
	RUNNING, //After start() has successfully run
	STOPPING, //stop() has been called but the component or its sub components have not yet stopped 
	STOPPED //Has been running and has now been stopped
}
