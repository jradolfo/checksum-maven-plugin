package br.dodo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;


@Mojo(name = "generate-checksum")
public class ChecksumMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "generate-checksum.propertyName", required = true)
    private String propertyName;

    @Parameter(property = "generate-checksum.hashAlgorithm", required = true, defaultValue = "SHA-1")
    private String hashAlgorithm;

    @Parameter(property = "generate-checksum.folder", required = true)
    private File folder;

    @Parameter(property = "generate-checksum.extensions")
    private List<String> extensions;

    @Parameter(property = "generate-checksum.excludes")
    private List<String> excludes;


    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Folder: " + folder);
        getLog().info("Hash Algorithm: " + hashAlgorithm);
        getLog().info("Extensions: " + extensions);
        getLog().info("Excludes: " + excludes);

        try (Stream<Path> stream = Files.walk(folder.toPath())) {
            MessageDigest mdigest = MessageDigest.getInstance(hashAlgorithm);

             stream.map(Path::normalize)
              .filter(Files::isRegularFile)
              .filter(this::filterExcludes)
              .filter(this::filterExtensions)
              .forEach(path -> {
                  getLog().info("Including: " + path.toString());
                  digestFile(mdigest, path);
              });

             String checksum = digestToString(mdigest);

             getLog().info("Computed Checksum: " + checksum);

             Properties properties = project.getProperties();
             properties.setProperty(propertyName, checksum);

        } catch (IOException | NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    private boolean filterExcludes(Path file) {
        boolean allow = true;

        if (excludes != null && !excludes.isEmpty()) {
            allow = !excludes.contains(file.getFileName().toString());
        }

        return allow;
    }

    private boolean filterExtensions(Path file) {
        boolean allow = true;

        if (extensions != null && !extensions.isEmpty()) {
            allow = extensions.stream().anyMatch(extension -> file.getFileName().toString().endsWith(extension));
        }

        return allow;
    }

    private void digestFile(MessageDigest digest, Path path)  {

        // Get file input stream for reading the file content
        try (FileInputStream fis = new FileInputStream(path.toFile())){

            // Create byte array to read data in chunks
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;

            // read the data from file and update that data in the message digest
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String digestToString(MessageDigest digest) {
     // store the bytes returned by the digest() method
        byte[] bytes = digest.digest();

        // this array of bytes has bytes in decimal format
        // so we need to convert it into hexadecimal format

        // for this we create an object of StringBuilder
        // since it allows us to update the string i.e. its
        // mutable
        StringBuilder sb = new StringBuilder();

        // loop through the bytes array
        for (int i = 0; i < bytes.length; i++) {

            // the following line converts the decimal into
            // hexadecimal format and appends that to the
            // StringBuilder object
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        // finally we return the complete hash
        return sb.toString();
    }

}
