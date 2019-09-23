package com.sap.bookshop.callbacks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.sap.bookshop.service.SBHelperUtil;
import com.sap.cloud.sdk.service.prov.api.EntityData;
import com.sap.cloud.sdk.service.prov.api.ExtensionHelper;
import com.sap.cloud.sdk.service.prov.api.annotations.AfterCreate;
import com.sap.cloud.sdk.service.prov.api.annotations.AfterDelete;
import com.sap.cloud.sdk.service.prov.api.annotations.AfterRead;
import com.sap.cloud.sdk.service.prov.api.annotations.AfterUpdate;
import com.sap.cloud.sdk.service.prov.api.annotations.BeforeCreate;
import com.sap.cloud.sdk.service.prov.api.annotations.BeforeDelete;
import com.sap.cloud.sdk.service.prov.api.annotations.BeforeRead;
import com.sap.cloud.sdk.service.prov.api.annotations.BeforeUpdate;
import com.sap.cloud.sdk.service.prov.api.exits.BeforeCreateResponse;
import com.sap.cloud.sdk.service.prov.api.exits.BeforeDeleteResponse;
import com.sap.cloud.sdk.service.prov.api.exits.BeforeReadResponse;
import com.sap.cloud.sdk.service.prov.api.exits.BeforeUpdateResponse;
import com.sap.cloud.sdk.service.prov.api.request.CreateRequest;
import com.sap.cloud.sdk.service.prov.api.request.DeleteRequest;
import com.sap.cloud.sdk.service.prov.api.request.ReadRequest;
import com.sap.cloud.sdk.service.prov.api.request.UpdateRequest;
import com.sap.cloud.sdk.service.prov.api.response.CreateResponse;
import com.sap.cloud.sdk.service.prov.api.response.CreateResponseAccessor;
import com.sap.cloud.sdk.service.prov.api.response.DeleteResponse;
import com.sap.cloud.sdk.service.prov.api.response.DeleteResponseAccessor;
import com.sap.cloud.sdk.service.prov.api.response.ReadResponse;
import com.sap.cloud.sdk.service.prov.api.response.ReadResponseAccessor;
import com.sap.cloud.sdk.service.prov.api.response.UpdateResponse;
import com.sap.cloud.sdk.service.prov.api.response.UpdateResponseAccessor;

@Profile("cloud")
@Component
public class CapCallbacks {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
	private SBHelperUtil sbHelperUtil;
    
    private static final Logger LOG = LoggerFactory.getLogger (CapCallbacks.class.getName());

	  @AfterRead (entity = "Books", serviceName="CatalogService")
	  public ReadResponse afterReadOrders (ReadRequest req, ReadResponseAccessor res, ExtensionHelper h) {
	  	LOG.info ("##### Books - beforeReadBooks ########");
	  	
	  	String testToken = sbHelperUtil.getSBToken("sales-broker-1");
	  	LOG.info("Sales broker token: ", testToken);
	  	
	    EntityData ed = res.getEntityData();
	    Integer stock = 0;
	    EntityData ex = EntityData.getBuilder(ed).addElement("stock", stock).buildEntityData("Books");
	    return ReadResponse.setSuccess().setData(ex).response();
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

}
