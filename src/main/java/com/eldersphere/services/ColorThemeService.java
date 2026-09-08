package com.eldersphere.services;

import com.eldersphere.dtos.ColorTheme.ColorThemeRequestDTO;
import com.eldersphere.dtos.ColorTheme.ColorThemeResponseDTO;
import com.eldersphere.exceptions.GenericException;

import java.util.List;

public interface ColorThemeService {

    ColorThemeResponseDTO create(ColorThemeRequestDTO request) throws GenericException;

    ColorThemeResponseDTO update(Long id, ColorThemeRequestDTO request) throws GenericException;

    ColorThemeResponseDTO getById(Long id) throws GenericException;

    /** ACTIVE themes only, ordered by id - powers the theme picker for any authenticated user. */
    List<ColorThemeResponseDTO> getAllActive();

    void delete(Long id) throws GenericException;
}
