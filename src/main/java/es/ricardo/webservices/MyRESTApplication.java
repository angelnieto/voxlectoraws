package es.ricardo.webservices;

import java.util.Set;
import java.util.HashSet;
import javax.ws.rs.core.Application;

import es.ricardo.ws.Autenticador;

public class MyRESTApplication extends Application {

	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> empty = new HashSet<Class<?>>();
	public MyRESTApplication(){
	     singletons.add(new HelloWorldResource());
	     singletons.add(new Autenticador());
	}
	@Override
	public Set<Class<?>> getClasses() {
	     return empty;
	}
	@Override
	public Set<Object> getSingletons() {
	     return singletons;
	}
}
