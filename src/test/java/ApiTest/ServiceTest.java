package ApiTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import io.restassured.RestAssured;
//import static io.restassured.RestAssured.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;


public class ServiceTest {
	private String token;
	private HashMap<String,Integer> userIds = new HashMap<String,Integer>();
	private SoftAssert softAssert = new SoftAssert();

	@Test(priority = 1)
	/**Tests login end point**/
	public void testLoginEndPoint() {
		String endpoint = "http://qa-takehome.dev.aetion.com:4440/login";
		int responseCode;
				Response response = RestAssured.given()
				.contentType("application/json")				
				.body("{\"username\": \"rellison\", \"password\": \"rellison123\"}")
				.when()
				.post(endpoint);
		
		responseCode = response.getStatusCode();
		Assert.assertEquals(responseCode, 200);
		
		String jsonResponse = response.asString();
		JsonPath jp = new JsonPath(jsonResponse);
		
		token = jp.get("token");				
		Assert.assertEquals(36, token.length());		
		
	}
	
	@Test(priority = 2)
	/**Tests User End point by creating and getting users**/
	public void testCreateUsersEndpoint(){
		String endpoint = "http://qa-takehome.dev.aetion.com:4440/user/";
		String email; 
		String firstName;
		String lastName;
		String age;
		int id;
		String fileLocation = "src/test/resources/userList.txt";
		StringTokenizer st;
		int postRespCode;
		int getRespCode;
		String jsonResponse;
		JsonPath jp; 
		
		try {
			Scanner in = new Scanner(new FileInputStream(fileLocation));
						
			while(in.hasNext()) {
				st = new StringTokenizer(in.nextLine());
				email = st.nextToken();
				firstName = st.nextToken();
				lastName = st.nextToken();
				age = st.nextToken();
				
				Response postResponse = RestAssured.given()
						.contentType("application/json")
						.header("X-Auth-Token", token)
						.body("{\"email\": \"" + email + "\", \"first_name\": \"" + firstName + "\", \"last_name\": \"" + lastName + "\", \"age\": \"" + age + "\"}")
						.when()
						.post(endpoint);

				postRespCode = postResponse.getStatusCode();
				Assert.assertEquals(postRespCode, 200);
				
				if(postRespCode == 200) {
					jsonResponse = postResponse.asString();
					jp = new JsonPath(jsonResponse);
					id = jp.get("id");					
					userIds.put(email, id);
					
					Response getResponse = RestAssured.given()
							.header("X-Auth-Token", token)
							.get(endpoint + id );
					getRespCode = getResponse.getStatusCode();
					
					if(getRespCode == 200) {
						jsonResponse = getResponse.asString();					
						jp = new JsonPath(jsonResponse);
						
						Assert.assertEquals(jp.get("id"), id);
						Assert.assertEquals(jp.get("email"), email);
						Assert.assertEquals(jp.get("first_name"), firstName);
						Assert.assertEquals(jp.get("last_name"), lastName);
						Assert.assertEquals(jp.get("age"), Integer.parseInt(age));
					}
										
				}				

			}
			in.close();
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Assert.fail("Could not locate userList.txt");
		}

		
	}
	
	@Test(priority = 3)
	/**Tests update End point**/
	public void testUpdateIdEndpoint(){
		String endpoint = "http://qa-takehome.dev.aetion.com:4440/user/";
		String fileLocation = "src/test/resources/updateList.txt";
		int id; 		
		String email;
		String firstName = "";
		String lastName = "";
		int age = 0;		
		String itemToChange;
		String oldValue;
		String newValue;		
		String jsonResponse;
		JsonPath jp;
		int putRespCode;
		int getRespCode;
		StringTokenizer st;
		
		
		try {
			Scanner in = new Scanner(new FileInputStream(fileLocation));
						
			while(in.hasNext()) {
				st = new StringTokenizer(in.nextLine());
				email = st.nextToken();
				itemToChange = st.nextToken();
				oldValue = st.nextToken();
				newValue = st.nextToken();
				System.out.println(newValue);
				id = userIds.get(email);
				
				
				Response getResponse = RestAssured.given()
						.header("X-Auth-Token", token)
						.get(endpoint + id);
				getRespCode = getResponse.getStatusCode();
				Assert.assertEquals(getRespCode, 200);
				
				if(getRespCode == 200) {
					jsonResponse = getResponse.asString();					
					jp = new JsonPath(jsonResponse);					
					
					firstName = jp.get("first_name");
					lastName = jp.get("last_name");
					age = jp.get("age");
				}
				
				
				
				if(itemToChange.equals("email")) {
					userIds.remove(email);
					userIds.put(newValue, id);
					email = newValue;					
				}
				else if(itemToChange.equals("first_name")) {
					firstName = newValue;					
				}
				else if(itemToChange.equals("last_name")) {
					lastName = newValue;				 	
				}
				else if(itemToChange.equals("age")) {
					age = Integer.parseInt(newValue);					
				}
				
				
				
				Response putResponse = RestAssured.given()
						.contentType("application/json")
						.header("X-Auth-Token", token)
						.body("{\"email\": \"" + email + "\", \"first_name\": \"" + firstName + "\", \"last_name\": \"" + lastName + "\", \"age\": \"" + age + "\"}")
						.when()
						.put(endpoint + id);

				putRespCode = putResponse.getStatusCode();
				Assert.assertEquals(putRespCode, 200);
				
				if(putRespCode == 200) {
					jsonResponse = putResponse.asString();
					jp = new JsonPath(jsonResponse);
											
						Assert.assertEquals(jp.get("id"), id);
						Assert.assertEquals(jp.get("email"), email);
						Assert.assertEquals(jp.get("first_name"), firstName);
						Assert.assertEquals(jp.get("last_name"), lastName);
						softAssert.assertEquals(jp.get("age"), age);
					}
										
				}
			
			in.close();
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Assert.fail("Could not locate updateList.txt");
		}		
			
	}
	
}
