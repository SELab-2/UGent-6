package com.ugent.pidgeon.auth;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * This class extends OncePerRequestFilter to provide a filter that decodes and verifies JWT tokens.
 * It uses JwkProvider to fetch the public key for verification.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // JwkProvider instance to fetch the public key for JWT verification
    private JwkProvider provider;

    /**
     * Constructor for JwtAuthenticationFilter.
     * It initializes the JwkProvider with the URL of the public key.
     * @param tenantId the tenantId used to construct the URL of the public key
     */
    public JwtAuthenticationFilter(String tenantId) {
        try {
            logger.info("tenantId: " + tenantId);
            provider = new UrlJwkProvider(new URL("https://login.microsoftonline.com/"+tenantId+"/discovery/v2.0/keys"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method is called for every request to filter requests based on JWT token.
     * It decodes the JWT token from the Authorization header, verifies it, and sets the authentication in the SecurityContext.
     * If the JWT token is not present or invalid, it sets the response status to UNAUTHORIZED.
     * @param request HttpServletRequest that is being processed
     * @param response HttpServletResponse that is being created
     * @param filterChain FilterChain for calling the next filter
     * @throws jakarta.servlet.ServletException in case of errors
     * @throws IOException in case of I/O errors
     */
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.info(request.getRequestURL().toString());

        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);

            DecodedJWT jwt = JWT.decode(token);
            Jwk jwk;
            Algorithm algorithm;

            try {
                jwk = provider.get(jwt.getKeyId());
                algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
                algorithm.verify(jwt);// if the token signature is invalid, the method will throw SignatureVerificationException


                // get the data from the token
                String displayName;
                String firstName;
                String lastName;
                String email;
                String oid;

                String version = jwt.getClaim("ver").asString();

                if (version.startsWith("1.0")) {
                    displayName = jwt.getClaim("name").asString();
                    firstName = jwt.getClaim("given_name").asString();
                    lastName = jwt.getClaim("family_name").asString();
                    email = jwt.getClaim("unique_name").asString();
                    oid = jwt.getClaim("oid").asString();
                } else if (version.startsWith("2.0")) {
                    displayName = jwt.getClaim("name").asString();
                    lastName = jwt.getClaim("surname").asString();
                    firstName = displayName.replace(lastName, "").strip();
                    email = jwt.getClaim("mail").asString();
                    oid = jwt.getClaim("oid").asString();
                } else {
                    throw new JwkException("Invalid OAuth version");
                }
                // print full object
                // logger.info(jwt.getClaims());



                User user = new User(displayName, firstName,lastName, email, oid);

                Auth authUser = new Auth(user, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(authUser);
                filterChain.doFilter(request, response);
            } catch (JwkException e) {
                e.printStackTrace();
                response.setStatus(HttpStatus.BAD_REQUEST.value());
            }catch(SignatureVerificationException e){

                System.out.println(e.getMessage());

                response.setStatus(HttpStatus.UNAUTHORIZED.value()); // Forbidden
            }
        } else {
            logger.warn("No token found!");
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // Unauthorized
        }

    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}