package com.helospark.glslplugin.transition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * list resources available from the classpath @ *
 */
// https://stackoverflow.com/a/3923182/8258222
public class ResourceList {

    /**
     * for all elements of java.class.path get a Collection of resources Pattern
     * pattern = Pattern.compile(".*"); gets all resources
     * 
     * @param pattern
     *            the pattern to match
     * @return the resources in the order they are found
     */
    public static List<String> getFileNamesInDirectory(
            String folder) {
        final ArrayList<String> retval = new ArrayList<>();
        final String classPath = System.getProperty("java.class.path", ".");
        final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
        for (final String element : classPathElements) {
            retval.addAll(getResources(element, folder));
        }
        return retval;
    }

    private static Collection<String> getResources(
            final String element,
            final String folder) {
        final ArrayList<String> retval = new ArrayList<>();
        final File file = new File(element);
        if (file.isDirectory()) {
            retval.addAll(getResourcesFromDirectory(file, folder));
        } else {
            retval.addAll(getResourcesFromJarFile(file, folder));
        }
        return retval;
    }

    private static Collection<String> getResourcesFromJarFile(
            final File file,
            final String folder) {
        final ArrayList<String> retval = new ArrayList<>();
        ZipFile zf;
        try {
            zf = new ZipFile(file);
        } catch (final ZipException e) {
            throw new Error(e);
        } catch (final IOException e) {
            throw new Error(e);
        }
        final Enumeration e = zf.entries();
        while (e.hasMoreElements()) {
            final ZipEntry ze = (ZipEntry) e.nextElement();
            String fileName = ze.getName();
            final boolean accept = fileName.startsWith(folder);

            int lastSlash = fileName.lastIndexOf('/');
            if (lastSlash != -1) {
                fileName = fileName.substring(lastSlash + 1);
            }

            if (accept && fileName.length() > 0) {
                retval.add(fileName);
            }
        }
        try {
            zf.close();
        } catch (final IOException e1) {
            throw new Error(e1);
        }
        return retval;
    }

    private static Collection<String> getResourcesFromDirectory(
            final File directory,
            final String folder) {
        File subFolder = new File(directory, folder);
        if (subFolder.exists()) {

            final ArrayList<String> retval = new ArrayList<>();
            final File[] fileList = subFolder.listFiles();
            for (final File file : fileList) {
                final String fileName = file.getName();
                retval.add(fileName);
            }
            return retval;
        } else {
            return Collections.emptyList();
        }
    }
}