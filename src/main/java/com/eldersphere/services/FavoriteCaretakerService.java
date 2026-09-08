package com.eldersphere.services;

import com.eldersphere.dtos.Search.CaretakerSearchResultDTO;
import com.eldersphere.exceptions.GenericException;

import java.util.List;

public interface FavoriteCaretakerService {
    CaretakerSearchResultDTO addFavorite(Long familyUserId, Long caretakerId) throws GenericException;

    void removeFavorite(Long familyUserId, Long caretakerId) throws GenericException;

    List<CaretakerSearchResultDTO> listFavorites(Long familyUserId);
}
