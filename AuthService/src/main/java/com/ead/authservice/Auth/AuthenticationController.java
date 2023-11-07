package com.ead.authservice.Auth;

import com.ead.authservice.Config.JwtAuthenticationFilter;
import com.ead.authservice.Config.JwtService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ){
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request
    ){
        return ResponseEntity.ok(service.authenticate(request));
    }

    @GetMapping("/validateToken")
    public ResponseEntity<String> validateToken(@NonNull HttpServletRequest request){
        try{
            final String authHeader = request.getHeader("Authorization");
            String jwtToken = authHeader.substring(7);

            // Validate if the token is well-formed
            jwtService.extractUserEmail(jwtToken); // This will throw a MalformedJwtException if the token is not well-formed

            final String userEmail = jwtService.extractUserEmail(jwtToken);
            if(userEmail != null || SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                if(jwtService.isTokenValid(jwtToken,userDetails)){
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
                return ResponseEntity.ok(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().get());
            }
            return ResponseEntity.badRequest().body("Token is invalid");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Token is invalid");
        }
    }

}
