package com.marbouh.locationdevetementstraditionnels.repository;

import com.marbouh.locationdevetementstraditionnels.model.Boutique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BoutiqueRepository extends JpaRepository< Boutique,Integer> {
    @Query("select b from Boutique b where b.vendeur.id =  :id")
    Boutique getBoutiqueByVendeur(int id);
    @Query("SELECT b.id FROM Boutique b WHERE b.vendeur.email= :email")
    Integer getBoutiqueIdByVendeurEmail(String email);
    @Modifying
    @Transactional
    @Query("DELETE FROM Boutique b WHERE b.vendeur.id = :vendeurId")
    void deleteByVendeurId(Integer vendeurId);

    List<Boutique> findByVendeurId(Integer vendeurId);



}
