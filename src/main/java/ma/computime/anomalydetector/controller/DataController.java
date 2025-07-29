// Emplacement : src/main/java/ma/computime/anomalydetector/controller/DataController.java
package ma.computime.anomalydetector.controller;


import ma.computime.anomalydetector.entity.Employe;
import ma.computime.anomalydetector.entity.Pointage;


import ma.computime.anomalydetector.repository.EmployeRepository;
import ma.computime.anomalydetector.repository.PointageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/data") 
public class DataController {

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private PointageRepository pointageRepository;

    
    @GetMapping("/employes")
    public List<Employe> getAllEmployes() {
        return employeRepository.findAll();
    }

    
    @GetMapping("/employes/{badge}")
    public ResponseEntity<Employe> getEmployeByBadge(@PathVariable String badge) {
        return employeRepository.findByBadge(badge)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    
    @GetMapping("/employes/{badge}/pointages")
    public List<Pointage> getPointagesByEmployeBadge(@PathVariable String badge) {
        return pointageRepository.findByBadgeEmploye(badge);
    }
    
    
}