package com.babyfs.tk.commons.utils;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 拷贝自{@link com.google.common.reflect.ClassPath},主要的修改是允许重复的资源
 */
public class ClassPathRecources {
    private static final Logger LOGGER = Logger.getLogger(ClassPathRecources.class.getName());

    /**
     * Separator for the Class-Path manifest attribute value in jar files.
     */
    private static final Splitter CLASS_PATH_ATTRIBUTE_SEPARATOR =
            Splitter.on(" ").omitEmptyStrings();

    private static final String CLASS_FILE_NAME_EXTENSION = ".class";

    private final ImmutableList<ResourceInfo> resources;

    private ClassPathRecources(ImmutableList<ResourceInfo> resources) {
        this.resources = resources;
    }

    /**
     * Returns a {@code ClassPath} representing all classes and resources loadable from {@code
     * classloader} and its parent class loaders.
     * <p/>
     * <p>Currently only {@link URLClassLoader} and only {@code file://} urls are supported.
     *
     * @throws IOException if the attempt to read class path resources (jar files or directories)
     *                             failed.
     */
    public static ClassPathRecources from(ClassLoader classloader) throws IOException {
        Scanner scanner = new Scanner();
        for (Map.Entry<URI, ClassLoader> entry : getClassPathEntries(classloader).entrySet()) {
            scanner.scan(entry.getKey(), entry.getValue());
        }
        return new ClassPathRecources(scanner.getResources());
    }

    /**
     * Returns all resources loadable from the current class path, including the class files of all
     * loadable classes but excluding the "META-INF/MANIFEST.MF" file.
     */
    public ImmutableList<ResourceInfo> getResources() {
        return resources;
    }

    /**
     * Represents a class path resource that can be either a class file or any other resource file
     * loadable from the class path.
     *
     * @since 14.0
     */
    @Beta
    public static class ResourceInfo {
        private final String resourceName;
        private final URL url;

        static ResourceInfo of(String resourceName, URL url) {
            return new ResourceInfo(resourceName, url);
        }

        ResourceInfo(String resourceName, URL url) {
            this.resourceName = checkNotNull(resourceName);
            this.url = checkNotNull(url);
        }


        /**
         * Returns the fully qualified name of the resource. Such as "com/mycomp/foo/bar.txt".
         */
        public final String getResourceName() {
            return resourceName;
        }

        public URL getUrl() {
            return url;
        }

        // Do not change this arbitrarily. We rely on it for sorting ResourceInfo.
        @Override
        public String toString() {
            return resourceName;
        }
    }

    @VisibleForTesting
    static ImmutableMap<URI, ClassLoader> getClassPathEntries(
            ClassLoader classloader) {
        LinkedHashMap<URI, ClassLoader> entries = Maps.newLinkedHashMap();
        // Search parent first, since it's the order ClassLoader#loadClass() uses.
        ClassLoader parent = classloader.getParent();
        if (parent != null) {
            entries.putAll(getClassPathEntries(parent));
        }
        if (classloader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classloader;
            for (URL entry : urlClassLoader.getURLs()) {
                URI uri;
                try {
                    uri = entry.toURI();
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(e);
                }
                if (!entries.containsKey(uri)) {
                    entries.put(uri, classloader);
                }
            }
        }
        return ImmutableMap.copyOf(entries);
    }

    @VisibleForTesting
    static final class Scanner {

        private final ImmutableList.Builder<ResourceInfo> resources =
                new ImmutableList.Builder<ResourceInfo>();
        private final Set<URI> scannedUris = Sets.newHashSet();

        ImmutableList<ResourceInfo> getResources() {
            return resources.build();
        }

        void scan(URI uri, ClassLoader classloader) throws IOException {
            if (uri.getScheme().equals("file") && scannedUris.add(uri)) {
                scanFrom(new File(uri), classloader);
            }
        }

        @VisibleForTesting
        void scanFrom(File file, ClassLoader classloader)
                throws IOException {
            if (!file.exists()) {
                return;
            }
            if (file.isDirectory()) {
                scanDirectory(file, classloader);
            } else {
                scanJar(file, classloader);
            }
        }

