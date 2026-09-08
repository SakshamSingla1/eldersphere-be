package com.eldersphere.services.impl;

import com.eldersphere.dao.caretaker.CaretakerProfileDao;
import com.eldersphere.dao.caretaker.FavoriteCaretakerDao;
import com.eldersphere.dao.file.FileAssetDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Search.CaretakerSearchResultDTO;
import com.eldersphere.entities.CaretakerProfile;
import com.eldersphere.entities.FavoriteCaretaker;
import com.eldersphere.entities.User;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FavoriteCaretakerServiceImpl}, focused on the uniqueness rule (a
 * family member can't favorite the same caretaker twice) and not-found handling.
 */
class FavoriteCaretakerServiceImplTest {

    private static final Long FAMILY_USER_ID = 100L;
    private static final Long CARETAKER_ID = 200L;

    private FavoriteCaretakerDao favoriteCaretakerDao;
    private CaretakerProfileDao caretakerProfileDao;
    private UserDao userDao;
    private FileAssetDao fileAssetDao;
    private FavoriteCaretakerServiceImpl favoriteCaretakerService;

    @BeforeEach
    void setUp() {
        favoriteCaretakerDao = mock(FavoriteCaretakerDao.class);
        caretakerProfileDao = mock(CaretakerProfileDao.class);
        userDao = mock(UserDao.class);
        fileAssetDao = mock(FileAssetDao.class);
        favoriteCaretakerService = new FavoriteCaretakerServiceImpl(favoriteCaretakerDao, caretakerProfileDao, userDao, fileAssetDao);
    }

    @Test
    void addFavorite_caretakerNotFound_throwsCaretakerProfileNotFound() {
        when(caretakerProfileDao.findById(eq(CARETAKER_ID), eq(true))).thenReturn(null);

        GenericException ex = assertThrows(GenericException.class,
                () -> favoriteCaretakerService.addFavorite(FAMILY_USER_ID, CARETAKER_ID));

        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND);
        verify(favoriteCaretakerDao, never()).save(any());
    }

    @Test
    void addFavorite_alreadyFavorited_throwsDuplicateFavoriteCaretaker() {
        when(caretakerProfileDao.findById(eq(CARETAKER_ID), eq(true)))
                .thenReturn(CaretakerProfile.builder().id(CARETAKER_ID).userId(300L).build());
        when(favoriteCaretakerDao.exists(FAMILY_USER_ID, CARETAKER_ID)).thenReturn(true);

        GenericException ex = assertThrows(GenericException.class,
                () -> favoriteCaretakerService.addFavorite(FAMILY_USER_ID, CARETAKER_ID));

        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.DUPLICATE_FAVORITE_CARETAKER);
        verify(favoriteCaretakerDao, never()).save(any());
    }

    @Test
    void addFavorite_notYetFavorited_savesAndReturnsCaretakerSummary() throws GenericException {
        when(caretakerProfileDao.findById(eq(CARETAKER_ID), eq(true)))
                .thenReturn(CaretakerProfile.builder().id(CARETAKER_ID).userId(300L).bio("Great caretaker").build());
        when(favoriteCaretakerDao.exists(FAMILY_USER_ID, CARETAKER_ID)).thenReturn(false);
        when(userDao.findById(eq(300L), eq(true))).thenReturn(User.builder().id(300L).fullName("Jane Caretaker").build());

        CaretakerSearchResultDTO result = favoriteCaretakerService.addFavorite(FAMILY_USER_ID, CARETAKER_ID);

        assertThat(result.getId()).isEqualTo(CARETAKER_ID);
        assertThat(result.getFullName()).isEqualTo("Jane Caretaker");
        verify(favoriteCaretakerDao, times(1)).save(any(FavoriteCaretaker.class));
    }

    @Test
    void removeFavorite_notFound_throwsFavoriteCaretakerNotFound() {
        when(favoriteCaretakerDao.find(FAMILY_USER_ID, CARETAKER_ID)).thenReturn(Optional.empty());

        GenericException ex = assertThrows(GenericException.class,
                () -> favoriteCaretakerService.removeFavorite(FAMILY_USER_ID, CARETAKER_ID));

        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.FAVORITE_CARETAKER_NOT_FOUND);
        verify(favoriteCaretakerDao, never()).delete(any(), any());
    }

    @Test
    void removeFavorite_found_deletesIt() throws GenericException {
        FavoriteCaretaker favorite = FavoriteCaretaker.builder().familyUserId(FAMILY_USER_ID).caretakerId(CARETAKER_ID).build();
        when(favoriteCaretakerDao.find(FAMILY_USER_ID, CARETAKER_ID)).thenReturn(Optional.of(favorite));

        favoriteCaretakerService.removeFavorite(FAMILY_USER_ID, CARETAKER_ID);

        verify(favoriteCaretakerDao).delete(FAMILY_USER_ID, CARETAKER_ID);
    }

    @Test
    void listFavorites_skipsCaretakersThatNoLongerExist() {
        FavoriteCaretaker fav1 = FavoriteCaretaker.builder().familyUserId(FAMILY_USER_ID).caretakerId(1L).build();
        FavoriteCaretaker fav2 = FavoriteCaretaker.builder().familyUserId(FAMILY_USER_ID).caretakerId(2L).build();
        when(favoriteCaretakerDao.findByFamilyUserId(FAMILY_USER_ID)).thenReturn(List.of(fav1, fav2));
        when(caretakerProfileDao.findById(eq(1L), eq(true))).thenReturn(CaretakerProfile.builder().id(1L).userId(10L).build());
        when(caretakerProfileDao.findById(eq(2L), eq(true))).thenReturn(null); // deleted caretaker profile
        when(userDao.findById(eq(10L), eq(true))).thenReturn(User.builder().id(10L).fullName("Still Here").build());

        List<CaretakerSearchResultDTO> results = favoriteCaretakerService.listFavorites(FAMILY_USER_ID);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(1L);
    }
}
