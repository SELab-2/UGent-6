package com.ugent.pidgeon.model.submissionTesting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SubmissionTemplateModel {
    private final List<FileEntry> requiredFiles = new ArrayList<>();
    private final List<FileEntry> deniedFiles = new ArrayList<>();

    private static class FileEntry {
        public String name;
        Pattern pattern;
        private String invert(String name){
            // invert . with \. for simpler filenames
            List<Integer> dotLocations = new ArrayList<>();
            List<Integer> escapedDotLocations = new ArrayList<>();
            for (int i = 0; i < name.length(); i++) {
                if(i > 0 && name.charAt(i - 1) == '\\' && name.charAt(i) == '.'){
                    escapedDotLocations.add(i);
                }else if (name.charAt(i) == '.') {
                    dotLocations.add(i);
                }
            }
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < name.length(); i++) {
                if(escapedDotLocations.contains(i + 1)){
                    // skip the break
                    continue;
                }else if(escapedDotLocations.contains(i)){
                    sb.append(".");
                }else if(dotLocations.contains(i)){
                    sb.append("\\.");
                }else{
                    sb.append(name.charAt(i));
                }
            }
            return sb.toString();

        }

        public FileEntry(String name) {

            this.name = invert(name);

            pattern = Pattern.compile("^" + this.name + "$"); // hat for defining start of the string, $ defines the end
        }

        public boolean matches(String fileName) {
            return pattern.matcher(fileName).find();
        }
    }

    public void parseSubmissionTemplate(String templateString) {
        /* the template structure is line per line, every line defining a folder or file. Example:

        src/
            index.js
        test/
            indextests/
                sampletest1.js
                sampletest2.js
        -node_modules/

         */


        templateString = templateString.replaceAll("\\r", ""); // remove windows \r
        String[] lines = templateString.split("\n");

        int mostSpaces = 0;
        HashMap<Integer, Integer> tabsPerSpaces = new HashMap<>(); // hashmap for tracking max spaces amount
        tabsPerSpaces.put(0, 0); // will make a normal file with tabs work normally
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int spaceAmount = line.lastIndexOf(' ') + 1;
            if (spaceAmount > mostSpaces) {
                tabsPerSpaces.put(spaceAmount, tabsPerSpaces.get(mostSpaces) + 1);
                mostSpaces = spaceAmount;
            }
            lines[i] = "\t".repeat(tabsPerSpaces.get(spaceAmount)) + line.replaceAll(" ", "");
        }

        // Create folder stack for keeping track of all the folders while exploring the insides
        List<String> folderStack = new ArrayList<>();

        for (String line : lines) {
            if (line.isEmpty() || line.charAt(line.lastIndexOf("\t") + 1) == '#') {
                continue; // parse empty lines or comment lines
            }
            int tabAmount = line.lastIndexOf('\t') + 1;
            String fileName = line.substring(tabAmount);
            boolean nonAllowMode = fileName.charAt(0) == '-';
            if (nonAllowMode) {
                fileName = fileName.substring(1);
            }
            if (tabAmount < folderStack.size()) {
                // All files of the folder have been processed
                folderStack = folderStack.subList(0, tabAmount);
            }
            FileEntry fe = new FileEntry((String.join("", folderStack) + fileName).strip());
            if (fileName.lastIndexOf('/') == fileName.length() - 1) {
                folderStack.add(fileName);
            } else {
                if (nonAllowMode) {
                    deniedFiles.add(fe);
                } else {
                    requiredFiles.add(fe);
                }
            }
        }
    }

    public static class SubmissionResult {
        public boolean passed;
        public String feedback;

        public SubmissionResult(boolean passed, String feedback) {
            this.passed = passed;
            this.feedback = feedback;
        }
    }

    public SubmissionResult checkSubmission(ZipFile file) throws IOException {
        try {
            Enumeration<? extends ZipEntry> entries = file.entries();
            List<String> folderItems = new LinkedList<>(); // linked list since there will be a lot of addition and removing
            List<Boolean> requiredItemsContained = new ArrayList<>(Collections.nCopies(requiredFiles.size(), false));
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                folderItems.add(entry.getName());
            }

            // check if all required items are in the list, and check if all items in the list are required.
            // then check for all items in the list if they are on the disallow list.

            List<String> filesMissing = new ArrayList<>();
            List<String> filesUnrequested = new ArrayList<>();
            List<String> filesDenied = new ArrayList<>();

            Iterator<String> fileIterator = folderItems.iterator();
            while (fileIterator.hasNext()) {
                boolean inTemplate = false;
                String currFile = fileIterator.next();
                // check if all files are required
                int pathIndex = 0;
                while (pathIndex < requiredFiles.size()) {
                    if (requiredFiles.get(pathIndex).matches(currFile)) {
                        inTemplate = true;
                        break;
                    }
                    pathIndex++;
                }
                // check if file is not denied
                for (FileEntry deniedFile : deniedFiles) {
                    if (deniedFile.matches(currFile)) {
                        // this file is not allowed
                        filesDenied.add(currFile + " <--- X " + deniedFile.name);
                    }
                }
                if (!inTemplate) {
                    filesUnrequested.add(currFile);
                } else {
                    fileIterator.remove();
                    // check that item is in the list
                    requiredItemsContained.set(pathIndex, true);
                }
            }
            // return true if all items in template are in the zip
            for (int i = 0; i < requiredItemsContained.size(); i++) {
                if (!requiredItemsContained.get(i)) {
                    filesMissing.add(requiredFiles.get(i).name);
                }
            }


            boolean passed = (filesMissing.size() + filesUnrequested.size() + filesDenied.size()) == 0;
            String feedback = passed ? "File structure is correct" : "File structure failed to pass the template, because: \n ";
            if (!filesMissing.isEmpty()) {
                feedback += " -The following files are required from the template and are not found in the project: \n    -";
                feedback += String.join("\n    -", filesMissing);
            }
            if (!filesUnrequested.isEmpty()) {
                feedback += "\n -The following files are not requested in the template: \n    -";
                feedback += String.join("\n    -", filesUnrequested);
            }
            if (!filesDenied.isEmpty()) {
                feedback += "\n -The following files are not allowed in the project: \n    -";
                feedback += String.join("\n    -", filesDenied);
            }

            return new SubmissionResult(passed, feedback);


        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public SubmissionResult checkSubmission(String file) throws IOException {
        return checkSubmission(new ZipFile(file));
    }

    // will throw error if there are errors in the template
    public static void tryTemplate(String template) throws IllegalArgumentException {
        List<String> lines = List.of(template.split("\n"));
        // check if the template is valid, control if every line contains a file parsable string
        // check if the file is in a valid folder location (indentation is correct)
        // check if the first file has indentation 0
        List<Integer> indentionAmounts = new ArrayList<>();
        indentionAmounts.add(0);
        if(getIndentation(lines.get(0)) != 0){
            throw new IllegalArgumentException("First file should not have any spaces or tabs.");
        }
        boolean newFolder = false;
        for(int line_index = 0; line_index < lines.size(); line_index++){
            String line = lines.get(line_index);
            int indentation = getIndentation(line);
            if(line.isEmpty()){
                throw new IllegalArgumentException("Empty file name in template, remove blank lines");
            }
            if(newFolder && indentation > indentionAmounts.get(indentionAmounts.size() - 1)){
                // since the indentation is larger than the previous, we are dealing with the first file in a new folder
                indentionAmounts.add(indentation);
                newFolder = false;
            }else{
                // we are dealing with a file in a folder, thus the indentation should be equal to one of the previous folders
                for(int i = indentionAmounts.size() - 1; i >= 0; i--){
                    if(indentionAmounts.get(i) == indentation){
                        break;
                    }
                    if(i == 0){
                        throw new IllegalArgumentException("File at line "+ line_index + " is not in a valid folder location (indentation is incorrect)");
                    }
                }
                // check if file is correct, since location is correct

                // first check if file contains valid file names
                if(line.substring(0,line.length() - 1).contains("/")){
                    throw new IllegalArgumentException("File/folder at line "+ (line_index+1) + " contains invalid characters");
                }
                // check if file is a folder
                if(line.charAt(line.length() - 1) == '/') {
                    newFolder = true;
                }
            }
            if(line.charAt(line.length() - 1) == '/'){
                // new folder start!
                newFolder = true;
            }
        }
    }


    private static int getIndentation(String line){
        int length = line.length();
        // one space is equal to a tab
        for(int i = 0; i < length; i++){
            if(line.charAt(i) != ' ' && line.charAt(i) != '\t'){
                return i;
            }
        }
        return length - 1;
    }

}
