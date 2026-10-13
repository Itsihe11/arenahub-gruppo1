package com.example.backend.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Services.TorneoService;

@RestController
@RequestMapping("/api/tornei")
public class TorneoController {
	
	@Autowired TorneoService TorneoDependency;
	
	public TorneoController(TorneoService TorneoDependency) {
		this.TorneoDependency = TorneoDependency;
	}
	
	@GetMapping
	public List<Map<String, Object>> elenco(@RequestParam(required = false) String stato){
		return TorneoDependency.elenco(stato);
	}
	
    @GetMapping("/{id}")
    public Map<String, Object> dettaglio(@PathVariable int id) {
        return TorneoDependency.dettaglio(id);
    }
    
	

}
