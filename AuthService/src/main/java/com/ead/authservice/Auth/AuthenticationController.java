package com.ead.authservice.Auth;

import com.ead.authservice.Config.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ){
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request
    ){
        return ResponseEntity.ok(service.authenticate(request));
    }

    @GetMapping("/validateToken")
    public ResponseEntity<String> validateToken(@NonNull HttpServletRequest request){
        return jwtService.validateToken(request);
    }

    @GetMapping("/checkEmailExists/{email}")
    public ResponseEntity<?> checkEmailExists(@PathVariable String email){
        return jwtService.checkEmailExists(email);
    }

}
