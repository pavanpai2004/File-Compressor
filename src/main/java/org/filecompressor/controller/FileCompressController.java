package org.filecompressor.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
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

        System.out.println(files.size());

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save ZIP File As");
        saveChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("ZIP Files (*.zip)", "*.zip"));

        Stage stage = (Stage) rootPane.getScene().getWindow();
        File outputZip = saveChooser.showSaveDialog(stage);

        if (outputZip == null) {
            return;
        }


        try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputZip))) {

            long totalFileSize = files.stream().mapToLong(File::length).sum();
            long processedFileSize = 0;

            for(File file : files){
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
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @FXML
    public void handleDecompress(){

    }
}
