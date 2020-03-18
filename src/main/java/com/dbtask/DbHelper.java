package com.dbtask;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.bson.Document;

class DbHelper {
    private static final Config CONFIG = ConfigFactory.load();
    private static final String MONGO_HOST = CONFIG.getString("mongoHost");
    private static final int MONGO_PORT = CONFIG.getInt("mongoPort");
    private static final int MONGO_CLIENT_TIMEOUT = CONFIG.getInt("mongoClientTimeout");
    private static final String MONGO_DB_NAME = CONFIG.getString("mongoDb");
    private static final String MONGO_COLLECTION_NAME = CONFIG.getString("mongoCollection");

    private MongoClient getMongoClient() {
        MongoClientOptions options = MongoClientOptions
                .builder()
                .socketTimeout(MONGO_CLIENT_TIMEOUT)
                .connectTimeout(MONGO_CLIENT_TIMEOUT)
                .serverSelectionTimeout(MONGO_CLIENT_TIMEOUT)
                .heartbeatSocketTimeout(MONGO_CLIENT_TIMEOUT)
                .heartbeatConnectTimeout(MONGO_CLIENT_TIMEOUT)
                .build();

        return new MongoClient(MONGO_HOST, options);
    }

    private MongoDatabase getDatabase() {
        return getMongoClient().getDatabase(MONGO_DB_NAME);
    }

    MongoCollection<Document> getCollection() {
        return getDatabase().getCollection(MONGO_COLLECTION_NAME);
    }
}
