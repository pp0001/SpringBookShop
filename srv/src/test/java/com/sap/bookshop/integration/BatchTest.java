package com.sap.bookshop.integration;

import com.sap.bookshop.LocalApplication;
import com.sap.bookshop.config.CapCallbacks;
import com.sap.bookshop.config.LocalSecurityConfig;
import com.sap.bookshop.utils.*;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LocalApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"default", "batch"})
public class BatchTest {
    private static Logger LOGGER = LoggerFactory.getLogger(BatchTest.class);
    @LocalServerPort
    private Integer port;
    @Autowired
    private TestRestTemplate restTemplate;
    private String baseUrl;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeClass
    public static void beforeClass() {
        String dburl = System.getenv("dburl");
        Assume.assumeTrue(dburl != null && !dburl.trim().isEmpty());
    }

    @Before
    public void prepare() {
        baseUrl = "http://localhost:" + port + "/odata/v2/";
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        jdbcTemplate.execute("DELETE FROM MY_BOOKSHOP_BOOKS");
        jdbcTemplate.execute("DELETE FROM MY_BOOKSHOP_AUTHORS");
        CapCallbacks.reset();
    }

    @Test
    public void readAuthors() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        // write two authors via POST
        createAuthor100And101(jwtAdmin);
        IntStream.rangeClosed(1, 2).forEach(
                i -> {
                    //====== read them via batch =============
                    Batch batch = new Batch("1");
                    BatchEntry batchEntry = new HttpBatchEntry("GET", "Authors(100)");
                    batch.add(batchEntry);
                    batchEntry = new HttpBatchEntry("GET", "Authors(101)");
                    batch.add(batchEntry);
                    String result = sendBatchRequest("CatalogService", jwtAdmin, batch);
                    Assert.assertNotNull(result);
                    Assert.assertThat(result, CoreMatchers.containsString("Mike100"));
                    Assert.assertThat(result, CoreMatchers.containsString("Mike101"));
                    //===== read entries as list ========
                    batch = new Batch("1");
                    batchEntry = new HttpBatchEntry("GET", "Authors");
                    batch.add(batchEntry);
                    result = sendBatchRequest("CatalogService", jwtAdmin, batch);
                    Assert.assertNotNull(result);
                    Assert.assertThat(result, CoreMatchers.containsString("Mike100"));
                    Assert.assertThat(result, CoreMatchers.containsString("Mike101"));
                });
    }

    @Test
    public void createAuthorsViaBatch() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        //===== create via batch ============
        createAuthor100And101(jwtAdmin);
        Batch batch;
        String result;
        //============ read new entries via batch ================
        batch = new Batch("1");
        BatchEntry batchEntry = new HttpBatchEntry("GET", "Authors(100)");
        batch.add(batchEntry);
        batchEntry = new HttpBatchEntry("GET", "Authors(101)");
        batch.add(batchEntry);
        result = sendBatchRequest("CatalogService", jwtAdmin, batch);
        Assert.assertNotNull(result);
        Assert.assertThat(result, CoreMatchers.containsString("Mike100"));
        Assert.assertThat(result, CoreMatchers.containsString("Mike101"));
    }

    @Test
    public void changeAuthors() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        //===== create via batch ============
        createAuthor100And101(jwtAdmin);
        Batch batch;
        String body;
        ChangeSetEntry changeSetEntry;
        ChangeSetBatchEntry changeSet;
        String result;
        //========== change new entries via batch =============================
        batch = new Batch("1");
        body = "{\n" +
                "\"name\": \"Mike100b\"\n" +
                "}";
        changeSetEntry = new ChangeSetEntry(body, "MERGE", "Authors(100)");
        changeSet = new ChangeSetBatchEntry("1");
        changeSet.add(changeSetEntry);
        body = "{\n" +
                "\"name\": \"Mike101b\"\n" +
                "}";
        changeSetEntry = new ChangeSetEntry(body, "MERGE", "Authors(101)");
        changeSet.add(changeSetEntry);
        batch.add(changeSet);
        result = sendBatchRequest("CatalogService", jwtAdmin, batch);
        Assert.assertNotNull(result);
        //============ read changed Entries via batch ================
        batch = new Batch("1");
        BatchEntry batchEntry = new HttpBatchEntry("GET", "Authors(100)");
        batch.add(batchEntry);
        batchEntry = new HttpBatchEntry("GET", "Authors(101)");
        batch.add(batchEntry);
        result = sendBatchRequest("CatalogService", jwtAdmin, batch);
        Assert.assertNotNull(result);
        Assert.assertThat(result, CoreMatchers.containsString("Mike100b"));
        Assert.assertThat(result, CoreMatchers.containsString("Mike101b"));
    }

    @Test
    public void changeOneAuthorWithMerge() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        //===== create via batch ============
        createAuthor100And101(jwtAdmin);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload="{\n" +
                "  \"name\": \"hans\"\n" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(payload,headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "CatalogService/Authors(100)", HttpMethod.PATCH, entity, String.class);
        Assert.assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
        Assert.assertTrue(CapCallbacks.cleanUpTransactionCalled);
        //============ read changed entry via batch ================
        Batch batch = new Batch("1");
        BatchEntry batchEntry = new HttpBatchEntry("GET", "Authors(100)");
        batch.add(batchEntry);
        String result = sendBatchRequest("CatalogService", jwtAdmin, batch);
        Assert.assertThat(result, CoreMatchers.containsString("hans"));
    }

    @Test
    public void deleteAuthors() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        //===== create via batch ============
        createAuthor100And101(jwtAdmin);
        Batch batch;
        String body;
        ChangeSetEntry changeSetEntry;
        ChangeSetBatchEntry changeSet;
        String result;
        //========== delete entries via batch =============================
        batch = new Batch("1");
        body = "";
        changeSetEntry = new ChangeSetEntry(body, "DELETE", "Authors(100)");
        changeSet = new ChangeSetBatchEntry("1");
        changeSet.add(changeSetEntry);
        body = "";
        changeSetEntry = new ChangeSetEntry(body, "DELETE", "Authors(101)");
        changeSet.add(changeSetEntry);
        batch.add(changeSet);
        result = sendBatchRequest("CatalogService", jwtAdmin, batch);
        Assert.assertNotNull(result);
        //===== check that entries are gone ====================
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "CatalogService/Authors(100)", HttpMethod.GET, entity, String.class);
        Assert.assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
        entity = new HttpEntity<>(headers);
        response = restTemplate.exchange(baseUrl + "CatalogService/Authors(101)", HttpMethod.GET, entity, String.class);
        Assert.assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    public void changeAndCreateAuthorsInOneBatch() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        //===== create via batch ============
        createAuthor100And101(jwtAdmin);
        Batch batch;
        ChangeSetBatchEntry changeSet;
        String body;
        ChangeSetEntry changeSetEntry;
        String result;
        //========== change and create entries in one change set =============================
        batch = new Batch("1");
        changeSet = new ChangeSetBatchEntry("1");
        body = "{\n" +
                "\"name\": \"Mike100b\"\n" +
                "}";
        changeSetEntry = new ChangeSetEntry(body, "MERGE", "Authors(100)");
        changeSet.add(changeSetEntry);
        body = "{\n" +
                "\"name\": \"Mike101b\"\n" +
                "}";
        changeSetEntry = new ChangeSetEntry(body, "MERGE", "Authors(101)");
        changeSet.add(changeSetEntry);
        body = "{\n" +
                "\"ID\": 102,\t\n" +
                "\"name\": \"Mike102\"\n" +
                "}";
        changeSetEntry = new ChangeSetEntry(body, "POST", "Authors");
        changeSet.add(changeSetEntry);
        body = "{\n" +
                "\"ID\": 103,\t\n" +
                "\"name\": \"Mike103\"\n" +
                "}";
        changeSetEntry = new ChangeSetEntry(body, "POST", "Authors");
        changeSet.add(changeSetEntry);
        batch.add(changeSet);
        result = sendBatchRequest("CatalogService", jwtAdmin, batch);
        Assert.assertNotNull(result);
        //====== test result above ========
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "CatalogService/Authors(100)", HttpMethod.GET, entity, String.class);
        Assert.assertThat(response.getStatusCode(), is(HttpStatus.OK));
        Assert.assertThat(response.getBody(), CoreMatchers.containsString("Mike100b"));
        entity = new HttpEntity<>(headers);
        response = restTemplate.exchange(baseUrl + "CatalogService/Authors(101)", HttpMethod.GET, entity, String.class);
        Assert.assertThat(response.getStatusCode(), is(HttpStatus.OK));
        Assert.assertThat(response.getBody(), CoreMatchers.containsString("Mike101b"));
        entity = new HttpEntity<>(headers);
        response = restTemplate.exchange(baseUrl + "CatalogService/Authors(102)", HttpMethod.GET, entity, String.class);
        Assert.assertThat(response.getStatusCode(), is(HttpStatus.OK));
        Assert.assertThat(response.getBody(), CoreMatchers.containsString("Mike102"));
        entity = new HttpEntity<>(headers);
        response = restTemplate.exchange(baseUrl + "CatalogService/Authors(103)", HttpMethod.GET, entity, String.class);
        Assert.assertThat(response.getStatusCode(), is(HttpStatus.OK));
        Assert.assertThat(response.getBody(), CoreMatchers.containsString("Mike103"));
    }

    @Test
    public void readAuthorWithExpandInBatch() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        createAuthor100And101(jwtAdmin);
        createBooks100And101(jwtAdmin, 100);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        // read 2 times
        IntStream.rangeClosed(1, 2).forEach(i -> {
            Batch batch = new Batch("1");
            HttpBatchEntry batchEntry = new HttpBatchEntry("GET", "Authors(100)?$expand=books");
            String body = "";
            batch.add(batchEntry);
            String result = sendBatchRequest("CatalogService", jwtAdmin, batch);
            Assert.assertNotNull(result);
            Assert.assertThat(result, CoreMatchers.containsString("Book100"));
            Assert.assertThat(result, CoreMatchers.containsString("Book101"));
        });
    }

    @Test
    public void readAuthorWithExpand() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        createAuthor100And101(jwtAdmin);
        createBooks100And101(jwtAdmin, 100);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        // read 2 times
        IntStream.rangeClosed(1, 2).forEach(i -> {
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + "CatalogService/Authors(100)?$expand=books", HttpMethod.GET, entity, String.class);
            Assert.assertThat(response.getStatusCode(), is(HttpStatus.OK));
            Assert.assertThat(response.getBody(), CoreMatchers.containsString("Book100"));
            Assert.assertThat(response.getBody(), CoreMatchers.containsString("Book101"));
        });
    }

    @Test
    public void readBooks() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        createBooks100And101(jwtAdmin, 100);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        // read 2 times
        IntStream.rangeClosed(1, 2).forEach(i -> {
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + "CatalogService/Books(100)", HttpMethod.GET, entity, String.class);
            Assert.assertThat(response.getStatusCode(), is(HttpStatus.OK));
            Assert.assertThat(response.getBody(), CoreMatchers.containsString("Book100"));
        });
    }

    @Test
    public void createCustomer() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        String body = "{\n" +
                "\"ID\": 100,\t\n" +
                "\"name\": \"Hans\"\n" +
                "}";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "CatalogService/Customers", HttpMethod.POST, entity, String.class);
        Assert.assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    }

    @Test
    public void readWithCount() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        createAuthor100And101(jwtAdmin);
        createBooks100And101(jwtAdmin, 100);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        Batch batch = new Batch("1");
        HttpBatchEntry batchEntryGetAuthors = new HttpBatchEntry("GET", "Authors?$skip=0&$top=20&$orderby=name%20asc&$inlinecount=allpages");
        String body = "";
        batch.add(batchEntryGetAuthors);
        HttpBatchEntry batchEntryCount = new HttpBatchEntry("GET", "Books/$count");
        batch.add(batchEntryCount);
        HttpBatchEntry batchEntryGetAuthorsWithExpand = new HttpBatchEntry("GET", "Authors?$skip=0&$top=20&$expand=books");
        batch.add(batchEntryGetAuthorsWithExpand);
        String result = sendBatchRequest("CatalogService", jwtAdmin, batch);
        Assert.assertNotNull(result);
        Assert.assertThat(result, CoreMatchers.containsString("Book100"));
        Assert.assertThat(result, CoreMatchers.containsString("Book101"));
    }

    @Test
    public void callAction() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        createAuthor100And101(jwtAdmin);
        createBooks100And101(jwtAdmin, 100);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "CatalogService/clearBookStock?bookID=100", HttpMethod.POST, entity, String.class);
        Assert.assertTrue(CapCallbacks.actionCalled);
    }

    @Test
    public void callSecondAction() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        createAuthor100And101(jwtAdmin);
        createBooks100And101(jwtAdmin, 100);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "CatalogService/clearBookStock2?bookID=100", HttpMethod.POST, entity, String.class);
        Assert.assertTrue(CapCallbacks.actionCalled);
    }

    @Test
    public void changeInTwoChangeSets() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        //===== create via batch ============
        createAuthor100And101(jwtAdmin);
        Batch batch;
        String body;
        String result;
        //========== change and create entries in one change set =============================
        batch = new Batch("1");
        ChangeSetBatchEntry changeSet = new ChangeSetBatchEntry("1");
        body = "{\n" +
                "\"name\": \"Mike100b\"\n" +
                "}";
        ChangeSetEntry changeSetEntry = new ChangeSetEntry(body, "MERGE", "Authors(100)");
        changeSet.add(changeSetEntry);
        batch.add(changeSet);
        ChangeSetBatchEntry changeSet2 = new ChangeSetBatchEntry("2");
        body = "{\n" +
                "\"name\": \"Mike101b\"\n" +
                "}";
        ChangeSetEntry changeSetEntry2 = new ChangeSetEntry(body, "MERGE", "Authors(101)");
        changeSet2.add(changeSetEntry2);
        batch.add(changeSet2);
        result = sendBatchRequest("CatalogService", jwtAdmin, batch);
        Assert.assertNotNull(result);
        //====== test result above ========
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "CatalogService/Authors(100)", HttpMethod.GET, entity, String.class);
        Assert.assertThat(response.getStatusCode(), is(HttpStatus.OK));
        Assert.assertThat(response.getBody(), CoreMatchers.containsString("Mike100b"));
        response = restTemplate.exchange(baseUrl + "CatalogService/Authors(101)", HttpMethod.GET, entity, String.class);
        Assert.assertThat(response.getStatusCode(), is(HttpStatus.OK));
        Assert.assertThat(response.getBody(), CoreMatchers.containsString("Mike101b"));

    }

    @Test
    public void changeOneDraftWithMerge() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        //===== create via batch ============
        String guidForMike= UUID.randomUUID().toString();
        String guidForJoe= UUID.randomUUID().toString();
        createVendorMikeAndJoe(jwtAdmin,guidForMike,guidForJoe);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload="{\n" +
                "  \"name\": \"Mikeb\"\n" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(payload,headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "VendorService/Vendors(ID=guid'"+guidForMike+"',IsActiveEntity=false)", HttpMethod.PATCH, entity, String.class);
        Assert.assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
        //============ read changed entry via batch ================
        Batch batch = new Batch("1");
        BatchEntry batchEntry = new HttpBatchEntry("GET", "Vendors(ID=guid'"+guidForMike+"',IsActiveEntity=false)");
        batch.add(batchEntry);
        String result = sendBatchRequest("VendorService", jwtAdmin, batch);
        Assert.assertThat(result, CoreMatchers.containsString("Mikeb"));
    }

    @Test
    public void draftChangeInTwoChangeSets() {
        String jwtAdmin = new JwtGenerator().getTokenForAuthorizationHeader("t1", "openid", LocalSecurityConfig.XSAPPNAME + ".admin");
        //===== create via batch ============
        String guidForMike= UUID.randomUUID().toString();
        String guidForJoe= UUID.randomUUID().toString();
        createVendorMikeAndJoe(jwtAdmin,guidForMike,guidForJoe);
        Batch batch;
        String body;
        String result;
        //========== change and create entries in one change set =============================
        batch = new Batch("1");
        ChangeSetBatchEntry changeSet = new ChangeSetBatchEntry("1");
        body = "{\n" +
                "\"name\": \"Mikeb\"\n" +
                "}";
        ChangeSetEntry changeSetEntry = new ChangeSetEntry(body, "MERGE", "Vendors(ID=guid'"+guidForMike+"',IsActiveEntity=false)");
        changeSet.add(changeSetEntry);
        batch.add(changeSet);
        ChangeSetBatchEntry changeSet2 = new ChangeSetBatchEntry("2");
        body = "{\n" +
                "\"name\": \"Joeb\"\n" +
                "}";
        ChangeSetEntry changeSetEntry2 = new ChangeSetEntry(body, "MERGE", "Vendors(ID=guid'"+guidForJoe+"',IsActiveEntity=false)");
        changeSet2.add(changeSetEntry2);
        batch.add(changeSet2);
        result = sendBatchRequest("VendorService", jwtAdmin, batch);
        Assert.assertNotNull(result);
        //====== test result above ========
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "VendorService/Vendors(ID=guid'"+guidForMike+"',IsActiveEntity=false)", HttpMethod.GET, entity, String.class);
        Assert.assertThat(response.getStatusCode(), is(HttpStatus.OK));
        Assert.assertThat(response.getBody(), CoreMatchers.containsString("Mikeb"));
        response = restTemplate.exchange(baseUrl + "VendorService/Vendors(ID=guid'"+guidForJoe+"',IsActiveEntity=false)", HttpMethod.GET, entity, String.class);
        Assert.assertThat(response.getStatusCode(), is(HttpStatus.OK));
        Assert.assertThat(response.getBody(), CoreMatchers.containsString("Joeb"));

    }

    private void createAuthor100And101(String jwtAdmin) {
        Batch batch = new Batch("1");
        String body = "{\n" +
                "\"ID\": 100,\t\n" +
                "\"name\": \"Mike100\"\n" +
                "}";
        ChangeSetEntry changeSetEntry = new ChangeSetEntry(body, "POST", "Authors");
        ChangeSetBatchEntry changeSet = new ChangeSetBatchEntry("1");
        changeSet.add(changeSetEntry);
        body = "{\n" +
                "\"ID\": 101,\t\n" +
                "\"name\": \"Mike101\"\n" +
                "}";
        changeSetEntry = new ChangeSetEntry(body, "POST", "Authors");
        changeSet.add(changeSetEntry);
        batch.add(changeSet);
        String result = sendBatchRequest("CatalogService", jwtAdmin, batch);
        Assert.assertNotNull(result);
    }

    private void createVendorMikeAndJoe(String jwtAdmin, String guidForMike, String guidForJoe) {
        Batch batch = new Batch("1");
        String body = "{\n" +
                "\"ID\": \""+guidForMike+ "\",\t\n" +
                "\"name\": \"Mike\"\n" +
                "}";
        ChangeSetEntry changeSetEntry = new ChangeSetEntry(body, "POST", "Vendors");
        ChangeSetBatchEntry changeSet = new ChangeSetBatchEntry("1");
        changeSet.add(changeSetEntry);
        body = "{\n" +
                "\"ID\": \""+ guidForJoe+"\",\t\n" +
                "\"name\": \"Joe\"\n" +
                "}";
        changeSetEntry = new ChangeSetEntry(body, "POST", "Vendors");
        changeSet.add(changeSetEntry);
        batch.add(changeSet);
        String result = sendBatchRequest("VendorService", jwtAdmin, batch);
        Assert.assertNotNull(result);
    }

    private void createBooks100And101(String jwtAdmin, int author_id) {
        Batch batch = new Batch("1");
        String body = "{\n" +
                "\"ID\": 100,\t\n" +
                "\"title\": \"Book100\",\n" +
                "\"author_ID\":" + author_id + ",\t\n" +
                "\"stock\": 100\n" +
                "}";
        ChangeSetEntry changeSetEntry = new ChangeSetEntry(body, "POST", "Books");
        ChangeSetBatchEntry changeSet = new ChangeSetBatchEntry("1");
        changeSet.add(changeSetEntry);
        body = "{\n" +
                "\"ID\": 101,\t\n" +
                "\"title\": \"Book101\",\n" +
                "\"author_ID\":" + author_id + ",\t\n" +
                "\"stock\": 101\n" +
                "}";
        changeSetEntry = new ChangeSetEntry(body, "POST", "Books");
        changeSet.add(changeSetEntry);
        batch.add(changeSet);
        String result = sendBatchRequest("CatalogService", jwtAdmin, batch);
        Assert.assertNotNull(result);
    }


    private void writePayload(HttpURLConnection httpConnection, String payload) throws IOException {
        try (OutputStream outputStream = httpConnection.getOutputStream();
             DataOutputStream writer = new DataOutputStream(outputStream)) {
            writer.writeBytes(payload);
            writer.flush();
        }
    }

    private String getResult(HttpURLConnection httpConnection) throws IOException {
        String line;
        StringBuilder builder = new StringBuilder();
        try (InputStream inputStream = httpConnection.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            while ((line = bufferedReader.readLine()) != null) builder.append(line);
        }
        return builder.toString();
    }

    private String sendBatchRequest(String service, String jwt, Batch batch) {
        String result = "";
        try {
            URL url = new URL(baseUrl + service + "/$batch");
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty("Authorization", jwt);
            httpConnection.setRequestProperty("Content-Type", " multipart/mixed;boundary=batch_1");
            httpConnection.setRequestProperty("Accept", "application/json");
            writePayload(httpConnection, batch.getPayload());
            result = getResult(httpConnection);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return result;
    }

}
