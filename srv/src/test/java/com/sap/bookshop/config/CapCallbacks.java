package com.sap.bookshop.config;

import com.sap.cloud.sdk.service.prov.api.ExtensionHelper;
import com.sap.cloud.sdk.service.prov.api.annotations.*;
import com.sap.cloud.sdk.service.prov.api.exits.BeforeCreateResponse;
import com.sap.cloud.sdk.service.prov.api.exits.BeforeDeleteResponse;
import com.sap.cloud.sdk.service.prov.api.exits.BeforeReadResponse;
import com.sap.cloud.sdk.service.prov.api.exits.BeforeUpdateResponse;
import com.sap.cloud.sdk.service.prov.api.operations.Create;
import com.sap.cloud.sdk.service.prov.api.operations.Read;
import com.sap.cloud.sdk.service.prov.api.request.*;
import com.sap.cloud.sdk.service.prov.api.response.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Component
public class CapCallbacks {

    private JdbcTemplate jdbcTemplate;
    public static boolean actionCalled = false;
    public static boolean cleanUpTransactionCalled = false;

    public CapCallbacks(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static void reset() {
        actionCalled = false;
        cleanUpTransactionCalled = false;
    }

    @BeforeCreate(entity = "Authors", serviceName = "CatalogService")
    public BeforeCreateResponse beforeCreateAuthor(CreateRequest createRequest, ExtensionHelper extensionHelper) {
        return BeforeCreateResponse.setSuccess().response();
    }

    @BeforeRead(entity = "Authors", serviceName = "CatalogService")
    public BeforeReadResponse beforeReadAuthor(ReadRequest readRequest, ExtensionHelper extensionHelper) {
        return BeforeReadResponse.setSuccess().response();
    }

    @BeforeUpdate(entity = "Authors", serviceName = "CatalogService")
    public BeforeUpdateResponse beforeUpdateAuthor(UpdateRequest updateRequest, ExtensionHelper extensionHelper) {
        return BeforeUpdateResponse.setSuccess().response();
    }

    @BeforeDelete(entity = "Authors", serviceName = "CatalogService")
    public BeforeDeleteResponse beforeDeleteAuthor(DeleteRequest deleteRequest, ExtensionHelper extensionHelper) {
        return BeforeDeleteResponse.setSuccess().response();
    }


    @AfterCreate(entity = "Authors", serviceName = "CatalogService")
    public CreateResponse afterCreateAuthor(CreateRequest createRequest, CreateResponseAccessor createResponseAccessor, ExtensionHelper extensionHelper) {
        return CreateResponse.setSuccess().response();
    }

    @AfterRead(entity = "Authors", serviceName = "CatalogService")
    public ReadResponse afterReadAuthor(ReadRequest readRequest, ReadResponseAccessor readResponseAccessor, ExtensionHelper extensionHelper) {
        return ReadResponse.setSuccess().response();
    }

    @AfterUpdate(entity = "Authors", serviceName = "CatalogService")
    public UpdateResponse afterUpdateAuthor(UpdateRequest updateRequest, UpdateResponseAccessor updateResponseAccessor, ExtensionHelper extensionHelper) {
        return UpdateResponse.setSuccess().response();
    }

    @AfterDelete(entity = "Authors", serviceName = "CatalogService")
    public DeleteResponse afterDeleteAuthor(DeleteRequest deleteRequest, DeleteResponseAccessor deleteResponseAccessor, ExtensionHelper extensionHelper) {
        return DeleteResponse.setSuccess().response();
    }

    @Read(entity = "Books", serviceName = "CatalogService")
    public ReadResponse readBook(ReadRequest readRequest, ExtensionHelper extensionHelper) {
        Map<String, Object> keys = readRequest.getKeys();
        Map<String, Object> data = jdbcTemplate.queryForMap("SELECT * FROM MY_BOOKSHOP_BOOKS WHERE ID= " + keys.get("ID"));
        //Map<Object, Object> data = new HashMap<>();
        return ReadResponse.setSuccess().setData(data).response();
    }

    @Create(entity = "Customers", serviceName = "CatalogService")
    public CreateResponse createBook(CreateRequest createRequest, ExtensionHelper extensionHelper) {
        return CreateResponse.setSuccess().setData(createRequest.getData()).response();
    }

    @CleanupTransaction(serviceNames = "CatalogService")
    public void cleanUpTransaction(boolean committed, List<Request> requests, ExtensionHelper extensionHelper) {
        cleanUpTransactionCalled = true;
    }

    @Action(Name = "clearBookStock", serviceName = "CatalogService")
    @Transactional //only to trigger creation of proxy
    public OperationResponse clearBookStock(OperationRequest operationRequest) {
        if (jdbcTemplate == null) {
            throw new RuntimeException("Jdbc template is null");
        }
        actionCalled = true;
        Integer bookID = Integer.parseInt((String) operationRequest.getParameters().get("bookID"));
        jdbcTemplate.execute("UPDATE MY_BOOKSHOP_BOOKS SET stock = 0 WHERE ID = " + bookID);
        Map<String, Object> data = jdbcTemplate.queryForMap("SELECT * FROM MY_BOOKSHOP_BOOKS WHERE ID= " + bookID);
        return OperationResponse.setSuccess().setData(Arrays.asList(data)).response();
    }

    @Action(Name = "clearBookStock2", serviceName = "CatalogService")
    @Transactional //only to trigger creation of proxy
    public OperationResponse clearBookStock2(OperationRequest operationRequest, ExtensionHelper extensionHelper) {
        if (jdbcTemplate == null) {
            throw new RuntimeException("Jdbc template is null");
        }
        actionCalled = true;
        Integer bookID = Integer.parseInt((String) operationRequest.getParameters().get("bookID"));
        jdbcTemplate.execute("UPDATE MY_BOOKSHOP_BOOKS SET stock = 0 WHERE ID = " + bookID);
        Map<String, Object> data = jdbcTemplate.queryForMap("SELECT * FROM MY_BOOKSHOP_BOOKS WHERE ID= " + bookID);
        return OperationResponse.setSuccess().setData(Arrays.asList(data)).response();
    }

    @BeforeCreate(entity = "Vendors", serviceName = "VendorService")
    public BeforeCreateResponse beforeCreateVendor(CreateRequest createRequest, ExtensionHelper extensionHelper) {
        return BeforeCreateResponse.setSuccess().response();
    }

    @BeforeRead(entity = "Vendors", serviceName = "VendorService")
    public BeforeReadResponse beforeReadVendor(ReadRequest readRequest, ExtensionHelper extensionHelper) {
        return BeforeReadResponse.setSuccess().response();
    }

    @BeforeUpdate(entity = "Vendors", serviceName = "VendorService")
    public BeforeUpdateResponse beforeUpdateVendor(UpdateRequest updateRequest, ExtensionHelper extensionHelper) {
        return BeforeUpdateResponse.setSuccess().response();
    }


}
