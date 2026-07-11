package com.hexaware.cozy_heaven.rest_controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {
	    "http://localhost:5173",
	    "https://d2xp6setbjof39.cloudfront.net",
	    "https://hariincode.github.io/cozy-heaven/",
	    "https://hariincode.github.io"
	})
@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Cozy Heaven Backend Running";
    }

}