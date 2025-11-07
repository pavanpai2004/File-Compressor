package org.filecompressor.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileCompressController {

    @FXML
    private ProgressBar progressBar;

    @FXML
    private TextArea logArea;

    /**
     * Handles compression of multiple files into a single ZIP file.
     * Opens a FileChooser to select files, then a Save Dialog to specify the ZIP output location.
     * Progress is updated in the progress bar, and logs are displayed in the TextArea.
     */
    @FXML
    public void handleCompress(){

        // Open a file chooser to select multiple files to compress
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Files to compress");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        List<File> files = fileChooser.showOpenMultipleDialog(null);

        if(files == null || files.isEmpty())  return;  // Exit if no files selected

        // Open a save dialog to choose the output ZIP file location
        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save ZIP File As");
        saveChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        saveChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("ZIP Files (*.zip)", "*.zip"));
        File outputZip = saveChooser.showSaveDialog(null);

        if (outputZip == null) return; // Exit if save location not selected

        try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputZip))) {

            // Total size of files
            long totalFileSize = files.stream().mapToLong(File::length).sum();

            // Tracks how much data has been processed
            long processedFileSize = 0;

            // Loop through each file and add it to the ZIP
            for(File file : files){
                // Show current file being compressed
                logArea.setText(file.getAbsolutePath());

                try(FileInputStream fis = new FileInputStream(file)){
                    zos.putNextEntry(new ZipEntry(file.getName()));  // Create a new entry in ZIP
                    byte[] buffer = new byte[1024];
                    int length;
                    while((length = fis.read(buffer)) > 0){
                        zos.write(buffer,0,length);  // Write bytes to ZIP
                        processedFileSize += length;

                        // Update progress
                        progressBar.setProgress((double) processedFileSize /totalFileSize);
                    }

                    // Close the current ZIP entry
                    zos.closeEntry();
                }
            }

            // Display final statistics in KB
            double beforeSizeInKb = (double) totalFileSize /1024;
            double afterSizeInKb = (double) outputZip.length() /1024;
            String stat = String.format("\nOriginal size: %.2f KB\nCompressed size: %.2f KB",beforeSizeInKb,afterSizeInKb);
            logArea.setText(stat);

        }catch (IOException e){
            // Print the error message in logArea and console
            e.printStackTrace();
            logArea.setText(e.getMessage());
        }

    }

    /**
     * Handles decompression of a ZIP file into a selected directory.
     * Opens a FileChooser to select a ZIP file and a DirectoryChooser to select extraction location.
     * Progress is updated in the progress bar, and logs are displayed in the TextArea.
     */
    @FXML
    public void handleDecompress() {
        // Open a file chooser to select a ZIP file to decompress
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Zip File to Decompress");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("ZIP Files", "*.zip")
        );

        File zipFile = fileChooser.showOpenDialog(null);
        if (zipFile == null) return; // Exit if no file selected

        // Open a directory chooser to select the extraction location
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Extraction location");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File outputDir = directoryChooser.showDialog(null);

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];
            long totalBytes = zipFile.length(); // Total size of ZIP file
            long processed = 0; // Track decompressed bytes

            // Iterate through each entry in the ZIP
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(outputDir, entry.getName());
                new File(newFile.getParent()).mkdirs(); // Ensure directories exist

                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length); // Write decompressed bytes
                        processed += length;

                        // Update progress
                        progressBar.setProgress((double) processed / totalBytes);
                    }
                }

                zis.closeEntry();

                // Display final statistics in KB
                double beforeSizeInKb = (double) totalBytes / 1024;
                double afterSizeInKb = (double) processed / 1024;
                String stat = String.format("\nOriginal size: %.2f KB\nDecompressed size: %.2f KB",
                        beforeSizeInKb, afterSizeInKb);
                logArea.setText(stat);
            }

        } catch (IOException e) {
            // Print the error message in console and logArea
            e.printStackTrace();
            logArea.setText(e.getMessage());
        }
    }
}