        private void scanDirectory(File directory, ClassLoader classloader) throws IOException {
            scanDirectory(directory, classloader, "", ImmutableSet.<File>of());
        }

        private void scanDirectory(
                File directory, ClassLoader classloader, String packagePrefix,
                ImmutableSet<File> ancestors) throws IOException {
            File canonical = directory.getCanonicalFile();
            if (ancestors.contains(canonical)) {
                // A cycle in the filesystem, for example due to a symbolic link.
                return;
            }
            File[] files = directory.listFiles();
            if (files == null) {
                LOGGER.warning("Cannot read directory " + directory);
                // IO error, just skip the directory
                return;
            }
            ImmutableSet<File> newAncestors = ImmutableSet.<File>builder()
                    .addAll(ancestors)
                    .add(canonical)
                    .build();
            for (File f : files) {
                String name = f.getName();
                if (f.isDirectory()) {
                    scanDirectory(f, classloader, packagePrefix + name + "/", newAncestors);
                } else {
                    String resourceName = packagePrefix + name;
                    if (!resourceName.equals(JarFile.MANIFEST_NAME)) {
                        resources.add(ResourceInfo.of(resourceName, f.toURI().toURL()));
                    }
                }
            }
        }

        private void scanJar(File file, ClassLoader classloader) throws IOException {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(file);
            } catch (IOException e) {
                // Not a jar file
                return;
            }

            try {
                for (URI uri : getClassPathFromManifest(file, jarFile.getManifest())) {
                    scan(uri, classloader);
                }
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.isDirectory() || entry.getName().equals(JarFile.MANIFEST_NAME)) {
                        continue;
                    }
                    resources.add(ResourceInfo.of(entry.getName(), file.toURI().toURL()));
                }
            } finally {
                try {
                    jarFile.close();
                } catch (IOException ignored) {
                }
            }
        }

        /**
         * Returns the class path URIs specified by the {@code Class-Path} manifest attribute, according
         * to <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Main%20Attributes">
         * JAR File Specification</a>. If {@code manifest} is null, it means the jar file has no
         * manifest, and an empty set will be returned.
         */
        @VisibleForTesting
        static ImmutableSet<URI> getClassPathFromManifest(
                File jarFile, @Nullable Manifest manifest) {
            if (manifest == null) {
                return ImmutableSet.of();
            }
            ImmutableSet.Builder<URI> builder = ImmutableSet.builder();
            String classpathAttribute = manifest.getMainAttributes()
                    .getValue(Attributes.Name.CLASS_PATH.toString());
            if (classpathAttribute != null) {
                for (String path : CLASS_PATH_ATTRIBUTE_SEPARATOR.split(classpathAttribute)) {
                    URI uri;
                    try {
                        uri = getClassPathEntry(jarFile, path);
                    } catch (URISyntaxException e) {
                        // Ignore bad entry
                        LOGGER.warning("Invalid Class-Path entry: " + path);
                        continue;
                    }
                    builder.add(uri);
                }
            }
            return builder.build();
        }

        /**
         * Returns the absolute uri of the Class-Path entry value as specified in
         * <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Main%20Attributes">
         * JAR File Specification</a>. Even though the specification only talks about relative urls,
         * absolute urls are actually supported too (for example, in Maven surefire plugin).
         */
        @VisibleForTesting
        static URI getClassPathEntry(File jarFile, String path)
                throws URISyntaxException {
            URI uri = new URI(path);
            if (uri.isAbsolute()) {
                return uri;
            } else {
                return new File(jarFile.getParentFile(), path.replace('/', File.separatorChar)).toURI();
            }
        }
    }

    @VisibleForTesting
    static String getClassName(String filename) {
        int classNameEnd = filename.length() - CLASS_FILE_NAME_EXTENSION.length();
        return filename.substring(0, classNameEnd).replace('/', '.');
    }

}
