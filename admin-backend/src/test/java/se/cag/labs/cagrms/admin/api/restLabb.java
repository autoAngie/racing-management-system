package se.cag.labs.cagrms.admin.api;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.web.servlet.RequestBuilder;

import static io.restassured.RestAssured.given;

public class restLabb {

//    header("Content-Type","application/json").
//    header("Accept","*/*").

    Header acceptHeader = new Header("Accept","*/*");
    Header contentHeader = new Header("Content-Type","application/json");

    @BeforeClass
    public static void setup(){
        RestAssured.baseURI = "http://localhost:10580";
    }

    @Test
    public void testaPing(){
        //curl -X GET --header 'Accept: */*' 'http://localhost:10580/ping'
        Response response = given().
                                header("Accept","*/*").
                            when().
                                get("http://localhost:10580/ping").
                            then().
                                extract().response();

        System.out.println("statuskoden: " + response.statusCode());
    }

    @Test
    public void createUser(){
//        curl -X POST --header 'Content-Type: application/json' --header 'Accept: */*' -d '{ \
//        "displayName": "engle", \
//        "password": "engle", \
//        "userId": "engle" \
//    }' 'http://localhost:10580/users'
        Response response = given().
                                header("Content-Type","application/json").
                                header("Accept","*/*").
                                body("{\"displayName\": \"engle\", \"password\": \"engle\",\"userId\": \"engle2\"}").
                            when().post("http://localhost:10580/users").
                            then().extract().response();
        System.out.println("statuskoden: " + response.statusCode());

    }

    @Test
    public void createUserJson(){
        User user = new User();
        user.displayName = "engle";
        user.userId = "engle9";
        user.password = "engle";

        Response response = given().
                header("Content-Type","application/json").
                header("Accept","*/*").
                body(user).
                when().post("http://localhost:10580/users").
                then().extract().response();

        System.out.println("statuskoden: " + response.statusCode());
    }

    @Test
    public void loginAndGetLogedinUser(){
        User user = new User();
        user.displayName = "engle";
        user.userId = "engle9";
        user.password = "engle";

        Response loginResponse = given().
                                    header(acceptHeader).
                                    header(contentHeader).
                                body(user).
                                    when().post("/login").
                                then().extract().response();
        String token = loginResponse.getHeader("x-authtoken");

        System.out.println("token: " + token);


        Response userResponse = getCall("/users", token);
        Response ping = getCall("/ping");
        System.out.println("Ping statuscode: " + ping.statusCode());
        User usr = getCallWithClass("/users", token, User.class);

        System.out.println("bodyn: " + userResponse.getBody().asString());
        User logedInUser = userResponse.as(User.class);
        System.out.println("User: " + logedInUser.displayName + ", userId: " + logedInUser.userId);
        System.out.println("User: " + usr.displayName + ", userId: " + usr.userId);
        Assert.assertEquals(user.displayName,logedInUser.displayName);
    }

    private Response getCall(String uri){
        return getCall(uri,null);
    }

    private Response getCall(String uri, String token){

        RequestSpecification requestSpecification;
        RequestSpecBuilder builder;

        builder = new RequestSpecBuilder();
        if(token != null){
            builder.addHeader("X-AuthToken", token);
        }
        builder.addHeader(acceptHeader.getName(), acceptHeader.getValue());

        requestSpecification = builder.build();

        Response userResponse = given().
                spec(requestSpecification).
                when().
                get(uri).
                then().
                extract().response();

        return userResponse;
    }

    private <T> T getCallWithClass(String uri, String token, Class<T> returnClass){
        Response userResponse = given().
                header(acceptHeader).
                header("X-AuthToken", token).
                when().
                get(uri).
                then().
                extract().response();

        return userResponse.as(returnClass);
    }

}
