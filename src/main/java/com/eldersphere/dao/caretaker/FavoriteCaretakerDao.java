package com.eldersphere.dao.caretaker;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.FavoriteCaretaker;
import com.eldersphere.repositories.FavoriteCaretakerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FavoriteCaretakerDao implements IDao<FavoriteCaretaker, Long> {

    private final FavoriteCaretakerRepository favoriteCaretakerRepository;

    @Override
    public JpaRepository<FavoriteCaretaker, Long> getRepository() {
        return favoriteCaretakerRepository;
    }

    public FavoriteCaretaker save(FavoriteCaretaker favorite) {
        return favoriteCaretakerRepository.save(favorite);
    }

    public boolean exists(Long familyUserId, Long caretakerId) {
        return favoriteCaretakerRepository.existsByFamilyUserIdAndCaretakerId(familyUserId, caretakerId);
    }

    public Optional<FavoriteCaretaker> find(Long familyUserId, Long caretakerId) {
        return favoriteCaretakerRepository.findByFamilyUserIdAndCaretakerId(familyUserId, caretakerId);
    }

    public List<FavoriteCaretaker> findByFamilyUserId(Long familyUserId) {
        return favoriteCaretakerRepository.findByFamilyUserIdOrderByCreatedAtDesc(familyUserId);
    }

    @Transactional
    public void delete(Long familyUserId, Long caretakerId) {
        favoriteCaretakerRepository.deleteByFamilyUserIdAndCaretakerId(familyUserId, caretakerId);
    }
}
