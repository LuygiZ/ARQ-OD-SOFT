package pt.psoft.lending.services;

import pt.psoft.lending.api.dto.CreateLendingRequest;
import pt.psoft.lending.api.dto.ReturnLendingRequest;
import pt.psoft.lending.model.command.LendingEntity;

/**
 * Command Service Interface for Lending operations (Write Side - CQRS)
 */
public interface LendingCommandService {

    /**
     * Create a new lending
     */
    LendingEntity createLending(CreateLendingRequest request);

    /**
     * Return a lending with comment and rating
     * This is the key method for Student C functionality
     */
    LendingEntity returnLending(String lendingNumber, ReturnLendingRequest request, Long expectedVersion);
}
