package dao;

import com.gym.crm.dao.impl.TrainerDAOImpl;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.storage.InMemoryStorage;
import com.gym.crm.storage.TrainerStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerDAOImplTest {
    @Mock
    private InMemoryStorage inMemoryStorage;
    @Mock
    private TrainerStorage trainerStorage;
    @InjectMocks
    private TrainerDAOImpl trainerDAO;

    @BeforeEach
    void setUp() {
        when(inMemoryStorage.getTrainerStorage()).thenReturn(trainerStorage);
        trainerDAO.setStorage(inMemoryStorage);
    }

    @Test
    void testCreate_ShouldCreateTrainerWithGeneratedId() {
        TrainingType specialization = new TrainingType("Yoga");
        Trainer trainer = new Trainer();
        trainer.setFirstName("Jane");
        trainer.setLastName("Smith");
        trainer.setUsername("jane.smith");
        trainer.setPassword("password456");
        trainer.setIsActive(true);
        trainer.setSpecialization(specialization);

        when(trainerStorage.getNextId()).thenReturn(1L);
        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        Trainer result = trainerDAO.create(trainer);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("jane.smith", result.getUsername());
        assertEquals("password456", result.getPassword());
        assertTrue(result.getIsActive());
        assertEquals(specialization, result.getSpecialization());

        verify(trainerStorage).getNextId();
        verify(trainerStorage, times(1)).getTrainers();
    }

    @Test
    void testCreate_ShouldCreateTrainerWithNullSpecialization() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Bob");
        trainer.setLastName("Johnson");
        trainer.setUsername("bob.johnson");
        trainer.setPassword("password789");
        trainer.setIsActive(false);

        when(trainerStorage.getNextId()).thenReturn(2L);
        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        Trainer result = trainerDAO.create(trainer);

        assertNotNull(result);
        assertEquals(2L, result.getUserId());
        assertEquals("Bob", result.getFirstName());
        assertEquals("Johnson", result.getLastName());
        assertEquals("bob.johnson", result.getUsername());
        assertEquals("password789", result.getPassword());
        assertFalse(result.getIsActive());
        assertNull(result.getSpecialization());
    }

    @Test
    void testFindById_ShouldReturnTrainerWhenExists() {
        Long id = 1L;
        Trainer trainer = createSampleTrainer(id);

        java.util.Map<Long, Trainer> trainersMap = new ConcurrentHashMap<>();
        trainersMap.put(id, trainer);
        when(trainerStorage.getTrainers()).thenReturn(trainersMap);

        Optional<Trainer> result = trainerDAO.findById(id);

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
        assertEquals(id, result.get().getUserId());
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Long id = 999L;
        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        Optional<Trainer> result = trainerDAO.findById(id);

        assertFalse(result.isPresent());
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testFindAll_ShouldReturnAllTrainers() {
        Trainer trainer1 = createSampleTrainer(1L);
        Trainer trainer2 = createSampleTrainer(2L);
        trainer2.setFirstName("Bob");
        trainer2.setUsername("bob.smith");
        trainer2.setSpecialization(new TrainingType("Pilates"));

        java.util.Map<Long, Trainer> trainersMap = new ConcurrentHashMap<>();
        trainersMap.put(1L, trainer1);
        trainersMap.put(2L, trainer2);
        when(trainerStorage.getTrainers()).thenReturn(trainersMap);

        List<Trainer> result = trainerDAO.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(trainer1));
        assertTrue(result.contains(trainer2));
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testFindAll_ShouldReturnEmptyListWhenNoTrainers() {
        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        List<Trainer> result = trainerDAO.findAll();

        assertTrue(result.isEmpty());
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testUpdate_ShouldUpdateExistingTrainer() {
        Long id = 1L;
        Trainer existingTrainer = createSampleTrainer(id);

        java.util.Map<Long, Trainer> trainersMap = new ConcurrentHashMap<>();
        trainersMap.put(id, existingTrainer);
        when(trainerStorage.getTrainers()).thenReturn(trainersMap);

        Trainer updatedTrainer = createSampleTrainer(id);
        updatedTrainer.setFirstName("Jane Updated");
        updatedTrainer.setIsActive(false);
        updatedTrainer.setSpecialization(new TrainingType("Pilates"));

        Trainer result = trainerDAO.update(updatedTrainer);

        assertEquals(updatedTrainer, result);
        assertEquals("Jane Updated", result.getFirstName());
        assertFalse(result.getIsActive());
        assertEquals("Pilates", result.getSpecialization().getTrainingTypeName());
        verify(trainerStorage, times(1)).getTrainers();
    }

    @Test
    void testUpdate_ShouldThrowExceptionWhenTrainerNotExists() {
        Trainer trainer = createSampleTrainer(999L);
        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> trainerDAO.update(trainer)
        );

        assertEquals("Trainer not found with ID: 999", exception.getMessage());
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testSetStorage_ShouldInitializeTrainerStorage() {
        InMemoryStorage newStorage = mock(InMemoryStorage.class);
        TrainerStorage newTrainerStorage = mock(TrainerStorage.class);
        when(newStorage.getTrainerStorage()).thenReturn(newTrainerStorage);

        trainerDAO.setStorage(newStorage);

        verify(newStorage).getTrainerStorage();
    }

    private Trainer createSampleTrainer(Long id) {
        Trainer trainer = new Trainer();
        trainer.setUserId(id);
        trainer.setFirstName("Jane");
        trainer.setLastName("Smith");
        trainer.setUsername("jane.smith");
        trainer.setPassword("password456");
        trainer.setIsActive(true);
        trainer.setSpecialization(new TrainingType("Yoga"));
        return trainer;
    }
}
