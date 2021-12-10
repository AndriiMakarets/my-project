package pack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;

//@SpringBootApplication
//@EnableCaching
public class HTTPServer {

    private static final byte[] KEY = "WpXq7uTs4ViJaxQWpXq7uTs4ViJaxQWpXq7uTs4ViJaxQWpXq7uTs4ViJaxQWpXq7uTs4ViJaxQWpXq7uTs4ViJaxQ".getBytes(StandardCharsets.UTF_8);

    public static void main(String[] args) throws IOException, SQLException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        {
            ObjectMapper objectMapper = new ObjectMapper();
            SQL sqlDb = new SQL();
            sqlDb.create("Users");
            //sqlDb.insertUser(new User("Andri", "Fisunia12"));
            //sqlDb.insertProduct(new Product("prod1", 14.5, 4.0));
            //sqlDb.insertProduct(new Product("prod2", 25.0, 6.0));
            server.start();


            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    exchange.getResponseHeaders().add("Content-Type", "aplication/json");
                    exchange.sendResponseHeaders(200, "{\"status\": \"OK\"}".getBytes(StandardCharsets.UTF_8).length);
                    exchange.getResponseBody().write("{\"status\": \"OK\"}".getBytes(StandardCharsets.UTF_8));

                    exchange.close();
                }
            });

          /*  //login
            server.createContext("/login", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if (exchange.getRequestMethod().equals("POST")) {
                        RequestUser user = objectMapper.readValue(exchange.getRequestBody(), RequestUser.class);
                        User DbUser = sqlDb.readUserByLogin(user.getLogin());
                        String password = DbUser.getPassword();
                        System.out.println(encryptPassword(DbUser.getPassword()));
                        System.out.println(user.getPassword());

                        if (DbUser != null) {
                            if (encryptPassword(DbUser.getPassword()).equals(user.getPassword())) {
                                exchange.getResponseHeaders().add("Authorization", createJWT(DbUser.getLogin(), 1000000000));
                                exchange.sendResponseHeaders(200, 0);
                            } else {
                                exchange.sendResponseHeaders(401, 0);
                            }
                        } else {
                            exchange.sendResponseHeaders(401, 0);
                        }
                        exchange.sendResponseHeaders(200, 0);
                    } else {
                        exchange.sendResponseHeaders(405, 0);
                    }
                    exchange.close();
                }
            });*/


            HttpContext context = server.createContext("/api/good", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                   URI ur = exchange.getRequestURI();

                    exchange.getResponseHeaders().add("Content-Type", "aplication/json");

                    //GET
                    if (exchange.getRequestMethod().equals("GET")) {
                        String str =(ur.toString());
                        str =str.split("/")[3];
                        System.out.println(str);
                      Product p= sqlDb.readProductByID(str);
                        if(p==null)exchange.sendResponseHeaders(404, 0);
                        else {
                            String name = p.getName();
                            Double price = p.getPrice();
                            Double amount = p.getAmount();
                            String resp = String.format("{\"name\": \"%s\"\n\"price\": \"%s\"\n\"amount\": \"%s\"}", name, price, amount);
                            exchange.sendResponseHeaders(200, resp.getBytes(StandardCharsets.UTF_8).length);

                            exchange.getResponseBody().write(resp.getBytes(StandardCharsets.UTF_8));
                        }
                        exchange.close();
                    }

                    //PUT
                    else  if (exchange.getRequestMethod().equals("PUT")) {
                        Gson g = new Gson();
                        Product product = g.fromJson(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8), Product.class);
                        System.out.println(product);
                        if(product.getAmount()<0|| product.getPrice()<0){
                            exchange.sendResponseHeaders(409, 0);
                        }
                        try {
                            product= sqlDb.insertProduct();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        String resp = String.format("{\"id\": \"%s\"}",product.getId());
                        exchange.sendResponseHeaders(201, resp .getBytes(StandardCharsets.UTF_8).length );
                        exchange.getResponseBody().write(resp.getBytes(StandardCharsets.UTF_8));
                        System.out.println(sqlDb.readProductByID("100"));
                        exchange.close();
                    }

                    //POST
                    else  if (exchange.getRequestMethod().equals("POST")) {
                        //System.out.println(sqlDb.readProductByID("1000"));
                        String str =(ur.toString());
                        str =str.split("/")[3];
                        Gson g = new Gson();
                        Product product = g.fromJson(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8), Product.class);
                        if(product.getAmount()<0|| product.getPrice()<0){
                            exchange.sendResponseHeaders(409, 0);
                        }
                        try {
                            sqlDb.updateProductByID(Integer.parseInt(str), product.getName(), product.getPrice(), product.getAmount());
                            if(sqlDb.readProductByID((str))==null)exchange.sendResponseHeaders(404, 0);
                            exchange.sendResponseHeaders(204,-1);
                        } catch (SQLException throwables) {
                            exchange.sendResponseHeaders(404, 0);
                        }

                        //System.out.println(sqlDb.readProductByID("1000"));
                        exchange.close();

                    }

                    //DELETE
                    else if(exchange.getRequestMethod().equals("DELETE")){
                        String str =(ur.toString());
                        str =str.split("/")[3];
                        if(sqlDb.readProductByID((str))==null)exchange.sendResponseHeaders(404, 0);
                        sqlDb.delete(Integer.parseInt(str));
                        exchange.sendResponseHeaders(204,-1);
                        exchange.close();
                    }

                }
            });


           /* context.setAuthenticator(new Authenticator() {
                @Override
                public Result authenticate(HttpExchange exch) {
                    String token = exch.getRequestHeaders().getFirst("Authorization");
                    if (token != null) {
                        String login = parseJWTLogin(token);
                        if(login.equals("FALE"))  return new Failure(403);
                        User user = sqlDb.readUserByLogin(login);
                        if (user != null) {
                            return new Success(new HttpPrincipal(login, " ddd"));
                        } else return new Failure(401);
                    }
                    return new Failure(403);
                }
            });*/
        }
    }

    //Sample method to construct a JWT
    private static String createJWT(String login, long ttlMillis) {

        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
        Key signingKey = new SecretKeySpec(KEY, signatureAlgorithm.getJcaName());

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setSubject(login)
                .signWith(signatureAlgorithm, signingKey);

        //if it has been specified, let's add the expiration
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    //Sample method to validate and read the JWT
    private static String parseJWTLogin(String jwt) {

        //This line will throw an exception if it is not a signed JWS (as expected)
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(new SecretKeySpec(KEY, SignatureAlgorithm.HS256.getJcaName()))
                    .parseClaimsJws(jwt).getBody();

            return claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            return "FALE";
        }

    }


    public static String encryptPassword(final String password) {

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(password.getBytes(), 0, password.length());
            String secured = new BigInteger(1, digest.digest()).toString(16);
            return secured;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
