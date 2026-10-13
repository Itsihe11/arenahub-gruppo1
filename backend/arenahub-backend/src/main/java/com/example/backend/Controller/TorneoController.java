package com.example.backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Services.TorneoService;

@RestController
@RequestMapping("/torneo")
public class TorneoController {
	
	@Autowired TorneoService TorneoDependency;
	
	

}
