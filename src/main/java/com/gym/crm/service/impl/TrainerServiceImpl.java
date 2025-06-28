package com.gym.crm.service.impl;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainer;
import com.gym.crm.service.TrainerService;
import com.gym.crm.util.UserCredentialsGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerServiceImpl implements TrainerService {
    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private TrainerDAO trainerDAO;
    private UserCredentialsGenerator userCredentialsGenerator;
    private TrainerMapper trainerMapper;

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Autowired
    public void setUserCredentialsGenerator(UserCredentialsGenerator userCredentialsGenerator) {
        this.userCredentialsGenerator = userCredentialsGenerator;
    }

    @Autowired
    public void setTrainerMapper(TrainerMapper trainerMapper) {
        this.trainerMapper = trainerMapper;
    }

    @Override
    public TrainerResponse create(TrainerCreateRequest request) {
        logger.debug("Creating trainer: {} {}", request.getFirstName(), request.getLastName());

        Trainer trainer = trainerMapper.toEntity(request);

        List<String> existingUsernames = trainerDAO.findAll().stream()
                .map(Trainer::getUsername)
                .toList();

        String username = userCredentialsGenerator.generateUsername(
                trainer.getFirstName(), trainer.getLastName(), existingUsernames);
        String password = userCredentialsGenerator.generatePassword();

        trainer.setUsername(username);
        trainer.setPassword(password);

        Trainer saved = trainerDAO.create(trainer);

        logger.info("Successfully created trainer with ID: {} and username: {}", saved.getUserId(), saved.getUsername());

        return trainerMapper.toResponse(saved);
    }

    @Override
    public Optional<TrainerResponse> findById(Long id) {
        logger.debug("Finding trainer by ID: {}", id);

        return trainerDAO.findById(id)
                .map(trainerMapper::toResponse);
    }

    @Override
    public TrainerResponse update(TrainerUpdateRequest request) {
        logger.debug("Updating trainer with ID: {}", request.getId());

        Optional<Trainer> existingTrainer = trainerDAO.findById(request.getId());
        if (existingTrainer.isEmpty()) {
            throw new CoreServiceException("Trainer not found with id: " + request.getId());
        }

        Trainer trainer = existingTrainer.get();

        trainer.setFirstName(request.getFirstName());
        trainer.setLastName(request.getLastName());
        trainer.setIsActive(request.getIsActive());
        trainer.setSpecialization(request.getSpecialization());

        Trainer updatedTrainer = trainerDAO.update(trainer);

        logger.info("Successfully updated trainer with ID: {}", request.getId());

        return trainerMapper.toResponse(updatedTrainer);
    }
}
