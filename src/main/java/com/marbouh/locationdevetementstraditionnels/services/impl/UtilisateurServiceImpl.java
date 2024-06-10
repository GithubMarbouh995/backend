package com.marbouh.locationdevetementstraditionnels.services.impl;
import com.marbouh.locationdevetementstraditionnels.dto.UtilisateurDto;
import com.marbouh.locationdevetementstraditionnels.exception.ErrorCodes;
import com.marbouh.locationdevetementstraditionnels.exception.InvalidEntityException;
import com.marbouh.locationdevetementstraditionnels.exception.InvalidOperationException;
import com.marbouh.locationdevetementstraditionnels.model.Boutique;
import com.marbouh.locationdevetementstraditionnels.model.Role;
import com.marbouh.locationdevetementstraditionnels.model.Utilisateur;
import com.marbouh.locationdevetementstraditionnels.repository.*;
import com.marbouh.locationdevetementstraditionnels.services.UtilisateurService;
import com.marbouh.locationdevetementstraditionnels.token.TokenRepository;
import com.marbouh.locationdevetementstraditionnels.validator.UtilisateurValidator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.marbouh.locationdevetementstraditionnels.model.Role.MANAGER;


@Service
@Slf4j
public class UtilisateurServiceImpl implements UtilisateurService {
    private UtilisateurRepository utilisateurRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BoutiqueRepository boutiqueRepository;
    @Autowired
    private AvisRepository avisRepository;
    @Autowired
    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository,
                                  PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UtilisateurDto save(UtilisateurDto dto) {
        List<String> errors = UtilisateurValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Utilisateur is not valid {}", dto);
            throw new InvalidEntityException("L'utilisateur n'est pas valide", ErrorCodes.UTILISATEUR_NOT_VALID,
                    Collections.singletonList("Un autre utilisateur avec le meme email existe deja dans la BDD"));

        }
        if (userAlreadyExists(dto.getEmail())) {
            throw new InvalidEntityException("Un autre utilisateur avec le meme email existe deja", ErrorCodes.UTILISATEUR_ALREADY_EXISTS,
                    Collections.singletonList("Un autre utilisateur avec le meme email existe deja dans la BDD"));
        }
        dto.setMoteDePasse(passwordEncoder.encode(dto.getMoteDePasse()));
       return UtilisateurDto.fromEntity(
                utilisateurRepository.save(
                        UtilisateurDto.toEntity(dto)
                )
        );
    }
    private boolean userAlreadyExists(String email) {
    Optional<Utilisateur> utilisateur=utilisateurRepository.findByEmail(email);
    return utilisateur.isPresent();
    }

    @Override
    public UtilisateurDto findById(Integer id) {
        if (id == null) {
            log.error("Utilisateur ID is null");
            return null;
        }
        return utilisateurRepository.findById(id)
                .map(UtilisateurDto::fromEntity)
                .orElseThrow(() -> new InvalidEntityException(
                        "Aucun utilisateur avec l'ID = " + id + " n'ete trouve dans la BDD",
                        ErrorCodes.UTILISATEUR_NOT_FOUND)
                );
    }

    @Override
    public List<UtilisateurDto> findAll() {
        return utilisateurRepository.findAll().stream()
                .map(UtilisateurDto::fromEntity)
                .collect(Collectors.toList());
    }

//    @Override
//    public void delete(Integer id) {
//       Utilisateur user= utilisateurRepository.findById(id)
//                .orElseThrow(() -> new InvalidOperationException("Aucun utilisateur avec l'ID = " + id + " n'ete trouve dans la BDD",
//                        ErrorCodes.UTILISATEUR_NOT_FOUND));
//         if (id == null) {
//            log.error("Utilisateur ID is null");
//            return;
//        }if (user.getRole()==Role.MANAGER){
//        utilisateurRepository.deleteById(id);
//        }
//
//    }



    @Override
    @Transactional
    public void delete(Integer id) {
        if (id == null) {
            log.error("Utilisateur ID is null");
            throw new InvalidOperationException("Utilisateur ID is null", ErrorCodes.UTILISATEUR_NOT_FOUND);
        }

        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Aucun utilisateur avec l'ID = " + id + " n'a été trouvé dans la BDD",
                        ErrorCodes.UTILISATEUR_NOT_FOUND));

        if (user.getRole() == MANAGER) {
            // Delete all the related records
            tokenRepository.deleteByUserId(id);
            locationRepository.deleteByClientId(id);
            reservationRepository.deleteByClientId(id);
            Boutique boutique = boutiqueRepository.getBoutiqueByVendeur(id);
            if (boutique != null) {
                avisRepository.deleteByBoutiqueId(boutique.getId());
                boutiqueRepository.deleteByVendeurId(id);
            }


            // Now delete the manager
            utilisateurRepository.deleteById(id);
        } else {
            log.error("Only users with the MANAGER role can be deleted");
            throw new InvalidOperationException("Only users with the MANAGER role can be deleted",
                    ErrorCodes.UTILISATEUR_NOT_DELETABLE);
        }
    }



    @Override
    public UtilisateurDto findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .map(UtilisateurDto::fromEntity)
                .orElseThrow(() -> new InvalidEntityException(
                        "Aucun utilisateur avec l'email = " + email + " n'ete trouve dans la BDD",
                        ErrorCodes.UTILISATEUR_NOT_FOUND)
                );


    }

    public Integer findId(String email){
      return utilisateurRepository.findIdByEmail(email);
    }

    public Utilisateur findById(int id){
        return utilisateurRepository.findbyId(id);
    }

    public Utilisateur update(Utilisateur utilisateur){
        if (utilisateur == null) {
            log.error("Utilisateur is null");
            return null;
        }
        return utilisateurRepository.save(utilisateur);
    }
     //Supprime ce bout de code
        public void changeRoleToSeller(Utilisateur user) {
            user.setRole(MANAGER);
            utilisateurRepository.save(user);
        }
    @Transactional
    public void deleteById(int id) {
        tokenRepository.deleteByUserId(id);
        locationRepository.deleteByClientId(id);
        reservationRepository.deleteByClientId(id);
        utilisateurRepository.deleteById(id);
    }
 public List<Utilisateur> getAllClient(){
        return utilisateurRepository.getAllClient();
    }

}
