package com.marbouh.locationdevetementstraditionnels.controller;


import com.marbouh.locationdevetementstraditionnels.exception.InvalidOperationException;
import com.marbouh.locationdevetementstraditionnels.model.Utilisateur;
import com.marbouh.locationdevetementstraditionnels.services.impl.UtilisateurServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "https://frontend-ebon-sigma.vercel.app")
@RequiredArgsConstructor
public class UtilisateurController {
    private final UtilisateurServiceImpl utilisateurService;

    @PutMapping("/update")
    public Utilisateur Update(@RequestBody Utilisateur utilisateur) {
        return utilisateurService.update(utilisateur);
    }
    @DeleteMapping("/client/delete/{id}")
    public void deleteUser(@PathVariable("id") int id) {
        utilisateurService.deleteById(id);
    }
    @GetMapping("/clients")
    public List<Utilisateur> getAllClient() {
        return utilisateurService.getAllClient();
    }

    @DeleteMapping("/manager/delete/{id}")
    public ResponseEntity<String> deleteManager(@PathVariable("id") int id) {

            utilisateurService.delete(id);
            return ResponseEntity.ok("Utilisateur and related records deleted successfully.");

    }


}

