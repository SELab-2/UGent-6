package com.ugent.pidgeon.config;

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

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private JwkProvider provider;



    public JwtAuthenticationFilter(String tenantId)
    {
        try {
            logger.info("tenantId: " + tenantId);
            provider = new UrlJwkProvider(new URL("https://login.microsoftonline.com/"+tenantId+"/discovery/v2.0/keys"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {
        logger.info(request.getRequestURL().toString());

        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);

            DecodedJWT jwt = JWT.decode(token);
            Jwk jwk =null;
            Algorithm algorithm=null;

            try {
                jwk = provider.get(jwt.getKeyId());
                algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
                algorithm.verify(jwt);// if the token signature is invalid, the method will throw SignatureVerificationException

                // get the data from the token
                String displayName = jwt.getClaim("name").asString();
                String firstName = jwt.getClaim("given_name").asString();
                String lastName = jwt.getClaim("family_name").asString();
                String email = jwt.getClaim("unique_name").asString();
                List<String> groups = jwt.getClaim("groups").asList(String.class);
                String oid = jwt.getClaim("oid").asString();

                // print full object
                //logger.info(jwt.getClaims());


                User user = new User(displayName,firstName,lastName, email, groups, oid);

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