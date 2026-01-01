package pt.psoft.lending.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pt.psoft.lending.api.dto.CreateLendingRequest;
import pt.psoft.lending.api.dto.ReturnLendingRequest;
import pt.psoft.lending.messaging.LendingEventPublisher;
import pt.psoft.lending.model.command.LendingEntity;
import pt.psoft.lending.repositories.jpa.LendingRepository;
import pt.psoft.shared.events.lending.LendingCreatedEvent;
import pt.psoft.shared.events.lending.LendingReturnedEvent;
import pt.psoft.shared.exceptions.BusinessException;
import pt.psoft.shared.exceptions.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LendingCommandService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LendingCommandService Tests")
class LendingCommandServiceTest {

    @Mock
    private LendingRepository lendingRepository;

    @Mock
    private LendingEventPublisher lendingEventPublisher;

    @InjectMocks
    private LendingCommandServiceImpl lendingCommandService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(lendingCommandService, "lendingDurationInDays", 14);
        ReflectionTestUtils.setField(lendingCommandService, "fineValuePerDayInCents", 50);
        ReflectionTestUtils.setField(lendingCommandService, "maxOutstandingBooks", 3);
    }

    @Test
    @DisplayName("Should create lending successfully")
    void shouldCreateLendingSuccessfully() {
        CreateLendingRequest request = new CreateLendingRequest("9782826012092", "2024/1");

        when(lendingRepository.findOutstandingByReaderNumber("2024/1")).thenReturn(Collections.emptyList());
        when(lendingRepository.countByYear(any())).thenReturn(0);
        when(lendingRepository.save(any(LendingEntity.class))).thenAnswer(i -> i.getArgument(0));

        LendingEntity result = lendingCommandService.createLending(request);

        assertNotNull(result);
        assertEquals("9782826012092", result.getBookId());
        assertEquals("2024/1", result.getReaderNumber());
        verify(lendingEventPublisher).publishLendingCreated(any(LendingCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when reader has max outstanding books")
    void shouldThrowExceptionWhenReaderHasMaxOutstandingBooks() {
        CreateLendingRequest request = new CreateLendingRequest("9782826012092", "2024/1");

        List<LendingEntity> outstandingLendings = List.of(
                new LendingEntity("book1", 1L, "2024/1", 1, 14, 50),
                new LendingEntity("book2", 1L, "2024/1", 2, 14, 50),
                new LendingEntity("book3", 1L, "2024/1", 3, 14, 50)
        );

        when(lendingRepository.findOutstandingByReaderNumber("2024/1")).thenReturn(outstandingLendings);

        assertThrows(BusinessException.class, () -> lendingCommandService.createLending(request));
        verify(lendingEventPublisher, never()).publishLendingCreated(any());
    }

    @Test
    @DisplayName("Should return lending with rating and comment")
    void shouldReturnLendingWithRatingAndComment() {
        ReturnLendingRequest request = new ReturnLendingRequest("Great book!", 8);
        LendingEntity lending = new LendingEntity("9782826012092", 1L, "2024/1", 1, 14, 50);

        when(lendingRepository.findByLendingNumber(anyInt(), anyInt())).thenReturn(Optional.of(lending));
        when(lendingRepository.save(any(LendingEntity.class))).thenAnswer(i -> i.getArgument(0));

        LendingEntity result = lendingCommandService.returnLending("2024/1", request, 0L);

        assertNotNull(result.getReturnedDate());
        assertEquals("Great book!", result.getComment());
        assertEquals(8, result.getRating());
        verify(lendingEventPublisher).publishLendingReturned(any(LendingReturnedEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when lending not found")
    void shouldThrowExceptionWhenLendingNotFound() {
        ReturnLendingRequest request = new ReturnLendingRequest("Comment", 5);

        when(lendingRepository.findByLendingNumber(anyInt(), anyInt())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                lendingCommandService.returnLending("2024/999", request, 0L));
        verify(lendingEventPublisher, never()).publishLendingReturned(any());
    }

    @Test
    @DisplayName("Should throw exception when lending already returned")
    void shouldThrowExceptionWhenLendingAlreadyReturned() {
        ReturnLendingRequest request = new ReturnLendingRequest("Comment", 5);
        LendingEntity lending = new LendingEntity("9782826012092", 1L, "2024/1", 1, 14, 50);
        lending.setReturned(0L, "First return", 5);

        when(lendingRepository.findByLendingNumber(anyInt(), anyInt())).thenReturn(Optional.of(lending));

        assertThrows(BusinessException.class, () ->
                lendingCommandService.returnLending("2024/1", request, 1L));
    }
}
