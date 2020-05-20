package es.ricardo.webservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldResource {
	
	@Autowired
	private Environment environment;

	@RequestMapping(path = "/MyRESTApp", produces = {"text/plain"})
	public String sayHello() {
		String profile = "default";
		if(environment.getActiveProfiles().length > 0) {
			profile = environment.getActiveProfiles()[0].toString();
		}
	    return "Hello " + profile + " user!";
	}
}
