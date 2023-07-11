package com.semicolon.grincultified.services.farmProjectService;

import com.semicolon.grincultified.data.models.FarmProject;
import com.semicolon.grincultified.dtos.requests.FarmProjectCreationRequest;

import java.util.List;

public interface FarmProjectService {
    FarmProject createFarmProject(FarmProjectCreationRequest farmProjectCreationRequest);

    List<FarmProject> getAllFarmProjects();

    void deleteAll();
}
