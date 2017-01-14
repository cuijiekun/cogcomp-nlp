/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader;

import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.TextCleaner;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.XmlFragmentWhitespacingDocumentReader;

import static edu.illinois.cs.cogcomp.core.io.IOUtils.getFileName;
import static edu.illinois.cs.cogcomp.core.io.IOUtils.getFileStem;

/**
 * Strips all useless XML markup from an ERE xml document leaving the original text and 
 * where needed, appropriate attribute values.
 * @author redman
 *
 */
public class EREDocumentReader extends XmlFragmentWhitespacingDocumentReader {
	
	/** these tags contain attributes we want to keep. */
    static private ArrayList<String> retainTags = new ArrayList<String>();
    /** the attributes to keep for the above tags. */
    static private ArrayList<String> retainAttributes = new ArrayList<String>();

    static {
    	retainTags.add("quote");
    	retainTags.add("post");
    }

    static {
        retainAttributes.add("orig_author");
        retainAttributes.add("author");
    }

    /**
     * @param corpusName the name of the corpus, this can be anything.
     * @param sourceDirectory the name of the directory containing the file.
     * @throws Exception
     */
    public EREDocumentReader(String corpusName, String sourceDirectory) throws Exception {
        super(corpusName, sourceDirectory);
    }



    /**
     * ERE corpus directory has two directories: source/ and ere/.  The source/ directory contains
     *    original text in an xml format. The ere/ directory contains markup files corresponding
     *    in a many-to-one relationship with the source/ files: related annotation files have the same
     *    prefix as the corresponding source file (up to the .xml suffix).
     *
     * This method generates a List of List of Paths: each component List has the source file as its
     *    first element, and markup files as its remaining elements.  It expects {@link super.getSourceDirectory()}
     *    to return the root directory of the ERE corpus, under which should be data/source/ and data/ere/
     *    directories containing source files and annotation files respectively.
     *
     * @return a list of Path objects corresponding to files containing corpus documents to process.
     */
    @Override
    public List<List<Path>> getFileListing() throws IOException {

        String sourceFileDir = super.getSourceDirectory() + "data/source/";
        String annotationDir = super.getSourceDirectory() + "data/ere/";
        FilenameFilter filter = (dir, name) -> name.endsWith(getRequiredFileExtension());
        /**
         * returns the FULL PATH of each file
         */
        List<String> sourceFileList= Arrays.asList(IOUtils.lsFilesRecursive(sourceFileDir, filter));
        LinkedList<String> annotationFileList = new LinkedList<>();
        for ( String f : Arrays.asList(IOUtils.lsFilesRecursive(annotationDir, filter))) {
            annotationFileList.add(getFileName(f));
        }

        List<List<Path>> pathList = new ArrayList<>();

        /**
         * fileList has multiple entries per single annotation: a source file plus one or more
         *    annotation files. These files share a prefix -- the stem of the file containing
         *    the source text.
         */
        for (String fileName : sourceFileList) {
            List<Path> sourceAndAnnotations = new ArrayList<>();
            sourceAndAnnotations.add(Paths.get(fileName));
            String stem = getFileStem(fileName);

            for (String annFile : annotationFileList) {
                if (annFile.startsWith(stem)) {
                    sourceAndAnnotations.add(Paths.get(annotationDir + annFile));
                    sourceAndAnnotations.remove(annFile);
                }
            }
            pathList.add(sourceAndAnnotations);
        }
        return pathList;
    }



    /**
     * Strip all XML markup, but leave all the text content, and possibly some attributes
     * for certain tags, while retaining the same offset for all remaining text content.
     * @param original the original text string.
     * @return the striped text.
     */
    protected String stripText(String original) {
    	return TextCleaner.removeXmlLeaveAttributes(original, retainTags, retainAttributes);
    }
    
    /**
     * Exclude any files not possessing this extension.
     * @return the required file extension.
     */
    protected String getRequiredFileExtension() {
        return ".xml";
    }
}
