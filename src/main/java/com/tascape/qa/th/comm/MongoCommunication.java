package com.tascape.qa.th.comm;

import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.tascape.qa.th.SystemConfiguration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wang, linsong
 */
public class MongoCommunication extends EntityCommunication {
    private static final Logger LOG = LoggerFactory.getLogger(MongoCommunication.class);

    public static final String SYSEPROP_MONGO_SERVERS = "qa.th.comm.mongo.SERVERS";

    public static final String SYSEPROP_MONGO_DATABASE = "qa.th.comm.mongo.DATABASE";

    public static final String SYSEPROP_MONGO_USER = "qa.th.comm.mongo.USER";

    public static final String SYSEPROP_MONGO_PASS = "qa.th.comm.mongo.PASS";

    private final MongoClient mongoClient;

    private final String db;

    private MongoDatabase database;

    /**
     * Creates a mongo client, getting all parameters from system properties.
     */
    public MongoCommunication() {
        this(
            SystemConfiguration.getInstance().getProperty(SYSEPROP_MONGO_SERVERS, ""),
            SystemConfiguration.getInstance().getProperty(SYSEPROP_MONGO_USER),
            SystemConfiguration.getInstance().getProperty(SYSEPROP_MONGO_PASS),
            SystemConfiguration.getInstance().getProperty(SYSEPROP_MONGO_DATABASE)
        );
    }

    /**
     * Creates a Mongo client.
     *
     * @param servers servers in format 192.168.0.3:27017,192.168.0.5:27017
     * @param user    user name
     * @param pass    password
     * @param db      database name
     */
    public MongoCommunication(String servers, String user, String pass, String db) {
        LOG.info("Use Mongo server(s) {}", servers);
        List<ServerAddress> addrs = Stream.of(servers.split(",")).map(s -> {
            String[] sp = s.split(":");
            if (sp.length == 1) {
                return new ServerAddress(sp[0]);
            } else {
                return new ServerAddress(sp[0], Integer.parseInt(sp[1]));
            }
        }).collect(Collectors.toList());
        MongoCredential credential = MongoCredential.createCredential(user, db, pass.toCharArray());
        this.mongoClient = new MongoClient(addrs, Lists.newArrayList(credential));
        this.db = db;
    }

    /**
     * Connects to create a reference of specified database.
     *
     * @throws Exception in case of any connection issue
     */
    @Override
    public void connect() throws Exception {
        this.database = this.mongoClient.getDatabase(this.db);
    }

    @Override
    public void disconnect() throws Exception {
        mongoClient.close();
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public static void main(String[] args) throws Exception {
        MongoCommunication db = new MongoCommunication();
        db.connect();
        MongoDatabase md = db.getDatabase();
        MongoCollection<Document> cs = md.getCollection("collection-name");
        LOG.debug("{}", cs.count());

        Document doc = cs.find().first();
        LOG.debug("\n{}", doc.toJson(new JsonWriterSettings(true)));
    }
}
