package org.brain2.test.restfuse;

import org.junit.Rule;
import org.junit.runner.RunWith;

import com.eclipsesource.restfuse.Destination;
import com.eclipsesource.restfuse.HttpJUnitRunner;
import com.eclipsesource.restfuse.Method;
import com.eclipsesource.restfuse.PollState;
import com.eclipsesource.restfuse.Response;
import com.eclipsesource.restfuse.annotation.Context;
import com.eclipsesource.restfuse.annotation.Header;
import com.eclipsesource.restfuse.annotation.HttpTest;
import com.eclipsesource.restfuse.annotation.Poll;

@RunWith( HttpJUnitRunner.class )
public class HelloWorld {

  @Rule
  public Destination destination = new Destination( "http://mapi.vnexpress.net" );
  

  @Context
  private Response response; // will be injected after every request
  
  @Context
  private PollState state;

  @HttpTest( method = Method.GET, path = "/articles?method=get" )
  public void testBasic() {
	  com.eclipsesource.restfuse.Assert.assertOk(response);
	  int responseCode = response.getStatus();
	  System.out.println(responseCode);
	  System.out.println(response.getBody(String.class));
  }
  
  @HttpTest( method = Method.GET, path = "/articles?method=get" )
  @Poll(times=2, interval=200)
  public void testPoll() {
	Response currentRes = state.getResponse(state.getTimes());
	org.junit.Assert.assertEquals(currentRes,response);
  }
  
  @HttpTest( method = Method.GET, path = "/articles?method=get", headers={@Header(name="User-Agent", value="Virus de")})
  public void testHeader() {
	  com.eclipsesource.restfuse.Assert.assertOk(response);
  }
  
  
  
}


