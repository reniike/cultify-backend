package com.semicolon.grincultified.controllers;

import com.semicolon.grincultified.data.models.FarmProject;
import com.semicolon.grincultified.dtos.requests.FarmProjectCreationRequest;
import com.semicolon.grincultified.dtos.responses.FarmProjectResponse;
import com.semicolon.grincultified.services.farmProjectService.FarmProjectService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController("/semicolon/cultify/v1/api/farmProject")
public class FarmProjectController {

    private final FarmProjectService farmProjectService;

    @PostMapping("/createFarmProject")
    public ResponseEntity<FarmProject> createFarmProject(@RequestBody FarmProjectCreationRequest farmProjectCreationRequest){
        return ResponseEntity.ok().body(farmProjectService.createFarmProject(farmProjectCreationRequest));
    }

    @GetMapping("/getAllFarmProjects")
    public ResponseEntity<List<FarmProject>> getAllFarmProjects(){
        return ResponseEntity.ok().body(farmProjectService.getAllFarmProjects());
    }

}