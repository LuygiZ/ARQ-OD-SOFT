package pt.psoft.user.services;

import pt.psoft.shared.dto.user.UserDTO;
import pt.psoft.shared.dto.user.CreateUserRequest;

public interface UserService {
    UserDTO create(CreateUserRequest request);
    // Add other methods as needed
}
