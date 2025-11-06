package org.filecompressor.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
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

    @FXML private ProgressBar progressBar;
    @FXML private TextArea logArea;
    @FXML private Button compressBtn;
    @FXML private BorderPane rootPane;

    @FXML
    public void handleCompress(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Files to compress");
        List<File> files = fileChooser.showOpenMultipleDialog(null);

        if(files == null || files.isEmpty())  return;

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save ZIP File As");
        saveChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("ZIP Files (*.zip)", "*.zip"));
        File outputZip = saveChooser.showSaveDialog(null);

        if (outputZip == null) {
            return;
        }

        try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputZip))) {

            long totalFileSize = files.stream().mapToLong(File::length).sum();
            long processedFileSize = 0;

            for(File file : files){
                logArea.setText(file.getAbsolutePath());
                try(FileInputStream fis = new FileInputStream(file)){
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    byte[] buffer = new byte[1024];
                    int length;
                    while((length = fis.read(buffer)) > 0){
                        zos.write(buffer,0,length);
                        processedFileSize += length;
                        progressBar.setProgress((double) processedFileSize /totalFileSize);
                    }
                    zos.closeEntry();
                }
            }


            String stat = "Original size: "+totalFileSize+" bytes\nCompressed size: "+outputZip.length()+" bytes";
            logArea.setText(stat);

        }catch (IOException e){
            e.printStackTrace();
            logArea.setText(e.getMessage());
        }

    }

    @FXML
    public void handleDecompress(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Zip File to Decompress");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("ZIP Files", "*.zip"));

        File zipFile = fileChooser.showOpenDialog(null);
        if (zipFile == null) {
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose location");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File outputDir =  directoryChooser.showDialog(null);


        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];
            long totalBytes = zipFile.length();
            long processed = 0;

            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(outputDir, entry.getName());
                new File(newFile.getParent()).mkdirs();

                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                        processed += length;
                        progressBar.setProgress((double) processed / totalBytes);
                    }
                }
                zis.closeEntry();
                String stat = "Original size: "+totalBytes+" bytes\nCompressed size: "+processed+" bytes";
                logArea.setText(stat);
            }
        }catch (IOException e){
            e.printStackTrace();
            logArea.setText(e.getMessage());
        }
    }
}
