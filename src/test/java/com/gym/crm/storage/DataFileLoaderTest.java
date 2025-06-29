package com.gym.crm.storage;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataFileLoaderTest {
    @Mock
    private TrainingTypeStorage trainingTypeStorage;
    @Mock
    private TraineeStorage traineeStorage;
    @Mock
    private TrainerStorage trainerStorage;
    @Mock
    private TrainingStorage trainingStorage;
    @InjectMocks
    private DataFileLoader dataFileLoader;

    private DataFileLoader spyDataFileLoader;

    @BeforeEach
    void setUp() {
        spyDataFileLoader = spy(dataFileLoader);
    }

    @Test
    void loadTrainingTypesFromFile_ShouldLoadTrainingTypesSuccessfully() throws IOException {
        String fileContent = """
                # Training Types
                [TRAINING_TYPES]
                Cardio
                Strength
                Yoga
                
                [TRAINEES]
                # This section should be ignored
                """;

        Map<String, TrainingType> trainingTypesMap = new ConcurrentHashMap<>();
        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        when(trainingTypeStorage.getTrainingTypes()).thenReturn(trainingTypesMap);
        doReturn(inputStream).when(spyDataFileLoader).getInputStream();

        TrainingTypeStorage result = spyDataFileLoader.loadTrainingTypesFromFile(trainingTypeStorage);

        assertNotNull(result);
        assertEquals(trainingTypeStorage, result);
        assertEquals(3, trainingTypesMap.size());
        assertTrue(trainingTypesMap.containsKey("Cardio"));
        assertTrue(trainingTypesMap.containsKey("Strength"));
        assertTrue(trainingTypesMap.containsKey("Yoga"));

        verify(trainingTypeStorage, times(3)).getTrainingTypes();
    }

    @Test
    void loadTraineesFromFile_ShouldLoadTraineesSuccessfully() throws IOException {
        String fileContent = """
                [TRAINEES]
                John,Doe,john.doe,password123,true,1990-01-15,123 Main St
                Jane,Smith,jane.smith,password456,false,1985-05-20,456 Oak Ave
                Bob,Johnson,bob.johnson,password789,true,1992-12-10
                
                [TRAINERS]
                # This section should be ignored
                """;

        Map<Long, Trainee> traineesMap = new ConcurrentHashMap<>();
        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        when(traineeStorage.getTrainees()).thenReturn(traineesMap);
        when(traineeStorage.getNextId()).thenReturn(1L, 2L, 3L);
        doReturn(inputStream).when(spyDataFileLoader).getInputStream();

        TraineeStorage result = spyDataFileLoader.loadTraineesFromFile(traineeStorage);

        assertNotNull(result);
        assertEquals(traineeStorage, result);
        assertEquals(3, traineesMap.size());

        Trainee johnDoe = traineesMap.get(1L);
        assertNotNull(johnDoe);
        assertEquals("John", johnDoe.getFirstName());
        assertEquals("Doe", johnDoe.getLastName());
        assertEquals("john.doe", johnDoe.getUsername());
        assertEquals("password123", johnDoe.getPassword());
        assertTrue(johnDoe.getIsActive());
        assertEquals(LocalDate.of(1990, 1, 15), johnDoe.getDateOfBirth());
        assertEquals("123 Main St", johnDoe.getAddress());

        Trainee bobJohnson = traineesMap.get(3L);
        assertNotNull(bobJohnson);
        assertEquals("Bob", bobJohnson.getFirstName());
        assertNull(bobJohnson.getAddress());

        verify(traineeStorage, times(3)).getNextId();
        verify(traineeStorage, times(3)).getTrainees();
    }

    @Test
    void loadTrainersFromFile_ShouldLoadTrainersSuccessfully() throws IOException {
        String fileContent = """
                [TRAINERS]
                Mike,Wilson,mike.wilson,password123,true,Cardio
                Sarah,Brown,sarah.brown,password456,false,Strength
                """;

        Map<Long, Trainer> trainersMap = new ConcurrentHashMap<>();
        Map<String, TrainingType> trainingTypesMap = new ConcurrentHashMap<>();
        TrainingType cardioType = new TrainingType("Cardio");
        TrainingType strengthType = new TrainingType("Strength");
        trainingTypesMap.put("Cardio", cardioType);
        trainingTypesMap.put("Strength", strengthType);

        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        when(trainerStorage.getTrainers()).thenReturn(trainersMap);
        when(trainerStorage.getNextId()).thenReturn(1L, 2L);
        when(trainingTypeStorage.getTrainingTypes()).thenReturn(trainingTypesMap);
        doReturn(inputStream).when(spyDataFileLoader).getInputStream();

        TrainerStorage result = spyDataFileLoader.loadTrainersFromFile(trainerStorage, trainingTypeStorage);

        assertNotNull(result);
        assertEquals(trainerStorage, result);
        assertEquals(2, trainersMap.size());

        Trainer mikeWilson = trainersMap.get(1L);
        assertNotNull(mikeWilson);
        assertEquals("Mike", mikeWilson.getFirstName());
        assertEquals("Wilson", mikeWilson.getLastName());
        assertEquals("mike.wilson", mikeWilson.getUsername());
        assertEquals("password123", mikeWilson.getPassword());
        assertTrue(mikeWilson.getIsActive());
        assertEquals(cardioType, mikeWilson.getSpecialization());

        Trainer sarahBrown = trainersMap.get(2L);
        assertNotNull(sarahBrown);
        assertEquals("Sarah", sarahBrown.getFirstName());
        assertEquals("Brown", sarahBrown.getLastName());
        assertEquals(strengthType, sarahBrown.getSpecialization());

        verify(trainerStorage, times(2)).getNextId();
        verify(trainerStorage, times(2)).getTrainers();
        verify(trainingTypeStorage, times(2)).getTrainingTypes();
    }

    @Test
    void loadTrainingsFromFile_ShouldLoadTrainingsSuccessfully() throws IOException {
        String fileContent = """
                [TRAININGS]
                1,2,Morning Workout,Cardio,2024-01-15,60
                3,4,Evening Session,Strength,2024-01-16,45
                """;

        Map<Long, Training> trainingsMap = new ConcurrentHashMap<>();
        Map<String, TrainingType> trainingTypesMap = new ConcurrentHashMap<>();
        TrainingType cardioType = new TrainingType("Cardio");
        TrainingType strengthType = new TrainingType("Strength");
        trainingTypesMap.put("Cardio", cardioType);
        trainingTypesMap.put("Strength", strengthType);

        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        when(trainingStorage.getTrainings()).thenReturn(trainingsMap);
        when(trainingStorage.getNextId()).thenReturn(1L, 2L);
        when(trainingTypeStorage.getTrainingTypes()).thenReturn(trainingTypesMap);
        doReturn(inputStream).when(spyDataFileLoader).getInputStream();

        TrainingStorage result = spyDataFileLoader.loadTrainingsFromFile(trainingStorage, trainingTypeStorage);

        assertNotNull(result);
        assertEquals(trainingStorage, result);
        assertEquals(2, trainingsMap.size());

        Training morningWorkout = trainingsMap.get(1L);
        assertNotNull(morningWorkout);
        assertEquals(1L, morningWorkout.getTraineeId());
        assertEquals(2L, morningWorkout.getTrainerId());
        assertEquals("Morning Workout", morningWorkout.getTrainingName());
        assertEquals(cardioType, morningWorkout.getTrainingType());
        assertEquals(LocalDate.of(2024, 1, 15), morningWorkout.getTrainingDate());
        assertEquals(60, morningWorkout.getDuration());

        Training eveningSession = trainingsMap.get(2L);
        assertNotNull(eveningSession);
        assertEquals(3L, eveningSession.getTraineeId());
        assertEquals(4L, eveningSession.getTrainerId());
        assertEquals("Evening Session", eveningSession.getTrainingName());
        assertEquals(strengthType, eveningSession.getTrainingType());
        assertEquals(LocalDate.of(2024, 1, 16), eveningSession.getTrainingDate());
        assertEquals(45, eveningSession.getDuration());

        verify(trainingStorage, times(2)).getNextId();
        verify(trainingStorage, times(2)).getTrainings();
        verify(trainingTypeStorage, times(2)).getTrainingTypes();
    }

    @Test
    void loadTrainingTypesFromFile_ShouldSkipEmptyLinesAndComments() throws IOException {
        String fileContent = """
                # This is a comment
                [TRAINING_TYPES]
                # Another comment
                Cardio
                
                # Empty line above
                Strength
                
                [TRAINEES]
                # This section should be ignored
                """;

        Map<String, TrainingType> trainingTypesMap = new ConcurrentHashMap<>();
        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        when(trainingTypeStorage.getTrainingTypes()).thenReturn(trainingTypesMap);
        doReturn(inputStream).when(spyDataFileLoader).getInputStream();

        TrainingTypeStorage result = spyDataFileLoader.loadTrainingTypesFromFile(trainingTypeStorage);

        assertNotNull(result);
        assertEquals(2, trainingTypesMap.size());
        assertTrue(trainingTypesMap.containsKey("Cardio"));
        assertTrue(trainingTypesMap.containsKey("Strength"));

        verify(trainingTypeStorage, times(2)).getTrainingTypes();
    }

    @Test
    void loadTraineesFromFile_ShouldSkipInvalidLines() throws IOException {
        String fileContent = """
                [TRAINEES]
                John,Doe,john.doe,password123,true,1990-01-15,123 Main St
                Invalid,Line,Only,Three,Fields
                Jane,Smith,jane.smith,password456,false,1985-05-20
                """;

        Map<Long, Trainee> traineesMap = new ConcurrentHashMap<>();
        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        when(traineeStorage.getTrainees()).thenReturn(traineesMap);
        when(traineeStorage.getNextId()).thenReturn(1L, 2L);
        doReturn(inputStream).when(spyDataFileLoader).getInputStream();

        TraineeStorage result = spyDataFileLoader.loadTraineesFromFile(traineeStorage);

        assertNotNull(result);
        assertEquals(2, traineesMap.size());

        Trainee johnDoe = traineesMap.get(1L);
        assertNotNull(johnDoe);
        assertEquals("John", johnDoe.getFirstName());

        Trainee janeSmith = traineesMap.get(2L);
        assertNotNull(janeSmith);
        assertEquals("Jane", janeSmith.getFirstName());

        verify(traineeStorage, times(3)).getNextId();
        verify(traineeStorage, times(2)).getTrainees();
    }

    @Test
    void loadTrainersFromFile_ShouldSkipInvalidLines() throws IOException {
        String fileContent = """
                [TRAINERS]
                Mike,Wilson,mike.wilson,password123,true,Cardio
                Invalid,Line
                Sarah,Brown,sarah.brown,password456,false,Strength
                """;

        Map<Long, Trainer> trainersMap = new ConcurrentHashMap<>();
        Map<String, TrainingType> trainingTypesMap = new ConcurrentHashMap<>();
        TrainingType cardioType = new TrainingType("Cardio");
        TrainingType strengthType = new TrainingType("Strength");
        trainingTypesMap.put("Cardio", cardioType);
        trainingTypesMap.put("Strength", strengthType);

        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        when(trainerStorage.getTrainers()).thenReturn(trainersMap);
        when(trainerStorage.getNextId()).thenReturn(1L, 2L);
        when(trainingTypeStorage.getTrainingTypes()).thenReturn(trainingTypesMap);
        doReturn(inputStream).when(spyDataFileLoader).getInputStream();

        TrainerStorage result = spyDataFileLoader.loadTrainersFromFile(trainerStorage, trainingTypeStorage);

        assertNotNull(result);
        assertEquals(2, trainersMap.size());

        verify(trainerStorage, times(3)).getNextId();
        verify(trainerStorage, times(2)).getTrainers();
    }

    @Test
    void loadTrainingsFromFile_ShouldSkipInvalidLines() throws IOException {
        String fileContent = """
                [TRAININGS]
                1,2,Morning Workout,Cardio,2024-01-15,60
                Invalid,Line,Only
                3,4,Evening Session,Strength,2024-01-16,45
                """;

        Map<Long, Training> trainingsMap = new ConcurrentHashMap<>();
        Map<String, TrainingType> trainingTypesMap = new ConcurrentHashMap<>();
        TrainingType cardioType = new TrainingType("Cardio");
        TrainingType strengthType = new TrainingType("Strength");
        trainingTypesMap.put("Cardio", cardioType);
        trainingTypesMap.put("Strength", strengthType);

        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        when(trainingStorage.getTrainings()).thenReturn(trainingsMap);
        when(trainingStorage.getNextId()).thenReturn(1L, 2L);
        when(trainingTypeStorage.getTrainingTypes()).thenReturn(trainingTypesMap);
        doReturn(inputStream).when(spyDataFileLoader).getInputStream();

        TrainingStorage result = spyDataFileLoader.loadTrainingsFromFile(trainingStorage, trainingTypeStorage);

        assertNotNull(result);
        assertEquals(2, trainingsMap.size());

        verify(trainingStorage, times(3)).getNextId();
        verify(trainingStorage, times(2)).getTrainings();
    }

    @Test
    void loadFromFile_ShouldHandleUnknownSections() throws IOException {
        String fileContent = """
                [UNKNOWN_SECTION]
                Some,Data,Here
                
                [TRAINING_TYPES]
                Cardio
                """;

        Map<String, TrainingType> trainingTypesMap = new ConcurrentHashMap<>();
        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        when(trainingTypeStorage.getTrainingTypes()).thenReturn(trainingTypesMap);
        doReturn(inputStream).when(spyDataFileLoader).getInputStream();

        TrainingTypeStorage result = spyDataFileLoader.loadTrainingTypesFromFile(trainingTypeStorage);

        assertNotNull(result);
        assertEquals(1, trainingTypesMap.size());
        assertTrue(trainingTypesMap.containsKey("Cardio"));
    }

    @Test
    void loadFromFile_ShouldHandleMultipleSections() throws IOException {
        String fileContent = """
                [TRAINING_TYPES]
                Cardio
                Strength
                
                [TRAINEES]
                John,Doe,john.doe,password123,true,1990-01-15,123 Main St
                
                [TRAINING_TYPES]
                Yoga
                """;

        Map<String, TrainingType> trainingTypesMap = new ConcurrentHashMap<>();
        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        when(trainingTypeStorage.getTrainingTypes()).thenReturn(trainingTypesMap);
        doReturn(inputStream).when(spyDataFileLoader).getInputStream();

        TrainingTypeStorage result = spyDataFileLoader.loadTrainingTypesFromFile(trainingTypeStorage);

        assertNotNull(result);
        assertEquals(3, trainingTypesMap.size());
        assertTrue(trainingTypesMap.containsKey("Cardio"));
        assertTrue(trainingTypesMap.containsKey("Strength"));
        assertTrue(trainingTypesMap.containsKey("Yoga"));
    }
}
