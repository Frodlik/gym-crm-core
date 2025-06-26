package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dto.trainee.TraineeCreateRequest;
import com.gym.crm.dto.trainee.TraineeResponse;
import com.gym.crm.dto.trainee.TraineeUpdateRequest;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.service.TraineeService;
import com.gym.crm.util.UserCredentialsGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeServiceImpl implements TraineeService {
    private TraineeDAO traineeDAO;
    private UserCredentialsGenerator userCredentialsGenerator;
    private TraineeMapper traineeMapper;

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setUserCredentialsGenerator(UserCredentialsGenerator userCredentialsGenerator) {
        this.userCredentialsGenerator = userCredentialsGenerator;
    }

    @Autowired
    public void setTraineeMapper(TraineeMapper traineeMapper) {
        this.traineeMapper = traineeMapper;
    }

    @Override
    public TraineeResponse create(TraineeCreateRequest request) {
        Trainee trainee = traineeMapper.toEntity(request);

        List<String> existingUsernames = traineeDAO.findAll().stream()
                .map(Trainee::getUsername)
                .toList();

        String username = userCredentialsGenerator.generateUsername(
                trainee.getFirstName(), trainee.getLastName(), existingUsernames);
        String password = userCredentialsGenerator.generatePassword();

        trainee.setPassword(password);
        trainee.setUsername(username);

        Trainee saved = traineeDAO.create(trainee);

        return traineeMapper.toResponse(saved);
    }

    @Override
    public Optional<TraineeResponse> findById(Long id) {
        return traineeDAO.findById(id)
                .map(traineeMapper::toResponse);
    }

    @Override
    public TraineeResponse update(TraineeUpdateRequest request) {
        Optional<Trainee> existingTrainee = traineeDAO.findById(request.getId());
        if (existingTrainee.isEmpty()) {
            throw new CoreServiceException("Trainee not found with id: " + request.getId());
        }

        Trainee trainee = existingTrainee.get();

        trainee.setFirstName(request.getFirstName());
        trainee.setLastName(request.getLastName());
        trainee.setIsActive(request.getIsActive());
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());

        Trainee updatedTrainee = traineeDAO.update(trainee);

        return traineeMapper.toResponse(updatedTrainee);
    }

    @Override
    public void delete(Long id) {
        traineeDAO.delete(id);
    }
}
