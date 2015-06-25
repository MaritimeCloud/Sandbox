/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.maritimecloud.internal.client.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.internal.msdl.db.DefaultMsdlDatabase;
import net.maritimecloud.internal.msdl.db.MsdlDatabaseConfiguration;

/**
 *
 * @author Kasper Nielsen
 */
public class ClientConfiguration {

    private final BroadcastConfiguration broadcasts = new BroadcastConfiguration();

    DefaultMsdlDatabase db;

    private String description;


    private final ServiceConfiguration endpoints = new ServiceConfiguration();

    final Path home;

    private MaritimeId id;

    private String mmsHost = "localhost";

    private Path msdlRoot = Paths.get("src/main/msdl");

    private String name;

    private String organization;

    private int port = 9090;

    private String positionSupplier;

    public ClientConfiguration(Path home) {
        this.home = home.toAbsolutePath();
    }

    /**
     * @return the broadcasts
     */
    public BroadcastConfiguration getBroadcasts() {
        return broadcasts;
    }

    /**
     * @return the db
     */
    public DefaultMsdlDatabase getDb() {
        return db;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the endpoints
     */
    public ServiceConfiguration getEndpoints() {
        return endpoints;
    }

    /**
     * @return the home
     */
    public Path getHome() {
        return home;
    }

    /**
     * @return the id
     */
    public MaritimeId getId() {
        return id;
    }

    /**
     * @return the mmsHost
     */
    public String getMmsHost() {
        return mmsHost;
    }

    /**
     * @return the msdlRoot
     */
    public Path getMsdlRoot() {
        return msdlRoot;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the organization
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the positionSupplier
     */
    public String getPositionSupplier() {
        return positionSupplier;
    }

    public void populate(Properties properties, Path home) {
        mmsHost = required(properties, "mms.host");
        id = MaritimeId.create(required(properties, "id"));
        name = properties.getProperty("name");
        description = properties.getProperty("description");
        msdlRoot = home.resolve(required(properties, "msdl.location"));
        organization = properties.getProperty("organization");
        port = Integer.parseInt(required(properties, "port"));
    }


    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @param id
     *            the id to set
     * @return
     */
    public ClientConfiguration setId(MaritimeId id) {
        this.id = id;
        return this;
    }


    /**
     * @param mmsHost
     *            the mmsHost to set
     */
    public ClientConfiguration setMmsHost(String mmsHost) {
        this.mmsHost = mmsHost;
        return this;
    }


    /**
     * @param msdlRoot
     *            the msdlRoot to set
     */
    public void setMsdlRoot(Path msdlRoot) {
        this.msdlRoot = msdlRoot;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param organization
     *            the organization to set
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * @param port
     *            the port to set
     */
    public ClientConfiguration setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * @param positionSupplier
     *            the positionSupplier to set
     */
    public void setPositionSupplier(String positionSupplier) {
        this.positionSupplier = positionSupplier;
    }

    /**
     * @param args
     * @return
     * @throws IOException
     */
    public static ClientConfiguration create(String[] args) throws IOException {
        System.out.println("Using MC Client home " + args[0]);
        Path home = Paths.get(args[0]);
        return fromPropertyFile(home);
    }

    public static ClientConfiguration fromPropertyFile(Path home) throws IOException {
        Path propertiesFile = home.resolve("conf/mc-client.properties");
        Properties properties = new Properties();
        properties.load(Files.newInputStream(propertiesFile.toAbsolutePath()));


        ClientConfiguration conf = new ClientConfiguration(home);
        conf.populate(properties, home);
        conf.db = MsdlDatabaseConfiguration.create(conf.getMsdlRoot().toAbsolutePath());

        Path path = Paths.get(required(properties, "broadcast.subscriptions"));
        Path subscriptionsPath = home.resolve(path).toAbsolutePath();
        conf.broadcasts.populate(conf.db, Files.newInputStream(subscriptionsPath));

        path = Paths.get(required(properties, "service.endpoints"));
        subscriptionsPath = home.resolve(path).toAbsolutePath();
        conf.endpoints.populate(conf.db, Files.newInputStream(subscriptionsPath));

        return conf;
    }


    static String required(Properties properties, String property) {
        String result = properties.getProperty(property);
        if (result == null) {
            throw new IllegalArgumentException("Missing property '" + property + "'");
        }
        return result;
    }
}
