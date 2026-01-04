package pt.psoft.user.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pt.psoft.shared.dto.user.CreateUserRequest;
import pt.psoft.shared.dto.user.UserDTO;
import pt.psoft.user.services.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@RequestBody CreateUserRequest request) {
        return userService.create(request);
    }
}
