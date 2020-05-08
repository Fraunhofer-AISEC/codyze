package de.fraunhofer.aisec.bouncycastle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaFile {

    private static final Logger log = LoggerFactory.getLogger(JavaFile.class);
    private static Pattern packagePattern = Pattern.compile("^package (.*);$");

    private File path;
    private String packageName;
    private String className;

    public JavaFile(File path, String packageName, String className) {
        this.path = path;
        this.packageName = packageName;
        this.className = className;
    }

    public static JavaFile parse(File path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String packageName = reader.lines().map(packagePattern::matcher).filter(Matcher::matches)
                .map(m -> m.group(1)).findFirst().orElse("");

            String className = path.getName()
                .replace(".java", "");
            return new JavaFile(path, packageName, className);
        } catch (IOException e) {
            log.error("Could not parse file {}: {}", path, e.getMessage());
        }
        return null;
    }

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFullyQualifiedName() {
        return packageName.isEmpty() ? className : packageName + "." + className;
    }

    public String getModulePrefix() {
        if (packageName.isEmpty()) {
            return path.getParent();
        }
        String packagePath = packageName.replace('.', File.separatorChar);
        if (!packagePath.isEmpty()) {
            // Ensure that this is a directory
            packagePath = File.separatorChar + packagePath + File.separatorChar;
        }
        return path.toString()
            .substring(0, path.toString()
                .lastIndexOf(packagePath));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaFile) {
            JavaFile other = (JavaFile) obj;
            return other.getPackageName()
                .equals(this.getPackageName()) && other.getPath()
                .equals(this.getPath()) && other.getClassName()
                .equals(this.getClassName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, packageName, className);
    }

    @Override
    public String toString() {
        return "JavaFile[" + getFullyQualifiedName() + ", path=" + path + "]";
    }
}
