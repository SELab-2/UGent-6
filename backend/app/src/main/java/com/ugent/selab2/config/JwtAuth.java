package com.ugent.selab2.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;

public class JwtAuth {

    public JwtAuth() {

    }

    public void verify(String token){
        DecodedJWT jwt = JWT.decode(token);
        System.out.println(jwt.getKeyId());

        JwkProvider provider = null;
        Jwk jwk =null;
        Algorithm algorithm=null;

        try {
            provider = new UrlJwkProvider(new URL("https://login.microsoftonline.com/62835335-e5c4-4d22-98f2-9d5b65a06d9d/discovery/v2.0/keys"));
            jwk = provider.get(jwt.getKeyId());
            algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            algorithm.verify(jwt);// if the token signature is invalid, the method will throw SignatureVerificationException

            // get the data from the token
            System.out.println(jwt.getClaim("name").asString());
            System.out.println(jwt.getClaim("email").asString());
            System.out.println(jwt.getClaim("groups").asList(String.class));
            // print id
            System.out.println(jwt.getClaim("oid").asString());
            System.out.println(jwt.getId());


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JwkException e) {
            e.printStackTrace();
        }catch(SignatureVerificationException e){

            System.out.println(e.getMessage());

        }
    }

}
