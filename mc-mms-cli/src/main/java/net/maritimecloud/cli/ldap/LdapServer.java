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
package net.maritimecloud.cli.ldap;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.google.inject.Injector;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.core.schema.SchemaPartition;
import org.apache.directory.server.protocol.shared.store.LdifFileLoader;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.apache.directory.shared.ldap.name.DN;
import org.apache.directory.shared.ldap.name.RDN;
import org.apache.directory.shared.ldap.schema.SchemaManager;
import org.apache.directory.shared.ldap.schema.ldif.extractor.SchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.ldif.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.loader.ldif.LdifSchemaLoader;
import org.apache.directory.shared.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.shared.ldap.schema.registries.SchemaLoader;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;

/**
 * Launches an Apache DS LDAP server.
 * <p>
 * When the LDAP server is initialized the first time around,
 * a Maritime Cloud-flavoured schema and test data is loaded.
 */
public class LdapServer extends AbstractLdapCommandLineTool {

    @Parameter(names = "-dir", description = "The data directory", converter = FileConverter.class)
    File workDir = null;

    /**
     * Add a new partition to the server
     *
     * @param service The directory service
     * @param partitionId The partition Id
     * @param partitionDn The partition DN
     * @return The newly added partition
     * @throws Exception If the partition can't be added
     */
    private Partition addPartition(DirectoryService service, String partitionId, String partitionDn ) throws Exception {
        // Create a new partition named 'foo'.
        JdbmPartition partition = new JdbmPartition();
        partition.setId( partitionId );
        partition.setPartitionDir( new File( service.getWorkingDirectory(), partitionId ) );
        partition.setSuffix( partitionDn );
        service.addPartition( partition );

        return partition;
    }


    /**
     * Add a new set of index on the given attributes
     *
     * @param partition The partition on which we want to add index
     * @param attrs The list of attributes to index
     */
    private void addIndex( Partition partition, String... attrs ) {
        // Index some attributes on the apache partition
        HashSet<Index<?, ServerEntry, Long>> indexedAttributes = new HashSet<>();

        for ( String attribute:attrs ) {
            indexedAttributes.add( new JdbmIndex<String,ServerEntry>( attribute ) );
        }

        ((JdbmPartition)partition).setIndexedAttributes( indexedAttributes );
    }

    /**
     * initialize the schema manager and add the schema partition to diectory service
     *
     * @throws Exception if the schema LDIF files are not found on the classpath
     */
    private void initSchemaPartition(DirectoryService service) throws Exception
    {
        SchemaPartition schemaPartition = service.getSchemaService().getSchemaPartition();

        // Init the LdifPartition
        LdifPartition ldifPartition = new LdifPartition();
        String workingDirectory = service.getWorkingDirectory().getPath();
        ldifPartition.setWorkingDirectory( workingDirectory + "/schema" );

        // Extract the schema on disk (a brand new one) and load the registries
        File schemaRepository = new File( workingDirectory, "schema" );
        SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor( new File( workingDirectory ) );
        extractor.extractOrCopy( true );

        schemaPartition.setWrappedPartition( ldifPartition );

        SchemaLoader loader = new LdifSchemaLoader( schemaRepository );
        SchemaManager schemaManager = new DefaultSchemaManager( loader );
        service.setSchemaManager( schemaManager );

        // We have to load the schema now, otherwise we won't be able
        // to initialize the Partitions, as we won't be able to parse
        // and normalize their suffix DN
        schemaManager.loadAllEnabled();

        schemaPartition.setSchemaManager( schemaManager );

        List<Throwable> errors = schemaManager.getErrors();

        if ( errors.size() != 0 ) {
            throw new Exception( "Schema load failed : " + errors );
        }
    }


    /**
     * Loads the given
     * @param service the directory service
     * @param file the ldif file
     * @throws URISyntaxException
     */
    protected void loadLdifFromResource(DirectoryService service, String file) throws Exception {
        // Sadly, the LdifFileLoader does not take an input stream, only a file path.
        // To be able to run the ldapserver from a jar, first copy the resource to a temp file
        Path tmpFile = Files.createTempFile("ldap_", ".ldif");
        Files.copy(getClass().getResourceAsStream(file), tmpFile, StandardCopyOption.REPLACE_EXISTING);

        new LdifFileLoader(service.getAdminSession(), tmpFile.toFile().getAbsolutePath()).execute();
        // NB: From command line: ldapadd -x -h 127.0.0.1:10389  -D "uid=admin,ou=system" -w secret -f maritimecloud-data.ldif
    }

    /**
     * Initialize the server. It creates the partition, adds the index, and
     * injects the context entries for the created partitions.
     *
     * @throws Exception if there were some problems while initializing the system
     */
    @Override
    protected final void run(Injector injector) throws Exception {
        try {

            if (workDir == null) {
                workDir = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "ldap").toFile();
            }
            System.out.println("Using work dir: " + workDir);

            // Initialize the LDAP service
            DirectoryService service = new DefaultDirectoryService();
            service.setWorkingDirectory(workDir);

            // first load the schema
            initSchemaPartition(service);

            // then the system partition
            // this is a MANDATORY partition
            Partition systemPartition = addPartition(service, "system", ServerDNConstants.SYSTEM_DN);
            service.setSystemPartition(systemPartition);

            // Disable the ChangeLog system
            service.getChangeLog().setEnabled(false);
            service.setDenormalizeOpAttrsEnabled(true);

            // Parse base dn
            DN dn = new DN(baseDN);
            List<RDN> rdsn = dn.getRdns();


            // Create a new partition named 'maritimecloud'.
            Partition partition = addPartition(service, rdsn.get(0).getAtav().getNormValue().getString().toLowerCase(), dn.getNormName().toLowerCase());

            // Index some attributes on the maritimecloud partition
            addIndex(partition, "objectClass", "ou", "uid");

            // And start the service
            service.startup();

            // Inject the maritimecloud root entry if it does not already exist
            if (!service.getAdminSession().exists(partition.getSuffixDn())) {
                ServerEntry serverEntry = service.newEntry(dn);
                serverEntry.add("objectClass", "top", "domain", "extensibleObject");
                serverEntry.add(rdsn.get(0).getAtav().getNormType(), rdsn.get(0).getAtav().getUpValue().toString().toLowerCase());
                service.getAdminSession().add(serverEntry);
            }

            // Load the maritimecloud schema if it does not already exist
            if (!service.getAdminSession().exists(new DN("ou=People," + baseDN))) {
                loadLdifFromResource(service, "/maritimecloud-schema.ldif");
                loadLdifFromResource(service, "/maritimecloud-data.ldif");
                System.out.println("Maritime Cloud schema and sample data loaded");
            }

            // Start an LDAP server
            org.apache.directory.server.ldap.LdapServer server = new org.apache.directory.server.ldap.LdapServer();
            server.setTransports(new TcpTransport(port));
            server.setDirectoryService(service);

            server.start();
            System.out.println("LDAP started");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Main method of the LdapServer class
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        new LdapServer().execute(args);
    }

    /**
     * Converts JCommander argument to a file
     */
    public static class FileConverter implements IStringConverter<File> {
        @Override
        public File convert(String value) {
            return new File(value);
        }
    }

}
