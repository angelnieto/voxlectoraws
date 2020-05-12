package es.ricardo.webservices;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/MyRESTApp")
public class HelloWorldResource {

	@GetMapping(produces = {"text/plain"})
	public String sayHello() {
	    return "Hello Ricardo!";
	}
}
