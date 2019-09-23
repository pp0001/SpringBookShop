package com.sap.bookshop.config;

import com.sap.cds.CdsDataStore;
import com.sap.cds.CdsDataStoreConnector;
import com.sap.cds.impl.JDBCDataStoreConnector;
import com.sap.cds.reflect.CdsModel;
import com.sap.cds.reflect.impl.CdsModelReader;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.function.Supplier;

@Configuration
@ConfigurationProperties("com.sap.cds")
public class Config {
    private String modelName = "csn.json";

    @Bean
    public CdsModel cdsModel() {
        String path = "edmx/" + "/" + modelName;
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream(path)) {
            return CdsModelReader.readCsn(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public CdsDataStoreConnector cdsDataStoreConnector(CdsModel cdsModel, DataSource ds, SpringTransactionManager transactionManager) {
        Supplier<Connection> connectionSupplier = () -> wrap(ds, DataSourceUtils.getConnection(ds));
        return new JDBCDataStoreConnector(cdsModel, connectionSupplier, transactionManager);
    }

    @Bean
    public CdsDataStore cdsDataStore(CdsDataStoreConnector cdsDataStoreConnector) {
        return cdsDataStoreConnector.connect();
    }

    private Connection wrap(DataSource ds, Connection connection) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("close".equals(method.getName())) {
                    DataSourceUtils.releaseConnection(connection, ds);
                    return null;
                }
                return method.invoke(connection, args);
            }
        };

        return (Connection) Proxy.newProxyInstance(Config.class.getClassLoader(), new Class[]{Connection.class},
                handler);
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}