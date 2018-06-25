package test.java.com.hexcore.vrmeeting_hostserver;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import main.java.com.hexcore.vrmeeting_hostserver.ServerComponent;
import main.java.com.hexcore.vrmeeting_hostserver.ServerComponentState;
import main.java.com.hexcore.vrmeeting_hostserver.exception.IllegalComponentStateException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;


/**
 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.ServerComponent}
 * @author Psymj1 (Marcus)
 *
 */
public class TestServerComponent {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private class MockServerComponent extends ServerComponent
	{
		@Override
		protected void startUp() throws StartupException {
			
		}

		@Override
		protected void shutdown() {
			
		}
		
	}
	
	private class BrokenMockServerComponent extends ServerComponent
	{

		@Override
		protected void startUp() throws StartupException {
			throw new StartupException("This is a fake exception");
		}

		@Override
		protected void shutdown() {
			
		}
		
	}
	
	MockServerComponent mockServerComponent;
	
	@Before
	public void beforeTest()
	{
		mockServerComponent = new MockServerComponent();
	}
	
	
	//Start Tests
	
	@Test
	public void startShouldUpdateComponentStateToRunningWhenSuccessful() throws StartupException, IllegalComponentStateException
	{
		mockServerComponent.start();
		ServerComponentState expectedState = ServerComponentState.RUNNING;
		assertEquals("The state of the component was not updated to " + expectedState,expectedState,mockServerComponent.getState());
	}
	
	@Test
	public void startShouldThrowExceptionIfComponentNotInNewState() throws StartupException, IllegalComponentStateException
	{
		mockServerComponent.start();
		thrown.expect(IllegalComponentStateException.class);
		thrown.expectMessage("Component cannot be started because it has already been started");
		
		mockServerComponent.start();
	}
	
	@Test
	public void startShouldNotUpdateComponentStateIfFails() throws StartupException, IllegalComponentStateException
	{
		BrokenMockServerComponent serverComponent = new BrokenMockServerComponent();
		ServerComponentState beforeState = serverComponent.getState();
		thrown.expect(StartupException.class);
		serverComponent.start();
		assertEquals("The state of the component was updated despite a startup exception being thrown",beforeState,serverComponent.getState());
	}
	
	//Stop Tests
	
	@Test
	public void stopShouldUpdateComponentStateToStoppingWhenRunning() throws StartupException, IllegalComponentStateException
	{
		mockServerComponent.start();
		mockServerComponent.stop();
		ServerComponentState expectedState = ServerComponentState.STOPPING;
		assertEquals("The state of the component was not updated to " + expectedState,expectedState,mockServerComponent.getState());
	}
	
	@Test
	public void stopShouldThrowExceptionIfComponentNotInRunningState() throws StartupException, IllegalComponentStateException
	{
		thrown.expect(IllegalComponentStateException.class);
		thrown.expectMessage("Component cannot be stopped as it is not running");
		mockServerComponent.stop();
	}
	
	@Test
	public void allSubComponentsShouldMoveToStoppingWhenStopCalled() throws StartupException, IllegalComponentStateException
	{
		ArrayList<MockServerComponent> components = new ArrayList<MockServerComponent>();
		int numComponents = (int)Math.floor(Math.random()*10)+1; //Generate a random number between 1 and 10
		for(int i = 0;i < numComponents;i++)
		{
			MockServerComponent component = new MockServerComponent();
			component.start();
			components.add(component);
			mockServerComponent.addSubServerComponent(component);
		}
		
		mockServerComponent.start();
		mockServerComponent.stop();
		ServerComponentState expectedState = ServerComponentState.STOPPING;
		
		for(int i = 0;i < numComponents;i++)
		{
			assertEquals("Component " + i + " out of " + numComponents + " was not set to stopping",components.get(i).getState(),expectedState);
		}
	} 
	
	@Test
	public void stateShouldBeStoppingIfSubComponentsAreNotStoppedState() throws StartupException, IllegalComponentStateException
	{
		
		ServerComponent componentThatStops = new ServerComponent() {
			
			@Override
			protected void startUp() throws StartupException {
				
			}

			@Override
			protected void shutdown() {
				setState(ServerComponentState.STOPPED);
			}
		};
		componentThatStops.start();
		
		int numComponents = (int)Math.floor(Math.random()*10)+1; //Generate a random number between 1 and 10
		for(int i = 0;i < numComponents;i++)
		{
			MockServerComponent component = new MockServerComponent();
			component.start();
			componentThatStops.addSubServerComponent(component);
		}
		
		componentThatStops.stop();
		assertEquals("The parent component's state is not stopping despite the sub components still being in the stopping state",ServerComponentState.STOPPING,componentThatStops.getState());
	}
}
