package com.eldersphere.services;

import com.eldersphere.dtos.Dashboard.CaretakerDashboardSummaryDTO;
import com.eldersphere.dtos.Dashboard.DashboardSummaryDTO;
import com.eldersphere.dtos.Dashboard.ElderDashboardSummaryDTO;
import com.eldersphere.dtos.Dashboard.FamilyDashboardSummaryDTO;
import com.eldersphere.exceptions.GenericException;

public interface DashboardService {
    DashboardSummaryDTO getSummary();

    FamilyDashboardSummaryDTO getFamilySummary(Long familyUserId);

    CaretakerDashboardSummaryDTO getCaretakerSummary(Long caretakerUserId) throws GenericException;

    ElderDashboardSummaryDTO getElderSummary(Long elderUserId) throws GenericException;
}
