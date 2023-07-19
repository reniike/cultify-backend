package com.semicolon.grincultified.services.farmProjectService;

import com.semicolon.grincultified.data.models.FarmProject;
import com.semicolon.grincultified.data.models.FarmProjectStatus;
import com.semicolon.grincultified.data.models.InvestmentPlan;
import com.semicolon.grincultified.data.repositories.FarmProjectRepository;
import com.semicolon.grincultified.dtos.requests.FarmProjectCreationRequest;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class FarmProjectServiceImplementation implements FarmProjectService {

    private final FarmProjectRepository farmProjectRepository;
    private final ModelMapper modelMapper;

    @Override
    public FarmProject createFarmProject(FarmProjectCreationRequest farmProjectCreationRequest) {
        InvestmentPlan investmentPlan = modelMapper.map(farmProjectCreationRequest, InvestmentPlan.class);
        FarmProject farmProject = modelMapper.map(farmProjectCreationRequest, FarmProject.class);
        farmProject.setInvestmentPlan(investmentPlan);
        FarmProject savedFarmProject = farmProjectRepository.save(farmProject);
        return savedFarmProject;
    }

    @Override
    public List<FarmProject> getAllFarmProjects() {
        List<FarmProject> farmProjects = farmProjectRepository.findAll();
        farmProjects.sort(Comparator.comparing(FarmProject::getUploadedAt).reversed());
        return farmProjects;
    }

    @Override
    public void deleteAll() {
        farmProjectRepository.deleteAll();
    }

    @Override
    public List<FarmProject> getAllFarmProjectsByStatus(FarmProjectStatus status) {
        return farmProjectRepository.findAllByStatus(status);
    }

    @Override
    public void updateProjectAvailability(Long farmProjectId, BigDecimal amount) {
        System.out.println(farmProjectId);
        Optional<FarmProject> foundFarmProject = farmProjectRepository.findById(farmProjectId);
        if (foundFarmProject.isPresent()){
            System.out.println(foundFarmProject.get());
            FarmProject farmProject = foundFarmProject.get();
            BigDecimal unitPrice = farmProject.getInvestmentPlan().getAmountPerUnit();
            int noOfUnits = Integer.parseInt(amount.divide(unitPrice).toPlainString());
            int numberOfUnitInvestedSoFar = farmProject.getNumberOfUnitInvestedSoFar();
            farmProject.setNumberOfUnitInvestedSoFar(numberOfUnitInvestedSoFar+noOfUnits);
            if (farmProject.getNumberOfUnitInvestedSoFar() == farmProject.getInvestmentPlan().getMaximumNumberOfUnit()){
                farmProject.setStatus(FarmProjectStatus.CLOSED);
            }
            farmProjectRepository.save(farmProject);
        }
    }
}
