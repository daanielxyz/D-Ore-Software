package com.dore.auth;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    record Me(String name) {
    }

    /** Solo sirve para que el navegador reciba la cookie XSRF-TOKEN. */
    @GetMapping("/csrf")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void csrf() {
    }

    @GetMapping("/me")
    Me me(Principal user) {
        return new Me(user.getName());
    }
}
