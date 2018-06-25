package main.java.com.hexcore.vrmeeting_hostserver;

import java.util.ArrayList;
import java.util.List;

import main.java.com.hexcore.vrmeeting_hostserver.exception.IllegalComponentStateException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;

/**
 * A class extending ServerComponent is a class that can be started and stopped. It also has a state.
 * Unlike threads a class extending this class isn't necessarily threaded. The class is just to enable clean startup and shutdown of the server.
 * A server componenet must be started after initilisation to act but can be stopped by another ServerComponenet.
 * ServerComponents can form a hierarchal structure as a ServerComponent can be a sub component of another. And if the parent component is instructed to stop then
 * so are all sub-components.
 * Classes extending this are expected to handle moving to the stopped state
 * @author Psymj1 (Marcus)
 */
public abstract class ServerComponent {
	private ServerComponentState currentState = ServerComponentState.NEW;
	private List<ServerComponent> subComponenets = new ArrayList<ServerComponent>();
	private ServerComponent parent;
	
	public ServerComponent(ServerComponent parent)
	{
		this.parent = parent;
	}
	
	public ServerComponent()
	{
		parent = null;
	}
	
	/**
	 * Executed when the component is started
	 * @throws StartupException If there is an error in the class during startup
	 * @throws IllegalComponentStateException If the component is not a new component
	 */
	public void start() throws StartupException, IllegalComponentStateException
	{
		if(!getState().equals(ServerComponentState.NEW))
		{
			throw new IllegalComponentStateException("Component cannot be started because it has already been started");
		}
		startUp();
		setState(ServerComponentState.RUNNING);
	}
	
	/**
	 * Called during start() providing subclasses the ability to perform pre-running configuration
	 * @throws StartupException Thrown if there is an error during pre-run configuration
	 */
	protected abstract void startUp() throws StartupException;
	
	/**
	 * Requests that the server component stops
	 * @throws IllegalComponentStateException If the component is not currently running
	 */
	public void stop() throws IllegalComponentStateException
	{
		if(!getState().equals(ServerComponentState.RUNNING))
		{
			throw new IllegalComponentStateException("Component cannot be stopped as it is not running");
		}
		setState(ServerComponentState.STOPPING);
		
		synchronized(subComponenets)
		{
			for(ServerComponent c : subComponenets)
			{
				c.stop();
			}
		}
		
		
		shutdown();
	}
	
	/**
	 * Called during stop() providing subclasses the ability to perform operations to cleanly shutdown
	 */
	protected abstract void shutdown();
	
	/**
	 *  If the component state is stopped but a sub component is still running or stopping then the overall component state is stopping
	 * @return The current state of the ServerComponent
	 */
	public ServerComponentState getState()
	{
		ServerComponentState temp;
		synchronized(currentState)
		{
			temp = currentState;
		}
		
		if(temp == ServerComponentState.STOPPED)
		{
			synchronized(subComponenets)
			{
				for(ServerComponent s: subComponenets)
				{
					if(s.getState() != ServerComponentState.STOPPED)
					{
						temp = ServerComponentState.STOPPING;
						break;
					}
				}
			}
		}
		
		return temp;
	}
	
	private void setParentComponent(ServerComponent component)
	{
		parent = component;
	}
	
	public void addSubServerComponent(ServerComponent component)
	{
		synchronized(subComponenets)
		{
			subComponenets.add(component);
			component.setParentComponent(this);
		}
	}
	
	public void removeSubServerComponent(ServerComponent component)
	{
		synchronized(subComponenets)
		{
			subComponenets.remove(component);
		}
	}
	
	protected void setState(ServerComponentState state)
	{
		synchronized(currentState)
		{
			currentState = state;
		}
	}
	
	protected ServerComponent getParentComponent()
	{
		return parent;
	}
	
	protected int getNumOfSubComponenets()
	{
		synchronized (subComponenets) {
			return subComponenets.size();
		}
	}
}
